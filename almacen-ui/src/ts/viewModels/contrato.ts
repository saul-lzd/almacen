/**
 * ViewModel: Nuevo / Editar Contrato
 * Convencion de nombres:
 * - frm*: valores editables ligados directamente al formulario
 * - list*: arrays de elementos capturados que se enviaran al backend
 * - dp*: DataProvider usado por componentes Oracle JET
 * - cat*: catalogos/listas recibidas desde API
 * - ui*: estado visual de pantalla
 * - calc*: computed/calculados
 * - cmd*: acciones disparadas desde UI
 * - load*: carga de datos desde API/backend
 * - clear*: limpieza de formularios internos
 * - map*: transformacion a payload/backend
 * - on*: handlers de eventos UI
 */

import * as AccUtils from "../accUtils";
import * as ko from "knockout";
import ArrayDataProvider = require("ojs/ojarraydataprovider");

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

// ================================================================
// TYPES BASE
// ================================================================

type ModoPantalla = "NUEVO" | "EDICION";

type CatalogoOption = {
  value: string;
  label: string;
  metadata?: Record<string, unknown>;
};

type ClavePresupuestalItem = {
  idLocal: number;
  clavePresupuestal: string;
  partidaEspecifica: string;
  montoAsignado: number;
};


type BienContratoItem = {
  idLocal: number;
  lote: number;
  partida: number;
  descripcionTecnica: string;
  codigoUnidadMedida: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
};

type ContratoPayload = {
  idContrato: number | null;
  numeroContrato: string;
  adquisicion: string;

  fechaOrigen: string | null;
  fechaTentativaLlegada: string | null;
  folioOrigen: string | null;

  idEstatusContrato: number | null;
  estatusContrato: string | null;

  comprador: {
    id: number | null;
    dependencia: string;
    nombre: string;
    cargo: string;
  };

  administradorContrato: {
    id: number | null;
    dependencia: string;
    nombre: string;
    cargo: string;
  };

  proveedor: {
    razonSocial: string;
    representanteEmpresa: string;
    direccion: string;
    caracterRepresentante: string;
  };

  detallesPago: {
    montoSinImpuestos: number;
    impuestos: number;
    montoTotal: number;
    montoAnticipo: number | null;
    montoFiniquito: number | null;
    porcentajeAnticipo: number | null;
    porcentajeFiniquito: number | null;
    tieneAnticipo: boolean | null;
    tieneFiniquito: boolean | null;
    numeroExhibiciones: number;
  };
  beneficiarios: string;
  clavesPresupuestales: ClavePresupuestalItem[];
  productos : BienContratoItem[];
};

class NuevoContratoViewModel {
  // ================================================================
  // ROUTER / UI STATE
  // ================================================================

  private router: any;

  public uiModo = ko.observable<ModoPantalla>("NUEVO");
  public uiCargandoCatalogos = ko.observable<boolean>(false);
  public uiGuardando = ko.observable<boolean>(false);
  public uiError = ko.observable<string>("");
  public uiEstatusContrato = ko.observable<string>("En captura");
  public uiBienDescripcionDialogOpened = ko.observable<boolean>(false);

  public contratoId = ko.observable<number | null>(null);

  public calcTituloPantalla = ko.pureComputed(() => {
    return this.uiModo() === "EDICION" ? "Editar Contrato" : "Nuevo Contrato";
  });

  public calcNumeroContratoHeader = ko.pureComputed(() => {
    return this.frmContratoNumero() || "Pendiente";
  });

  public calcProveedorHeader = ko.pureComputed(() => {
    return this.frmProveedorEmpresa() || "Pendiente";
  });

  // ================================================================
  // CONTRATO - FORM
  // ================================================================

  public frmContratoNumero = ko.observable<string>("");
  public frmContratoAdquisicion = ko.observable<string>("");
  public frmContratoCotizacion = ko.observable<string>("");

  // ================================================================
  // COMPRADOR - FORM
  // ================================================================

  public frmCompradorTitularDependencia = ko.observable<string>("");
  public frmCompradorTitularNombre = ko.observable<string>("");
  public frmCompradorTitularCaracter = ko.observable<string>("");

