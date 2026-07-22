import * as AccUtils from "../accUtils";
import * as ko from "knockout";
import { mapEstatusToLabel, mapEstatusToBadge } from "../utils/contratoUtils";
import { getRole } from "../utils/auth";
import { contratosApi, almacenBienesApi, contratoBienesApi, catalogosApi } from "../utils/api";
import { CFilePickerElement } from 'oj-c/file-picker';
import "oj-c/dialog";

import "oj-c/form-layout";
import "oj-c/input-text";
import "oj-c/button";
import "oj-c/progress-bar";
import "oj-c/radioset";
import "oj-c/file-picker";
import "ojs/ojswitch";

// crypto.randomUUID solo existe en "contextos seguros" (HTTPS o localhost) —
// en un deploy por HTTP plano sobre IP (sin dominio/TLS) el navegador no
// expone la función y truena "crypto.randomUUID is not a function".
function generarIdCliente(): string {
    if (typeof crypto !== "undefined" && typeof crypto.randomUUID === "function") {
        return crypto.randomUUID();
    }
    return `${Date.now().toString(36)}-${Math.random().toString(36).slice(2)}`;
}

// ================================================================
// TIPOS
// ================================================================

type EvidenciaImagen = {
    id: string;
    file: File;
    previewUrl: string;
};

/** Fila de la plantilla de componentes esperados de un grupo "Conjunto" — objeto plano (no observables) por el patrón de oj-bind-for-each + oj-c-*. */
type ComponenteEsperadoItem = {
    idLocal: number;
    nombre: string;
};

type ComponenteGuardado = {
    nombreComponente: string;
    numeroSerie: string;
    tieneFoto: boolean;
};

/** Fila del diálogo de captura de componentes de una unidad "Conjunto". */
type ComponenteCapturado = {
    nombreComponente: string;
    frmNumeroSerie: ko.Observable<string>;
    evidencia: ko.Observable<EvidenciaImagen | null>;
};

type UnidadProcesamiento = {
    idAlmacenBien: number;
    codigoInterno: string;
    displayLabel: string;
    frmNumeroSerie: ko.Observable<string>;
    uiEstatus: ko.Observable<string>;
    uiTotalEvidencias: ko.Observable<number>;
    uiComponentesGuardados: ko.Observable<ComponenteGuardado[]>;
};

type GrupoProcesamiento = {
    idContratoBien: number;
    lote: number | null;
    partida: number | null;
    descripcion: string;
    unidadMedida: string;
    totalUnidades: number;
    unidades: UnidadProcesamiento[];
    frmMarca: ko.Observable<string>;
    frmModelo: ko.Observable<string>;
    /** Switch — ¿este bien lleva número de serie/motor? */
    uiRequiereNumeroSerie: ko.Observable<boolean>;
    /** SIMPLE | CONJUNTO | null (sin elegir) — solo relevante si uiRequiereNumeroSerie() es true */
    uiSubTipoCapturaSerie: ko.Observable<string | null>;
    /** NINGUNO | SIMPLE | CONJUNTO — deriva de los dos anteriores; solo maneja la visibilidad del editor de componentes, sin guardar todavía */
    calcTipoCapturaEfectivo: ko.PureComputed<string>;
    /** NINGUNO | SIMPLE | CONJUNTO — la última configuración YA guardada en servidor; controla las secciones por unidad (evita re-renderizar la lista completa en cada click del radio) */
    uiTipoCapturaGuardado: ko.Observable<string>;
    listaComponentesEsperados: ko.ObservableArray<ComponenteEsperadoItem>;
    uiTotalEvidenciasGrupo: ko.Observable<number>;
    calcEsVehiculo: ko.PureComputed<boolean>;
    calcProcesados: ko.PureComputed<number>;
    calcTodosProcesados: ko.PureComputed<boolean>;
    calcListoParaFinalizar: ko.PureComputed<boolean>;
    uiGuardandoGrupo: ko.Observable<boolean>;
    uiGuardandoConfig: ko.Observable<boolean>;
};

