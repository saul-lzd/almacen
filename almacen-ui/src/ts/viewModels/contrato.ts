/**
 * ViewModel: Nuevo / Editar Contrato
 *
 * Convencion de nombres:
 * - frm*:   valores editables ligados directamente al formulario
 * - list*:  arrays de elementos capturados que se enviaran al backend
 * - dp*:    DataProvider usado por componentes Oracle JET
 * - cat*:   catalogos/listas recibidas desde API
 * - ui*:    estado visual de pantalla
 * - calc*:  computed/calculados
 * - cmd*:   acciones disparadas desde UI
 * - load*:  carga de datos desde API/backend
 * - clear*: limpieza de formularios internos
 * - map*:   transformacion a payload/backend
 * - on*:    handlers de eventos UI
 */

import * as AccUtils from "../accUtils";
import * as ko from "knockout";
import ArrayDataProvider = require("ojs/ojarraydataprovider");
import "../jet-composites/quill-editor/quill-editor"; // Importar el componente QuillEditorViewModel para que se registre globalmente
import { mapEstatusToLabel } from "../utils/contratoUtils";
import { getRole } from "../utils/auth";
import { catalogosApi, contratosApi } from "../utils/api";

import "oj-c/collapsible";
import "oj-c/list-view";
import "oj-c/list-item-layout";
import "oj-c/form-layout";
import "oj-c/select-single";
import "oj-c/text-area";
import "oj-c/button";
import "oj-c/dialog";
import "oj-c/input-text";
import "oj-c/input-number";
import "oj-c/input-date-text";
import 'oj-c/input-date-picker';
import 'ojs/ojformlayout';
import "ojs/ojdatetimepicker";
import "ojs/ojinputtext";

// ================================================================
// TIPOS
// ================================================================

type ModoPantalla = "NUEVO" | "EDICION";

type CatalogoOption = {
  value: string;
  label: string;
  metadata?: Record<string, unknown>;
};

type FuncionarioOption = {
  value: number;
  label: string;
  dependencia: string;
  caracter: string;
  tipo: "TITULAR" | "ADMINISTRADOR";
};

type ClavePresupuestalItem = {
  idLocal:           number;
  claveId:           string | null;
  partidaEspecifica: string;
  montoAsignado:     number | null;
};

type BienContratoItem = {
  idLocal: number;
  idContratoBien: number | null;  // null si es nuevo, número si ya existe en BD
  lote: number;
  partida: number;
  descripcionTecnica: string;     // HTML generado por Quill
  descripcionCorta: string;       // texto plano truncado para mostrar en el listado
  idUnidadMedida: number;
  unidadMedida: string;           // nombre para mostrar en el listado
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
};

// DTO de salida hacia el backend — alineado con ContratoCreateRequestDto.java
type ContratoRequestPayload = {
  numeroContrato: string;
  adquisicion: string;
  fechaTentativaLlegada: string | null;
  montoSinImpuestos: number | null;
  impuestos: number | null;
  montoTotal: number | null;
  proveedor: {
    razonSocial: string;
    domicilioFiscal: string;
    representante: string;
    caracter: string;
  } | null;
  comprador: {
    id: number | null;
  } | null;
  administradorContrato: {
    id: number | null;
  } | null;
  beneficiarios: string;
  clavesPresupuestales: {
    clave: string;
    partidaEspecifica: string;
    montoAsignado: number;
  }[];
  bienes: {
    idContratoBien: number | null;
    lote: number;
    partida: number;
    descripcionTecnica: string;
    idUnidadMedida: number;
    cantidad: number;
    precioUnitario: number;
    subtotal: number;
  }[];
};

// DTO de entrada desde el backend — alineado con ContratoDto.java
type ContratoResponsePayload = {
  idContrato: number;
  numeroContrato: string;
  adquisicion: string;
  fechaTentativaLlegada: string | null;
  montoSinImpuestos: number | null;
  impuestos: number | null;
  montoTotal: number | null;
  estatus: string;
  proveedor: {
    razonSocial: string;
    domicilioFiscal: string;
    representante: string;
    caracter: string;
  } | null;
  comprador: {
    id: number;
    nombre: string;
    dependencia: string;
    caracter: string;
  } | null;
  administradorContrato: {
    id: number;
    nombre: string;
    dependencia: string;
    caracter: string;
  } | null;
  beneficiarios: string;
  clavesPresupuestales: {
    clave: string;
    partidaEspecifica: string;
    montoAsignado: number;
  }[];
  bienes: {
    idContratoBien: number;
    lote: number;
    partida: number;
    descripcionTecnica: string;
    descripcionCorta: string;
    idUnidadMedida: number;
    unidadMedida: string;
    cantidad: number;
    precioUnitario: number;
    subtotal: number;
  }[];
};

// ================================================================
// VIEWMODEL
// ================================================================

class NuevoContratoViewModel {

  // ----------------------------------------------------------------
  // ROUTER / UI STATE
  // ----------------------------------------------------------------

  private router: any;

