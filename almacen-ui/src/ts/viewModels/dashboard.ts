import * as AccUtils from "../accUtils";
import * as ko from "knockout";
import { calcEstatusEfectivo, mapEstatusToLabel, mapEstatusToBadge, removeAccents, EstatusEfectivo } from "../utils/contratoUtils";
import { getRole } from "../utils/auth";
import { contratosApi } from "../utils/api";
import 'ojs/ojtoolbar';
import "oj-c/button";
import "oj-c/input-text";
import "oj-c/form-layout";
import "oj-c/menu-button";
import "oj-c/buttonset-single";

// ================================================================
// TIPOS
// ================================================================

// Estatus efectivos visibles para el almacenista (excluye CAPTURA y CERRADO)
const ESTATUS_ALMACEN: EstatusEfectivo[] = [
    "EN_ALMACEN", "LISTO_ENTREGAR"
];

type ResumenBienes = {
    totalContratados: number;
    totalRecibidos: number;
    enProceso: number;
    procesados: number;
    listos: number;
    entregados: number;
};

type ContratoItem = {
    idContrato: number;
    numeroContrato: string;
    adquisicion: string;
    estatus: string;
    estatusEfectivo: EstatusEfectivo;
    estatusLabel: string;
    estatusBadge: string;
    primeraRecepcionRegistrada: boolean;
    primeraEntregaAutorizada:   boolean;
    todosLosBienesRecibidos:    boolean;
    contratoCerrado:            boolean;
    beneficiarios: string;
    numeroBeneficiarios: number;
    proveedor: string;
    fechaTentativaLlegada: string | null;
    fechaTentativaLlegadaFormateada: string;
    montoSinImpuestos: string;
    montoTotal: string;
    resumenBienes: ResumenBienes;
    pctRecibido: number;
    pctEntregado: number;
};

// ================================================================
// VIEWMODEL
// ================================================================

class DashboardViewModel {

    private router: any;

    public uiCargando  = ko.observable<boolean>(false);
    public uiError     = ko.observable<string>("");
    public uiVista     = ko.observable<"lista" | "grid">(
        (localStorage.getItem("almacen.dashboard.vista") as "lista" | "grid") ?? "lista"
    );

    public frmBuscar   = ko.observable<string>("");
    public frmEstatus  = ko.observable<string | null>(null);

    private contratos  = ko.observableArray<ContratoItem>([]);

    // ----------------------------------------------------------------
    // Rol de usuario (placeholder hasta auth real)
    // ----------------------------------------------------------------
    private readonly userRole: string = getRole() ?? "ALMACEN";

    public readonly calcEsAdmin = ko.pureComputed(() => this.userRole === "ADMINISTRADOR");
    public readonly calcEsAlmacenista = ko.pureComputed(() => this.userRole !== "ADMINISTRADOR");

    // Filtrado reactivo: rol + búsqueda + estatus
    public calcFiltrados = ko.pureComputed<ContratoItem[]>(() => {
        const buscar  = removeAccents(this.frmBuscar().trim().toLowerCase());
        const estatus = this.frmEstatus();

        return this.contratos().filter(c => {
            if (this.calcEsAlmacenista() && !ESTATUS_ALMACEN.includes(c.estatusEfectivo)) return false;
            const matchEstatus = !estatus || c.estatusEfectivo === estatus;
            const matchBuscar  = !buscar
                || removeAccents(c.numeroContrato.toLowerCase()).includes(buscar)
                || removeAccents(c.adquisicion.toLowerCase()).includes(buscar)
                || removeAccents(c.beneficiarios.toLowerCase()).includes(buscar)
                || removeAccents(c.proveedor.toLowerCase()).includes(buscar);
            return matchEstatus && matchBuscar;
        });
    });

    public calcHayResultados = ko.pureComputed(() => this.calcFiltrados().length > 0);

    // Lista completamente vacía en BD (no es un problema de filtros)
    public calcListaVacia = ko.pureComputed(() =>
        !this.uiCargando() && this.contratos().length === 0
    );

    // ----------------------------------------------------------------
    // Filtro de estatus — split menu button
    // ----------------------------------------------------------------
    public readonly itemsEstatus = [
        { key: "",                   label: "Todos" },
        ...(this.userRole === "ADMINISTRADOR" ? [
            { key: "CAPTURA",        label: "En captura" },
        ] : []),
        { key: "EN_ALMACEN",         label: "En almacén" },
        { key: "LISTO_ENTREGAR",     label: "Listo para entregar" },
        ...(this.userRole === "ADMINISTRADOR" ? [
            { key: "CERRADO",        label: "Cerrado" },
        ] : []),
    ];

    public calcLabelFiltroEstatus = ko.pureComputed(() => {
        const estatus = this.frmEstatus();
        if (!estatus) return "Todos";
        return this.itemsEstatus.find(i => i.key === estatus)?.label ?? estatus;
    });

    public cmdFiltrarEstatus = (event: CustomEvent): void => {
        const key = event.detail.key as string;
        this.frmEstatus(key || null);
    };