type ClavePresupuestal = {
    clave: string;
    partidaEspecifica: string;
    montoAsignado: string;
    montoAsignadoNum: number;
};

type ContratoResumen = {
    idContrato: number;
    numeroContrato: string;
    adquisicion: string;
    estatus: string;
    estatusLabel: string;
    estatusBadge: string;
    proveedor: string;
    beneficiarios: string;
    montoSinImpuestos: string;
    impuestos: string;
    montoTotal: string;
    clavesPresupuestales: ClavePresupuestal[];
};

type RecepcionInfo = {
    numero: number;
    fechaRecepcion: string;
    totalBienes: number;
    totalProcesadosInicial: number;
};

// ================================================================
// VIEWMODEL
// ================================================================

class ProcesamientoViewModel {

    private router: any;
    private contratoId: number | null = null;
    private recepcionId: number | null = null;

    public uiCargando  = ko.observable<boolean>(false);
    public uiError     = ko.observable<string>("");
    public uiExito     = ko.observable<string>("");

    private readonly userRole: string = getRole() ?? "ALMACEN";
    public calcEsAdmin = ko.pureComputed(() => this.userRole === "ADMINISTRADOR");

    public contrato      = ko.observable<ContratoResumen | null>(null);
    public recepcionInfo = ko.observable<RecepcionInfo | null>(null);
    public listaGrupos   = ko.observableArray<GrupoProcesamiento>([]);

    public kpiTotalBienes = ko.observable<number>(0);

    // ── Diálogos de info admin ────────────────────────────────────
    public uiDialogoFinanciero    = ko.observable<boolean>(false);
    public uiDialogoBeneficiarios = ko.observable<boolean>(false);

    public calcBeneficiariosLista = ko.pureComputed(() =>
        (this.contrato()?.beneficiarios || "")
            .split(",").map(s => s.trim()).filter(s => s.length > 0)
    );

    public calcTotalClaves = ko.pureComputed(() => {
        const total = (this.contrato()?.clavesPresupuestales ?? [])
            .reduce((sum, c) => sum + c.montoAsignadoNum, 0);
        return this.formatMonto(total);
    });

    public cmdVerFinanciero    = (): void => { this.uiDialogoFinanciero(true); };
    public cmdVerBeneficiarios = (): void => { this.uiDialogoBeneficiarios(true); };

    public calcBienesRecepcion = ko.pureComputed(() =>
        this.recepcionInfo()?.totalBienes ?? 0
    );

    public calcProcesadosTotal = ko.pureComputed(() => {
        const inicial   = this.recepcionInfo()?.totalProcesadosInicial ?? 0;
        const enSesion  = this.listaGrupos().reduce((sum, g) => sum + g.calcProcesados(), 0);
        return inicial + enSesion;
    });

    public calcTotalPendientes = ko.pureComputed(() =>
        this.listaGrupos().reduce((sum, g) =>
            sum + g.unidades.filter(u => u.uiEstatus() !== "PROCESADO").length, 0)
    );

    public calcTodoCompleto = ko.pureComputed(() =>
        this.listaGrupos().length > 0 && this.calcTotalPendientes() === 0
    );

    // ================================================================
    // EVIDENCIAS — catálogo de bienes procesados (grupo + unidad + conjunto)
    // ================================================================

    public readonly minEvidenciasGrupo    = 5;
    public readonly minEvidenciasVehiculo = 5;
    public readonly minEvidenciasSimple   = 1;

    public readonly listaSubTipoCapturaOptions = [
        { value: "SIMPLE",   label: "Un número de serie" },
        { value: "CONJUNTO", label: "Conjunto de componentes" },
    ];

    public listaCatalogoComponentes = ko.observableArray<string>([]);
    private seqComponenteEsperado = 0;

