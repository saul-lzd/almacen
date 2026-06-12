import * as AccUtils from "../accUtils";
import * as ko from "knockout";
import { mapEstatusToLabel, mapEstatusToBadge } from "../utils/contratoUtils";
import { getRole } from "../utils/auth";
import { contratosApi } from "../utils/api";
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
    cantidadProcesadaTotal: number;
    cantidadEntregadaTotal: number;
};

type RecepcionItem = {
    idRecepcionAlmacen: number;
    folio: string;
    numero: number;
    titulo: string;
    fechaRecepcion: string;
    transportista: string;
    almacenista: string;
    estatus: string;
    estatusLabel: string;
    estatusDot: string;
    totalBienes: number;
    totalProcesados: number;
    progresoPct: number;
    progresoLabel: string;
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
    todoRecibido: ko.Observable<boolean>;
};

type ClavePresupuestal = {
    clave: string;
    partidaEspecifica: string;
    montoAsignado: string;
    montoAsignadoNum: number;
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
    clavesPresupuestales: ClavePresupuestal[];
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
    public contrato         = ko.observable<Contrato | null>(null);
    public listRecepciones  = ko.observableArray<RecepcionItem>([]);
    public uiCargandoRecepciones = ko.observable<boolean>(false);

    // ── Fecha tentativa editable ─────────────────────────────────
    public frmFecha        = ko.observable<string>("");
    public uiEditandoFecha = ko.observable<boolean>(false);

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
    private readonly userRole: string = getRole() ?? "ALMACEN";
    public calcEsAdmin       = ko.pureComputed(() => this.userRole === "ADMINISTRADOR");
    public calcEsAlmacenista = ko.pureComputed(() => this.userRole !== "ADMINISTRADOR");

    // ── Computed: permisos según estatus Y ROL ────────────────────
    // Recibir, procesar y entregar → sólo ALMACEN
    public calcPuedeRecibirNuevo = ko.pureComputed(() => {
        if (!this.calcEsAlmacenista()) return false;
        const e = this.contrato()?.estatus;
        if (e !== "POR_RECIBIR" && e !== "RECEPCION_PARCIAL") return false;
        const r = this.contrato()?.resumenBienes;
        if (r && r.totalRecibidos >= r.totalContratados && r.totalContratados > 0) return false;
        return true;
    });

    public calcPuedeProcesar = ko.pureComputed(() => {
        if (!this.calcEsAlmacenista()) return false;
        return (this.contrato()?.resumenBienes?.enProceso ?? 0) > 0;
    });

    // Autorizar entrega → sólo ADMIN
    public calcPuedeAutorizar = ko.pureComputed(() => {
        if (!this.calcEsAdmin()) return false;
        const r = this.contrato()?.resumenBienes;
        if (!r) return false;
        return r.procesados > 0 && this.contrato()?.estatus === "POR_RECIBIR";
    });

    // Entregar bienes → sólo ALMACEN
    public calcPuedeEntregar = ko.pureComputed(() => {
        if (!this.calcEsAlmacenista()) return false;
        return (this.contrato()?.resumenBienes?.listos ?? 0) > 0;
    });

    public calcFechaEditable = ko.pureComputed(() =>
        this.calcEsAdmin() &&
        this.contrato()?.estatus !== "CAPTURA" &&
        this.listRecepciones().length === 0
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
            void this.loadRecepciones(this.contratoId);
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
            const c = await contratosApi.obtenerPorId(id);

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
                clavesPresupuestales: (c.clavesPresupuestales ?? []).map((cl: any) => ({
                    clave:             cl.clave || "—",
                    partidaEspecifica: cl.partidaEspecifica || "—",
                    montoAsignadoNum:  parseFloat(cl.montoAsignado) || 0,
                    montoAsignado:     this.formatMonto(cl.montoAsignado),
                })),
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
                    cantidadRecibidaTotal:  b.cantidadRecibidaTotal ?? 0,
                    cantidadProcesadaTotal: b.cantidadProcesadaTotal ?? 0,
                    cantidadEntregadaTotal: b.cantidadEntregadaTotal ?? 0,
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
    public cmdGoBack = (): void => {
        this.router?.go({ path: "dashboard" });
    };

    public cmdActualizar = (): void => {
        if (this.contratoId) {
            void this.loadContrato(this.contratoId);
            void this.loadRecepciones(this.contratoId);
        }
    };

    public cmdEditarContrato = (): void => {
        if (this.contratoId == null) return;
        document.body.style.cursor = "wait";
        this.router?.go({ path: "contrato", params: { id: this.contratoId } });
    };

    public cmdProcesarBienes = (recepcion: { idRecepcionAlmacen: number }): void => {
        if (!this.contratoId) return;
        this.router?.go({ path: "procesamiento", params: { id: this.contratoId, recepcionId: recepcion.idRecepcionAlmacen } });
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
            await contratosApi.autorizarEntrega(this.contratoId);
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
            await contratosApi.actualizarFechaTentativa(this.contratoId, { fechaTentativaLlegada: this.frmFecha() + "T00:00:00" });
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
                const todoRecibido = ko.observable<boolean>(false);
                todoRecibido.subscribe((checked: boolean) => {
                    cantidadRecibida(checked ? pendiente : null);
                });
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
                    todoRecibido,
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
            await contratosApi.registrarRecepcion(this.contratoId, payload);
            this.uiDialogoRecepcion(false);
            this.uiExito("Recepción registrada correctamente.");
            await this.loadContrato(this.contratoId);
            void this.loadRecepciones(this.contratoId);
        } catch (err: any) {
            console.error("Error al registrar recepción:", err);
            this.uiErrorDialogo(err.message || "No se pudo registrar la recepción.");
        } finally {
            this.uiGuardandoRecepcion(false);
        }
    };

    // ================================================================
    // LOAD — Recepciones
    // ================================================================
    private async loadRecepciones(id: number): Promise<void> {
        this.uiCargandoRecepciones(true);
        try {
            const data: any[] = await contratosApi.listarRecepciones(id);
            const items: RecepcionItem[] = data.map((r, idx) => {
                const num = data.length - idx;
                const dot = this.estatusToDot(r.estatus);
                const pct = r.totalBienes > 0
                    ? Math.round(r.totalProcesados / r.totalBienes * 100)
                    : 0;
                return {
                    idRecepcionAlmacen: r.idRecepcionAlmacen,
                    folio:              r.folio,
                    numero:             num,
                    titulo:             `Recepción ${num} — ${r.totalBienes} bien${r.totalBienes !== 1 ? "es" : ""}`,
                    fechaRecepcion:     this.formatFecha(r.fechaRecepcion),
                    transportista:      r.transportista || "—",
                    almacenista:        r.almacenista || "—",
                    estatus:            r.estatus,
                    estatusLabel:       this.estatusRecepcionToLabel(r.estatus),
                    estatusDot:         dot,
                    totalBienes:        r.totalBienes,
                    totalProcesados:    r.totalProcesados,
                    progresoPct:        pct,
                    progresoLabel:      `${r.totalProcesados} / ${r.totalBienes} procesados`,
                };
            });
            this.listRecepciones(items);
        } catch (err: any) {
            console.error("Error al cargar recepciones:", err);
            this.uiError("No se pudieron cargar las recepciones: " + (err.message || "error desconocido"));
        } finally {
            this.uiCargandoRecepciones(false);
        }
    }

    // ================================================================
    // COMMANDS — Recepciones
    // ================================================================
    public cmdVerDetallesRecepcion = (_recepcion: RecepcionItem): void => {
        // stub — navigation or detail dialog to be implemented
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

    private estatusToDot(estatus: string): string {
        switch (estatus) {
            case "PROCESADA":  return "green";
            case "ENTREGADA":  return "blue";
            case "EN_PROCESO": return "amber";
            default:           return "gray";
        }
    }

    private estatusRecepcionToLabel(estatus: string): string {
        switch (estatus) {
            case "INICIADA":   return "Iniciada";
            case "EN_PROCESO": return "En proceso";
            case "PROCESADA":  return "Procesada";
            case "ENTREGADA":  return "Entregada";
            default:           return estatus;
        }
    }

    private formatMonto(val: number | null | undefined): string {
        if (val == null) return "—";
        return new Intl.NumberFormat("es-MX", { style: "currency", currency: "MXN" }).format(val);
    }
}

export = ContratoDetalleViewModel;