  public frmCompradorAdministradorDependencia = ko.observable<string>("");
  public frmCompradorAdministradorNombre = ko.observable<string>("");
  public frmCompradorAdministradorCaracter = ko.observable<string>("");

  // ================================================================
  // PROVEEDOR - FORM
  // ================================================================

  public frmProveedorEmpresa = ko.observable<string>("");
  public frmProveedorDomicilioFiscal = ko.observable<string>("");
  public frmProveedorRepresentante = ko.observable<string>("");
  public frmProveedorCaracter = ko.observable<string>("");

  // ================================================================
  // PAGOS - FORM / CALC
  // ================================================================

  public frmPagoMontoSinImpuestos = ko.observable<number>(0);
  public frmPagoImpuestos = ko.observable<number>(0);

  public calcPagoMontoTotal = ko.pureComputed(() => {
    return Number(this.frmPagoMontoSinImpuestos() || 0) + Number(this.frmPagoImpuestos() || 0);
  });

  // ================================================================
  // CLAVES PRESUPUESTALES - FORM / LIST / DP / CAT
  // ================================================================

  public frmClavePresupuestalValue = ko.observable<string | null>(null);
  public frmClavePresupuestalPartidaEspecifica = ko.observable<string>("");
  public frmClavePresupuestalMontoAsignado = ko.observable<number>(0);

  public listClavesPresupuestales = ko.observableArray<ClavePresupuestalItem>([]);

  public dpClavesPresupuestales = new ArrayDataProvider(this.listClavesPresupuestales, {
    keyAttributes: "idLocal"
  });

  public catClavesPresupuestales = ko.observableArray<CatalogoOption>([]);

  public dpCatClavesPresupuestales = new ArrayDataProvider(this.catClavesPresupuestales, {
    keyAttributes: "value"
  });

  private seqClavePresupuestal = 1;

  public calcHayClavesPresupuestales = ko.pureComputed(() => {
    return this.listClavesPresupuestales().length > 0;
  });

  // ================================================================
  // BENEFICIARIOS - FORM
  // ================================================================

  public frmBeneficiariosTexto = ko.observable<string>("");

  // ================================================================
  // BIENES / ADQUISICION - FORM / LIST / DP / CAT
  // ================================================================

  public frmBienLote = ko.observable<number>(0);
  public frmBienPartida = ko.observable<number>(0);
  public frmBienUnidadMedidaValue = ko.observable<string | null>(null);
  public frmBienDescripcion = ko.observable<string>("");
  public frmBienCantidad = ko.observable<number>(0);
  public frmBienPrecioUnitario = ko.observable<number>(0);

  public calcBienSubtotal = ko.pureComputed(() => {
    return Number(this.frmBienCantidad() || 0) * Number(this.frmBienPrecioUnitario() || 0);
  });

  public listBienes = ko.observableArray<BienContratoItem>([]);
  public dpBienes = new ArrayDataProvider(this.listBienes, {
    keyAttributes: "idLocal"
  });

  public catUnidadesMedida = ko.observableArray<CatalogoOption>([]);
  public dpCatUnidadesMedida = new ArrayDataProvider(this.catUnidadesMedida, {
    keyAttributes: "value"
  });

  private seqBien = 1;

  public uiDescripcionBienSeleccionada = ko.observable<string>("N/A");

  public calcHayBienes = ko.pureComputed(() => {
    return this.listBienes().length > 0;
  });

  // ================================================================
  // CONSTRUCTOR / LIFECYCLE
  // ================================================================

  constructor(params: any) {
    var contratoIdParam = params?.routerState?.params?.id;
    if (contratoIdParam) {
      this.uiModo("EDICION");
      this.contratoId(Number(contratoIdParam));
      console.log("Modo edición, contratoId >>", this.contratoId());
    }
  }

  connected(): void {
    AccUtils.announce("Edicion Contrato page loaded.");
    document.title = this.calcTituloPantalla();

    void this.loadInicial();
  }

  disconnected(): void {
    // Implementar si se requieren limpiezas o cancelaciones.
  }