    // ── Diálogo de evidencias fotográficas (reutilizado para grupo y unidad) ──
    public uiDialogoEvidencia        = ko.observable<boolean>(false);
    public uiDialogoEvidenciaTitulo  = ko.observable<string>("");
    public uiDialogoEvidenciaMin     = ko.observable<number>(5);
    public uiDialogoEvidenciaPrevias = ko.observable<number>(0);
    public listaEvidenciasDialogo    = ko.observableArray<EvidenciaImagen>([]);
    public uiGuardandoEvidencia      = ko.observable<boolean>(false);
    private evidenciaDialogoTipo: "GRUPO" | "UNIDAD" | null = null;
    private evidenciaDialogoGrupo: GrupoProcesamiento | null = null;
    private evidenciaDialogoUnidad: UnidadProcesamiento | null = null;

    public calcTotalEvidenciaDialogo = ko.pureComputed(() =>
        this.uiDialogoEvidenciaPrevias() + this.listaEvidenciasDialogo().length
    );
    public calcFaltanEvidenciaDialogo = ko.pureComputed(() =>
        this.calcTotalEvidenciaDialogo() < this.uiDialogoEvidenciaMin()
    );

    // ── Diálogo de componentes (unidad "Conjunto") ──
    public uiDialogoComponentes       = ko.observable<boolean>(false);
    public uiDialogoComponentesTitulo = ko.observable<string>("");
    public listaComponentesDialogo    = ko.observableArray<ComponenteCapturado>([]);
    public uiGuardandoComponentes     = ko.observable<boolean>(false);
    private componentesDialogoUnidad: UnidadProcesamiento | null = null;

    public calcPuedeGuardarComponentes = ko.pureComputed(() =>
        this.listaComponentesDialogo().length > 0 &&
        this.listaComponentesDialogo().every(c => !!c.frmNumeroSerie().trim() && c.evidencia() !== null)
    );

    constructor(params: any) {
        this.router = params?.router;
        const idParam = params?.routerState?.params?.id;
        const recepcionParam = params?.routerState?.params?.recepcionId;
        console.log("[Procesamiento] params recibidos:", { idParam, recepcionParam });
        if (idParam) {
            this.contratoId = Number(idParam);
        }
        if (recepcionParam) {
            this.recepcionId = Number(recepcionParam);
        }
    }

    connected(): void {
        AccUtils.announce("Procesamiento de bienes.");
        document.title = "Procesamiento — Gestión de Almacén";
        if (this.contratoId) {
            void this.loadBienes(this.contratoId);
            void this.loadCatalogoComponentes();
        } else {
            this.uiError("No se especificó un contrato.");
        }
    }

    private async loadCatalogoComponentes(): Promise<void> {
        try {
            this.listaCatalogoComponentes(await catalogosApi.obtenerComponentes());
        } catch (err) {
            console.error("Error al cargar catálogo de componentes:", err);
        }
    }

    disconnected(): void {
        this.limpiarEvidenciasDialogo();
        this.limpiarComponentesDialogo();
    }
    transitionCompleted(): void {}

