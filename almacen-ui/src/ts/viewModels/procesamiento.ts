import * as AccUtils from "../accUtils";
import * as ko from "knockout";
import { mapEstatusToLabel, mapEstatusToBadge } from "../utils/contratoUtils";
import { contratosApi, almacenBienesApi } from "../utils/api";

import "oj-c/form-layout";
import "oj-c/input-text";
import "oj-c/button";

// ================================================================
// TIPOS
// ================================================================

type UnidadProcesamiento = {
    idAlmacenBien: number;
    codigoInterno: string;
    folioRecepcion: string;
    fechaRecepcion: string | null;
    esVehiculo: boolean;
    frmNumeroSerie: ko.Observable<string>;
    frmNumeroMotor: ko.Observable<string>;
    frmMarca: ko.Observable<string>;
    frmModelo: ko.Observable<string>;
    frmDescripcion: ko.Observable<string>;
    uiEstatus: ko.Observable<string>;
    uiEditando: ko.Observable<boolean>;
    uiEditable: ko.PureComputed<boolean>;
    uiGuardando: ko.Observable<boolean>;
};

type GrupoProcesamiento = {
    idContratoBien: number;
    lote: number | null;
    partida: number | null;
    descripcion: string;
    unidadMedida: string;
    totalUnidades: number;
    esVehiculo: boolean;
    unidades: UnidadProcesamiento[];
    uiModoBloque: ko.Observable<boolean>;
    uiEditandoBloque: ko.Observable<boolean>;
    frmBulkMarca: ko.Observable<string>;
    frmBulkModelo: ko.Observable<string>;
    frmBulkDescripcion: ko.Observable<string>;
    calcProcesados: ko.PureComputed<number>;
    calcTodosProcesados: ko.PureComputed<boolean>;
    uiGuardandoGrupo: ko.Observable<boolean>;
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
};

// ================================================================
// VIEWMODEL
// ================================================================

class ProcesamientoViewModel {

    private router: any;
    private contratoId: number | null = null;

    public uiCargando  = ko.observable<boolean>(false);
    public uiError     = ko.observable<string>("");
    public uiExito     = ko.observable<string>("");

    public contrato    = ko.observable<ContratoResumen | null>(null);
    public listaGrupos = ko.observableArray<GrupoProcesamiento>([]);

