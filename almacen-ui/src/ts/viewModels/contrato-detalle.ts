import * as AccUtils from "../accUtils";
import * as ko from "knockout";
import { mapEstatusToLabel, mapEstatusToBadge } from "../utils/contratoUtils";
import "oj-c/button";
import "oj-c/dialog";
import "oj-c/input-text";
import "oj-c/input-number";
import "oj-c/text-area";
import "oj-c/form-layout";

// ================================================================
// TIPOS
// ================================================================

type ResumenBienes = {
    totalContratados: number;
    totalRecibidos: number;
    enProceso: number;
    procesados: number;
    listos: number;
    entregados: number;
};

type BienContrato = {
    idContratoBien: number;
    lote: number | null;
    partida: number | null;
    descripcionCorta: string;
    unidadMedida: string;
    cantidad: number;
    precioUnitario: string;
    subtotal: string;
    cantidadRecibidaTotal: number;
};

type BienDialogo = {
    idContratoBien: number;
    lote: number | null;
    partida: number | null;
    descripcion: string;
    unidadMedida: string;
    cantidadEsperada: number;
    cantidadRecibida: ko.Observable<number | null>;
    calcDiferencia: ko.PureComputed<number | null>;
};

type Contrato = {
    idContrato: number;
    numeroContrato: string;
    adquisicion: string;
    estatus: string;
    estatusLabel: string;
    estatusBadge: string;
    fechaTentativaLlegada: string;
    proveedor: string;
    beneficiarios: string;
    montoSinImpuestos: string;
    impuestos: string;
    montoTotal: string;
    resumenBienes: ResumenBienes;
    bienes: BienContrato[];
};

// ================================================================
// VIEWMODEL
// ================================================================

class ContratoDetalleViewModel {

    private router: any;
    private contratoId: number | null = null;

    // ── Estado general ──────────────────────────────────────────
    public uiCargando   = ko.observable<boolean>(false);
    public uiError      = ko.observable<string>("");
    public uiExito      = ko.observable<string>("");
    public uiAccion     = ko.observable<boolean>(false);

    // ── Datos ───────────────────────────────────────────────────
    public contrato     = ko.observable<Contrato | null>(null);

    // ── Fecha tentativa editable ─────────────────────────────────
    public frmFecha        = ko.observable<string>("");
    public uiEditandoFecha = ko.observable<boolean>(false);

    // ── Diálogo de recepción ────────────────────────────────────
    public uiDialogoRecepcion  = ko.observable<boolean>(false);
    public uiGuardandoRecepcion = ko.observable<boolean>(false);
    public uiErrorDialogo      = ko.observable<string>("");
    public frmTransportista    = ko.observable<string>("");
    public frmObservaciones    = ko.observable<string>("");
    public listaBienesDialogo  = ko.observableArray<BienDialogo>([]);

    public calcPuedeConfirmarRecepcion = ko.pureComputed(() => {
        if (!this.frmTransportista().trim()) return false;
        if (this.listaBienesDialogo().length === 0) return false;
        return this.listaBienesDialogo().every(b =>
            b.cantidadRecibida() !== null && b.cantidadRecibida()! >= 0
        );
    });

    public calcHayDiferencias = ko.pureComputed(() =>
        this.listaBienesDialogo().some(b =>
            b.cantidadRecibida() !== null && b.cantidadRecibida()! < b.cantidadEsperada
        )
    );

    // ── Rol del usuario (conectar con auth cuando esté implementado) ─
    private readonly userRole: string = localStorage.getItem("almacen.userRole") ?? "ALMACEN";
    public calcEsAdmin       = ko.pureComputed(() => this.userRole === "ADMIN");
    public calcEsAlmacenista = ko.pureComputed(() => this.userRole !== "ADMIN");

    // ── Computed: permisos según estatus Y ROL ────────────────────
    // Recibir, procesar y entregar → sólo ALMACEN
    public calcPuedeRecibirNuevo = ko.pureComputed(() => {
        if (!this.calcEsAlmacenista()) return false;
        const e = this.contrato()?.estatus;
        return e === "POR_RECIBIR" || e === "RECEPCION_PARCIAL";
    });

    public calcPuedeProcesar = ko.pureComputed(() => {
        if (!this.calcEsAlmacenista()) return false;
        const e = this.contrato()?.estatus;
        return e === "EN_ALMACEN" || e === "RECEPCION_PARCIAL";
    });