    // ================================================================
    // LOAD
    // ================================================================
    private async loadBienes(id: number): Promise<void> {
        this.uiCargando(true);
        this.uiError("");

        try {
            const [dataContrato, dataGrupos, dataRecepciones] = await Promise.all([
                contratosApi.obtenerPorId(id),
                contratosApi.obtenerBienesAlmacen(id, this.recepcionId ?? undefined),
                this.recepcionId ? contratosApi.listarRecepciones(id) : Promise.resolve(null)
            ]);

            this.contrato({
                idContrato:     dataContrato.idContrato,
                numeroContrato: dataContrato.numeroContrato,
                adquisicion:    dataContrato.adquisicion,
                estatus:        dataContrato.estatus,
                estatusLabel:   mapEstatusToLabel(dataContrato.estatus),
                estatusBadge:   mapEstatusToBadge(dataContrato.estatus),
                proveedor:      dataContrato.proveedor?.razonSocial || "Sin proveedor asignado",
                beneficiarios:  dataContrato.beneficiarios || "",
                montoSinImpuestos: this.formatMonto(dataContrato.montoSinImpuestos),
                impuestos:         this.formatMonto(dataContrato.impuestos),
                montoTotal:        this.formatMonto(dataContrato.montoTotal),
                clavesPresupuestales: (dataContrato.clavesPresupuestales ?? []).map((cl: any) => ({
                    clave:             cl.clave || "—",
                    partidaEspecifica: cl.partidaEspecifica || "—",
                    montoAsignadoNum:  parseFloat(cl.montoAsignado) || 0,
                    montoAsignado:     this.formatMonto(cl.montoAsignado),
                })),
            });

            this.kpiTotalBienes(
                (dataContrato.bienes || []).reduce(
                    (sum: number, b: any) => sum + (parseFloat(b.cantidad) || 0), 0
                )
            );

            if (this.recepcionId && dataRecepciones) {
                const idx = (dataRecepciones as any[]).findIndex(
                    (r: any) => r.idRecepcionAlmacen === this.recepcionId
                );
                if (idx !== -1) {
                    const r = (dataRecepciones as any[])[idx];
                    this.recepcionInfo({
                        numero:                  (dataRecepciones as any[]).length - idx,
                        fechaRecepcion:          this.formatFecha(r.fechaRecepcion ? r.fechaRecepcion.split("T")[0] : null),
                        totalBienes:             r.totalBienes,
                        totalProcesadosInicial:  r.totalProcesados,
                    });
                }
            }

            const grupos: GrupoProcesamiento[] = dataGrupos.map(g => {
                const esVehiculo = g.unidadMedida === "Vehículo";

                const unidades: UnidadProcesamiento[] = g.unidades.map((u: any, uIdx: number) => {
                    const numeroInicial: string = (esVehiculo ? u.numeroMotor : u.numeroSerie) || "";
                    const totalEvidenciasInicial: number = u.totalEvidencias || 0;
                    return {
                        idAlmacenBien:  u.idAlmacenBien,
                        codigoInterno:  u.codigoInterno,
                        displayLabel:   `Unidad ${uIdx + 1}`,
                        frmNumeroSerie: ko.observable<string>(numeroInicial),
                        uiEstatus:      ko.observable<string>(u.estatus),
                        uiTotalEvidencias: ko.observable<number>(totalEvidenciasInicial),
                        uiComponentesGuardados: ko.observable<ComponenteGuardado[]>(
                            (u.componentes || []).map((c: any) => ({
                                nombreComponente: c.nombreComponente,
                                numeroSerie:      c.numeroSerie,
                                tieneFoto:        !!c.tieneFoto,
                            }))
                        ),
                    };
                });

                const calcProcesados = ko.pureComputed(() =>
                    unidades.filter(u => u.uiEstatus() === "PROCESADO").length
                );

                const frmMarca  = ko.observable<string>((g.unidades[0]?.marca as string) || "");
                const frmModelo = ko.observable<string>((g.unidades[0]?.modelo as string) || "");

                const listaComponentesEsperados = ko.observableArray<ComponenteEsperadoItem>(
                    (g.componentesEsperados || []).map((nombre: string) => ({
                        idLocal: this.seqComponenteEsperado++,
                        nombre,
                    }))
                );

                const tipoCapturaSerieInicial: string = g.tipoCapturaSerie || "NINGUNO";
                const totalEvidenciasGrupoInicial: number = g.totalEvidenciasGrupo || 0;

                const uiRequiereNumeroSerie = ko.observable<boolean>(tipoCapturaSerieInicial !== "NINGUNO");
                const uiSubTipoCapturaSerie = ko.observable<string | null>(
                    tipoCapturaSerieInicial === "NINGUNO" ? null : tipoCapturaSerieInicial
                );
                const calcTipoCapturaEfectivo = ko.pureComputed(() =>
                    uiRequiereNumeroSerie() ? (uiSubTipoCapturaSerie() || "NINGUNO") : "NINGUNO"
                );
                const uiTipoCapturaGuardado  = ko.observable<string>(tipoCapturaSerieInicial);
                const uiTotalEvidenciasGrupo = ko.observable<number>(totalEvidenciasGrupoInicial);

                const calcEvidenciaCompleta = ko.pureComputed(() => {
                    if (esVehiculo) {
                        return unidades.every(u => u.uiTotalEvidencias() >= this.minEvidenciasVehiculo);
                    }
                    if (uiTotalEvidenciasGrupo() < this.minEvidenciasGrupo) return false;
                    if (uiTipoCapturaGuardado() === "SIMPLE") {
                        return unidades.every(u => u.uiTotalEvidencias() >= this.minEvidenciasSimple);
                    }
                    if (uiTipoCapturaGuardado() === "CONJUNTO") {
                        const esperados = listaComponentesEsperados().length;
                        return esperados > 0 && unidades.every(u =>
                            u.uiComponentesGuardados().length === esperados &&
                            u.uiComponentesGuardados().every(c => c.tieneFoto));
                    }
                    return true;
                });

                return {
                    idContratoBien:  g.idContratoBien,
                    lote:            g.lote,
                    partida:         g.partida,
                    descripcion:     g.descripcion,
                    unidadMedida:    g.unidadMedida,
                    totalUnidades:   g.totalUnidades,
                    unidades,
                    frmMarca,
                    frmModelo,
                    uiRequiereNumeroSerie,
                    uiSubTipoCapturaSerie,
                    calcTipoCapturaEfectivo,
                    uiTipoCapturaGuardado,
                    listaComponentesEsperados,
                    uiTotalEvidenciasGrupo,
                    calcEsVehiculo: ko.pureComputed(() => esVehiculo),
                    calcProcesados,
                    uiGuardandoConfig: ko.observable<boolean>(false),
                    calcTodosProcesados: ko.pureComputed(() =>
                        calcProcesados() === unidades.length
                    ),
                    calcListoParaFinalizar: ko.pureComputed(() =>
                        unidades.every(u => u.uiEstatus() === "EN_PROCESO" || u.uiEstatus() === "PROCESADO")
                        && calcEvidenciaCompleta()
                    ),
                    uiGuardandoGrupo: ko.observable<boolean>(false),
                };
            });

            this.listaGrupos(grupos);
        } catch (err: any) {
            console.error("Error al cargar procesamiento:", err);
            this.uiError("No se pudo cargar la información. Intenta de nuevo.");
        } finally {
            this.uiCargando(false);
        }
    }

