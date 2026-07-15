import * as AccUtils from "../accUtils";
import * as ko from "knockout";
import { mapEstatusToLabel, mapEstatusToBadge } from "../utils/contratoUtils";
import { getRole } from "../utils/auth";
import { contratosApi } from "../utils/api";
import { CFilePickerElement } from 'oj-c/file-picker';
import "oj-c/button";
import "oj-c/dialog";
import "oj-c/input-text";
import "oj-c/input-number";
import "oj-c/text-area";
import "oj-c/form-layout";
import "oj-c/file-picker";

// crypto.randomUUID solo existe en "contextos seguros" (HTTPS o localhost) —
// en un deploy por HTTP plano sobre IP (sin dominio/TLS) el navegador no
// expone la función y truena "crypto.randomUUID is not a function", sobre
// todo notorio en móvil al capturar evidencia con la cámara.
function generarIdCliente(): string {
    if (typeof crypto !== "undefined" && typeof crypto.randomUUID === "function") {
        return crypto.randomUUID();
    }
    return `${Date.now().toString(36)}-${Math.random().toString(36).slice(2)}`;
}

// ================================================================
// TIPOS
// ================================================================

type ResumenBienes = {
    totalContratados: number;
    totalRecibidos: number;
    enProceso: number;
    procesados: number;
    pendientesAutorizar: number;
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
    totalPendientesAutorizar: number;
    pendientesAutorizarPct: number;
    pendientesAutorizarLabel: string;
    totalListos: number;
    listosPct: number;
    listosLabel: string;
    totalEntregados: number;
    entregasPct: number;
    entregasLabel: string;
};

type EvidenciaImagen = {
    id: string;
    file: File;
    previewUrl: string;
};

type BienDialogo = {
    idContratoBien: number;
    lote: number | null;
    partida: number | null;
    descripcion: string;
    unidadMedida: string;
    cantidadFaltante: number;
    cantidadRecibida: ko.Observable<number | null>;
    calcDiferencia: ko.PureComputed<number | null>;
    todoRecibido: ko.Observable<boolean>;
};

type UnidadDetalle = {
    idAlmacenBien: number;
    codigoInterno: string;
    numeroSerie: string | null;
    numeroMotor: string | null;
    marca: string | null;
    modelo: string | null;
    estatus: string;
    estatusLabel: string;
    estatusDot: string;
};

