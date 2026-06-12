import * as AccUtils from "../accUtils";
import * as ko from "knockout";
import { mapEstatusToLabel, mapEstatusToBadge } from "../utils/contratoUtils";
import { getRole } from "../utils/auth";
import { contratosApi, almacenBienesApi } from "../utils/api";
import "oj-c/dialog";

import "oj-c/form-layout";
import "oj-c/input-text";
import "oj-c/button";
import "oj-c/progress-bar";
import "ojs/ojswitch";

// ================================================================
// TIPOS
// ================================================================

type UnidadProcesamiento = {
    idAlmacenBien: number;
    codigoInterno: string;
    displayLabel: string;
    frmNumeroSerie: ko.Observable<string>;
    uiEstatus: ko.Observable<string>;
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
    uiRequiereNumeroSerie: ko.Observable<boolean>;
    calcProcesados: ko.PureComputed<number>;
    calcTodosProcesados: ko.PureComputed<boolean>;
    calcListoParaFinalizar: ko.PureComputed<boolean>;
    uiGuardandoGrupo: ko.Observable<boolean>;
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
        } else {
            this.uiError("No se especificó un contrato.");
        }
    }

    disconnected(): void {}
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
                const unidades: UnidadProcesamiento[] = g.unidades.map((u: any, uIdx: number) => ({
                    idAlmacenBien:  u.idAlmacenBien,
                    codigoInterno:  u.codigoInterno,
                    displayLabel:   `Unidad ${uIdx + 1}`,
                    frmNumeroSerie: ko.observable<string>(u.numeroSerie || ""),
                    uiEstatus:      ko.observable<string>(u.estatus),
                }));

                const calcProcesados = ko.pureComputed(() =>
                    unidades.filter(u => u.uiEstatus() === "PROCESADO").length
                );

                const frmMarca              = ko.observable<string>((g.unidades[0]?.marca as string) || "");
                const frmModelo             = ko.observable<string>((g.unidades[0]?.modelo as string) || "");
                const uiRequiereNumeroSerie = ko.observable<boolean>(
                    g.unidades.some((u: any) => !!u.numeroSerie) as boolean
                );

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
                    calcProcesados,
                    calcTodosProcesados: ko.pureComputed(() =>
                        calcProcesados() === unidades.length
                    ),
                    calcListoParaFinalizar: ko.pureComputed(() => {
                        if (!frmMarca().trim() && !frmModelo().trim()) return false;
                        if (uiRequiereNumeroSerie()) {
                            return unidades.every(u => !!u.frmNumeroSerie().trim());
                        }
                        return true;
                    }),
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
                if (grupo.uiRequiereNumeroSerie() && u.frmNumeroSerie().trim()) {
                    payload.numeroSerie = u.frmNumeroSerie().trim();
                }
                return almacenBienesApi.guardarDatos(u.idAlmacenBien, payload).then(() => {
                    u.uiEstatus("EN_PROCESO");
                });
            }));
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
            await Promise.all(pendientes.map(u => {
                const payload: any = {};
                if (marca)  payload.marca  = marca;
                if (modelo) payload.modelo = modelo;
                if (grupo.uiRequiereNumeroSerie() && u.frmNumeroSerie().trim()) {
                    payload.numeroSerie = u.frmNumeroSerie().trim();
                }
                return almacenBienesApi.procesarUnidad(u.idAlmacenBien, payload).then(() => {
                    u.uiEstatus("PROCESADO");
                });
            }));
        } catch (err: any) {
            console.error("Error al confirmar grupo:", err);
            this.uiError(err.message || "No se pudo confirmar el procesamiento.");
        } finally {
            grupo.uiGuardandoGrupo(false);
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