    // ================================================================
    // COMMANDS
    // ================================================================
    public cmdRegresar = (): void => {
        this.router?.go({ path: "contrato-detalle", params: { id: this.contratoId } });
    };

    public cmdActualizar = (): void => {
        if (this.contratoId) void this.loadBienes(this.contratoId);
    };

    // Guarda marca, modelo y números de serie sin marcar como PROCESADO.
    // RECIBIDO → EN_PROCESO automáticamente en el backend.
    public cmdGuardarGrupo = async (grupo: GrupoProcesamiento): Promise<void> => {
        const pendientes = grupo.unidades.filter(u => u.uiEstatus() !== "PROCESADO");
        if (pendientes.length === 0) return;

        grupo.uiGuardandoGrupo(true);
        this.uiError("");

        const marca  = grupo.frmMarca().trim();
        const modelo = grupo.frmModelo().trim();

        try {
            await Promise.all(pendientes.map(u => {
                const payload: any = {};
                if (marca)  payload.marca  = marca;
                if (modelo) payload.modelo = modelo;
                if (grupo.calcEsVehiculo() && u.frmNumeroSerie().trim()) {
                    payload.numeroMotor = u.frmNumeroSerie().trim();
                } else if (grupo.uiTipoCapturaGuardado() === "SIMPLE" && u.frmNumeroSerie().trim()) {
                    payload.numeroSerie = u.frmNumeroSerie().trim();
                }
                return almacenBienesApi.guardarDatos(u.idAlmacenBien, payload).then(() => {
                    u.uiEstatus("EN_PROCESO");
                });
            }));
            this.uiExito("Datos guardados correctamente.");
            setTimeout(() => this.uiExito(""), 3000);
        } catch (err: any) {
            console.error("Error al guardar grupo:", err);
            this.uiError(err.message || "No se pudieron guardar los datos.");
        } finally {
            grupo.uiGuardandoGrupo(false);
        }
    };

