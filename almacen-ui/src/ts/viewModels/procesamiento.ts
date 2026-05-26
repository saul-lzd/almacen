/**
 * ViewModel: Procesamiento de Bienes
 *
 * Captura de datos individuales (número de serie, marca, modelo) para cada
 * unidad física recibida en almacén. Soporta dos modos:
 *   - Individual: un formulario por unidad (electrónicos, vehículos)
 *   - Por lote:   marca/modelo compartidos aplicados a todas las unidades del grupo
 *
 * Convención de nombres:
 * - frm*:  valores editables del formulario
 * - ui*:   estado visual
 * - calc*: computed/calculados
 * - cmd*:  acciones desde UI
 * - load*: carga desde API
 */

import * as AccUtils from "../accUtils";
import * as ko from "knockout";
import { mapEstatusToLabel } from "../utils/contratoUtils";

import "oj-c/form-layout";
import "oj-c/input-text";
import "oj-c/button";

// ================================================================
// TIPOS
// ================================================================

type UnidadProcesamiento = {
    idAlmacenBien: number;
    codigoInterno: string;
    estatus: string;
    folioRecepcion: string;
    fechaRecepcion: string | null;
    esVehiculo: boolean;
    frmNumeroSerie: ko.Observable<string>;
    frmNumeroMotor: ko.Observable<string>;
    frmMarca: ko.Observable<string>;
    frmModelo: ko.Observable<string>;
    frmDescripcion: ko.Observable<string>;
    uiGuardando: ko.Observable<boolean>;
    uiProcesado: ko.Observable<boolean>;
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
    proveedor: string;
    beneficiarios: string;
};

// ================================================================
// VIEWMODEL
// ================================================================

class ProcesamientoViewModel {

    private router: any;
    private contratoId: number | null = null;

    // ----------------------------------------------------------------
    // ESTADO
    // ----------------------------------------------------------------
    public uiCargando  = ko.observable<boolean>(false);
    public uiError     = ko.observable<string>("");
    public uiExito     = ko.observable<string>("");

    // ----------------------------------------------------------------
    // DATOS
    // ----------------------------------------------------------------
    public contrato    = ko.observable<ContratoResumen | null>(null);
    public listaGrupos = ko.observableArray<GrupoProcesamiento>([]);

    // ----------------------------------------------------------------
    // COMPUTED GLOBALES
    // ----------------------------------------------------------------
    public calcTotalRecibidos = ko.pureComputed(() =>
        this.listaGrupos().reduce((sum, g) =>
            sum + g.unidades.filter(u => !u.uiProcesado()).length, 0)
    );

    public calcTodoCompleto = ko.pureComputed(() =>
        this.listaGrupos().length > 0 && this.calcTotalRecibidos() === 0
    );

    // ----------------------------------------------------------------
    // CONSTRUCTOR / LIFECYCLE
    // ----------------------------------------------------------------
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
            const [resContrato, resBienes] = await Promise.all([
                fetch(`http://localhost:8080/api/contratos/${id}`),
                fetch(`http://localhost:8080/api/contratos/${id}/almacen-bienes`)
            ]);

            if (!resContrato.ok) throw new Error(`Error al cargar contrato ${id}`);
            if (!resBienes.ok) throw new Error(`Error al cargar bienes del contrato ${id}`);

            const dataContrato = await resContrato.json();
            const dataGrupos: any[] = await resBienes.json();

            this.contrato({
                idContrato:   dataContrato.idContrato,
                numeroContrato: dataContrato.numeroContrato,
                adquisicion:  dataContrato.adquisicion,
                estatus:      dataContrato.estatus,
                estatusLabel: mapEstatusToLabel(dataContrato.estatus),
                proveedor:    dataContrato.proveedor?.razonSocial || "Sin proveedor asignado",
                beneficiarios: dataContrato.beneficiarios || "",
            });