  private readonly userRole = getRole() ?? "ALMACEN";
  public calcEsAdmin = ko.pureComputed(() => this.userRole === "ADMINISTRADOR");

  public uiModo                      = ko.observable<ModoPantalla>("NUEVO");
  public uiCargando                  = ko.observable<boolean>(false);
  public uiGuardando                 = ko.observable<boolean>(false);
  public uiError                     = ko.observable<string>("");
  public uiExito                     = ko.observable<string>("");
  public uiEstatusContrato           = ko.observable<string>("En captura");
  public uiEstatusRaw                = ko.observable<string>("CAPTURA");
  public uiBienDescripcionDialogOpen = ko.observable<boolean>(false);
  public uiBienDialogOpen            = ko.observable<boolean>(false);
  public uiInfoExpanded              = ko.observable<boolean>(false);
  public uiEditandoFuncionarios      = ko.observable<boolean>(false);

  // ── Navegación de secciones ──
  public readonly listSecciones = [
    { id: "general",       label: "Datos generales",        hint: "Número de contrato, objeto del contrato y fecha tentativa de llegada." },
    { id: "partes",        label: "Partes",                 hint: "Datos del proveedor y funcionarios responsables del contrato." },
    { id: "pagos",         label: "Información financiera", hint: "Montos del contrato y asignación de claves presupuestales." },
    { id: "beneficiarios", label: "Beneficiarios",          hint: "Municipios o dependencias que recibirán los bienes de esta adquisición." },
    { id: "bienes",        label: "Adquisición",            hint: "Bienes contratados: lote, partida, cantidad, precio y descripción técnica." },
  ];

  public uiSeccionActiva = ko.observable<string>("general");

  public calcSeccionActiva = ko.pureComputed(() =>
    this.listSecciones.find(s => s.id === this.uiSeccionActiva()) ?? this.listSecciones[0]
  );

  public cmdNavegar = (id: string): void => {
    this.uiSeccionActiva(id);
  };

  public calcPuedeEditarFuncionarios = ko.pureComputed(() =>
    this.uiEstatusRaw() === "CAPTURA"
  );

  public contratoId = ko.observable<number | null>(null);

  // ----------------------------------------------------------------
  // COMPUTED - HEADER
  // ----------------------------------------------------------------

  public calcTituloPantalla = ko.pureComputed(() =>
    this.uiModo() === "EDICION" ? "Editar Contrato" : "Nuevo Contrato"
  );

  public calcNumeroContratoHeader = ko.pureComputed(() =>
    this.frmNumeroContrato() || "Sin capturar"
  );

  public calcAdquisicionHeader = ko.pureComputed(() =>
    this.frmAdquisicion() || "Sin capturar"
  );

  public calcProveedorHeader = ko.pureComputed(() =>
    this.frmProveedorRazonSocial() || "Sin capturar"
  );

  public calcMontoSinImpuestosHeader = ko.pureComputed(() =>
    Number(this.frmMontoSinImpuestos() || 0) > 0
      ? Number(this.frmMontoSinImpuestos()).toLocaleString('es-MX', { style: 'currency', currency: 'MXN' })
      : "Sin capturar"
  );

  // ----------------------------------------------------------------
  // INFORMACIÓN DEL CONTRATO
  // ----------------------------------------------------------------

  public frmNumeroContrato        = ko.observable<string>("");
  public frmAdquisicion           = ko.observable<string>("");
  public frmFechaTentativaLlegada = ko.observable<string>(new Date().toISOString().split("T")[0]); // formato "YYYY-MM-DD"


  // ----------------------------------------------------------------
  // COMPRADOR Y ADMINISTRADOR DEL CONTRATO
  // Se seleccionan desde un dropdown del catálogo de funcionarios.
  // Los campos de texto son solo lectura — se llenan al seleccionar.
  // ----------------------------------------------------------------

  public frmCompradorId = ko.observable<number | null>(null);
  public frmAdministradorId = ko.observable<number | null>(null);

  // Solo lectura — se actualizan al seleccionar del dropdown
  public uiCompradorNombre      = ko.observable<string>("");
  public uiCompradorDependencia = ko.observable<string>("");
  public uiCompradorCaracter    = ko.observable<string>("");

  public uiAdministradorNombre      = ko.observable<string>("");
  public uiAdministradorDependencia = ko.observable<string>("");
  public uiAdministradorCaracter    = ko.observable<string>("");

  public catFuncionarios      = ko.observableArray<FuncionarioOption>([]);
  public catCompradores       = ko.observableArray<FuncionarioOption>([]);
  public catAdministradores   = ko.observableArray<FuncionarioOption>([]);

  public dpCatCompradores     = new ArrayDataProvider(this.catCompradores,     { keyAttributes: "value" });
  public dpCatAdministradores = new ArrayDataProvider(this.catAdministradores, { keyAttributes: "value" });

  // ----------------------------------------------------------------
  // PROVEEDOR
  // ----------------------------------------------------------------

