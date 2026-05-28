/**
 * ViewModel: Entrega de Bienes a Beneficiario
 *
 * El almacenista selecciona qué grupos de bienes entrega, captura
 * los datos del receptor y confirma la salida del almacén.
 *
 * Convención de nombres:
 * - frm*:  valores editables del formulario
 * - list*: arrays de datos
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
import "oj-c/select-single";
import "oj-c/checkbox";
import "oj-c/text-area";
import "oj-c/button";

import ArrayDataProvider = require("ojs/ojarraydataprovider");

// ================================================================
// TIPOS
// ================================================================

type BeneficiarioItem = {
    idBeneficiario: number;
    nombre: string;
};

type UnidadEntrega = {
    idAlmacenBien: number;
    codigoInterno: string;
    numeroSerie: string | null;
    marca: string | null;
    modelo: string | null;
};

type GrupoEntrega = {
    idContratoBien: number;
    lote: number | null;
    partida: number | null;
    descripcion: string;
    unidadMedida: string;
    totalUnidades: number;
    unidades: UnidadEntrega[];
    uiSeleccionado: ko.Observable<boolean>;
};

type ContratoEntrega = {
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

class EntregaViewModel {

    private router: any;
    private contratoId: number | null = null;

    // ----------------------------------------------------------------
    // ESTADO
    // ----------------------------------------------------------------
    public uiCargando  = ko.observable<boolean>(false);
    public uiGuardando = ko.observable<boolean>(false);
    public uiError     = ko.observable<string>("");
    public uiExito     = ko.observable<string>("");

    // ----------------------------------------------------------------
    // DATOS
    // ----------------------------------------------------------------
    public contrato    = ko.observable<ContratoEntrega | null>(null);
    public listaGrupos = ko.observableArray<GrupoEntrega>([]);
    public listaBeneficiarios = ko.observableArray<BeneficiarioItem>([]);

    public dpBeneficiarios = ko.pureComputed(() =>
        new ArrayDataProvider(this.listaBeneficiarios(), {
            keyAttributes: "idBeneficiario"
        })
    );

    // ----------------------------------------------------------------
    // FORMULARIO
    // ----------------------------------------------------------------
    public frmIdBeneficiario          = ko.observable<number | null>(null);
    public frmNombreEntregaAlmacen    = ko.observable<string>("");
    public frmNombreRecibeBeneficiario = ko.observable<string>("");
    public frmBeneficiarioFirma       = ko.observable<boolean>(false);
    public frmObservaciones           = ko.observable<string>("");

    // ----------------------------------------------------------------
    // COMPUTED
    // ----------------------------------------------------------------
    public calcGruposSeleccionados = ko.pureComputed(() =>
        this.listaGrupos().filter(g => g.uiSeleccionado())
    );

    public calcTotalBienesSeleccionados = ko.pureComputed(() =>
        this.calcGruposSeleccionados().reduce((sum, g) => sum + g.totalUnidades, 0)
    );

    public calcPuedeConfirmar = ko.pureComputed(() => {
        if (!this.frmIdBeneficiario()) return false;
        if (!this.frmNombreEntregaAlmacen().trim()) return false;
        if (!this.frmNombreRecibeBeneficiario().trim()) return false;
        if (this.calcGruposSeleccionados().length === 0) return false;
        return true;
    });

    public calcTituloContrato = ko.pureComputed(() =>
        this.contrato()?.numeroContrato ?? "Cargando..."
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
        AccUtils.announce("Entrega de bienes.");
        document.title = "Entrega — Gestión de Almacén";
        if (this.contratoId) {
            void this.loadDatos(this.contratoId);
        } else {
            this.uiError("No se especificó un contrato. Regresa a la lista del almacén.");
        }
    }

    disconnected(): void {}
    transitionCompleted(): void {}

    // ================================================================
    // LOAD
    // ================================================================
    private async loadDatos(id: number): Promise<void> {
        this.uiCargando(true);
        this.uiError("");

        try {
            const [resContrato, resBienes] = await Promise.all([
                fetch(`http://localhost:8080/api/contratos/${id}`),
                fetch(`http://localhost:8080/api/contratos/${id}/bienes-entrega`)
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

            const beneficiarios: BeneficiarioItem[] = (dataContrato.listaBeneficiarios ?? [])
                .map((b: any) => ({ idBeneficiario: b.idBeneficiario, nombre: b.nombre }));
            this.listaBeneficiarios(beneficiarios);

            const grupos: GrupoEntrega[] = dataGrupos.map(g => ({
                idContratoBien: g.idContratoBien,
                lote:           g.lote,
                partida:        g.partida,
                descripcion:    g.descripcion,
                unidadMedida:   g.unidadMedida,
                totalUnidades:  g.totalUnidades,
                unidades:       (g.unidades ?? []).map((u: any) => ({
                    idAlmacenBien: u.idAlmacenBien,
                    codigoInterno: u.codigoInterno,
                    numeroSerie:   u.numeroSerie || null,
                    marca:         u.marca || null,
                    modelo:        u.modelo || null,
                })),
                uiSeleccionado: ko.observable<boolean>(true),
            }));
            this.listaGrupos(grupos);

        } catch (err: any) {
            console.error("Error al cargar entrega:", err);
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

    public cmdConfirmarEntrega = async (): Promise<void> => {
        if (!this.calcPuedeConfirmar() || !this.contratoId) return;

        this.uiGuardando(true);
        this.uiError("");
        this.uiExito("");

        const idsAlmacenBien = this.calcGruposSeleccionados()
            .flatMap(g => g.unidades.map(u => u.idAlmacenBien));

        const payload = {
            idBeneficiario:             this.frmIdBeneficiario(),
            nombreEntregaAlmacen:       this.frmNombreEntregaAlmacen().trim(),
            nombreRecibeBeneficiario:   this.frmNombreRecibeBeneficiario().trim(),
            beneficiarioFirma:          this.frmBeneficiarioFirma(),
            observaciones:              this.frmObservaciones().trim() || null,
            idsAlmacenBien,
        };

        try {
            const res = await fetch(
                `http://localhost:8080/api/contratos/${this.contratoId}/entrega`,
                {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(payload)
                }
            );

            if (!res.ok) {
                const errData = await res.json().catch(() => null);
                throw new Error(errData?.errores?.[0] ?? errData?.mensaje ?? `Error ${res.status}`);
            }

            this.uiExito("Entrega registrada correctamente.");
            await this.loadDatos(this.contratoId);

        } catch (err: any) {
            console.error("Error al confirmar entrega:", err);
            this.uiError(err.message || "No se pudo registrar la entrega. Intenta de nuevo.");
        } finally {
            this.uiGuardando(false);
        }
    };

    // ================================================================
    // HELPERS
    // ================================================================
    public formatMonto(monto: number | null): string {
        if (monto === null || monto === undefined) return "—";
        return monto.toLocaleString("es-MX", { style: "currency", currency: "MXN", minimumFractionDigits: 2 });
    }
}

export = EntregaViewModel;