  transitionCompleted(): void {
    // Implementar si se requiere logica posterior a transicion.
  }

  // ================================================================
  // LOAD - API / BACKEND
  // ================================================================

  private async loadInicial(): Promise<void> {
    this.uiCargandoCatalogos(true);
    this.uiError("");

    try {
      await Promise.all([
        this.loadClavesPresupuestales(),
        this.loadUnidadesMedida()
      ]);

      if (this.uiModo() === "EDICION" && this.contratoId()) {
        await this.loadContrato(this.contratoId() as number);
      }
    } catch (error) {
      console.error("Error en carga inicial:", error);
      this.uiError("No se pudo cargar la información inicial.");
    } finally {
      this.uiCargandoCatalogos(false);
    }
  }

  private async loadContrato(idContrato: number): Promise<void> {
    const apiContrato = 'http://localhost:8080/api/contratos/' + idContrato;
    console.log("API contrato >>", apiContrato);

    const response = await fetch(apiContrato);

    if (!response.ok) {
      throw new Error("Error al obtener el contrato");
    }

    const contratoPayload : ContratoPayload = await response.json();
    console.log("Contrato cargado >>", contratoPayload);

    this.mapPayloadToContratoUI(contratoPayload);

  }

  private async loadClavesPresupuestales(): Promise<void> {
    const response = await fetch("http://localhost:8080/api/claves-presupuestales");

    if (!response.ok) {
      throw new Error("Error al obtener claves presupuestales");
    }

    const data: CatalogoOption[] = await response.json();
    this.catClavesPresupuestales(data);
  }

  private async loadUnidadesMedida(): Promise<void> {
    const response = await fetch("http://localhost:8080/api/unidadesMedida");

    if (!response.ok) {
      throw new Error("Error al obtener unidades de medida");
    }

    const data: CatalogoOption[] = await response.json();
    this.catUnidadesMedida(data);
  }



  // ================================================================
  // EVENT HANDLERS - COMBOBOX / UI
  // ================================================================

  public onClavePresupuestalChanged = (event: CustomEvent): void => {
    if (event.detail.updatedFrom === "external") {
      return;
    }

    const selectedValue = event.detail.value as string;
    const selected = this.catClavesPresupuestales().find((item) => item.value === selectedValue);

    const partidaEspecifica = selected?.metadata?.partida_especifica as string | undefined;
    this.frmClavePresupuestalPartidaEspecifica(partidaEspecifica || "");
  };

  public onBienUnidadMedidaChanged = (event: CustomEvent): void => {
    if (event.detail.updatedFrom === "external") {
      return;
    }

    // Se conserva por si luego se requiere metadata de la unidad seleccionada.
    const selectedValue = event.detail.value as string;
    const selected = this.catUnidadesMedida().find((item) => item.value === selectedValue);

    if (!selected) {
      this.frmBienUnidadMedidaValue("");
    }
  };

  // ================================================================
  // COMMANDS - NAVEGACION
  // ================================================================

  public cmdGoToInicio = (): void => {
    this.router?.go({ path: "dashboard" });
  };

  // ================================================================
  // COMMANDS - CLAVES PRESUPUESTALES
  // ================================================================

  public cmdAgregarClavePresupuestal = (): void => {

    const selectedClavePresupuestal = this.catClavesPresupuestales()
      .find(item => item.value === this.frmClavePresupuestalValue());

    // if (!this.frmClavePresupuestalValue() || !this.frmClavePresupuestalPartidaEspecifica() || this.frmClavePresupuestalMontoAsignado() < 1) {
    //   alert("Faltan datos de la clave presupuestal.");
    //   return;
    // }

    if (!selectedClavePresupuestal || !this.frmClavePresupuestalPartidaEspecifica || this.frmClavePresupuestalMontoAsignado() < 1) {
      alert("Faltan datos de la clave presupuestal");
      return;
    }

    const item: ClavePresupuestalItem = {
      idLocal: this.seqClavePresupuestal++,
      clavePresupuestal: selectedClavePresupuestal.value,
      //descripcion: selected.label,
      partidaEspecifica: this.frmClavePresupuestalPartidaEspecifica(),
      montoAsignado: Number(this.frmClavePresupuestalMontoAsignado() || 0)
    };

    this.listClavesPresupuestales.push(item);
    this.clearFrmClavePresupuestal();
  };