  public frmProveedorRazonSocial        = ko.observable<string>("");
  public frmProveedorDomicilioFiscal    = ko.observable<string>("");
  public frmProveedorRepresentante      = ko.observable<string>("");
  public frmProveedorCaracter           = ko.observable<string>("");

  // ----------------------------------------------------------------
  // PAGOS / MONTOS
  // ----------------------------------------------------------------

  public frmMontoSinImpuestos = ko.observable<number | null>(0);
  public frmImpuestos         = ko.observable<number | null>(0);

  public calcMontoTotal = ko.pureComputed(() => {
    const base = Number(this.frmMontoSinImpuestos() || 0);
    const imp  = Number(this.frmImpuestos() || 0);
    return base + imp;
  });

  // ----------------------------------------------------------------
  // CLAVES PRESUPUESTALES
  // ----------------------------------------------------------------

  public listClaves = ko.observableArray<ClavePresupuestalItem>([]);
  public dpListClaves = new ArrayDataProvider(this.listClaves, {
    keyAttributes: "idLocal"
  });

  public catClaves = ko.observableArray<CatalogoOption>([]);
  public dpCatClaves = new ArrayDataProvider(this.catClaves, {
    keyAttributes: "value"
  });

  private seqClave = 1;

  private makeClaveItem(idLocal: number, claveIdVal: string | null = null, montoVal: number | null = null): ClavePresupuestalItem {
    const found = this.catClaves().find(c => c.value === claveIdVal);
    return {
      idLocal,
      claveId: claveIdVal,
      partidaEspecifica: (found?.metadata?.partida_especifica as string) || "",
      montoAsignado: montoVal ?? 0,
    };
  }

  public calcHayClaves = ko.pureComputed(() =>
    this.listClaves().length > 0
  );

  public calcPuedeIngresarClaves = ko.pureComputed(() =>
    Number(this.frmMontoSinImpuestos() || 0) > 0
  );

  public calcSumaClaves = ko.pureComputed(() =>
    this.listClaves().reduce((sum, c) => sum + Number(c.montoAsignado || 0), 0)
  );

  public calcFaltaAsignar = ko.pureComputed(() =>
    Math.max(0, this.calcMontoTotal() - this.calcSumaClaves())
  );

  public calcClavesIncompletas = ko.pureComputed(() =>
    this.calcFaltaAsignar() > 0
  );
  public calcClavesExceden = ko.pureComputed(() =>
    this.calcSumaClaves() > this.calcMontoTotal() + 0.01
  );
  public calcSobranteClavesStr = ko.pureComputed(() =>
    (this.calcSumaClaves() - this.calcMontoTotal())
      .toLocaleString('es-MX', { style: 'currency', currency: 'MXN' })
  );

  public calcSumaClavesStr = ko.pureComputed(() =>
    this.calcSumaClaves().toLocaleString('es-MX', { style: 'currency', currency: 'MXN' })
  );

  public calcFaltaAsignarStr = ko.pureComputed(() =>
    this.calcFaltaAsignar().toLocaleString('es-MX', { style: 'currency', currency: 'MXN' })
  );

  public calcMontoTotalClavesStr = ko.pureComputed(() =>
    this.calcMontoTotal().toLocaleString('es-MX', { style: 'currency', currency: 'MXN' })
  );

  // ----------------------------------------------------------------
  // BENEFICIARIOS
  // Por ahora textarea — autocomplete en versión futura
  // ----------------------------------------------------------------

  public frmBeneficiariosTexto = ko.observable<string>("");

  // ----------------------------------------------------------------
  // BIENES
  // ----------------------------------------------------------------

  public frmBienLote           = ko.observable<number>(1);
  public frmBienPartida        = ko.observable<number>(1);
  public frmBienIdUnidadMedida = ko.observable<number | string | null>(null);
  public frmBienCantidad       = ko.observable<number>(1);
  public frmBienPrecioUnitario = ko.observable<number>(0);

  // La descripción técnica la maneja Quill directamente en el DOM.
  // Este observable almacena el HTML resultante para incluirlo en el payload.
  public frmBienDescripcionHtml = ko.observable<string>("");

  public calcBienSubtotal = ko.pureComputed(() =>
    Number(this.frmBienCantidad() || 0) * Number(this.frmBienPrecioUnitario() || 0)
  );

  public listBienes   = ko.observableArray<BienContratoItem>([]);
  public dpListBienes = new ArrayDataProvider(this.listBienes, {
    keyAttributes: "idLocal"
  });

  public catUnidadesMedida   = ko.observableArray<CatalogoOption>([]);
  public dpCatUnidadesMedida = new ArrayDataProvider<string, CatalogoOption>(this.catUnidadesMedida, {
    keyAttributes: "value"
  });

  private seqBien = 1;

