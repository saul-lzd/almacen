/**
 * ViewModel: Recepción de Contrato
 *
 * Registro de la recepción de bienes cuando llega el proveedor.
 * Captura: fecha/hora de llegada, representante del proveedor y cantidades recibidas.
 * Convención de nombres:
 * - frm*:  valores editables del formulario
 * - list*: arrays de datos
 * - ui*:   estado visual
 * - calc*: computed/calculados
 * - cmd*:  acciones desde UI
 * - load*: carga desde API
 * - map*:  transformaciones
 */

import * as AccUtils from "../accUtils";
import * as ko from "knockout";
import { mapEstatusToLabel } from "../utils/contratoUtils";

import "oj-c/form-layout";
import "oj-c/input-text";
import "oj-c/input-number";
import "oj-c/text-area";
import "oj-c/button";
import "oj-c/collapsible";

// ================================================================
// TIPOS
// ================================================================

type BienRecepcion = {
    idContratoBien: number;
    lote: number;
    partida: number;
    descripcion: string;      // primeros 100 chars del texto plano de descripcionTecnica
    unidadMedida: string;
    cantidadEsperada: number;
    cantidadRecibida: ko.Observable<number | null>;
    precioUnitario: number;
};

type ContratoRecepcion = {
    idContrato: number;
    numeroContrato: string;
    adquisicion: string;
    estatus: string;
    estatusLabel: string;
    proveedor: string;
    representanteProveedor: string;
    montoTotal: number | null;
    fechaTentativaLlegada: string | null;
    beneficiarios: string;
};

// ================================================================
// VIEWMODEL
// ================================================================

class RecepcionViewModel {

    private router: any;
    private contratoId: number | null = null;

    // ----------------------------------------------------------------
    // ESTADO
    // ----------------------------------------------------------------
    public uiCargando    = ko.observable<boolean>(false);
    public uiGuardando   = ko.observable<boolean>(false);
    public uiError       = ko.observable<string>("");
    public uiExito       = ko.observable<string>("");
    public uiModoLectura = ko.observable<boolean>(false);

    // ----------------------------------------------------------------
    // DATOS DEL CONTRATO (solo lectura)
    // ----------------------------------------------------------------
    public contrato    = ko.observable<ContratoRecepcion | null>(null);
    public listaBienes = ko.observableArray<BienRecepcion>([]);

    // ----------------------------------------------------------------
    // FORMULARIO DE RECEPCIÓN
    // ----------------------------------------------------------------
    public frmRepresentante  = ko.observable<string>("");
    public frmObservaciones  = ko.observable<string>("");

    // ----------------------------------------------------------------
    // COMPUTED
    // ----------------------------------------------------------------
    public calcTituloContrato = ko.pureComputed(() =>
        this.contrato()?.numeroContrato ?? "Cargando..."
    );

    // public calcTotalRecibido = ko.pureComputed(() =>
    //     this.listaBienes().reduce((sum, b) => {
    //         const rec = b.cantidadRecibida() ?? 0;
    //         return sum + rec * b.precioUnitario;
    //     }, 0)
    // );

    public calcPuedeConfirmar = ko.pureComputed(() => {
        if (!this.frmRepresentante().trim()) return false;
        if (this.listaBienes().length === 0) return false;
        return this.listaBienes().every(b =>
            b.cantidadRecibida() !== null && b.cantidadRecibida()! >= 0
        );
    });

    public calcHayDiferencias = ko.pureComputed(() =>
        this.listaBienes().some(b =>
            b.cantidadRecibida() !== null &&
            b.cantidadRecibida()! < b.cantidadEsperada
        )
    );

    public calcBienesCompletos = ko.pureComputed(() =>
        this.listaBienes().filter(b =>
            b.cantidadRecibida() !== null && b.cantidadRecibida()! > 0
        ).length
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
        AccUtils.announce("Recepción de contrato.");
        document.title = "Recepción — Gestión de Almacén";
        if (this.contratoId) {
            void this.loadContrato(this.contratoId);
        } else {
            this.uiError("No se especificó un contrato. Regresa a la lista del almacén.");
        }
    }

    disconnected(): void {}
    transitionCompleted(): void {}

    // ================================================================
    // LOAD
    // ================================================================
    private async loadContrato(id: number): Promise<void> {
        this.uiCargando(true);
        this.uiError("");

        try {
            const res = await fetch(`http://localhost:8080/api/contratos/${id}`);
            if (!res.ok) throw new Error(`Error al cargar contrato ${id}`);
            const data = await res.json();
            this.mapResponseToUI(data);

            const esEditable = data.estatus === "POR_RECIBIR" || data.estatus === "RECEPCION_PARCIAL";
            this.uiModoLectura(!esEditable);
            if (!esEditable) {
                await this.loadRecepcion(id);
            }
        } catch (err: any) {
            console.error("Error al cargar contrato:", err);
            this.uiError("No se pudo cargar el contrato. Intenta de nuevo.");
        } finally {
            this.uiCargando(false);
        }
    }

