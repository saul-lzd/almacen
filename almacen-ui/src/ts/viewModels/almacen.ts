/**
 * ViewModel: Almacén — Lista de contratos pendientes
 *
 * Muestra contratos en estatus POR_RECIBIR, EN_ALMACEN y LISTO_PARA_ENTREGAR.
 * Convención de nombres:
 * - list*: arrays de datos
 * - ui*:   estado visual
 * - cmd*:  acciones desde UI
 * - load*: carga desde API
 * - map*:  transformaciones
 */

import * as AccUtils from "../accUtils";
import * as ko from "knockout";
import { mapEstatusToLabel, mapEstatusToBadge } from "../utils/contratoUtils";

import "oj-c/button";
import "oj-c/input-text";
import "oj-c/form-layout";

// ================================================================
// TIPOS
// ================================================================

type EstatusAlmacen = "POR_RECIBIR" | "RECEPCION_PARCIAL" | "EN_ALMACEN" | "LISTO_PARA_ENTREGAR" | "ENTREGA_PARCIAL";
type TabAlmacen = "todos" | "POR_RECIBIR" | "RECEPCION_PARCIAL" | "EN_ALMACEN" | "LISTO_PARA_ENTREGAR";

type ContratoAlmacenItem = {
    idContrato: number;
    numeroContrato: string;
    adquisicion: string;
    estatus: EstatusAlmacen;
    estatusLabel: string;
    estatusBadge: string;
    proveedor: string;
    montoTotal: number | null;
    fechaTentativaLlegada: string | null;
    beneficiarios: string;
    totalBienes: number;
};

const ESTATUS_RELEVANTES: string[] = [
    "POR_RECIBIR", "RECEPCION_PARCIAL", "EN_ALMACEN", "LISTO_PARA_ENTREGAR", "ENTREGA_PARCIAL"
];

// ================================================================
// VIEWMODEL
// ================================================================

class AlmacenViewModel {

    private router: any;

    // ----------------------------------------------------------------
    // ESTADO
    // ----------------------------------------------------------------
    public uiCargando = ko.observable<boolean>(false);
    public uiError    = ko.observable<string>("");

    // ----------------------------------------------------------------
    // DATOS
    // ----------------------------------------------------------------
    private todosLosContratos = ko.observableArray<ContratoAlmacenItem>([]);

    // ----------------------------------------------------------------
    // FILTROS
    // ----------------------------------------------------------------
    public uiTabActivo = ko.observable<TabAlmacen>("todos");
    public uiBusqueda  = ko.observable<string>("");

    // ----------------------------------------------------------------
    // COMPUTED
    // ----------------------------------------------------------------
    public contratosFiltrados = ko.pureComputed(() => {
        const tab   = this.uiTabActivo();
        const query = this.uiBusqueda().trim().toLowerCase();
        let lista   = this.todosLosContratos();

        if (tab !== "todos") {
            lista = lista.filter(c => c.estatus === tab);
        }

        if (query) {
            lista = lista.filter(c =>
                c.numeroContrato.toLowerCase().includes(query) ||
                c.proveedor.toLowerCase().includes(query) ||
                c.adquisicion.toLowerCase().includes(query)
            );
        }

        return lista;
    });

    public countPorEstatus(estatus: EstatusAlmacen): ko.PureComputed<number> {
        return ko.pureComputed(() =>
            this.todosLosContratos().filter(c => c.estatus === estatus).length
        );
    }

    public calcTotalContratos = ko.pureComputed(() =>
        this.todosLosContratos().length
    );

    public isTabActive(tab: TabAlmacen): ko.PureComputed<boolean> {
        return ko.pureComputed(() => this.uiTabActivo() === tab);
    }

    // ----------------------------------------------------------------
    // CONSTRUCTOR / LIFECYCLE
    // ----------------------------------------------------------------
    constructor(params: any) {
        this.router = params?.router;
    }

    connected(): void {
        AccUtils.announce("Vista de almacén cargada.");
        document.title = "Almacén — Gestión de Almacén";
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
            const almacen = data
                .filter(c => ESTATUS_RELEVANTES.includes(c.estatus))
                .map(c => this.mapToAlmacenItem(c));
            this.todosLosContratos(almacen);
        } catch (err: any) {
            console.error("Error al cargar contratos:", err);
            this.uiError("No se pudo cargar la información. Intenta de nuevo.");
        } finally {
            this.uiCargando(false);
        }
    }

    // ================================================================
    // MAPPERS
    // ================================================================
    private mapToAlmacenItem(c: any): ContratoAlmacenItem {
        return {
            idContrato:            c.idContrato,
            numeroContrato:        c.numeroContrato,
            adquisicion:           c.adquisicion,
            estatus:               c.estatus as EstatusAlmacen,
            estatusLabel:          mapEstatusToLabel(c.estatus),
            estatusBadge:          mapEstatusToBadge(c.estatus),
            proveedor:             c.proveedor?.razonSocial || "Sin proveedor asignado",
            montoTotal:            c.montoTotal ?? null,
            fechaTentativaLlegada: c.fechaTentativaLlegada
                ? c.fechaTentativaLlegada.split("T")[0]
                : null,
            beneficiarios: c.beneficiarios || "",
            totalBienes:   c.bienes?.length ?? 0
        };
    }

    // ================================================================
    // COMMANDS
    // ================================================================
    public cmdSetTab = (tab: TabAlmacen): void => {
        this.uiTabActivo(tab);
    };

    public cmdActualizar = (): void => {
        void this.loadContratos();
    };

    public cmdRecibirProveedor = (contrato: ContratoAlmacenItem): void => {
        this.router?.go({ path: "recepcion", params: { id: contrato.idContrato } });
    };

    public cmdVerDetalle = (contrato: ContratoAlmacenItem): void => {
        this.router?.go({ path: "recepcion", params: { id: contrato.idContrato } });
    };

    public cmdProcesarBienes = (contrato: ContratoAlmacenItem): void => {
        this.router?.go({ path: "procesamiento", params: { id: contrato.idContrato } });
    };

    public cmdEntregarBienes = (contrato: ContratoAlmacenItem): void => {
        this.router?.go({ path: "entrega", params: { id: contrato.idContrato } });
    };

    // ================================================================
    // HELPERS — UI
    // ================================================================
    public formatMonto(monto: number | null): string {
        if (monto === null || monto === undefined) return "";
        return monto.toLocaleString("es-MX", {
            style: "currency",
            currency: "MXN",
            minimumFractionDigits: 2
        });
    }

    public formatFecha(fecha: string | null): string {
        if (!fecha) return "";
        const [year, month, day] = fecha.split("-");
        const meses = ["ene","feb","mar","abr","may","jun","jul","ago","sep","oct","nov","dic"];
        return `${day} ${meses[parseInt(month) - 1]} ${year}`;
    }
}

export = AlmacenViewModel;