            const grupos: GrupoProcesamiento[] = dataGrupos.map(g => {
                const esVehiculo = (g.unidadMedida as string).toLowerCase().includes("veh");

                const unidades: UnidadProcesamiento[] = g.unidades.map((u: any) => ({
                    idAlmacenBien:  u.idAlmacenBien,
                    codigoInterno:  u.codigoInterno,
                    estatus:        u.estatus,
                    folioRecepcion: u.folioRecepcion || "—",
                    fechaRecepcion: u.fechaRecepcion ? u.fechaRecepcion.split("T")[0] : null,
                    esVehiculo,
                    frmNumeroSerie: ko.observable<string>(u.numeroSerie || ""),
                    frmNumeroMotor: ko.observable<string>(u.numeroMotor || ""),
                    frmMarca:       ko.observable<string>(u.marca || ""),
                    frmModelo:      ko.observable<string>(u.modelo || ""),
                    frmDescripcion: ko.observable<string>(u.descripcionComplementaria || ""),
                    uiGuardando:    ko.observable<boolean>(false),
                    uiProcesado:    ko.observable<boolean>(u.estatus === "PROCESADO"),
                }));

                const calcProcesados = ko.pureComputed(() =>
                    unidades.filter(u => u.uiProcesado()).length
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
                    uiModoBloque:     ko.observable<boolean>(false),
                    frmBulkMarca:     ko.observable<string>(""),
                    frmBulkModelo:    ko.observable<string>(""),
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
        this.router?.go({ path: "almacen" });
    };

    public cmdGuardarGrupo = async (grupo: GrupoProcesamiento): Promise<void> => {
        const pendientes = grupo.unidades.filter(u => !u.uiProcesado());
        if (pendientes.length === 0) return;

        grupo.uiGuardandoGrupo(true);
        this.uiError("");

        try {
            await Promise.all(pendientes.map(async unidad => {
                const payload: any = {};
                if (unidad.frmNumeroSerie().trim())  payload.numeroSerie  = unidad.frmNumeroSerie().trim();
                if (unidad.frmNumeroMotor().trim())  payload.numeroMotor  = unidad.frmNumeroMotor().trim();
                if (unidad.frmMarca().trim())        payload.marca        = unidad.frmMarca().trim();
                if (unidad.frmModelo().trim())       payload.modelo       = unidad.frmModelo().trim();
                if (unidad.frmDescripcion().trim())  payload.descripcionComplementaria = unidad.frmDescripcion().trim();

                const res = await fetch(
                    `http://localhost:8080/api/almacen-bienes/${unidad.idAlmacenBien}/procesar`,
                    { method: "PATCH", headers: { "Content-Type": "application/json" }, body: JSON.stringify(payload) }
                );

                if (!res.ok) {
                    const errData = await res.json().catch(() => null);
                    throw new Error(errData?.errores?.[0] ?? errData?.mensaje ?? `Error ${res.status}`);
                }

                unidad.uiProcesado(true);
                unidad.estatus = "PROCESADO";
            }));
        } catch (err: any) {
            console.error("Error al guardar grupo:", err);
            this.uiError(err.message || "No se pudieron guardar los cambios.");
        } finally {
            grupo.uiGuardandoGrupo(false);
        }
    };

    public cmdAplicarBloque = async (grupo: GrupoProcesamiento): Promise<void> => {
        const pendientes = grupo.unidades.filter(u => !u.uiProcesado());
        if (pendientes.length === 0) return;

        this.uiError("");

        const payload: any = {
            ids: pendientes.map(u => u.idAlmacenBien),
        };
        if (grupo.frmBulkMarca().trim())       payload.marca        = grupo.frmBulkMarca().trim();
        if (grupo.frmBulkModelo().trim())       payload.modelo       = grupo.frmBulkModelo().trim();
        if (grupo.frmBulkDescripcion().trim())  payload.descripcionComplementaria = grupo.frmBulkDescripcion().trim();

        try {
            const res = await fetch(
                `http://localhost:8080/api/almacen-bienes/procesar-bloque`,
                {
                    method: "PATCH",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(payload)
                }
            );

            if (!res.ok) {
                const errData = await res.json().catch(() => null);
                const msg = errData?.errores?.[0] ?? errData?.mensaje ?? `Error ${res.status}`;
                throw new Error(msg);
            }

            pendientes.forEach(u => {
                u.uiProcesado(true);
                u.frmMarca(grupo.frmBulkMarca());
                u.frmModelo(grupo.frmBulkModelo());
                u.frmDescripcion(grupo.frmBulkDescripcion());
            });
        } catch (err: any) {
            console.error("Error al procesar bloque:", err);
            this.uiError(err.message || "No se pudo procesar el bloque de bienes.");
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
