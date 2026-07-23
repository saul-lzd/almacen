/**
 * ViewModel: Ver Contrato (solo lectura)
 *
 * Vista de solo lectura del registro completo de un contrato — pensada para
 * el admin cuando el contrato ya no está en CAPTURA (una vez enviado a
 * almacén, `contrato.ts` deja de ser alcanzable). Muestra los mismos datos
 * que el formulario de captura, incluyendo la descripción técnica completa
 * de cada bien, sin ninguna de las piezas de edición (diálogo de captura,
 * validación de claves, editor Quill editable, etc.).
 *
 * Si el admin necesita editar, "Editar contrato" lo manda al formulario real
 * (`contrato`) — las reglas de qué es corregible ahí se definen en una fase
 * posterior, ver [[project_tareas_futuras]].
 */

import * as AccUtils from "../accUtils";
import * as ko from "knockout";
import "../jet-composites/quill-editor/quill-editor";
import { mapEstatusToLabel, mapEstatusToBadge, calcEstatusEfectivo } from "../utils/contratoUtils";
import { getRole } from "../utils/auth";
import { contratosApi } from "../utils/api";

import "oj-c/button";
import "oj-c/dialog";

// ================================================================
// TIPOS
// ================================================================

type ClaveItem = {
    clave: string;
    partidaEspecifica: string;
    montoAsignado: string; // formateado MXN
};

type BienItem = {
    idContratoBien: number;
    lote: number | null;
    partida: number | null;
    descripcionCorta: string;
    descripcionTecnica: string; // HTML de Quill, completo
    unidadMedida: string;
    cantidad: number;
    precioUnitarioStr: string;
    subtotalStr: string;
};

// ================================================================
// VIEWMODEL
// ================================================================

class ContratoLecturaViewModel {

    private router: any;
    private contratoId: number | null = null;

    private readonly userRole = getRole() ?? "ALMACEN";
    public calcEsAdmin = ko.pureComputed(() => this.userRole === "ADMINISTRADOR");

    public uiCargando = ko.observable<boolean>(false);
    public uiError    = ko.observable<string>("");

    // ── Header ──
    public uiNumeroContrato = ko.observable<string>("");
    public uiAdquisicion    = ko.observable<string>("");
    public uiProveedorNombre = ko.observable<string>("");
    public uiEstatusLabel   = ko.observable<string>("");
    public uiEstatusBadge   = ko.observable<string>("");

    // ── Datos generales ──
    public uiFechaTentativaLlegada = ko.observable<string>("—");

    // ── Proveedor ──
    public uiProveedorRazonSocial     = ko.observable<string>("—");
    public uiProveedorDomicilioFiscal = ko.observable<string>("—");
    public uiProveedorRepresentante   = ko.observable<string>("—");
    public uiProveedorCaracter        = ko.observable<string>("—");

    // ── Comprador / Administrador ──
    public uiCompradorNombre      = ko.observable<string>("—");
    public uiCompradorDependencia = ko.observable<string>("—");
    public uiCompradorCaracter    = ko.observable<string>("—");
    public uiAdministradorNombre      = ko.observable<string>("—");
    public uiAdministradorDependencia = ko.observable<string>("—");
    public uiAdministradorCaracter    = ko.observable<string>("—");

    // ── Información financiera ──
    public uiMontoSinImpuestos = ko.observable<string>("—");
    public uiImpuestos         = ko.observable<string>("—");
    public uiMontoTotal        = ko.observable<string>("—");
    public listClaves = ko.observableArray<ClaveItem>([]);

    // ── Beneficiarios ──
    public listBeneficiarios = ko.observableArray<string>([]);
    public calcTotalBeneficiarios = ko.pureComputed(() => this.listBeneficiarios().length);

    // ── Bienes ──
    public listBienes = ko.observableArray<BienItem>([]);
    public calcTotalBienes = ko.pureComputed(() => this.listBienes().length);

    // ── Dialog descripción de un bien ──
    public uiBienDescripcionDialogOpen   = ko.observable<boolean>(false);
    public uiDescripcionBienSeleccionada = ko.observable<string>("");
    public uiTituloBienSeleccionado      = ko.observable<string>("");

    constructor(params: any) {
        this.router = params?.router;
        const idParam = params?.routerState?.params?.id;
        if (idParam) this.contratoId = Number(idParam);
    }