type GrupoDetalle = {
    idContratoBien: number;
    lote: number | null;
    partida: number | null;
    descripcion: string;
    unidadMedida: string;
    totalUnidades: number;
    unidades: UnidadDetalle[];
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
    primeraEntregaAutorizada: boolean;
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

    // ── Diálogo de detalle de recepción ────────────────────────
    public uiDialogoDetalle     = ko.observable<boolean>(false);
    public uiCargandoDetalle    = ko.observable<boolean>(false);
    public uiErrorDetalle       = ko.observable<string>("");
    public detalleTitulo        = ko.observable<string>("");
    public listaGruposDetalle   = ko.observableArray<GrupoDetalle>([]);

    // ── Diálogo de recepción ────────────────────────────────────
    public uiDialogoRecepcion  = ko.observable<boolean>(false);
    public uiGuardandoRecepcion = ko.observable<boolean>(false);
    public uiErrorDialogo      = ko.observable<string>("");
    public frmTransportista    = ko.observable<string>("");
    public frmObservaciones    = ko.observable<string>("");
    public listaBienesDialogo  = ko.observableArray<BienDialogo>([]);

    // ── Evidencias fotográficas de la recepción ─────────────────
    // Mínimo requerido para poder confirmar — no es un tope, se pueden agregar más.
    public readonly minEvidencias = 5;
    public listaEvidencias     = ko.observableArray<EvidenciaImagen>([]);

    public calcEvidenciaContador = ko.pureComputed(() =>
        `${this.listaEvidencias().length} / ${this.minEvidencias}`
    );

    public calcFaltanEvidencias = ko.pureComputed(() =>
        this.listaEvidencias().length < this.minEvidencias
    );

    public onSelectImageListener = (event: CFilePickerElement.ojSelect) => {
        const archivos = Array.from(event.detail.files);

        const nuevasImgs: EvidenciaImagen[] = archivos.map(file => ({
            id: generarIdCliente(),
            file,
            previewUrl: URL.createObjectURL(file),
        }));

        // oj-bind-for-each solo detecta reemplazos completos del array, no mutaciones
        // incrementales (push/splice) — por eso se reconstruye el array completo aquí.
        this.listaEvidencias([...this.listaEvidencias(), ...nuevasImgs]);
    };

    public cmdRemoverEvidencia = (evidencia: EvidenciaImagen): void => {
        URL.revokeObjectURL(evidencia.previewUrl);
        this.listaEvidencias(this.listaEvidencias().filter(e => e.id !== evidencia.id));
    };

    private limpiarEvidencias(): void {
        this.listaEvidencias().forEach(e => URL.revokeObjectURL(e.previewUrl));
        this.listaEvidencias([]);
    }

    public calcPuedeConfirmarRecepcion = ko.pureComputed(() => {
        if (!this.frmTransportista().trim()) return false;
        if (this.listaBienesDialogo().length === 0) return false;
        if (this.listaEvidencias().length < this.minEvidencias) return false;
        return this.listaBienesDialogo().every(b =>
            b.cantidadRecibida() !== null && b.cantidadRecibida()! >= 0
        );
    });

    public calcHayDiferencias = ko.pureComputed(() =>
        this.listaBienesDialogo().some(b =>
            b.cantidadRecibida() !== null && b.cantidadRecibida()! < b.cantidadFaltante
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

    // Autorizar entrega → sólo ADMIN; visible siempre que no sea CAPTURA
    public calcPuedeAutorizar = ko.pureComputed(() => {
        if (!this.calcEsAdmin()) return false;
        return this.contrato()?.estatus !== "CAPTURA" && this.contrato() !== null;
    });

    // Habilitado cuando hay al menos una recepción PROCESADA con bienes aún no autorizados.
    // El backend no cambia el estatus de la recepción al autorizar, pero sí mueve
    // los bienes de procesados → listos; por eso chequeamos totalProcesados > 0.
    public calcAutorizarDisabled = ko.pureComputed(() => {
        if (this.uiAccion()) return true;
        if (!this.contrato()) return true;
        return !this.listRecepciones().some(r =>
            r.estatus === "PROCESADA" && r.totalProcesados > 0
        );
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
            console.log("[contrato-detalle] API response:", JSON.stringify({
                estatus: c.estatus,
                primeraEntregaAutorizada: c.primeraEntregaAutorizada,
                resumenBienes: c.resumenBienes,
            }, null, 2));

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
                resumenBienes: { ...r, pendientesAutorizar: r.pendientesAutorizar ?? 0 },
                primeraEntregaAutorizada: c.primeraEntregaAutorizada ?? false,
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
            await Promise.all([
                this.loadContrato(this.contratoId),
                this.loadRecepciones(this.contratoId),
            ]);
            console.log("[autorizar] recepciones tras autorizar:", this.listRecepciones().map(r => ({ id: r.idRecepcionAlmacen, estatus: r.estatus, totalProcesados: r.totalProcesados, totalBienes: r.totalBienes })));
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

        // Solo muestra bienes con pendiente > 0
        const bienes: BienDialogo[] = c.bienes
            .filter(b => {
                const pendiente = b.cantidad - b.cantidadRecibidaTotal;
                return pendiente > 0;
            })
            .map(b => {
                const pendiente = b.cantidad - b.cantidadRecibidaTotal;
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
                    cantidadFaltante: pendiente,
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
        this.limpiarEvidencias();
        this.uiDialogoRecepcion(true);
    };

    public cmdCerrarRecepcion = (): void => {
        if (this.uiGuardandoRecepcion()) return;
        this.limpiarEvidencias();
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

        const evidencias = this.listaEvidencias().map(e => e.file);

        try {
            await contratosApi.registrarRecepcion(this.contratoId, payload, evidencias);
            this.uiDialogoRecepcion(false);
            this.limpiarEvidencias();
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
                const pendientesAutorizarPct = r.totalBienes > 0
                    ? Math.round(r.totalPendientesAutorizar / r.totalBienes * 100)
                    : 0;
                const listosPct = r.totalBienes > 0
                    ? Math.round(r.totalListos / r.totalBienes * 100)
                    : 0;
                const entregasPct = r.totalBienes > 0
                    ? Math.round(r.totalEntregados / r.totalBienes * 100)
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
                    totalProcesados:          r.totalProcesados,
                    progresoPct:              pct,
                    progresoLabel:            `${r.totalProcesados} / ${r.totalBienes} procesados`,
                    totalPendientesAutorizar: r.totalPendientesAutorizar,
                    pendientesAutorizarPct,
                    pendientesAutorizarLabel: `${r.totalPendientesAutorizar} / ${r.totalBienes} por autorizar`,
                    totalListos:              r.totalListos,
                    listosPct,
                    listosLabel:        `${r.totalListos} / ${r.totalBienes} listos`,
                    totalEntregados:    r.totalEntregados,
                    entregasPct,
                    entregasLabel:      `${r.totalEntregados} / ${r.totalBienes} entregados`,
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
    public cmdVerDetallesRecepcion = async (recepcion: RecepcionItem): Promise<void> => {
        if (!this.contratoId) return;
        this.detalleTitulo(recepcion.titulo);
        this.listaGruposDetalle([]);
        this.uiErrorDetalle("");
        this.uiDialogoDetalle(true);
        this.uiCargandoDetalle(true);
        try {
            const data: any[] = await contratosApi.obtenerBienesDetalleRecepcion(
                this.contratoId, recepcion.idRecepcionAlmacen
            );
            const grupos: GrupoDetalle[] = data.map(g => ({
                idContratoBien: g.idContratoBien,
                lote:           g.lote,
                partida:        g.partida,
                descripcion:    g.descripcion,
                unidadMedida:   g.unidadMedida,
                totalUnidades:  g.totalUnidades,
                unidades: (g.unidades ?? []).map((u: any) => ({
                    idAlmacenBien: u.idAlmacenBien,
                    codigoInterno: u.codigoInterno,
                    numeroSerie:   u.numeroSerie || null,
                    numeroMotor:   u.numeroMotor || null,
                    marca:         u.marca || null,
                    modelo:        u.modelo || null,
                    estatus:       u.estatus,
                    estatusLabel:  this.estatusUnidadToLabel(u.estatus),
                    estatusDot:    this.estatusUnidadToDot(u.estatus),
                })),
            }));
            this.listaGruposDetalle(grupos);
        } catch (err: any) {
            this.uiErrorDetalle("No se pudieron cargar los detalles de la recepción.");
        } finally {
            this.uiCargandoDetalle(false);
        }
    };

    // ================================================================
    // HELPERS
    // ================================================================
    private estatusUnidadToDot(estatus: string): string {
        switch (estatus) {
            case "RECIBIDO":            return "gray";
            case "EN_PROCESO":          return "amber";
            case "PROCESADO":           return "blue";
            case "LISTO_PARA_ENTREGAR": return "green";
            case "ENTREGADO":           return "blue";
            default:                    return "gray";
        }
    }

    private estatusUnidadToLabel(estatus: string): string {
        switch (estatus) {
            case "RECIBIDO":            return "Recibido";
            case "EN_PROCESO":          return "En proceso";
            case "PROCESADO":           return "Procesado";
            case "LISTO_PARA_ENTREGAR": return "Para entregar";
            case "ENTREGADO":           return "Entregado";
            default:                    return estatus;
        }
    }

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
