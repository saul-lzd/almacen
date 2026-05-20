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
import { IntlDateTimeConverter } from "ojs/ojconverter-datetime";
import "../jet-composites/quill-editor/quill-editor"; // Importar el componente QuillEditorViewModel para que se registre globalmente
import { mapEstatusToLabel } from "../utils/contratoUtils";

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
};

type ClavePresupuestalItem = {
  idLocal: number;
  clave: string;
  partidaEspecifica: string;
  montoAsignado: number;
};

type BienContratoItem = {
  idLocal: number;
  idContratoBien: number | null;  // null si es nuevo, número si ya existe en BD
  lote: number;
  partida: number;
  descripcionTecnica: string;     // HTML generado por Quill
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

  public uiModo                      = ko.observable<ModoPantalla>("NUEVO");
  public uiCargando                  = ko.observable<boolean>(false);
  public uiGuardando                 = ko.observable<boolean>(false);
  public uiError                     = ko.observable<string>("");
  public uiEstatusContrato           = ko.observable<string>("En captura");
  public uiBienDescripcionDialogOpen = ko.observable<boolean>(false);

  public contratoId = ko.observable<number | null>(null);

  // ----------------------------------------------------------------
  // COMPUTED - HEADER
  // ----------------------------------------------------------------

  public calcTituloPantalla = ko.pureComputed(() =>
    this.uiModo() === "EDICION" ? "Editar Contrato" : "Nuevo Contrato"
  );

  public calcNumeroContratoHeader = ko.pureComputed(() =>
    this.frmNumeroContrato() || "Pendiente"
  );

  public calcProveedorHeader = ko.pureComputed(() =>
    this.frmProveedorRazonSocial() || "Pendiente"
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

  public catFuncionarios   = ko.observableArray<FuncionarioOption>([]);
  public dpCatFuncionarios = new ArrayDataProvider(this.catFuncionarios, {
    keyAttributes: "value"
  });

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

  public frmClaveValue              = ko.observable<string | null>(null);
  public frmClavePartidaEspecifica  = ko.observable<string>("");
  public frmClaveMontoAsignado      = ko.observable<number>(0);

  public listClaves                 = ko.observableArray<ClavePresupuestalItem>([]);
  public dpListClaves               = new ArrayDataProvider(this.listClaves, {
    keyAttributes: "idLocal"
  });

  public catClaves                  = ko.observableArray<CatalogoOption>([]);
  public dpCatClaves                = new ArrayDataProvider(this.catClaves, {
    keyAttributes: "value"
  });

  private seqClave = 1;

  public calcHayClaves = ko.pureComputed(() =>
    this.listClaves().length > 0
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
  public frmBienIdUnidadMedida = ko.observable<number | null>(null);
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
  public dpCatUnidadesMedida = new ArrayDataProvider(this.catUnidadesMedida, {
    keyAttributes: "value"
  });

  private seqBien = 1;

  public calcHayBienes = ko.pureComputed(() =>
    this.listBienes().length > 0
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

    const idParam = params?.routerState?.params?.id;
    if (idParam) {
      this.uiModo("EDICION");
      this.contratoId(Number(idParam));
    }
  }

  connected(): void {
    AccUtils.announce(this.calcTituloPantalla());
    document.title = this.calcTituloPantalla();
    void this.loadInicial();
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
    const res = await fetch(`http://localhost:8080/api/contratos/${id}`);
    if (!res.ok) throw new Error(`Error al cargar contrato ${id}`);

    const data: ContratoResponsePayload = await res.json();
    console.log("=== JSON CRUDO DEL BACKEND ===", JSON.stringify(data, null, 2));
    this.mapResponseToUI(data);
  }

  private async loadCatalogoClaves(): Promise<void> {
    const res = await fetch("http://localhost:8080/api/claves-presupuestales");
    if (!res.ok) throw new Error("Error al cargar claves presupuestales");

    const data: CatalogoOption[] = await res.json();
    this.catClaves(data);
  }

  private async loadCatalogoUnidadesMedida(): Promise<void> {
    const res = await fetch("http://localhost:8080/api/unidadesMedida");
    if (!res.ok) throw new Error("Error al cargar unidades de medida");

    const data: CatalogoOption[] = await res.json();
    this.catUnidadesMedida(data);
  }

  private async loadCatalogoFuncionarios(): Promise<void> {
    const res = await fetch("http://localhost:8080/api/funcionarios");
    if (!res.ok) throw new Error("Error al cargar funcionarios");

    const data: any[] = await res.json();

    // El backend devuelve { id, nombre, dependencia, caracter }
    // Lo adaptamos al formato que necesita oj-c-select-single
    const options: FuncionarioOption[] = data.map(f => ({
      value: f.id,
      label: f.nombre,
      dependencia: f.dependencia,
      caracter: f.caracter
    }));

    this.catFuncionarios(options);
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

  /**
   * Al seleccionar una clave presupuestal, autocompleta la partida específica.
   */
  public onClaveChanged = (event: CustomEvent): void => {
    if (event.detail.updatedFrom === "external") return;

    const clave = this.catClaves().find(c => c.value === event.detail.value);
    this.frmClavePartidaEspecifica(
      (clave?.metadata?.partida_especifica as string) || ""
    );
  };

  // ================================================================
  // COMMANDS — NAVEGACIÓN
  // ================================================================

  public cmdGoToInicio = (): void => {
    this.router.go({ path: "dashboard" });
  };

  // ================================================================
  // COMMANDS — CLAVES PRESUPUESTALES
  // ================================================================

  public cmdAgregarClave = (): void => {
    const clave = this.catClaves().find(c => c.value === this.frmClaveValue());

    if (!clave || this.frmClaveMontoAsignado() < 1) {
      alert("Selecciona una clave presupuestal y asigna un monto mayor a 0.");
      return;
    }

    // Verificar que la clave no esté ya en la lista
    const yaExiste = this.listClaves().some(c => c.clave === clave.value);
    if (yaExiste) {
      alert("Esta clave presupuestal ya fue agregada.");
      return;
    }

    this.listClaves.push({
      idLocal: this.seqClave++,
      clave: clave.value,
      partidaEspecifica: this.frmClavePartidaEspecifica(),
      montoAsignado: Number(this.frmClaveMontoAsignado())
    });

    this.clearFrmClave();
  };

  public cmdEliminarClave = (item: ClavePresupuestalItem): void => {
    this.listClaves.remove(item);
  };

  private clearFrmClave(): void {
    this.frmClaveValue(null);
    this.frmClavePartidaEspecifica("");
    this.frmClaveMontoAsignado(0);
  }

  // ================================================================
  // COMMANDS — BIENES
  // ================================================================

  /**
   * Agrega un bien al listado.
   * La descripción técnica se toma del observable frmBienDescripcionHtml,
   * que debe ser actualizado por el editor Quill antes de llamar este método.
   * Ver contrato.html para la integración con Quill.
   */
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

    this.listBienes.push({
      idLocal:           this.seqBien++,
      idContratoBien:    null,          // null = nuevo, el backend genera el ID
      lote:              Number(this.frmBienLote()),
      partida:           Number(this.frmBienPartida()),
      descripcionTecnica: this.frmBienDescripcionHtml(),
      idUnidadMedida:    idUnidad,
      unidadMedida:      unidad.label,  // nombre para mostrar en el listado
      cantidad:          Number(this.frmBienCantidad()),
      precioUnitario:    Number(this.frmBienPrecioUnitario()),
      subtotal:          Number(this.calcBienSubtotal())
    });

    console.log("Bien agregado:", {
      lote: this.frmBienLote(),
      partida: this.frmBienPartida(),
      descripcionTecnica: this.frmBienDescripcionHtml(),
      idUnidadMedida: this.frmBienIdUnidadMedida(),
      cantidad: this.frmBienCantidad(),
      precioUnitario: this.frmBienPrecioUnitario(),
      subtotal: this.calcBienSubtotal()
    });

    this.clearFrmBien();
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
    // El reset del editor Quill se maneja en el HTML con un evento custom
    document.dispatchEvent(new CustomEvent("quill:reset"));
  }

  // ================================================================
  // COMMANDS — CONTRATO
  // ================================================================

  public cmdGuardarBorrador = async (): Promise<void> => {
    this.uiGuardando(true);
    this.uiError("");

    console.log("=== PAYLOAD QUE SE ENVIARÁ AL BACKEND ===", JSON.stringify(this.mapUIToRequest(), null, 2));

    try {
      const payload = this.mapUIToRequest();
      const isUpdate = !!this.contratoId();
      const method   = isUpdate ? "PUT" : "POST";
      const url      = isUpdate
        ? `http://localhost:8080/api/contratos/${this.contratoId()}`
        : "http://localhost:8080/api/contratos";

      const res = await fetch(url, {
        method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });

      if (!res.ok) {
        const errBody = await res.text();
        throw new Error(`HTTP ${res.status}: ${errBody}`);
      }

      const saved: ContratoResponsePayload = await res.json();

      // Si era nuevo, actualizar el ID para que los siguientes guardados sean PUT
      if (!isUpdate) {
        this.contratoId(saved.idContrato);
        this.uiModo("EDICION");
      }

      // Recargar el contrato para sincronizar IDs de bienes y claves generados por el backend
      await this.loadContrato(saved.idContrato);

      console.log("Contrato guardado:", saved.idContrato);
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
      const res = await fetch(
        `http://localhost:8080/api/contratos/${this.contratoId()}/enviar-almacen`,
        { method: "PATCH" }
      );

      if (!res.ok) {
        const errBody = await res.json();
        // El backend devuelve { mensaje, errores: string[] }
        const errores = errBody.errores?.join("\n") || "Error desconocido.";
        alert("No se puede enviar al almacén:\n\n" + errores);
        return;
      }

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

      clavesPresupuestales: this.listClaves().map(c => ({
        clave:            c.clave,
        partidaEspecifica: c.partidaEspecifica,
        montoAsignado:    c.montoAsignado
      })),

      bienes: this.listBienes().map(b => ({
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

    // Etiqueta de estatus traducida para la UI
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
    this.listClaves(
      data.clavesPresupuestales.map((c, i) => ({
        idLocal:           i + 1,
        clave:             c.clave,
        partidaEspecifica: c.partidaEspecifica,
        montoAsignado:     c.montoAsignado
      }))
    );
    this.seqClave = data.clavesPresupuestales?.length + 1;

    // Bienes — incluyen idContratoBien para que el PUT pueda hacer upsert
    this.listBienes(
      data.bienes.map((b, i) => ({
        idLocal:           i + 1,
        idContratoBien:    b.idContratoBien,
        lote:              b.lote,
        partida:           b.partida,
        descripcionTecnica: b.descripcionTecnica,
        idUnidadMedida:    b.idUnidadMedida,
        unidadMedida:      b.unidadMedida,
        cantidad:          b.cantidad,
        precioUnitario:    b.precioUnitario,
        subtotal:          b.subtotal
      }))
    );
    this.seqBien = data.bienes.length + 1;

  }

  // /**
  //  * Convierte el enum del backend a etiqueta en español para la UI.
  //  */
  // private mapEstatusToLabel(estatus: string): string {
  //   const etiquetas: Record<string, string> = {
  //     CAPTURA:               "En captura",
  //     POR_RECIBIR:           "Pendiente de recibir",
  //     EN_ALMACEN:            "En almacén",
  //     LISTO_PARA_ENTREGAR:   "Listo para entregar",
  //     ENTREGA_PARCIAL:       "Entrega parcial",
  //     ENTREGADO:             "Entregado",
  //     CERRADO:               "Cerrado"
  //   };
  //   return etiquetas[estatus] || estatus;
  // }

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

    // Converter para mostrar dd/MM/yyyy al usuario
    // El valor interno sigue siendo YYYY-MM-DD (ISO)
    public converterFecha = new IntlDateTimeConverter({
        pattern: "dd/MM/yyyy"
    });

}

export = NuevoContratoViewModel;