    connected(): void {
        AccUtils.announce("Ver contrato.");
        document.title = "Ver Contrato — Gestión de Almacén";

        if (!this.contratoId) {
            this.uiError("No se encontró el contrato solicitado.");
            return;
        }

        void this.loadContrato(this.contratoId);
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
            const data: any = await contratosApi.obtenerPorId(id);

            this.uiNumeroContrato(data.numeroContrato || "—");
            this.uiAdquisicion(data.adquisicion || "—");
            this.uiProveedorNombre(data.proveedor?.razonSocial || "Sin proveedor asignado");

            const resumenBienes = data.resumenBienes;
            const hayListos = !!resumenBienes && resumenBienes.listos > 0;
            const todosBienesProcesados = !!resumenBienes && resumenBienes.totalRecibidos > 0 &&
                (resumenBienes.procesados + resumenBienes.listos) >= resumenBienes.totalRecibidos;

            const efectivo = calcEstatusEfectivo({
                estatus: data.estatus,
                primeraRecepcionRegistrada: data.primeraRecepcionRegistrada ?? false,
                primeraEntregaAutorizada:   data.primeraEntregaAutorizada || hayListos,
                todosLosBienesRecibidos:    data.todosLosBienesRecibidos    ?? false,
                contratoCerrado:            data.contratoCerrado            ?? false,
                todosBienesProcessados:     todosBienesProcesados,
            });
            this.uiEstatusLabel(mapEstatusToLabel(efectivo));
            this.uiEstatusBadge(mapEstatusToBadge(efectivo));

            this.uiFechaTentativaLlegada(this.formatFecha(data.fechaTentativaLlegada));

            if (data.proveedor) {
                this.uiProveedorRazonSocial(data.proveedor.razonSocial || "—");
                this.uiProveedorDomicilioFiscal(data.proveedor.domicilioFiscal || "—");
                this.uiProveedorRepresentante(data.proveedor.representante || "—");
                this.uiProveedorCaracter(data.proveedor.caracter || "—");
            }

            if (data.comprador) {
                this.uiCompradorNombre(data.comprador.nombre || "—");
                this.uiCompradorDependencia(data.comprador.dependencia || "—");
                this.uiCompradorCaracter(data.comprador.caracter || "—");
            }

            if (data.administradorContrato) {
                this.uiAdministradorNombre(data.administradorContrato.nombre || "—");
                this.uiAdministradorDependencia(data.administradorContrato.dependencia || "—");
                this.uiAdministradorCaracter(data.administradorContrato.caracter || "—");
            }

            this.uiMontoSinImpuestos(this.formatMonto(data.montoSinImpuestos));
            this.uiImpuestos(this.formatMonto(data.impuestos));
            this.uiMontoTotal(this.formatMonto(data.montoTotal));

            this.listClaves((data.clavesPresupuestales ?? []).map((c: any) => ({
                clave: c.clave,
                partidaEspecifica: c.partidaEspecifica || "—",
                montoAsignado: this.formatMonto(c.montoAsignado),
            })));

            this.listBeneficiarios(
                (data.beneficiarios || "")
                    .split(",")
                    .map((b: string) => b.trim())
                    .filter(Boolean)
            );

            this.listBienes((data.bienes ?? []).map((b: any) => ({
                idContratoBien: b.idContratoBien,
                lote: b.lote,
                partida: b.partida,
                descripcionCorta: b.descripcionCorta || "Sin descripción",
                descripcionTecnica: b.descripcionTecnica || "",
                unidadMedida: b.unidadMedida || "—",
                cantidad: b.cantidad,
                precioUnitarioStr: this.formatMonto(b.precioUnitario),
                subtotalStr: this.formatMonto(b.subtotal),
            })));

        } catch (err: any) {
            console.error("Error al cargar contrato:", err);
            this.uiError("No se pudo cargar la información del contrato.");
        } finally {
            this.uiCargando(false);
        }
    }

    // ================================================================
    // COMMANDS
    // ================================================================
    public cmdGoBack = (): void => {
        this.router?.go({ path: "dashboard" });
    };

    public cmdEditarContrato = (): void => {
        this.router?.go({ path: "contrato", params: { id: this.contratoId } });
    };

    public cmdVerDescripcionBien = (bien: BienItem): void => {
        this.uiTituloBienSeleccionado(`Lote ${bien.lote ?? "—"} · Partida ${bien.partida ?? "—"}`);
        this.uiDescripcionBienSeleccionada(bien.descripcionTecnica);
        this.uiBienDescripcionDialogOpen(true);
    };

    // ================================================================
    // HELPERS
    // ================================================================
    private formatMonto(val: number | null | undefined): string {
        if (val == null) return "—";
        return new Intl.NumberFormat("es-MX", { style: "currency", currency: "MXN" }).format(val);
    }

    private formatFecha(fecha: string | null): string {
        if (!fecha) return "Sin fecha asignada";
        const soloFecha = fecha.split("T")[0];
        const [year, month, day] = soloFecha.split("-");
        const meses = ["ene","feb","mar","abr","may","jun","jul","ago","sep","oct","nov","dic"];
        return `${day} ${meses[parseInt(month) - 1]} ${year}`;
    }
}

export = ContratoLecturaViewModel;