  public uiBienEnEdicion        = ko.observable<BienContratoItem | null>(null);
  public calcHayBienEnEdicion   = ko.pureComputed(() => this.uiBienEnEdicion() !== null);
  public calcLabelBtnBien       = ko.pureComputed(() =>
    this.uiBienEnEdicion() ? "Guardar Cambios" : "Agregar Bien"
  );
  public calcTituloDialogBien   = ko.pureComputed(() =>
    this.uiBienEnEdicion() ? "Editar Bien" : "Agregar Bien"
  );

  public calcHayBienes = ko.pureComputed(() =>
    this.listBienes().length > 0
  );

  public calcTotalBienes    = ko.pureComputed(() => this.listBienes().length);
  public calcSumaBienes     = ko.pureComputed(() =>
    this.listBienes().reduce((sum, b) => sum + Number(b.subtotal || 0), 0)
  );
  public calcSumaBienesStr  = ko.pureComputed(() =>
    this.calcSumaBienes().toLocaleString('es-MX', { style: 'currency', currency: 'MXN' })
  );
  public calcMontoSinImpuestosStr = ko.pureComputed(() =>
    Number(this.frmMontoSinImpuestos() || 0).toLocaleString('es-MX', { style: 'currency', currency: 'MXN' })
  );
  public calcImpuestosStr = ko.pureComputed(() =>
    Number(this.frmImpuestos() || 0).toLocaleString('es-MX', { style: 'currency', currency: 'MXN' })
  );

  // ── Diálogos de info admin ──────────────────────────────────────
  public uiDialogoFinanciero    = ko.observable<boolean>(false);
  public uiDialogoBeneficiarios = ko.observable<boolean>(false);
  public cmdVerFinanciero    = (): void => { this.uiDialogoFinanciero(true); };
  public cmdVerBeneficiarios = (): void => { this.uiDialogoBeneficiarios(true); };
  public calcBienesIncompletos = ko.pureComputed(() =>
    Math.abs(this.calcSumaBienes() - Number(this.frmMontoSinImpuestos() || 0)) > 0.01
  );
  public calcBienesExceden = ko.pureComputed(() =>
    this.calcSumaBienes() > Number(this.frmMontoSinImpuestos() || 0) + 0.01
  );
  public calcBienesFaltan = ko.pureComputed(() =>
    this.calcSumaBienes() < Number(this.frmMontoSinImpuestos() || 0) - 0.01
  );
  public calcDiferenciaBienesStr = ko.pureComputed(() =>
    Math.abs(this.calcSumaBienes() - Number(this.frmMontoSinImpuestos() || 0))
      .toLocaleString('es-MX', { style: 'currency', currency: 'MXN' })
  );
  public calcBienesNuevos   = ko.pureComputed(() => this.listBienes().filter(b => b.idContratoBien === null).length);
  public calcBienesExistentes = ko.pureComputed(() => this.listBienes().filter(b => b.idContratoBien !== null).length);

  // ── Estado de secciones (para badges del sidebar) ──
  public calcSeccionGeneralDone = ko.pureComputed(() =>
    !!this.contratoId() &&
    !!this.frmNumeroContrato() &&
    !!this.frmFechaTentativaLlegada() &&
    !!this.frmAdquisicion()
  );
  public calcSeccionPartesDone = ko.pureComputed(() =>
    !!this.contratoId() &&
    !!this.frmCompradorId() &&
    !!this.frmAdministradorId() &&
    !!this.frmProveedorRazonSocial() &&
    !!this.frmProveedorDomicilioFiscal() &&
    !!this.frmProveedorRepresentante() &&
    !!this.frmProveedorCaracter()
  );
  public calcSeccionFinancieraDone = ko.pureComputed(() =>
    !!this.contratoId() &&
    Number(this.frmMontoSinImpuestos() || 0) > 0 &&
    this.calcClavesExceden()
  );
  public calcSeccionBeneficiariosDone = ko.pureComputed(() =>
    this.frmBeneficiariosTexto().trim().length > 0
  );
  public calcSeccionBienesDone = ko.pureComputed(() =>
    this.calcHayBienes() && !this.calcBienesIncompletos()
  );

  public calcTodasSeccionesDone = ko.pureComputed(() =>
    this.calcSeccionGeneralDone() &&
    this.calcSeccionPartesDone() &&
    this.calcSeccionFinancieraDone() &&
    this.calcSeccionBeneficiariosDone() &&
    this.calcSeccionBienesDone()
  );

  public calcBeneficiariosChips = ko.pureComputed(() =>
    this.frmBeneficiariosTexto()
      .split(",")
      .map(s => s.trim())
      .filter(s => s.length > 0)
  );

  // Descripción seleccionada para mostrar en el dialog (HTML renderizado)
  public uiDescripcionBienSeleccionada = ko.observable<string>("");

  public onQuillChanged = (event: CustomEvent): void => {
    this.frmBienDescripcionHtml(event.detail.value);
  };

  // ================================================================
  // CONSTRUCTOR / LIFECYCLE
  // ================================================================