    // Marca todos los bienes pendientes del grupo como PROCESADO.
    public cmdConfirmarGrupo = async (grupo: GrupoProcesamiento): Promise<void> => {
        const pendientes = grupo.unidades.filter(u => u.uiEstatus() !== "PROCESADO");
        if (pendientes.length === 0) return;

        grupo.uiGuardandoGrupo(true);
        this.uiError("");

        const marca  = grupo.frmMarca().trim();
        const modelo = grupo.frmModelo().trim();

        try {
            const payload: any = { ids: pendientes.map(u => u.idAlmacenBien) };
            if (marca)  payload.marca  = marca;
            if (modelo) payload.modelo = modelo;
            await almacenBienesApi.procesarBloque(payload);
            pendientes.forEach(u => u.uiEstatus("PROCESADO"));
        } catch (err: any) {
            console.error("Error al confirmar grupo:", err);
            this.uiError(err.message || "No se pudo confirmar el procesamiento.");
        } finally {
            grupo.uiGuardandoGrupo(false);
        }
    };

    // ================================================================
    // CONFIGURACIÓN DEL GRUPO — tipo de captura de serie + componentes esperados
    // ================================================================

    public cmdAgregarComponenteEsperado = (grupo: GrupoProcesamiento): void => {
        grupo.listaComponentesEsperados([
            ...grupo.listaComponentesEsperados(),
            { idLocal: this.seqComponenteEsperado++, nombre: "" },
        ]);
    };

    public cmdEliminarComponenteEsperado = (grupo: GrupoProcesamiento, item: ComponenteEsperadoItem): void => {
        grupo.listaComponentesEsperados(
            grupo.listaComponentesEsperados().filter(c => c.idLocal !== item.idLocal)
        );
    };

    public cmdSetNombreComponenteEsperado = (grupo: GrupoProcesamiento, item: ComponenteEsperadoItem, valor: string): void => {
        const idx = grupo.listaComponentesEsperados().findIndex(c => c.idLocal === item.idLocal);
        if (idx === -1) return;
        grupo.listaComponentesEsperados.splice(idx, 1, { ...item, nombre: valor });
    };

    public cmdGuardarCapturaSerie = async (grupo: GrupoProcesamiento): Promise<void> => {
        if (grupo.uiRequiereNumeroSerie() && !grupo.uiSubTipoCapturaSerie()) {
            this.uiError("Selecciona si es un número de serie o un conjunto de componentes.");
            return;
        }

        grupo.uiGuardandoConfig(true);
        this.uiError("");

        const tipo = grupo.uiRequiereNumeroSerie() ? grupo.uiSubTipoCapturaSerie()! : "NINGUNO";
        const nombres = grupo.listaComponentesEsperados()
            .map(c => c.nombre.trim())
            .filter(n => n.length > 0);

        if (tipo === "CONJUNTO" && nombres.length === 0) {
            this.uiError("Agrega al menos un componente antes de guardar.");
            grupo.uiGuardandoConfig(false);
            return;
        }

        try {
            await contratoBienesApi.definirCapturaSerie(grupo.idContratoBien, {
                tipoCapturaSerie: tipo,
                componentes: tipo === "CONJUNTO" ? nombres : [],
            });
            grupo.uiTipoCapturaGuardado(tipo);
            void this.loadCatalogoComponentes();
            this.uiExito("Configuración guardada.");
            setTimeout(() => this.uiExito(""), 3000);
        } catch (err: any) {
            console.error("Error al guardar configuración de captura:", err);
            this.uiError(err.message || "No se pudo guardar la configuración.");
        } finally {
            grupo.uiGuardandoConfig(false);
        }
    };

