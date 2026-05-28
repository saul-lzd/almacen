/**
 * ViewModel: Dashboard — Tablero Kanban de Contratos
 *
 * Convencion de nombres igual que contrato.ts:
 * - list*: arrays de datos
 * - dp*:   DataProvider para componentes JET
 * - ui*:   estado visual
 * - cmd*:  acciones desde UI
 * - load*: carga desde API
 * - map*:  transformaciones
 */

import * as AccUtils from "../accUtils";
import * as ko from "knockout";
import { mapEstatusToLabel, mapEstatusToBadge } from "../utils/contratoUtils";

import "oj-c/form-layout";
import "oj-c/input-text";
import "oj-c/buttonset-multiple";
import "oj-c/button";
import "oj-c/dialog";

// ================================================================
// TIPOS
// ================================================================

type EstatusContrato =
    | "CAPTURA"
    | "POR_RECIBIR"
    | "RECEPCION_PARCIAL"
    | "EN_ALMACEN"
    | "LISTO_PARA_ENTREGAR"
    | "ENTREGA_PARCIAL"
    | "ENTREGADO"
    | "CERRADO";

type ContratoKanbanItem = {
    idContrato: number;
    numeroContrato: string;
    adquisicion: string;
    estatus: EstatusContrato;
    estatusLabel: string;
    estatusBadge: string;
    proveedor: string;
    montoTotal: number | null;
    fechaTentativaLlegada: string | null;
    beneficiarios: string;
    totalBienes: number;
};

// Columnas del Kanban para el administrador
const COLUMNAS_ADMIN: { key: EstatusContrato; label: string }[] = [
    { key: "CAPTURA",             label: "En captura" },
    { key: "POR_RECIBIR",         label: "Por recibir" },
    { key: "RECEPCION_PARCIAL",   label: "Recepción parcial" },
    { key: "EN_ALMACEN",          label: "En almacén" },
    { key: "LISTO_PARA_ENTREGAR", label: "Listo p/ entregar" },
    { key: "CERRADO",             label: "Cerrado" }
];

// ================================================================
// VIEWMODEL
// ================================================================

class DashboardViewModel {

    private router: any;

    // ----------------------------------------------------------------
    // ESTADO DE CARGA
    // ----------------------------------------------------------------
    public uiCargando = ko.observable<boolean>(false);
    public uiError    = ko.observable<string>("");
    public uiExito    = ko.observable<string>("");

    // ----------------------------------------------------------------
    // DATOS COMPLETOS
    // ----------------------------------------------------------------
    private todosLosContratos = ko.observableArray<ContratoKanbanItem>([]);

    // ----------------------------------------------------------------
    // BÚSQUEDA Y FILTROS
    // ----------------------------------------------------------------
    public uiBusqueda       = ko.observable<string>("");
    public uiBusquedaRaw    = ko.observable<string>("");
    public uiFiltroActivo   = ko.observable<string>("todos");

    // ----------------------------------------------------------------
    // COLUMNAS KANBAN — computed por estatus
    // ----------------------------------------------------------------
    public columnas = COLUMNAS_ADMIN;

    // Contratos filtrados según búsqueda activa
    private contratosFiltrados = ko.pureComputed(() => {
        const query  = this.uiBusquedaRaw().trim().toLowerCase();
        const filtro = this.uiFiltroActivo();
        const todos  = this.todosLosContratos();

        if (!query) return todos;

        return todos.filter(c => {
            switch (filtro) {
                case "numero":
                    return c.numeroContrato.toLowerCase().includes(query);
                case "adquisicion":
                    return c.adquisicion.toLowerCase().includes(query);
                case "beneficiario":
                    return c.beneficiarios.toLowerCase().includes(query);
                default: // "todos"
                    return (
                        c.numeroContrato.toLowerCase().includes(query) ||
                        c.adquisicion.toLowerCase().includes(query) ||
                        c.beneficiarios.toLowerCase().includes(query) ||
                        c.proveedor.toLowerCase().includes(query)
                    );
            }
        });
    });

    // Agrupa los contratos filtrados por estatus para cada columna
    public contratosEnColumna(estatus: EstatusContrato): ko.PureComputed<ContratoKanbanItem[]> {
        return ko.pureComputed(() =>
            this.contratosFiltrados().filter(c => c.estatus === estatus)
        );
    }

    // Cuenta contratos por columna para el badge
    public countEnColumna(estatus: EstatusContrato): ko.PureComputed<number> {
        return ko.pureComputed(() =>
            this.contratosFiltrados().filter(c => c.estatus === estatus).length
        );
    }

    // Resultado de búsqueda
    public calcResultadoBusqueda = ko.pureComputed(() => {
        if (!this.uiBusquedaRaw()) return;

        const query = this.uiBusquedaRaw().trim();
        if (!query) return "";
        const count = this.contratosFiltrados().length;
        return count === 0
            ? `Sin resultados para "${query}"`
            : `${count} contrato${count > 1 ? "s" : ""} encontrado${count > 1 ? "s" : ""} para "${query}"`;
    });

    public calcTotalContratos = ko.pureComputed(() =>
        this.todosLosContratos().length
    );

    public calcTotalFiltrados = ko.pureComputed(() =>
        this.contratosFiltrados().length
    );