  constructor(params: any) {
    this.router = params?.router;

    this.frmFechaTentativaLlegada(this.calcDiasHabiles(10)); // fecha tentativa por defecto: 10 días hábiles a partir de hoy

    // Row vacío inicial en claves presupuestales
    this.listClaves([this.makeClaveItem(1)]);
    this.seqClave = 2;

    const idParam = params?.routerState?.params?.id;
    if (idParam) {
      this.uiModo("EDICION");
      this.contratoId(Number(idParam));
      this.uiInfoExpanded(false);
    } else {
      this.uiInfoExpanded(true);
    }
  }

  connected(): void {
    document.body.style.cursor = "";
    AccUtils.announce(this.calcTituloPantalla());
    document.title = this.calcTituloPantalla();
    void this.loadInicial();

    if (sessionStorage.getItem("contrato.recienGuardado")) {
      sessionStorage.removeItem("contrato.recienGuardado");
      this.uiExito("Contrato guardado correctamente.");
      setTimeout(() => this.uiExito(""), 3000);
    }
  }

  disconnected(): void {}
  transitionCompleted(): void {}

  // ================================================================
  // LOAD
  // ================================================================

  private async loadInicial(): Promise<void> {
    this.uiCargando(true);
    this.uiError("");

    try {
      await Promise.all([
        this.loadCatalogoClaves(),
        this.loadCatalogoUnidadesMedida(),
        this.loadCatalogoFuncionarios()
      ]);

      if (this.uiModo() === "EDICION" && this.contratoId()) {
        await this.loadContrato(this.contratoId()!);
      }
    } catch (err) {
      console.error("Error en carga inicial:", err);
      this.uiError("No se pudo cargar la información. Intenta de nuevo.");
    } finally {
      this.uiCargando(false);
    }
  }

  private async loadContrato(id: number): Promise<void> {
    const data: ContratoResponsePayload = await contratosApi.obtenerPorId(id);
    console.log("=== JSON CRUDO DEL BACKEND ===", JSON.stringify(data, null, 2));
    this.mapResponseToUI(data);
    this.navegarPrimerSeccionIncompleta();
  }

  private navegarPrimerSeccionIncompleta(): void {
    const secciones = [
      { id: "general",       done: this.calcSeccionGeneralDone },
      { id: "partes",        done: this.calcSeccionPartesDone },
      { id: "pagos",         done: this.calcSeccionFinancieraDone },
      { id: "beneficiarios", done: this.calcSeccionBeneficiariosDone },
      { id: "bienes",        done: this.calcSeccionBienesDone },
    ];
    const primera = secciones.find(s => !s.done());
    if (primera) {
      this.uiSeccionActiva(primera.id);
    }
  }

  private async loadCatalogoClaves(): Promise<void> {
    const data: CatalogoOption[] = await catalogosApi.obtenerClavesPresupuestales();
    this.catClaves(data);
  }

  private async loadCatalogoUnidadesMedida(): Promise<void> {
    const data: CatalogoOption[] = await catalogosApi.obtenerUnidadesMedida();
    this.catUnidadesMedida(data);
  }

  private async loadCatalogoFuncionarios(): Promise<void> {
    const data: any[] = await catalogosApi.obtenerFuncionarios();

    // El backend devuelve { id, nombre, dependencia, caracter }
    // Lo adaptamos al formato que necesita oj-c-select-single
    const options: FuncionarioOption[] = data.map(f => ({
      value: f.id,
      label: f.nombre,
      dependencia: f.dependencia,
      caracter: f.caracter,
      tipo: f.tipoFuncionario as "TITULAR" | "ADMINISTRADOR"
    }));

    this.catFuncionarios(options);
    this.catCompradores(options.filter(f => f.tipo === "TITULAR"));
    this.catAdministradores(options.filter(f => f.tipo === "ADMINISTRADOR"));

    if (this.uiModo() === "NUEVO") {
      const titular = options.find(f => f.tipo === "TITULAR");
      if (titular) {
        this.frmCompradorId(titular.value);
        this.uiCompradorNombre(titular.label);
        this.uiCompradorDependencia(titular.dependencia);
        this.uiCompradorCaracter(titular.caracter);
      }

      const admin = options.find(f => f.tipo === "ADMINISTRADOR");
      if (admin) {
        this.frmAdministradorId(admin.value);
        this.uiAdministradorNombre(admin.label);
        this.uiAdministradorDependencia(admin.dependencia);
        this.uiAdministradorCaracter(admin.caracter);
      }
    }
  }

  // ================================================================
  // EVENT HANDLERS
  // ================================================================

  /**
   * Al seleccionar un comprador del dropdown, actualiza los campos
   * informativos de solo lectura con los datos del funcionario.
   */
  public onCompradorChanged = (event: CustomEvent): void => {
    if (event.detail.updatedFrom === "external") return;

    const id = event.detail.value as number;
    const funcionario = this.catFuncionarios().find(f => f.value === id);

    this.uiCompradorNombre(funcionario?.label || "");
    this.uiCompradorDependencia(funcionario?.dependencia || "");
    this.uiCompradorCaracter(funcionario?.caracter || "");
  };

