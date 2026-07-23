import * as AccUtils from "../accUtils";
import * as ko from "knockout";
import { mapEstatusToLabel, mapEstatusToBadge } from "../utils/contratoUtils";
import { getRole } from "../utils/auth";
import { contratosApi, almacenBienesApi, contratoBienesApi, catalogosApi } from "../utils/api";
import { CFilePickerElement } from 'oj-c/file-picker';
import "../jet-composites/quill-editor/quill-editor";
import "oj-c/dialog";

import "oj-c/form-layout";
import "oj-c/input-text";
import "oj-c/button";
import "oj-c/progress-bar";
import "oj-c/file-picker";

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

/** Fila del formulario de captura de componentes de una unidad "Conjunto" (Sección C). */
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

type EstadoGrupo = "PENDIENTE" | "CONFIGURADO" | "COMPLETADO";

type GrupoProcesamiento = {
    idContratoBien: number;
    lote: number | null;
    partida: number | null;
    descripcion: string;
    descripcionCompleta: string;
    unidadMedida: string;
    totalUnidades: number;
    unidades: UnidadProcesamiento[];
    frmMarca: ko.Observable<string>;
    frmModelo: ko.Observable<string>;
    /** NINGUNO | SIMPLE | CONJUNTO — la última configuración YA guardada en servidor */
    uiTipoCapturaGuardado: ko.Observable<string>;
    listaComponentesEsperados: ko.ObservableArray<ComponenteEsperadoItem>;
    uiTotalEvidenciasGrupo: ko.Observable<number>;
    calcEsVehiculo: ko.PureComputed<boolean>;
    calcProcesados: ko.PureComputed<number>;
    calcTodosProcesados: ko.PureComputed<boolean>;
    calcListoParaFinalizar: ko.PureComputed<boolean>;
    uiGuardandoGrupo: ko.Observable<boolean>;
    uiGuardandoConfig: ko.Observable<boolean>;

    // ── Navegación / estado (sidebar + tabs) ──
    uiTabActiva: ko.Observable<"CONFIGURACION" | "CAPTURA">;
    calcEstadoGrupo: ko.PureComputed<EstadoGrupo>;

    // ── Wizard de configuración (estado propio por grupo, ya no un diálogo compartido) ──
    wizPaso1: ko.Observable<"SI" | "NO" | null>;
    wizPaso2: ko.Observable<"SIMPLE" | "CONJUNTO" | null>;
    wizComponentes: ko.ObservableArray<ComponenteEsperadoItem>;
    calcWizEsConjunto: ko.PureComputed<boolean>;
    calcWizPuedeConfirmar: ko.PureComputed<boolean>;

    // ── Sección C — entrada secuencial (Simple / Vehículo / Conjunto) ──
    /** idAlmacenBien en edición vía "Editar" de la tabla; null = la próxima unidad sin llenar */
    secEditandoUnidadId: ko.Observable<number | null>;
    secFrmNumero: ko.Observable<string>;
    secFrmEvidencias: ko.ObservableArray<EvidenciaImagen>;
    secFrmComponentes: ko.ObservableArray<ComponenteCapturado>;
    /** Filas ya capturadas para la tabla — filtro puro sobre `unidades`, nunca se muta con push/remove */
    calcFilasCapturadas: ko.PureComputed<UnidadProcesamiento[]>;
    uiGuardandoSecFila: ko.Observable<boolean>;

    // ── Sección B — grid de fotos generales del bien (inline, ya no diálogo) ──
    wizSecBFotos: ko.ObservableArray<EvidenciaImagen>;
    uiSubiendoFotoGrupo: ko.Observable<boolean>;
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

    // ── Sidebar — grupo seleccionado ────────────────────────────────
    public uiGrupoSeleccionadoId = ko.observable<number | null>(null);

    public calcGrupoSeleccionado = ko.pureComputed<GrupoProcesamiento | null>(() =>
        this.listaGrupos().find(g => g.idContratoBien === this.uiGrupoSeleccionadoId()) || null
    );

    public cmdSeleccionarGrupo = (grupo: GrupoProcesamiento): void => {
        this.uiGrupoSeleccionadoId(grupo.idContratoBien);
    };

    public cmdCambiarTab = (grupo: GrupoProcesamiento, tab: "CONFIGURACION" | "CAPTURA"): void => {
        if (tab === "CAPTURA" && grupo.calcEstadoGrupo() === "PENDIENTE") return;
        grupo.uiTabActiva(tab);
    };

    // ── Diálogo de descripción técnica completa del bien ───────────
    public uiBienDescripcionDialogOpen   = ko.observable<boolean>(false);
    public uiDescripcionBienSeleccionada = ko.observable<string>("");
    public uiTituloBienSeleccionado      = ko.observable<string>("");

    public cmdVerDescripcionCompleta = (grupo: GrupoProcesamiento): void => {
        this.uiTituloBienSeleccionado(`Lote ${grupo.lote ?? "—"} · Partida ${grupo.partida ?? "—"}`);
        this.uiDescripcionBienSeleccionada(grupo.descripcionCompleta);
        this.uiBienDescripcionDialogOpen(true);
    };

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

    public readonly listaPaso1Options = [
        { value: "SI", label: "Sí, requiere número de serie" },
        { value: "NO", label: "No requiere número de serie" },
    ];

    public listaCatalogoComponentes = ko.observableArray<string>([]);
    private seqComponenteEsperado = 0;

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
        this.listaGrupos().forEach(g => {
            g.secFrmEvidencias().forEach(e => URL.revokeObjectURL(e.previewUrl));
            g.secFrmComponentes().forEach(c => {
                const e = c.evidencia();
                if (e) URL.revokeObjectURL(e.previewUrl);
            });
            g.wizSecBFotos().forEach(e => URL.revokeObjectURL(e.previewUrl));
        });
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

            const descripcionCompletaPorBien = new Map<number, string>(
                (dataContrato.bienes || []).map((b: any) => [b.idContratoBien, b.descripcionTecnica || ""])
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

                const calcTodosProcesados = ko.pureComputed(() =>
                    calcProcesados() === unidades.length
                );

                // Heurística LOCKED: "Pendiente" solo si nunca se tocó nada de este grupo.
                // No se puede distinguir en servidor "nunca configurado" de "configurado
                // explícitamente como NINGUNO" (columna NOT NULL, default NINGUNO) — decisión
                // explícita del usuario de aceptar este límite en vez de tocar el esquema.
                const calcSinTocar = ko.pureComputed(() =>
                    !esVehiculo &&
                    uiTipoCapturaGuardado() === "NINGUNO" &&
                    frmMarca().trim() === "" && frmModelo().trim() === "" &&
                    unidades.every(u => u.uiEstatus() === "RECIBIDO" && u.frmNumeroSerie().trim() === "") &&
                    uiTotalEvidenciasGrupo() === 0
                );

                const calcEstadoGrupo = ko.pureComputed<EstadoGrupo>(() => {
                    if (calcTodosProcesados()) return "COMPLETADO";
                    if (esVehiculo) return "CONFIGURADO";
                    return calcSinTocar() ? "PENDIENTE" : "CONFIGURADO";
                });

                const estadoInicial = calcEstadoGrupo();

                const wizPaso1 = ko.observable<"SI" | "NO" | null>(
                    (esVehiculo || estadoInicial === "PENDIENTE")
                        ? null
                        : (tipoCapturaSerieInicial === "NINGUNO" ? "NO" : "SI")
                );
                const wizPaso2 = ko.observable<"SIMPLE" | "CONJUNTO" | null>(
                    (tipoCapturaSerieInicial === "SIMPLE" || tipoCapturaSerieInicial === "CONJUNTO")
                        ? (tipoCapturaSerieInicial as "SIMPLE" | "CONJUNTO")
                        : null
                );
                const wizComponentes = ko.observableArray<ComponenteEsperadoItem>(
                    listaComponentesEsperados().map(c => ({ ...c }))
                );
                const calcWizEsConjunto = ko.pureComputed(() => wizPaso2() === "CONJUNTO");
                const calcWizPuedeConfirmar = ko.pureComputed(() => {
                    if (wizPaso1() === null) return false;
                    if (wizPaso1() === "NO") return true;
                    if (wizPaso2() === null) return false;
                    if (wizPaso2() === "CONJUNTO") {
                        return wizComponentes().some(c => c.nombre.trim().length > 0);
                    }
                    return true;
                });

                const calcFilasCapturadas = ko.pureComputed(() => {
                    if (uiTipoCapturaGuardado() === "CONJUNTO") {
                        return unidades.filter(u => u.uiComponentesGuardados().length > 0);
                    }
                    return unidades.filter(u => u.frmNumeroSerie().trim() !== "");
                });

                const grupo: GrupoProcesamiento = {
                    idContratoBien:      g.idContratoBien,
                    lote:                g.lote,
                    partida:             g.partida,
                    descripcion:         g.descripcion,
                    descripcionCompleta: descripcionCompletaPorBien.get(g.idContratoBien) || "",
                    unidadMedida:        g.unidadMedida,
                    totalUnidades:       g.totalUnidades,
                    unidades,
                    frmMarca,
                    frmModelo,
                    uiTipoCapturaGuardado,
                    listaComponentesEsperados,
                    uiTotalEvidenciasGrupo,
                    calcEsVehiculo: ko.pureComputed(() => esVehiculo),
                    calcProcesados,
                    calcTodosProcesados,
                    calcListoParaFinalizar: ko.pureComputed(() =>
                        unidades.every(u => u.uiEstatus() === "EN_PROCESO" || u.uiEstatus() === "PROCESADO")
                        && calcEvidenciaCompleta()
                    ),
                    uiGuardandoGrupo:  ko.observable<boolean>(false),
                    uiGuardandoConfig: ko.observable<boolean>(false),

                    uiTabActiva: ko.observable<"CONFIGURACION" | "CAPTURA">(
                        (esVehiculo || estadoInicial !== "PENDIENTE") ? "CAPTURA" : "CONFIGURACION"
                    ),
                    calcEstadoGrupo,

                    wizPaso1,
                    wizPaso2,
                    wizComponentes,
                    calcWizEsConjunto,
                    calcWizPuedeConfirmar,

                    secEditandoUnidadId: ko.observable<number | null>(null),
                    secFrmNumero:        ko.observable<string>(""),
                    secFrmEvidencias:    ko.observableArray<EvidenciaImagen>([]),
                    secFrmComponentes:   ko.observableArray<ComponenteCapturado>(
                        tipoCapturaSerieInicial === "CONJUNTO" ? this.filasComponentesVacias(listaComponentesEsperados()) : []
                    ),
                    calcFilasCapturadas,
                    uiGuardandoSecFila: ko.observable<boolean>(false),

                    wizSecBFotos:        ko.observableArray<EvidenciaImagen>([]),
                    uiSubiendoFotoGrupo: ko.observable<boolean>(false),
                };

                grupo.wizPaso2.subscribe(tipo => {
                    if (tipo === "CONJUNTO" && grupo.wizComponentes().length === 0) {
                        this.cmdAgregarComponenteWizard(grupo);
                    }
                });

                return grupo;
            });

            this.listaGrupos(grupos);
            this.uiGrupoSeleccionadoId(grupos.length > 0 ? grupos[0].idContratoBien : null);
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

    // Guarda marca y modelo sin marcar como PROCESADO. El número de serie/motor
    // y los componentes se guardan al vuelo vía cmdSiguienteCaptura (Sección C).
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
    // PESTAÑA 1 — Wizard de configuración de tipo de captura de serie
    // ================================================================

    public cmdSetWizPaso1 = (grupo: GrupoProcesamiento, valor: "SI" | "NO"): void => {
        grupo.wizPaso1(valor);
    };

    public cmdSetWizPaso2 = (grupo: GrupoProcesamiento, valor: "SIMPLE" | "CONJUNTO"): void => {
        if (grupo.wizPaso1() !== "SI") return;
        grupo.wizPaso2(valor);
    };

    public cmdAgregarComponenteWizard = (grupo: GrupoProcesamiento): void => {
        grupo.wizComponentes([
            ...grupo.wizComponentes(),
            { idLocal: this.seqComponenteEsperado++, nombre: "" },
        ]);
    };

    public cmdEliminarComponenteWizard = (grupo: GrupoProcesamiento, item: ComponenteEsperadoItem): void => {
        grupo.wizComponentes(grupo.wizComponentes().filter(c => c.idLocal !== item.idLocal));
    };

    public cmdSetNombreComponenteWizard = (grupo: GrupoProcesamiento, item: ComponenteEsperadoItem, valor: string): void => {
        const idx = grupo.wizComponentes().findIndex(c => c.idLocal === item.idLocal);
        if (idx === -1) return;
        grupo.wizComponentes.splice(idx, 1, { ...item, nombre: valor });
    };

    public cmdConfirmarConfigCaptura = async (grupo: GrupoProcesamiento): Promise<void> => {
        if (!grupo.calcWizPuedeConfirmar()) return;

        const tipo = grupo.wizPaso1() === "NO" ? "NINGUNO" : grupo.wizPaso2()!;
        const nombres = grupo.wizComponentes()
            .map(c => c.nombre.trim())
            .filter(n => n.length > 0);

        if (tipo === "CONJUNTO" && nombres.length === 0) {
            this.uiError("Agrega al menos un componente antes de confirmar.");
            return;
        }

        await this.guardarCapturaSerie(grupo, tipo, nombres);
    };

    private async guardarCapturaSerie(grupo: GrupoProcesamiento, tipo: string, nombres: string[]): Promise<void> {
        grupo.uiGuardandoConfig(true);
        this.uiError("");

        try {
            await contratoBienesApi.definirCapturaSerie(grupo.idContratoBien, {
                tipoCapturaSerie: tipo,
                componentes: tipo === "CONJUNTO" ? nombres : [],
            });
            grupo.uiTipoCapturaGuardado(tipo);
            grupo.listaComponentesEsperados(
                nombres.map(nombre => ({ idLocal: this.seqComponenteEsperado++, nombre }))
            );
            grupo.secFrmComponentes(
                tipo === "CONJUNTO" ? this.filasComponentesVacias(grupo.listaComponentesEsperados()) : []
            );
            void this.loadCatalogoComponentes();
            grupo.uiTabActiva("CAPTURA");
            this.uiExito("Configuración guardada.");
            setTimeout(() => this.uiExito(""), 3000);
        } catch (err: any) {
            console.error("Error al guardar configuración de captura:", err);
            this.uiError(err.message || "No se pudo guardar la configuración.");
        } finally {
            grupo.uiGuardandoConfig(false);
        }
    }

    // ================================================================
    // PESTAÑA 2 — SECCIÓN B — fotos generales del bien (grid inline)
    // ================================================================

    public cmdSeleccionarFotoGrupo = (grupo: GrupoProcesamiento, event: CFilePickerElement.ojSelect): void => {
        const archivos = Array.from(event.detail.files);
        if (archivos.length === 0) return;
        void this.subirFotosGrupo(grupo, archivos);
    };

    private async subirFotosGrupo(grupo: GrupoProcesamiento, archivos: File[]): Promise<void> {
        grupo.uiSubiendoFotoGrupo(true);
        this.uiError("");
        try {
            await contratoBienesApi.subirEvidencia(grupo.idContratoBien, archivos);
            const nuevas: EvidenciaImagen[] = archivos.map(file => ({
                id: generarIdCliente(),
                file,
                previewUrl: URL.createObjectURL(file),
            }));
            grupo.wizSecBFotos([...grupo.wizSecBFotos(), ...nuevas]);
            grupo.uiTotalEvidenciasGrupo(grupo.uiTotalEvidenciasGrupo() + archivos.length);
        } catch (err: any) {
            console.error("Error al subir evidencia de grupo:", err);
            this.uiError(err.message || "No se pudieron subir las fotos.");
        } finally {
            grupo.uiSubiendoFotoGrupo(false);
        }
    }

    // ================================================================
    // PESTAÑA 2 — SECCIÓN C — entrada secuencial (Simple / Vehículo / Conjunto)
    // ================================================================

    public cmdEditarFilaCaptura = (grupo: GrupoProcesamiento, unidad: UnidadProcesamiento): void => {
        grupo.secEditandoUnidadId(unidad.idAlmacenBien);
        grupo.secFrmEvidencias().forEach(e => URL.revokeObjectURL(e.previewUrl));
        grupo.secFrmEvidencias([]);

        if (grupo.uiTipoCapturaGuardado() === "CONJUNTO") {
            const guardados = unidad.uiComponentesGuardados();
            grupo.secFrmComponentes(
                grupo.listaComponentesEsperados().map(c => {
                    const previo = guardados.find(g => g.nombreComponente === c.nombre);
                    return {
                        nombreComponente: c.nombre,
                        frmNumeroSerie: ko.observable<string>(previo?.numeroSerie || ""),
                        evidencia: ko.observable<EvidenciaImagen | null>(null),
                    };
                })
            );
        } else {
            grupo.secFrmNumero(unidad.frmNumeroSerie());
        }
    };

    public cmdSeleccionarFotoSecuencial = (grupo: GrupoProcesamiento, event: CFilePickerElement.ojSelect): void => {
        const archivos = Array.from(event.detail.files);
        const nuevas: EvidenciaImagen[] = archivos.map(file => ({
            id: generarIdCliente(),
            file,
            previewUrl: URL.createObjectURL(file),
        }));
        const combinadas = [...grupo.secFrmEvidencias(), ...nuevas];
        grupo.secFrmEvidencias(grupo.calcEsVehiculo() ? combinadas : combinadas.slice(0, 1));
    };

    public cmdRemoverFotoSecuencial = (grupo: GrupoProcesamiento, foto: EvidenciaImagen): void => {
        URL.revokeObjectURL(foto.previewUrl);
        grupo.secFrmEvidencias(grupo.secFrmEvidencias().filter(f => f.id !== foto.id));
    };

    public cmdSeleccionarFotoComponenteSecuencial = (comp: ComponenteCapturado, event: CFilePickerElement.ojSelect): void => {
        const archivo = event.detail.files[0];
        if (!archivo) return;
        const previa = comp.evidencia();
        if (previa) URL.revokeObjectURL(previa.previewUrl);
        comp.evidencia({ id: generarIdCliente(), file: archivo, previewUrl: URL.createObjectURL(archivo) });
    };

    public cmdRemoverFotoComponenteSecuencial = (comp: ComponenteCapturado): void => {
        const previa = comp.evidencia();
        if (previa) URL.revokeObjectURL(previa.previewUrl);
        comp.evidencia(null);
    };

    public cmdSiguienteCaptura = async (grupo: GrupoProcesamiento): Promise<void> => {
        const unidad = this.resolverUnidadObjetivo(grupo);
        if (!unidad) {
            this.uiError("Ya se capturaron todas las unidades de este bien.");
            return;
        }

        const esVehiculo = grupo.calcEsVehiculo();
        const esConjunto  = grupo.uiTipoCapturaGuardado() === "CONJUNTO";

        if (esConjunto) {
            const filas = grupo.secFrmComponentes();
            const completo = filas.length > 0 && filas.every(c => !!c.frmNumeroSerie().trim() && c.evidencia() !== null);
            if (!completo) {
                this.uiError("Completa el número de serie y la foto de cada componente.");
                return;
            }
        } else {
            const numero = grupo.secFrmNumero().trim();
            if (!numero) {
                this.uiError(esVehiculo ? "Captura el número de motor." : "Captura el número de serie.");
                return;
            }
            const minFotos = esVehiculo ? this.minEvidenciasVehiculo : this.minEvidenciasSimple;
            // Al editar una unidad que ya tenía fotos guardadas, no se exige repetirlas si no se seleccionaron nuevas.
            if (grupo.secFrmEvidencias().length + unidad.uiTotalEvidencias() < minFotos) {
                this.uiError(`Se requiere${minFotos > 1 ? "n" : ""} al menos ${minFotos} foto${minFotos > 1 ? "s" : ""}.`);
                return;
            }
        }

        grupo.uiGuardandoSecFila(true);
        this.uiError("");

        try {
            if (esConjunto) {
                const filas = grupo.secFrmComponentes();
                const payload = filas.map(c => ({ nombreComponente: c.nombreComponente, numeroSerie: c.frmNumeroSerie().trim() }));
                const files = filas.map(c => c.evidencia()!.file);
                await almacenBienesApi.guardarComponentes(unidad.idAlmacenBien, payload, files);
                unidad.uiComponentesGuardados(payload.map(p => ({ ...p, tieneFoto: true })));
            } else {
                const numero = grupo.secFrmNumero().trim();
                const payload: any = esVehiculo ? { numeroMotor: numero } : { numeroSerie: numero };
                const files = grupo.secFrmEvidencias().map(e => e.file);
                await almacenBienesApi.guardarDatos(unidad.idAlmacenBien, payload);
                if (files.length > 0) {
                    await almacenBienesApi.subirEvidencia(unidad.idAlmacenBien, files);
                    unidad.uiTotalEvidencias(unidad.uiTotalEvidencias() + files.length);
                }
                unidad.frmNumeroSerie(numero);
            }
            if (unidad.uiEstatus() === "RECIBIDO") unidad.uiEstatus("EN_PROCESO");
            this.limpiarFormularioSeccionC(grupo);
            this.uiExito("Datos guardados correctamente.");
            setTimeout(() => this.uiExito(""), 3000);
        } catch (err: any) {
            console.error("Error al capturar unidad:", err);
            this.uiError(err.message || "No se pudo guardar la captura.");
        } finally {
            grupo.uiGuardandoSecFila(false);
        }
    };

    private resolverUnidadObjetivo(grupo: GrupoProcesamiento): UnidadProcesamiento | null {
        if (grupo.secEditandoUnidadId() != null) {
            return grupo.unidades.find(u => u.idAlmacenBien === grupo.secEditandoUnidadId()) ?? null;
        }
        if (grupo.uiTipoCapturaGuardado() === "CONJUNTO") {
            return grupo.unidades.find(u => u.uiComponentesGuardados().length < grupo.listaComponentesEsperados().length) ?? null;
        }
        return grupo.unidades.find(u => u.frmNumeroSerie().trim() === "") ?? null;
    }

    private limpiarFormularioSeccionC(grupo: GrupoProcesamiento): void {
        grupo.secEditandoUnidadId(null);
        grupo.secFrmNumero("");
        grupo.secFrmEvidencias().forEach(e => URL.revokeObjectURL(e.previewUrl));
        grupo.secFrmEvidencias([]);
        grupo.secFrmComponentes().forEach(c => {
            const e = c.evidencia();
            if (e) URL.revokeObjectURL(e.previewUrl);
        });
        grupo.secFrmComponentes(
            grupo.uiTipoCapturaGuardado() === "CONJUNTO" ? this.filasComponentesVacias(grupo.listaComponentesEsperados()) : []
        );
    }

    private filasComponentesVacias(esperados: ComponenteEsperadoItem[]): ComponenteCapturado[] {
        return esperados.map(c => ({
            nombreComponente: c.nombre,
            frmNumeroSerie: ko.observable<string>(""),
            evidencia: ko.observable<EvidenciaImagen | null>(null),
        }));
    }

    // ================================================================
    // HELPERS
    // ================================================================

    /** Genera un arreglo [0..n) para renderizar N placeholders con oj-bind-for-each. */
    public rangoArray = (n: number): number[] => Array.from({ length: Math.max(0, n) }, (_, i) => i);

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