    // Autorizar entrega → sólo ADMIN
    public calcPuedeAutorizar = ko.pureComputed(() => {
        if (!this.calcEsAdmin()) return false;
        const r = this.contrato()?.resumenBienes;
        const e = this.contrato()?.estatus;
        if (!r) return false;
        return r.procesados > 0 && (e === "EN_ALMACEN" || e === "RECEPCION_PARCIAL");
    });

    // Entregar bienes → sólo ALMACEN
    public calcPuedeEntregar = ko.pureComputed(() => {
        if (!this.calcEsAlmacenista()) return false;
        return (this.contrato()?.resumenBienes?.listos ?? 0) > 0;
    });

    public calcFechaEditable = ko.pureComputed(() =>
        this.contrato()?.estatus === "POR_RECIBIR"
    );

    // ================================================================
    // LIFECYCLE
    // ================================================================
    constructor(params: any) {
        this.router = params?.router;
        const idParam = params?.routerState?.params?.id;
        if (idParam) this.contratoId = Number(idParam);
    }

    connected(): void {
        AccUtils.announce("Detalle del contrato.");
        document.title = "Detalle — Gestión de Almacén";
        if (this.contratoId) {
            void this.loadContrato(this.contratoId);
        } else {
            this.uiError("No se especificó un contrato.");
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
            if (!res.ok) throw new Error(`Error ${res.status}`);
            const c = await res.json();

            const r: ResumenBienes = c.resumenBienes ?? {
                totalContratados: 0, totalRecibidos: 0,
                enProceso: 0, procesados: 0, listos: 0, entregados: 0,
            };

            this.contrato({
                idContrato:       c.idContrato,
                numeroContrato:   c.numeroContrato,
                adquisicion:      c.adquisicion || "—",
                estatus:          c.estatus,
                estatusLabel:     mapEstatusToLabel(c.estatus),
                estatusBadge:     mapEstatusToBadge(c.estatus),
                fechaTentativaLlegada: this.formatFecha(c.fechaTentativaLlegada),
                proveedor:        c.proveedor?.razonSocial || "—",
                beneficiarios:    c.beneficiarios || "—",
                montoSinImpuestos: this.formatMonto(c.montoSinImpuestos),
                impuestos:        this.formatMonto(c.impuestos),
                montoTotal:       this.formatMonto(c.montoTotal),
                resumenBienes:    r,
                bienes: (c.bienes ?? []).map((b: any) => ({
                    idContratoBien:       b.idContratoBien,
                    lote:                 b.lote,
                    partida:              b.partida,
                    descripcionCorta:     b.descripcionCorta || "—",
                    unidadMedida:         b.unidadMedida || "—",
                    cantidad:             b.cantidad ?? 0,
                    precioUnitario:       this.formatMonto(b.precioUnitario),
                    subtotal:             this.formatMonto(b.subtotal),
                    cantidadRecibidaTotal: b.cantidadRecibidaTotal ?? 0,
                })),
            });

            if (c.fechaTentativaLlegada) {
                this.frmFecha(c.fechaTentativaLlegada.split("T")[0]);
            }

            document.title = `${c.numeroContrato} — Gestión de Almacén`;
        } catch (err: any) {
            console.error("Error al cargar contrato:", err);
            this.uiError("No se pudo cargar el contrato.");
        } finally {
            this.uiCargando(false);
        }
    }

    // ================================================================
    // COMMANDS — Navegación
    // ================================================================
    public cmdRegresar = (): void => {
        this.router?.go({ path: "dashboard" });
    };

    public cmdEditarContrato = (): void => {
        if (this.contratoId == null) return;
        document.body.style.cursor = "wait";
        this.router?.go({ path: "contrato", params: { id: this.contratoId } });
    };

    public cmdProcesarBienes = (): void => {
        if (!this.contratoId) return;
        this.router?.go({ path: "procesamiento", params: { id: this.contratoId } });
    };

    public cmdEntregarBienes = (): void => {
        if (!this.contratoId) return;
        this.router?.go({ path: "entrega", params: { id: this.contratoId } });
    };

    // ================================================================
    // COMMANDS — Autorizar entrega
    // ================================================================
    public cmdAutorizarEntrega = async (): Promise<void> => {
        if (!this.contratoId) return;
        this.uiAccion(true);
        this.uiError("");
        this.uiExito("");
        try {
            const res = await fetch(
                `http://localhost:8080/api/contratos/${this.contratoId}/autorizar-entrega`,
                { method: "PATCH" }
            );
            if (!res.ok) {
                const err = await res.json().catch(() => null);
                throw new Error(err?.errores?.[0] ?? err?.mensaje ?? `Error ${res.status}`);
            }
            this.uiExito("Entrega autorizada. Los bienes están listos para ser entregados.");
            await this.loadContrato(this.contratoId);
        } catch (err: any) {
            this.uiError(err.message || "No se pudo autorizar la entrega.");
        } finally {
            this.uiAccion(false);
        }
    };