  /**
   * Al seleccionar un administrador del dropdown, actualiza los campos
   * informativos de solo lectura.
   */
  public onAdministradorChanged = (event: CustomEvent): void => {
    if (event.detail.updatedFrom === "external") return;

    const id = event.detail.value as number;
    const funcionario = this.catFuncionarios().find(f => f.value === id);

    this.uiAdministradorNombre(funcionario?.label || "");
    this.uiAdministradorDependencia(funcionario?.dependencia || "");
    this.uiAdministradorCaracter(funcionario?.caracter || "");
  };


  // ================================================================
  // COMMANDS — NAVEGACIÓN
  // ================================================================

  public calcLabelRegresar = ko.pureComputed(() =>
    this.uiModo() === "EDICION" ? "Regresar" : "Ir a Inicio"
  );

  public cmdEditarFuncionarios = (): void => {
    this.uiEditandoFuncionarios(true);
  };

  public cmdCancelarEditarFuncionarios = (): void => {
    this.uiEditandoFuncionarios(false);
  };

  public cmdGoBack = (): void => {
    if (this.uiModo() === "EDICION" && this.contratoId()) {
      this.router.go({ path: "contrato-detalle", params: { id: this.contratoId() } });
    } else {
      this.router.go({ path: "dashboard" });
    }
  };

  // ================================================================
  // COMMANDS — CLAVES PRESUPUESTALES
  // ================================================================

  public cmdAgregarClave = (): void => {
    this.listClaves.push(this.makeClaveItem(this.seqClave++));
  };

  public cmdEliminarClave = (item: ClavePresupuestalItem): void => {
    this.listClaves.remove(item);
  };

  public cmdSetClaveId = (item: ClavePresupuestalItem, ev: CustomEvent): void => {
    const detail = ev.detail as any;
    if (detail?.updatedFrom === "external") return;
    const newClaveId: string | null = detail?.value ?? null;
    const found = this.catClaves().find(c => c.value === newClaveId);
    const newPartida = (found?.metadata?.partida_especifica as string) || "";
    const idx = this.listClaves().indexOf(item);
    if (idx >= 0) {
      this.listClaves.splice(idx, 1, { idLocal: item.idLocal, claveId: newClaveId, partidaEspecifica: newPartida, montoAsignado: item.montoAsignado });
    }
  };

  public cmdSetMonto = (item: ClavePresupuestalItem, ev: CustomEvent): void => {
    const detail = ev.detail as any;
    if (detail?.updatedFrom === "external") return;
    const idx = this.listClaves().indexOf(item);
    if (idx >= 0) {
      this.listClaves.splice(idx, 1, { ...item, montoAsignado: detail?.value ?? null });
    }
  };


  // ================================================================
  // COMMANDS — BIENES
  // ================================================================

  /**
   * Agrega un bien al listado.
   * La descripción técnica se toma del observable frmBienDescripcionHtml,
   * que debe ser actualizado por el editor Quill antes de llamar este método.
   * Ver contrato.html para la integración con Quill.
   */
  public cmdAbrirNuevoBien = (): void => {
    this.uiBienEnEdicion(null);
    this.clearFrmBien();
    this.uiBienDialogOpen(true);
  };

  public cmdAgregarBien = (): void => {
    const idUnidad = this.frmBienIdUnidadMedida();
    const unidad   = this.catUnidadesMedida().find(u => Number(u.value) === Number(idUnidad));

    if (!idUnidad || !unidad) {
      alert("Selecciona una unidad de medida.");
      return;
    }

    if (!this.frmBienDescripcionHtml() || this.frmBienDescripcionHtml() === "<p><br></p>") {
      alert("Escribe la descripción técnica del bien.");
      return;
    }

    if (this.frmBienCantidad() < 1 || this.frmBienPrecioUnitario() < 1) {
      alert("La cantidad y el precio unitario deben ser mayores a 0.");
      return;
    }

    const enEdicion = this.uiBienEnEdicion();
    const newBien: BienContratoItem = {
      idLocal:            enEdicion ? enEdicion.idLocal : this.seqBien++,
      idContratoBien:     enEdicion ? enEdicion.idContratoBien : null,
      lote:               Number(this.frmBienLote()),
      partida:            Number(this.frmBienPartida()),
      descripcionTecnica: this.frmBienDescripcionHtml(),
      descripcionCorta:   this.stripHtml(this.frmBienDescripcionHtml()),
      idUnidadMedida:     Number(idUnidad),
      unidadMedida:       unidad.label,
      cantidad:           Number(this.frmBienCantidad()),
      precioUnitario:     Number(this.frmBienPrecioUnitario()),
      subtotal:           Number(this.calcBienSubtotal())
    };

    if (enEdicion) {
      const idx = this.listBienes().indexOf(enEdicion);
      if (idx >= 0) {
        this.listBienes.splice(idx, 1, newBien);
      } else {
        this.listBienes.push(newBien);
      }
    } else {
      this.listBienes.push(newBien);
    }

    this.uiBienEnEdicion(null);
    this.clearFrmBien();
    this.uiBienDialogOpen(false);
  };