    // Cuenta RECIBIDO + EN_PROCESO (ambos son pendientes de procesamiento)
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
        if (idParam) {
            this.contratoId = Number(idParam);
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
            const [dataContrato, dataGrupos] = await Promise.all([
                contratosApi.obtenerPorId(id),
                contratosApi.obtenerBienesAlmacen(id)
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
            });

            const grupos: GrupoProcesamiento[] = dataGrupos.map(g => {
                const esVehiculo = (g.unidadMedida as string).toLowerCase().includes("veh");

                const unidades: UnidadProcesamiento[] = g.unidades.map((u: any) => {
                    const uiEstatus   = ko.observable<string>(u.estatus);
                    const uiEditando  = ko.observable<boolean>(false);
                    return {
                        idAlmacenBien:  u.idAlmacenBien,
                        codigoInterno:  u.codigoInterno,
                        folioRecepcion: u.folioRecepcion || "—",
                        fechaRecepcion: u.fechaRecepcion ? u.fechaRecepcion.split("T")[0] : null,
                        esVehiculo,
                        frmNumeroSerie: ko.observable<string>(u.numeroSerie || ""),
                        frmNumeroMotor: ko.observable<string>(u.numeroMotor || ""),
                        frmMarca:       ko.observable<string>(u.marca || ""),
                        frmModelo:      ko.observable<string>(u.modelo || ""),
                        frmDescripcion: ko.observable<string>(u.descripcionComplementaria || ""),
                        uiEstatus,
                        uiEditando,
                        uiEditable: ko.pureComputed(() => uiEstatus() !== "PROCESADO" || uiEditando()),
                        uiGuardando: ko.observable<boolean>(false),
                    };
                });

                const calcProcesados = ko.pureComputed(() =>
                    unidades.filter(u => u.uiEstatus() === "PROCESADO").length
                );

                return {
                    idContratoBien: g.idContratoBien,
                    lote:           g.lote,
                    partida:        g.partida,
                    descripcion:    g.descripcion,
                    unidadMedida:   g.unidadMedida,
                    esVehiculo,
                    totalUnidades:  g.totalUnidades,
                    unidades,
                    uiModoBloque:      ko.observable<boolean>(false),
                    uiEditandoBloque:  ko.observable<boolean>(false),
                    frmBulkMarca:      ko.observable<string>(""),
                    frmBulkModelo:     ko.observable<string>(""),
                    frmBulkDescripcion: ko.observable<string>(""),
                    calcProcesados,
                    calcTodosProcesados: ko.pureComputed(() =>
                        calcProcesados() === unidades.length
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

    // Guarda datos del bien sin marcarlo como procesado.
    // RECIBIDO → EN_PROCESO automáticamente; EN_PROCESO permanece; PROCESADO → EN_PROCESO.
    public cmdGuardarDatos = async (unidad: UnidadProcesamiento): Promise<void> => {
        unidad.uiGuardando(true);
        this.uiError("");

        const payload: any = {};
        if (unidad.frmNumeroSerie().trim())  payload.numeroSerie  = unidad.frmNumeroSerie().trim();
        if (unidad.frmNumeroMotor().trim())  payload.numeroMotor  = unidad.frmNumeroMotor().trim();
        if (unidad.frmMarca().trim())        payload.marca        = unidad.frmMarca().trim();
        if (unidad.frmModelo().trim())       payload.modelo       = unidad.frmModelo().trim();
        if (unidad.frmDescripcion().trim())  payload.descripcionComplementaria = unidad.frmDescripcion().trim();

        try {
            await almacenBienesApi.guardarDatos(unidad.idAlmacenBien, payload);
            unidad.uiEstatus("EN_PROCESO");
            unidad.uiEditando(false);
        } catch (err: any) {
            console.error("Error al guardar datos:", err);
            this.uiError(err.message || "No se pudieron guardar los datos.");
        } finally {
            unidad.uiGuardando(false);
        }
    };

    // Marca el bien como PROCESADO de forma explícita.
    // Solo válido cuando el bien está EN_PROCESO.
    public cmdProcesarUnidad = async (unidad: UnidadProcesamiento): Promise<void> => {
        unidad.uiGuardando(true);
        this.uiError("");

        const payload: any = {};
        if (unidad.frmNumeroSerie().trim())  payload.numeroSerie  = unidad.frmNumeroSerie().trim();
        if (unidad.frmNumeroMotor().trim())  payload.numeroMotor  = unidad.frmNumeroMotor().trim();
        if (unidad.frmMarca().trim())        payload.marca        = unidad.frmMarca().trim();
        if (unidad.frmModelo().trim())       payload.modelo       = unidad.frmModelo().trim();
        if (unidad.frmDescripcion().trim())  payload.descripcionComplementaria = unidad.frmDescripcion().trim();

        try {
            await almacenBienesApi.procesarUnidad(unidad.idAlmacenBien, payload);
            unidad.uiEstatus("PROCESADO");
            unidad.uiEditando(false);
        } catch (err: any) {
            console.error("Error al procesar unidad:", err);
            this.uiError(err.message || "No se pudo procesar el bien.");
        } finally {
            unidad.uiGuardando(false);
        }
    };

    // Activa el modo de edición en un bien ya PROCESADO.
    public cmdEditarUnidad = (unidad: UnidadProcesamiento): void => {
        unidad.uiEditando(true);
    };

    // Modo lote: aplica datos compartidos y marca todos los pendientes como PROCESADO.
    // El backend acepta RECIBIDO y EN_PROCESO, por lo que no se necesita el paso intermedio.
    public cmdAplicarBloque = async (grupo: GrupoProcesamiento): Promise<void> => {
        const pendientes = grupo.uiEditandoBloque()
            ? grupo.unidades  // re-edición: aplica a todos
            : grupo.unidades.filter(u => u.uiEstatus() !== "PROCESADO");

        if (pendientes.length === 0) return;

        grupo.uiGuardandoGrupo(true);
        this.uiError("");

        const marca      = grupo.frmBulkMarca().trim();
        const modelo     = grupo.frmBulkModelo().trim();
        const descripcion = grupo.frmBulkDescripcion().trim();

        const bloquePayload: any = { ids: pendientes.map(u => u.idAlmacenBien) };
        if (marca)      bloquePayload.marca = marca;
        if (modelo)     bloquePayload.modelo = modelo;
        if (descripcion) bloquePayload.descripcionComplementaria = descripcion;

        try {
            await almacenBienesApi.procesarBloque(bloquePayload);
            pendientes.forEach(u => {
                u.uiEstatus("PROCESADO");
                if (marca)      u.frmMarca(marca);
                if (modelo)     u.frmModelo(modelo);
                if (descripcion) u.frmDescripcion(descripcion);
            });
            grupo.uiEditandoBloque(false);
        } catch (err: any) {
            console.error("Error al procesar bloque:", err);
            this.uiError(err.message || "No se pudo procesar el bloque de bienes.");
        } finally {
            grupo.uiGuardandoGrupo(false);
        }
    };

    // ================================================================
    // HELPERS
    // ================================================================
    public formatFecha(fecha: string | null): string {
        if (!fecha) return "—";
        const [year, month, day] = fecha.split("-");
        const meses = ["ene","feb","mar","abr","may","jun","jul","ago","sep","oct","nov","dic"];
        return `${parseInt(day)} ${meses[parseInt(month) - 1]} ${year}`;
    }
}

export = ProcesamientoViewModel;