    // ================================================================
    // COMMANDS — Fecha tentativa
    // ================================================================
    public cmdGuardarFecha = async (): Promise<void> => {
        if (!this.contratoId || !this.frmFecha().trim()) return;
        this.uiAccion(true);
        this.uiError("");
        try {
            const res = await fetch(
                `http://localhost:8080/api/contratos/${this.contratoId}/fecha-tentativa`,
                {
                    method: "PATCH",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ fechaTentativaLlegada: this.frmFecha() + "T00:00:00" }),
                }
            );
            if (!res.ok) {
                const err = await res.json().catch(() => null);
                throw new Error(err?.errores?.[0] ?? err?.mensaje ?? `Error ${res.status}`);
            }
            this.uiEditandoFecha(false);
            this.uiExito("Fecha actualizada.");
            await this.loadContrato(this.contratoId);
        } catch (err: any) {
            this.uiError(err.message || "No se pudo actualizar la fecha.");
        } finally {
            this.uiAccion(false);
        }
    };

    // ================================================================
    // COMMANDS — Diálogo de recepción
    // ================================================================
    public cmdAbrirRecepcion = (): void => {
        if (!this.contrato()) return;
        const c = this.contrato()!;
        const esParcial = c.estatus === "RECEPCION_PARCIAL";

        // Solo muestra bienes con pendiente > 0
        const bienes: BienDialogo[] = c.bienes
            .filter(b => {
                const pendiente = b.cantidad - b.cantidadRecibidaTotal;
                return pendiente > 0;
            })
            .map(b => {
                const pendiente = esParcial
                    ? b.cantidad - b.cantidadRecibidaTotal
                    : b.cantidad;
                const cantidadRecibida = ko.observable<number | null>(null);
                return {
                    idContratoBien:  b.idContratoBien,
                    lote:            b.lote,
                    partida:         b.partida,
                    descripcion:     b.descripcionCorta,
                    unidadMedida:    b.unidadMedida,
                    cantidadEsperada: pendiente,
                    cantidadRecibida,
                    calcDiferencia: ko.pureComputed(() => {
                        const rec = cantidadRecibida();
                        return rec === null ? null : rec - pendiente;
                    }),
                };
            });

        this.listaBienesDialogo(bienes);
        this.frmTransportista("");
        this.frmObservaciones("");
        this.uiErrorDialogo("");
        this.uiDialogoRecepcion(true);
    };

    public cmdCerrarRecepcion = (): void => {
        if (this.uiGuardandoRecepcion()) return;
        this.uiDialogoRecepcion(false);
    };

    public cmdConfirmarRecepcion = async (): Promise<void> => {
        if (!this.calcPuedeConfirmarRecepcion() || !this.contratoId) return;

        this.uiGuardandoRecepcion(true);
        this.uiErrorDialogo("");

        const payload = {
            transportista:  this.frmTransportista().trim(),
            observaciones:  this.frmObservaciones().trim() || null,
            bienes: this.listaBienesDialogo().map(b => ({
                idContratoBien:   b.idContratoBien,
                cantidadRecibida: b.cantidadRecibida() ?? 0,
            })),
        };

        try {
            const res = await fetch(
                `http://localhost:8080/api/contratos/${this.contratoId}/recepcion`,
                {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(payload),
                }
            );

            if (!res.ok) {
                const err = await res.json().catch(() => null);
                throw new Error(err?.errores?.[0] ?? err?.mensaje ?? `Error ${res.status}`);
            }

            this.uiDialogoRecepcion(false);
            this.uiExito("Recepción registrada correctamente.");
            await this.loadContrato(this.contratoId);
        } catch (err: any) {
            console.error("Error al registrar recepción:", err);
            this.uiErrorDialogo(err.message || "No se pudo registrar la recepción.");
        } finally {
            this.uiGuardandoRecepcion(false);
        }
    };

    // ================================================================
    // HELPERS
    // ================================================================
    private formatFecha(val: string | null | undefined): string {
        if (!val) return "—";
        const d = new Date(val);
        if (isNaN(d.getTime())) return val;
        return d.toLocaleDateString("es-MX", { day: "numeric", month: "long", year: "numeric" });
    }

    private formatMonto(val: number | null | undefined): string {
        if (val == null) return "—";
        return new Intl.NumberFormat("es-MX", { style: "currency", currency: "MXN" }).format(val);
    }
}

export = ContratoDetalleViewModel;