  public cmdEliminarBien = (bien: BienContratoItem): void => {
    this.listBienes.remove(bien);
  };

  /**
   * Abre el dialog de descripción del bien.
   * Renderiza el HTML de Quill directamente en el dialog.
   * Ver contrato.html para el binding con innerHTML.
   */
  public cmdVerDescripcionBien = (bien: BienContratoItem): void => {
    this.uiDescripcionBienSeleccionada(bien.descripcionTecnica);
    this.uiBienDescripcionDialogOpen(true);
  };

  public cmdEditarBien = (bien: BienContratoItem): void => {
    this.uiBienEnEdicion(bien);
    this.frmBienLote(bien.lote);
    this.frmBienPartida(bien.partida);
    this.frmBienIdUnidadMedida(String(bien.idUnidadMedida));
    this.frmBienDescripcionHtml(bien.descripcionTecnica);
    this.frmBienCantidad(bien.cantidad);
    this.frmBienPrecioUnitario(bien.precioUnitario);
    this.uiBienDialogOpen(true);
  };

  public cmdCancelarEditarBien = (): void => {
    this.uiBienEnEdicion(null);
    this.clearFrmBien();
    this.uiBienDialogOpen(false);
  };

  public cmdCerrarDescripcionDialog = (): void => {
    this.uiBienDescripcionDialogOpen(false);
  };

  private clearFrmBien(): void {
    this.frmBienLote(1);
    this.frmBienPartida(1);
    this.frmBienIdUnidadMedida(null);
    this.frmBienDescripcionHtml("");
    this.frmBienCantidad(1);
    this.frmBienPrecioUnitario(0);
  }

  // ================================================================
  // COMMANDS — CONTRATO
  // ================================================================

  public cmdGuardarBorrador = async (): Promise<void> => {
    this.uiGuardando(true);
    this.uiError("");

    try {
      const payload  = this.mapUIToRequest();
      const isUpdate = !!this.contratoId();

      const saved: ContratoResponsePayload = isUpdate
        ? await contratosApi.actualizar(this.contratoId()!, payload)
        : await contratosApi.crear(payload);

      if (!isUpdate) {
        // Primer guardado: marcar para mostrar banner en la nueva instancia y navegar con ID
        sessionStorage.setItem("contrato.recienGuardado", "1");
        this.router?.go({ path: "contrato", params: { id: saved.idContrato } });
        return;
      }

      // Guardados subsecuentes: recargar para sincronizar IDs del backend
      await this.loadContrato(saved.idContrato);
      this.uiExito("Contrato guardado correctamente.");
      setTimeout(() => this.uiExito(""), 3000);
    } catch (err: any) {
      console.error("Error al guardar:", err);
      this.uiError(err.message || "Error desconocido al guardar.");
    } finally {
      this.uiGuardando(false);
    }
  };

  public cmdEnviarAlmacen = async (): Promise<void> => {
    if (!this.contratoId()) {
      alert("Guarda el contrato antes de enviarlo al almacén.");
      return;
    }

    try {
      await contratosApi.enviarAlmacen(this.contratoId()!);
      this.uiEstatusContrato("Pendiente de recibir");
      alert("Contrato enviado al almacén correctamente.");
      this.router.go({ path: "dashboard" });
    } catch (err: any) {
      console.error("Error al enviar al almacén:", err);
      this.uiError(err.message || "Error al enviar al almacén.");
    }
  };

  // ================================================================
  // MAPPERS
  // ================================================================

  /**
   * UI → Request payload (hacia el backend)
   */
  private mapUIToRequest(): ContratoRequestPayload {
    return {
      numeroContrato:        this.frmNumeroContrato(),
      adquisicion:           this.frmAdquisicion(),
      fechaTentativaLlegada: this.toDateTime(this.frmFechaTentativaLlegada()),
      montoSinImpuestos:     this.frmMontoSinImpuestos(),
      impuestos:             this.frmImpuestos(),
      montoTotal:            this.calcMontoTotal(),

      proveedor: this.frmProveedorRazonSocial() ? {
        razonSocial:     this.frmProveedorRazonSocial(),
        domicilioFiscal: this.frmProveedorDomicilioFiscal(),
        representante:   this.frmProveedorRepresentante(),
        caracter:        this.frmProveedorCaracter()
      } : null,

      comprador: this.frmCompradorId() ? {
        id: this.frmCompradorId()
      } : null,

      administradorContrato: this.frmAdministradorId() ? {
        id: this.frmAdministradorId()
      } : null,

      beneficiarios: this.frmBeneficiariosTexto(),

      clavesPresupuestales: this.listClaves()
        .filter(c => !!c.claveId)
        .map(c => ({
          clave:             c.claveId!,
          partidaEspecifica: c.partidaEspecifica,
          montoAsignado:     c.montoAsignado ?? 0
        })),

      bienes: this.listBienes()
        .filter(b => !!b.idUnidadMedida)
        .map(b => ({
          idContratoBien:    b.idContratoBien,
          lote:              b.lote,
          partida:           b.partida,
          descripcionTecnica: b.descripcionTecnica,
          idUnidadMedida:    b.idUnidadMedida,
          cantidad:          b.cantidad,
          precioUnitario:    b.precioUnitario,
          subtotal:          b.subtotal
        }))
    };
  }