    // ----------------------------------------------------------------
    // DIALOG BENEFICIARIOS
    // ----------------------------------------------------------------
    public uiBeneficiariosDialogOpen  = ko.observable<boolean>(false);
    public uiBeneficiariosDialogTitle = ko.observable<string>("");
    public uiListaBeneficiarios       = ko.observableArray<string>([]);

    // ----------------------------------------------------------------
    // CONSTRUCTOR / LIFECYCLE
    // ----------------------------------------------------------------
    constructor(params: any) {
        this.router = params?.router;
    }

    connected(): void {
        AccUtils.announce("Tablero de contratos cargado.");
        document.title = "Tablero — Gestión de Almacén";
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
            if (!res.ok) throw new Error("Error al obtener contratos");

            const data: any[] = await res.json();
            this.todosLosContratos(data.map(c => this.mapToKanbanItem(c)));
        } catch (err: any) {
            console.error("Error al cargar contratos:", err);
            this.uiError("No se pudo cargar el tablero. Intenta de nuevo.");
        } finally {
            this.uiCargando(false);
        }
    }

    // ================================================================
    // MAPPERS
    // ================================================================
    private mapToKanbanItem(c: any): ContratoKanbanItem {
        return {
            idContrato:            c.idContrato,
            numeroContrato:        c.numeroContrato,
            adquisicion:           c.adquisicion,
            estatus:               c.estatus as EstatusContrato,
            estatusLabel:          mapEstatusToLabel(c.estatus),
            estatusBadge:          mapEstatusToBadge(c.estatus),
            proveedor:             c.proveedor?.razonSocial || "Pendiente de asignar",
            montoTotal:            c.montoTotal ?? null,
            fechaTentativaLlegada: c.fechaTentativaLlegada
                ? c.fechaTentativaLlegada.split("T")[0]
                : null,
            beneficiarios: c.beneficiarios || "",
            totalBienes:   c.bienes?.length ?? 0
        };
    }

    // ================================================================
    // COMMANDS — BÚSQUEDA
    // ================================================================
    public cmdSetFiltro = (filtro: string): void => {
        this.uiFiltroActivo(filtro);
    };


    // ================================================================
    // COMMANDS — NAVEGACIÓN
    // ================================================================
    public cmdNuevoContrato = (): void => {
        this.router?.go({ path: "contrato" });
    };

    public cmdEditarContrato = (event: Event, context: any): void => {
        const id = context.item?.data?.idContrato ?? context?.idContrato;
        if (id) this.router?.go({ path: "contrato", params: { id } });
    };

    // Recibe el contrato directamente (para llamadas desde botones en cards)
    public cmdIrAContrato = (contrato: ContratoKanbanItem): void => {
        this.router?.go({ path: "contrato", params: { id: contrato.idContrato } });
    };

    // ================================================================
    // COMMANDS — DIALOG BENEFICIARIOS
    // ================================================================
    public cmdVerBeneficiarios = (contrato: ContratoKanbanItem): void => {
        const nombres = contrato.beneficiarios
            ? contrato.beneficiarios.split(",").map(n => n.trim()).filter(n => n)
            : [];

        this.uiListaBeneficiarios(nombres);
        this.uiBeneficiariosDialogTitle(contrato.numeroContrato);
        this.uiBeneficiariosDialogOpen(true);
    };

    public cmdCerrarBeneficiariosDialog = (): void => {
        this.uiBeneficiariosDialogOpen(false);
    };

    // ================================================================
    // HELPERS — UI
    // ================================================================

    // Determina si el contrato puede editarse según su estatus
    public puedeEditar(estatus: EstatusContrato): boolean {
        return estatus === "CAPTURA";
    }

    // Formatea el monto total para mostrar en la card
    public formatMonto(monto: number | null): string {
        if (monto === null || monto === undefined) return "";
        return monto.toLocaleString("es-MX", {
            style: "currency",
            currency: "MXN",
            minimumFractionDigits: 2
        });
    }

    // Formatea la fecha de llegada
    public formatFecha(fecha: string | null): string {
        if (!fecha) return "";
        const [year, month, day] = fecha.split("-");
        const meses = ["ene","feb","mar","abr","may","jun","jul","ago","sep","oct","nov","dic"];
        return `${day} ${meses[parseInt(month) - 1]} ${year}`;
    }

    // Recargar tablero
    public cmdActualizar = (): void => {
        void this.loadContratos();
    };

    // ================================================================
    // COMMANDS — AUTORIZAR ENTREGA
    // ================================================================
    public cmdAutorizarEntrega = async (contrato: ContratoKanbanItem): Promise<void> => {
        this.uiError("");
        this.uiExito("");

        try {
            const res = await fetch(
                `http://localhost:8080/api/contratos/${contrato.idContrato}/autorizar-entrega`,
                { method: "PATCH" }
            );

            if (!res.ok) {
                const errData = await res.json().catch(() => null);
                throw new Error(errData?.errores?.[0] ?? errData?.mensaje ?? `Error ${res.status}`);
            }

            this.uiExito(`Contrato ${contrato.numeroContrato} autorizado para entrega.`);
            await this.loadContratos();
        } catch (err: any) {
            this.uiError(err.message || "No se pudo autorizar la entrega.");
        }
    };
}

export = DashboardViewModel;