    // ================================================================
    // DIÁLOGO — Evidencias fotográficas (reutilizado para grupo y unidad)
    // ================================================================

    public cmdAbrirEvidenciaGrupo = (grupo: GrupoProcesamiento): void => {
        this.evidenciaDialogoTipo = "GRUPO";
        this.evidenciaDialogoGrupo = grupo;
        this.evidenciaDialogoUnidad = null;
        this.uiDialogoEvidenciaTitulo(`Evidencias de catálogo — ${grupo.descripcion}`);
        this.uiDialogoEvidenciaMin(this.minEvidenciasGrupo);
        this.uiDialogoEvidenciaPrevias(grupo.uiTotalEvidenciasGrupo());
        this.listaEvidenciasDialogo([]);
        this.uiDialogoEvidencia(true);
    };

    public cmdAbrirEvidenciaUnidad = (grupo: GrupoProcesamiento, unidad: UnidadProcesamiento): void => {
        this.evidenciaDialogoTipo = "UNIDAD";
        this.evidenciaDialogoGrupo = grupo;
        this.evidenciaDialogoUnidad = unidad;
        this.uiDialogoEvidenciaTitulo(`Evidencias — ${unidad.displayLabel} (${unidad.codigoInterno})`);
        this.uiDialogoEvidenciaMin(grupo.calcEsVehiculo() ? this.minEvidenciasVehiculo : this.minEvidenciasSimple);
        this.uiDialogoEvidenciaPrevias(unidad.uiTotalEvidencias());
        this.listaEvidenciasDialogo([]);
        this.uiDialogoEvidencia(true);
    };

    public onSelectEvidenciaDialogo = (event: CFilePickerElement.ojSelect) => {
        const archivos = Array.from(event.detail.files);
        const nuevas: EvidenciaImagen[] = archivos.map(file => ({
            id: generarIdCliente(),
            file,
            previewUrl: URL.createObjectURL(file),
        }));
        this.listaEvidenciasDialogo([...this.listaEvidenciasDialogo(), ...nuevas]);
    };

    public cmdRemoverEvidenciaDialogo = (ev: EvidenciaImagen): void => {
        URL.revokeObjectURL(ev.previewUrl);
        this.listaEvidenciasDialogo(this.listaEvidenciasDialogo().filter(e => e.id !== ev.id));
    };

    private limpiarEvidenciasDialogo(): void {
        this.listaEvidenciasDialogo().forEach(e => URL.revokeObjectURL(e.previewUrl));
        this.listaEvidenciasDialogo([]);
    }

    public cmdCerrarEvidenciaDialogo = (): void => {
        if (this.uiGuardandoEvidencia()) return;
        this.limpiarEvidenciasDialogo();
        this.uiDialogoEvidencia(false);
    };

    public cmdGuardarEvidenciaDialogo = async (): Promise<void> => {
        const nuevas = this.listaEvidenciasDialogo();
        if (nuevas.length === 0) { this.uiDialogoEvidencia(false); return; }

        this.uiGuardandoEvidencia(true);
        this.uiError("");
        const files = nuevas.map(e => e.file);

        try {
            if (this.evidenciaDialogoTipo === "GRUPO" && this.evidenciaDialogoGrupo) {
                const grupo = this.evidenciaDialogoGrupo;
                await contratoBienesApi.subirEvidencia(grupo.idContratoBien, files);
                grupo.uiTotalEvidenciasGrupo(grupo.uiTotalEvidenciasGrupo() + files.length);
            } else if (this.evidenciaDialogoTipo === "UNIDAD" && this.evidenciaDialogoUnidad) {
                const unidad = this.evidenciaDialogoUnidad;
                await almacenBienesApi.subirEvidencia(unidad.idAlmacenBien, files);
                unidad.uiTotalEvidencias(unidad.uiTotalEvidencias() + files.length);
            }
            this.limpiarEvidenciasDialogo();
            this.uiDialogoEvidencia(false);
        } catch (err: any) {
            console.error("Error al subir evidencias:", err);
            this.uiError(err.message || "No se pudieron subir las evidencias.");
        } finally {
            this.uiGuardandoEvidencia(false);
        }
    };