  public cmdEliminarClavePresupuestal = (clave: ClavePresupuestalItem): void => {
    this.listClavesPresupuestales.remove(clave);
  };

  private clearFrmClavePresupuestal(): void {
    this.frmClavePresupuestalValue(null);
    this.frmClavePresupuestalPartidaEspecifica("");
    this.frmClavePresupuestalMontoAsignado(0);
  }

  // ================================================================
  // COMMANDS - BIENES
  // ================================================================

  public cmdAgregarBien = (): void => {

    const selectedUnidadMedida = this.catUnidadesMedida()
      .find(item => item.value === this.frmBienUnidadMedidaValue());

    if (!selectedUnidadMedida ||
      !this.frmBienDescripcion() ||
      this.frmBienCantidad() < 1 ||
      this.frmBienPrecioUnitario() < 1
    ) {
      alert("Faltan datos del producto.");
      return;
    }

    const item: BienContratoItem = {
      idLocal: this.seqBien++,
      lote: Number(this.frmBienLote() || 0),
      partida: Number(this.frmBienPartida() || 0),
      codigoUnidadMedida: selectedUnidadMedida.value,
      descripcionTecnica: this.frmBienDescripcion(),
      cantidad: Number(this.frmBienCantidad() || 0),
      precioUnitario: Number(this.frmBienPrecioUnitario() || 0),
      subtotal: Number(this.calcBienSubtotal() || 0)
    };

    this.listBienes.push(item);
    this.clearFrmBien();
  };

  public cmdEliminarBien = (bien: BienContratoItem): void => {
    this.listBienes.remove(bien);
  };

  public cmdVerDescripcionBien = (bien: BienContratoItem): void => {
    this.uiDescripcionBienSeleccionada(bien.descripcionTecnica);
    this.cmdOpenBienDescripcionDialog();
  };

  public cmdOpenBienDescripcionDialog = (): void => {
    this.uiBienDescripcionDialogOpened(true);
  };

  public cmdCloseBienDescripcionDialog = (): void => {
    this.uiBienDescripcionDialogOpened(false);
  };

  private clearFrmBien(): void {
    this.frmBienLote(0);
    this.frmBienPartida(0);
    this.frmBienUnidadMedidaValue(null);
    this.frmBienDescripcion("");
    this.frmBienCantidad(0);
    this.frmBienPrecioUnitario(0);
  }

  // ================================================================
  // COMMANDS - CONTRATO
  // ================================================================


  public cmdEnviarAlmacen = (): void => {
    const payload = this.mapContratoToPayload();
    console.log("Enviar a almacén >>", payload);
  };
    

  public cmdGuardarBorrador = async (): Promise<void> => {
    try {
      const payload: ContratoPayload = this.mapContratoToPayload();
      console.log("Guardar borrador >>", payload);
      const response = await fetch(
        "http://localhost:8080/api/contratos",
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json"
          },
          body: JSON.stringify(payload)
        }
      );

      if (!response.ok) {
        const errorBody = await response.text();
        console.error("Error saving contract:", errorBody);
        throw new Error(
          `Error ${response.status}: ${response.statusText}`
        );
      }