  /**
   * Response payload (desde el backend) → UI
   * Se llama al cargar el contrato en modo edición y después de guardar.
   */
  private mapResponseToUI(data: ContratoResponsePayload): void {

    // Datos básicos
    this.frmNumeroContrato(data.numeroContrato);
    this.frmAdquisicion(data.adquisicion);
    this.frmFechaTentativaLlegada(this.toDateOnly(data.fechaTentativaLlegada));

    this.uiEstatusRaw(data.estatus);
    this.uiEstatusContrato(mapEstatusToLabel(data.estatus));

    // Montos
    this.frmMontoSinImpuestos(data.montoSinImpuestos);
    this.frmImpuestos(data.impuestos);


    // Proveedor
    if (data.proveedor) {
      this.frmProveedorRazonSocial(data.proveedor?.razonSocial || "");
      this.frmProveedorDomicilioFiscal(data.proveedor?.domicilioFiscal || "");
      this.frmProveedorRepresentante(data.proveedor?.representante || "");
      this.frmProveedorCaracter(data.proveedor?.caracter || "");
    }

    // Comprador — seleccionar en dropdown y actualizar campos informativos
    if (data.comprador) {
      this.frmCompradorId(data.comprador.id);
      this.uiCompradorNombre(data.comprador?.nombre || "");
      this.uiCompradorDependencia(data.comprador?.dependencia || "");
      this.uiCompradorCaracter(data.comprador?.caracter || "");
    }

    // Administrador del contrato
    if (data.administradorContrato) {
      this.frmAdministradorId(data.administradorContrato.id);
      this.uiAdministradorNombre(data.administradorContrato?.nombre || "");
      this.uiAdministradorDependencia(data.administradorContrato?.dependencia || "");
      this.uiAdministradorCaracter(data.administradorContrato?.caracter || "");
    }

    // Beneficiarios
    this.frmBeneficiariosTexto(data.beneficiarios || "");

    // Claves presupuestales
    const clavesFromApi = (data.clavesPresupuestales ?? []).map((c, i) =>
      this.makeClaveItem(i + 1, c.clave, c.montoAsignado)
    );
    this.seqClave = clavesFromApi.length + 1;
    this.listClaves(
      clavesFromApi.length > 0 ? clavesFromApi : [this.makeClaveItem(this.seqClave++)]
    );

    // Bienes — incluyen idContratoBien para que el PUT pueda hacer upsert
    this.listBienes(
      data.bienes.map((b, i) => ({
        idLocal:           i + 1,
        idContratoBien:    b.idContratoBien,
        lote:              b.lote,
        partida:           b.partida,
        descripcionTecnica: b.descripcionTecnica,
        descripcionCorta:   b.descripcionCorta,
        idUnidadMedida:    b.idUnidadMedida,
        unidadMedida:      b.unidadMedida,
        cantidad:          b.cantidad,
        precioUnitario:    b.precioUnitario,
        subtotal:          b.subtotal
      }))
    );
    this.seqBien = data.bienes.length + 1;

  }

    // Extrae texto plano del HTML de Quill y lo trunca para mostrar en listados
    private stripHtml(html: string): string {
        const div = document.createElement("div");
        div.innerHTML = html;
        const text = (div.textContent || div.innerText || "").replace(/\s+/g, " ").trim();
        return text.length > 100 ? text.slice(0, 100) + "…" : text;
    }

    // Convierte "2026-06-15T09:00:00" → "2026-06-15" para el input-date-text
    private toDateOnly(isoDateTime: string | null): string {
        console.log("Convirtiendo fecha:", isoDateTime);
        if (!isoDateTime) return this.calcDiasHabiles(10); // valor por defecto si no hay fecha);
        return isoDateTime.split("T")[0];
    }

    // Convierte "2026-06-15" → "2026-06-15T00:00:00" para el backend
    private toDateTime(dateOnly: string | null): string | null {
        if (!dateOnly) return null;
        return dateOnly + "T00:00:00";
    }

     // Utilidad: calcula N días hábiles a partir de hoy
    private calcDiasHabiles(diasHabiles: number): string {
        const fecha = new Date();
        let contados = 0;

        while (contados < diasHabiles) {
            fecha.setDate(fecha.getDate() + 1);
            const diaSemana = fecha.getDay();
            // 0 = domingo, 6 = sábado
            if (diaSemana !== 0 && diaSemana !== 6) {
                contados++;
            }
        }
        // Retorna formato YYYY-MM-DD
        return fecha.toISOString().split("T")[0];
    }

}

export = NuevoContratoViewModel;