    private async loadRecepcion(id: number): Promise<void> {
        try {
            const res = await fetch(`http://localhost:8080/api/contratos/${id}/recepcion`);
            if (!res.ok) return; // sin recepción guardada — no sobreescribir
            const data = await res.json();
            this.frmRepresentante(data.nombreEntrega ?? "");
            this.frmObservaciones(data.observaciones ?? "");
        } catch (err) {
            console.warn("No se pudo cargar la recepción guardada:", err);
        }
    }

    // ================================================================
    // MAPPERS
    // ================================================================
    private mapResponseToUI(data: any): void {
        this.contrato({
            idContrato:             data.idContrato,
            numeroContrato:         data.numeroContrato,
            adquisicion:            data.adquisicion,
            estatus:                data.estatus,
            estatusLabel:           mapEstatusToLabel(data.estatus),
            proveedor:              data.proveedor?.razonSocial || "Sin proveedor asignado",
            representanteProveedor: data.proveedor?.representante || "",
            montoTotal:             data.montoTotal ?? null,
            fechaTentativaLlegada:  data.fechaTentativaLlegada
                ? data.fechaTentativaLlegada.split("T")[0]
                : null,
            beneficiarios: data.beneficiarios || "",
        });

        const esEntregaParcial = data.estatus === "RECEPCION_PARCIAL";

        const bienes: BienRecepcion[] = (data.bienes ?? [])
            .filter((b: any) => {
                if (!esEntregaParcial) return true;
                const pendiente = b.cantidad - (b.cantidadRecibidaTotal ?? 0);
                return pendiente > 0;
            })
            .map((b: any) => {
                const cantidadEsperada = esEntregaParcial
                    ? b.cantidad - (b.cantidadRecibidaTotal ?? 0)
                    : b.cantidad;
                return {
                    idContratoBien:   b.idContratoBien,
                    lote:             b.lote,
                    partida:          b.partida,
                    descripcion:      b.descripcionCorta ?? "",
                    unidadMedida:     b.unidadMedida || "—",
                    cantidadEsperada,
                    cantidadRecibida: ko.observable<number | null>(null),
                    precioUnitario:   b.precioUnitario ?? 0,
                };
            });
        this.listaBienes(bienes);

    }

    // ================================================================
    // COMMANDS
    // ================================================================
    public cmdRegresar = (): void => {
        this.router?.go({ path: "dashboard" });
    };

    public cmdConfirmarRecepcion = async (): Promise<void> => {
        if (!this.calcPuedeConfirmar() || !this.contratoId) return;

        this.uiGuardando(true);
        this.uiError("");
        this.uiExito("");

        const payload = {
            transportista:  this.frmRepresentante().trim(),
            observaciones:  this.frmObservaciones().trim() || null,
            bienes: this.listaBienes().map(b => ({
                idContratoBien:   b.idContratoBien,
                cantidadRecibida: b.cantidadRecibida() ?? 0,
            }))
        };

        try {
            const res = await fetch(
                `http://localhost:8080/api/contratos/${this.contratoId}/recepcion`,
                {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(payload)
                }
            );

            if (!res.ok) {
                const errData = await res.json().catch(() => null);
                throw new Error(errData?.message ?? `Error ${res.status}`);
            }

            this.uiExito("Recepción registrada. El contrato está ahora en almacén.");
            this.uiModoLectura(true);
            await this.loadContrato(this.contratoId);
        } catch (err: any) {
            console.error("Error al confirmar recepción:", err);
            this.uiError(err.message || "No se pudo registrar la recepción. Intenta de nuevo.");
        } finally {
            this.uiGuardando(false);
        }
    };

    // ================================================================
    // HELPERS — por bien
    // ================================================================
    public diferenciaBien(bien: BienRecepcion): ko.PureComputed<number | null> {
        return ko.pureComputed(() => {
            const rec = bien.cantidadRecibida();
            if (rec === null) return null;
            return rec - bien.cantidadEsperada;
        });
    }

    // ================================================================
    // HELPERS — formato
    // ================================================================
    public formatMonto(monto: number | null): string {
        if (monto === null || monto === undefined) return "—";
        return monto.toLocaleString("es-MX", {
            style: "currency",
            currency: "MXN",
            minimumFractionDigits: 2
        });
    }

    public formatFecha(fecha: string | null): string {
        if (!fecha) return "No especificada";
        const [year, month, day] = fecha.split("-");
        const meses = ["enero","febrero","marzo","abril","mayo","junio",
                       "julio","agosto","septiembre","octubre","noviembre","diciembre"];
        return `${parseInt(day)} de ${meses[parseInt(month) - 1]} de ${year}`;
    }

}

export = RecepcionViewModel;