      const savedContrato = await response.json();
      console.log("Contract saved successfully:", savedContrato);
    } catch (error) {
      console.error(
        "Unexpected error while saving contract:",
        error
      );
    }
  }

  // ================================================================
  // MAPPERS
  // ================================================================

  private mapContratoToPayload(): ContratoPayload {

    return {
      idContrato: this.contratoId(),
      numeroContrato: this.frmContratoNumero(),
      adquisicion: this.frmContratoAdquisicion(),
      fechaOrigen: null,
      fechaTentativaLlegada: null,
      folioOrigen: this.frmContratoCotizacion() || null,
      idEstatusContrato: null,
      // fechaOrigen: this.frmContratoFechaOrigen() || null,
      // fechaTentativaLlegada: this.frmContratoFechaTentativaLlegada() || null,
      // folioOrigen: this.frmContratoFolioOrigen() || null,
      // idEstatusContrato: this.frmContratoEstatusId() || null,

      comprador: {
        id: null,
        dependencia: this.frmCompradorTitularDependencia(),
        nombre: this.frmCompradorTitularNombre(),
        cargo: this.frmCompradorTitularCaracter()
      },
      administradorContrato: {
        id: null,
        dependencia: this.frmCompradorAdministradorDependencia(),
        nombre: this.frmCompradorAdministradorNombre(),
        cargo: this.frmCompradorAdministradorCaracter()
      },

      proveedor: {
        razonSocial: this.frmProveedorEmpresa(),
        representanteEmpresa: this.frmProveedorRepresentante(),
        direccion: this.frmProveedorDomicilioFiscal(),
        caracterRepresentante: this.frmProveedorCaracter()
      },
      detallesPago: {
        montoSinImpuestos: Number(this.frmPagoMontoSinImpuestos() || 0),
        impuestos: Number(this.frmPagoImpuestos() || 0),
        montoTotal: Number(this.calcPagoMontoTotal() || 0),
        montoAnticipo: null,
        montoFiniquito: null,
        porcentajeAnticipo: null,
        porcentajeFiniquito: null,
        tieneAnticipo: null,
        tieneFiniquito: null,
        numeroExhibiciones: 0
      },
      beneficiarios: this.frmBeneficiariosTexto(),
      clavesPresupuestales: this.listClavesPresupuestales(),
      estatusContrato: null,
      productos: this.listBienes()
      //estatusContrato: this.frmContratoEstatusDescripcion()
    };
  }


  private mapPayloadToContratoUI(payload: ContratoPayload): void {
    debugger;
    // Cargar datos del Contrato
    this.frmContratoNumero(payload.numeroContrato);
    this.frmContratoAdquisicion(payload.adquisicion);
    this.frmContratoCotizacion(payload.folioOrigen || "");

    // Cargar datos del Comprador - Titular
    this.frmCompradorTitularDependencia(payload.comprador.dependencia);
    this.frmCompradorTitularNombre(payload.comprador.nombre);
    this.frmCompradorTitularCaracter(payload.comprador.cargo);

    // Cargar datos del Administrador del Contrato
    this.frmCompradorAdministradorDependencia(payload.administradorContrato.dependencia);
    this.frmCompradorAdministradorNombre(payload.administradorContrato.nombre);
    this.frmCompradorAdministradorCaracter(payload.administradorContrato.cargo);

    // Cargar Beneficiarios
    this.frmProveedorEmpresa(payload.proveedor.razonSocial);
    this.frmProveedorRepresentante(payload.proveedor.representanteEmpresa);
    this.frmProveedorDomicilioFiscal(payload.proveedor.direccion);
    this.frmProveedorCaracter(payload.proveedor.caracterRepresentante);

    // Cargar Beneficiarios
    this.frmBeneficiariosTexto(payload.beneficiarios);
    
    // Cargar claves presupuestales
    const claves = payload.clavesPresupuestales.map((item, index) => ({
      idLocal: index + 1,
      clavePresupuestal: item.clavePresupuestal,
      partidaEspecifica: item.partidaEspecifica,
      montoAsignado: item.montoAsignado
    }));
    this.listClavesPresupuestales(claves);
    this.seqClavePresupuestal = claves.length + 1;
  

    // Cargar bienes / productos
    const bienesCargados = payload.productos.map((item, index) => ({
      idLocal: index + 1,
      lote: item.lote,
      partida: item.partida,
      descripcionTecnica: item.descripcionTecnica,
      codigoUnidadMedida: item.codigoUnidadMedida,
      cantidad: item.cantidad,
      precioUnitario: item.precioUnitario,
      subtotal: item.subtotal
    }));
    this.listBienes(bienesCargados);
    this.seqBien = bienesCargados.length + 1;
  }


}

export = NuevoContratoViewModel;