    // ================================================================
    // DIÁLOGO — Componentes de una unidad "Conjunto"
    // ================================================================

    public cmdAbrirComponentes = (grupo: GrupoProcesamiento, unidad: UnidadProcesamiento): void => {
        this.componentesDialogoUnidad = unidad;
        this.uiDialogoComponentesTitulo(`Componentes — ${unidad.displayLabel} (${unidad.codigoInterno})`);

        const guardados = unidad.uiComponentesGuardados();
        const items: ComponenteCapturado[] = grupo.listaComponentesEsperados().map(c => {
            const previo = guardados.find(g => g.nombreComponente === c.nombre);
            return {
                nombreComponente: c.nombre,
                frmNumeroSerie: ko.observable<string>(previo?.numeroSerie || ""),
                evidencia: ko.observable<EvidenciaImagen | null>(null),
            };
        });
        this.listaComponentesDialogo(items);
        this.uiDialogoComponentes(true);
    };

    public onSelectComponenteFoto = (item: ComponenteCapturado, event: CFilePickerElement.ojSelect) => {
        const archivo = event.detail.files[0];
        if (!archivo) return;
        const previa = item.evidencia();
        if (previa) URL.revokeObjectURL(previa.previewUrl);
        item.evidencia({ id: generarIdCliente(), file: archivo, previewUrl: URL.createObjectURL(archivo) });
    };

    public cmdRemoverComponenteFoto = (item: ComponenteCapturado): void => {
        const previa = item.evidencia();
        if (previa) URL.revokeObjectURL(previa.previewUrl);
        item.evidencia(null);
    };

    private limpiarComponentesDialogo(): void {
        this.listaComponentesDialogo().forEach(c => {
            const e = c.evidencia();
            if (e) URL.revokeObjectURL(e.previewUrl);
        });
        this.listaComponentesDialogo([]);
    }

    public cmdCerrarComponentes = (): void => {
        if (this.uiGuardandoComponentes()) return;
        this.limpiarComponentesDialogo();
        this.uiDialogoComponentes(false);
    };

    public cmdGuardarComponentes = async (): Promise<void> => {
        if (!this.calcPuedeGuardarComponentes() || !this.componentesDialogoUnidad) return;

        this.uiGuardandoComponentes(true);
        this.uiError("");

        const unidad = this.componentesDialogoUnidad;
        const items = this.listaComponentesDialogo();
        const payload = items.map(c => ({
            nombreComponente: c.nombreComponente,
            numeroSerie: c.frmNumeroSerie().trim(),
        }));
        const files = items.map(c => c.evidencia()!.file);

        try {
            await almacenBienesApi.guardarComponentes(unidad.idAlmacenBien, payload, files);
            unidad.uiComponentesGuardados(payload.map(p => ({ ...p, tieneFoto: true })));
            this.limpiarComponentesDialogo();
            this.uiDialogoComponentes(false);
        } catch (err: any) {
            console.error("Error al guardar componentes:", err);
            this.uiError(err.message || "No se pudieron guardar los componentes.");
        } finally {
            this.uiGuardandoComponentes(false);
        }
    };

    // ================================================================
    // HELPERS
    // ================================================================
    private formatMonto(val: any): string {
        const n = parseFloat(val);
        if (isNaN(n)) return "—";
        return n.toLocaleString("es-MX", { style: "currency", currency: "MXN" });
    }

    public formatFecha(fecha: string | null): string {
        if (!fecha) return "—";
        const [year, month, day] = fecha.split("-");
        const meses = ["ene","feb","mar","abr","may","jun","jul","ago","sep","oct","nov","dic"];
        return `${parseInt(day)} ${meses[parseInt(month) - 1]} ${year}`;
    }
}

export = ProcesamientoViewModel;