    public readonly vistaOptions = [
        { value: "lista", label: "Lista",      startIcon: { class: "oj-ux-ico-list-bulleted" } },
        { value: "grid",  label: "Cuadrícula", startIcon: { class: "oj-ux-ico-grid-view-small" } },
    ];

    constructor(params: any) {
        this.router = params?.router;
        this.uiVista.subscribe(v => localStorage.setItem("almacen.dashboard.vista", v));
    }

    connected(): void {
        AccUtils.announce("Gestión de almacén.");
        document.title = "Inicio — Gestión de Almacén";
        void this.loadContratos();
    }

    disconnected(): void {}
    transitionCompleted(): void {}

    // ================================================================
    // LOAD
    // ================================================================
    private async loadContratos(): Promise<void> {
        this.uiCargando(true);
        this.uiError("");
        try {
            const data: any[] = await contratosApi.listarTodos();

            this.contratos([...data].reverse().map(c => {
                const fechaRaw: string | null = c.fechaTentativaLlegada
                    ? c.fechaTentativaLlegada.split("T")[0]
                    : null;
                const efectivo = calcEstatusEfectivo(c);
                return {
                    idContrato:     c.idContrato,
                    numeroContrato: c.numeroContrato,
                    adquisicion:    c.adquisicion || "—",
                    estatus:        c.estatus,
                    estatusEfectivo: efectivo,
                    estatusLabel:   mapEstatusToLabel(efectivo),
                    estatusBadge:   mapEstatusToBadge(efectivo),
                    primeraRecepcionRegistrada: c.primeraRecepcionRegistrada ?? false,
                    primeraEntregaAutorizada:   c.primeraEntregaAutorizada   ?? false,
                    todosLosBienesRecibidos:    c.todosLosBienesRecibidos    ?? false,
                    contratoCerrado:            c.contratoCerrado            ?? false,
                    beneficiarios:  c.beneficiarios || "—",
                    numeroBeneficiarios: c.beneficiarios
                        ? c.beneficiarios.split(",").map((b: string) => b.trim()).filter(Boolean).length
                        : 0,
                    proveedor:      c.proveedor?.razonSocial || "Sin proveedor asignado",
                    fechaTentativaLlegada:          fechaRaw,
                    fechaTentativaLlegadaFormateada: this.formatFecha(fechaRaw),
                    montoSinImpuestos: this.formatMonto(c.montoSinImpuestos),
                    montoTotal:     this.formatMonto(c.montoTotal),
                    resumenBienes:  c.resumenBienes ?? {
                        totalContratados: 0, totalRecibidos: 0,
                        enProceso: 0, procesados: 0, listos: 0, entregados: 0,
                    },
                    pctRecibido:    c.resumenBienes?.totalContratados
                        ? Math.round(c.resumenBienes.totalRecibidos / c.resumenBienes.totalContratados * 100) : 0,
                    pctEntregado:   c.resumenBienes?.totalContratados
                        ? Math.round(c.resumenBienes.entregados / c.resumenBienes.totalContratados * 100) : 0,
                };
            }));
        } catch (err: any) {
            console.error("Error al cargar contratos:", err);
            this.uiError("No se pudo cargar el listado de contratos.");
        } finally {
            this.uiCargando(false);
        }
    }

    // ================================================================
    // COMMANDS
    // ================================================================
    public cmdVerDetalle = (contrato: ContratoItem): void => {
        this.router?.go({ path: "contrato-detalle", params: { id: contrato.idContrato } });
    };

    public cmdEditarContrato = (contrato: ContratoItem): void => {
        this.router?.go({ path: "contrato", params: { id: contrato.idContrato } });
    };

    public cmdNuevoContrato = (): void => {
        this.router?.go({ path: "contrato" });
    };

    public cmdActualizar = (): void => {
        void this.loadContratos();
    };

    public cmdRecibirProveedor = (contrato: ContratoItem): void => {
        this.router?.go({ path: "recepcion", params: { id: contrato.idContrato } });
    };

    public cmdProcesarBienes = (contrato: ContratoItem): void => {
        this.router?.go({ path: "procesamiento", params: { id: contrato.idContrato } });
    };

    public cmdEntregarBienes = (contrato: ContratoItem): void => {
        this.router?.go({ path: "entrega", params: { id: contrato.idContrato } });
    };

    // ================================================================
    // HELPERS
    // ================================================================
    private formatMonto(val: number | null | undefined): string {
        if (val == null) return "—";
        return new Intl.NumberFormat("es-MX", { style: "currency", currency: "MXN" }).format(val);
    }

    private formatFecha(fecha: string | null): string {
        if (!fecha) return "";
        const [year, month, day] = fecha.split("-");
        const meses = ["ene","feb","mar","abr","may","jun","jul","ago","sep","oct","nov","dic"];
        return `${day} ${meses[parseInt(month) - 1]} ${year}`;
    }

    public calcProgreso(r: ResumenBienes): number {
        if (!r.totalContratados) return 0;
        return Math.round((r.entregados / r.totalContratados) * 100);
    }
}

export = DashboardViewModel;
