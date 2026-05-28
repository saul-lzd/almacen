import * as AccUtils from "../accUtils";
import * as ko from "knockout";
import { mapEstatusToLabel, mapEstatusToBadge, removeAccents } from "../utils/contratoUtils";
import 'ojs/ojtoolbar';
import "oj-c/button";
import "oj-c/input-text";
import "oj-c/form-layout";
import "oj-c/menu-button";
import "oj-c/buttonset-single";

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

type ContratoItem = {
    idContrato: number;
    numeroContrato: string;
    adquisicion: string;
    estatus: string;
    estatusLabel: string;
    estatusBadge: string;
    beneficiarios: string;
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

    // Filtrado reactivo por búsqueda y estatus
    public calcFiltrados = ko.pureComputed<ContratoItem[]>(() => {
        const buscar  = removeAccents(this.frmBuscar().trim().toLowerCase());
        const estatus = this.frmEstatus();
        return this.contratos().filter(c => {
            const matchEstatus = !estatus || c.estatus === estatus;
            const matchBuscar  = !buscar
                || removeAccents(c.numeroContrato.toLowerCase()).includes(buscar)
                || removeAccents(c.adquisicion.toLowerCase()).includes(buscar)
                || removeAccents(c.beneficiarios.toLowerCase()).includes(buscar);
            return matchEstatus && matchBuscar;
        });
    });

    public calcHayResultados = ko.pureComputed(() => this.calcFiltrados().length > 0);

    // ----------------------------------------------------------------
    // Filtro de estatus — split menu button
    // ----------------------------------------------------------------
    public readonly itemsEstatus = [
        { key: "",                      label: "Todos" },
        { key: "CAPTURA",               label: "En captura" },
        { key: "POR_RECIBIR",           label: "Por recibir" },
        { key: "RECEPCION_PARCIAL",     label: "Recepción parcial" },
        { key: "EN_ALMACEN",            label: "En almacén" },
        { key: "LISTO_PARA_ENTREGAR",   label: "Listo para entregar" },
        { key: "ENTREGA_PARCIAL",       label: "Entrega parcial" },
        { key: "ENTREGADO",             label: "Entregado" },
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
            const res = await fetch("http://localhost:8080/api/contratos");
            if (!res.ok) throw new Error(`Error ${res.status}`);
            const data: any[] = await res.json();

            this.contratos(data.map(c => ({
                idContrato:     c.idContrato,
                numeroContrato: c.numeroContrato,
                adquisicion:    c.adquisicion || "—",
                estatus:        c.estatus,
                estatusLabel:   mapEstatusToLabel(c.estatus),
                estatusBadge:   mapEstatusToBadge(c.estatus),
                beneficiarios:  c.beneficiarios || "—",
                montoTotal:     this.formatMonto(c.montoTotal),
                resumenBienes:  c.resumenBienes ?? {
                    totalContratados: 0, totalRecibidos: 0,
                    enProceso: 0, procesados: 0, listos: 0, entregados: 0,
                },
                pctRecibido:    c.resumenBienes?.totalContratados
                    ? Math.round(c.resumenBienes.totalRecibidos / c.resumenBienes.totalContratados * 100) : 0,
                pctEntregado:   c.resumenBienes?.totalContratados
                    ? Math.round(c.resumenBienes.entregados / c.resumenBienes.totalContratados * 100) : 0,
            })));
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

    public cmdNuevoContrato = (): void => {
        this.router?.go({ path: "contrato" });
    };

    public cmdSetVista = (vista: "lista" | "grid"): void => {
        this.uiVista(vista);
    };

    // ================================================================
    // HELPERS
    // ================================================================
    private formatMonto(val: number | null | undefined): string {
        if (val == null) return "—";
        return new Intl.NumberFormat("es-MX", { style: "currency", currency: "MXN" }).format(val);
    }

    public calcProgreso(r: ResumenBienes): number {
        if (!r.totalContratados) return 0;
        return Math.round((r.entregados / r.totalContratados) * 100);
    }
}

export = DashboardViewModel;
