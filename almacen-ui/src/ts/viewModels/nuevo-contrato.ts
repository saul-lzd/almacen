/**
 * @license
 * Copyright (c) 2014, 2026, Oracle and/or its affiliates.
 * Licensed under The Universal Permissive License (UPL), Version 1.0
 * as shown at https://oss.oracle.com/licenses/upl/
 * @ignore
 */
import * as AccUtils from "../accUtils";
import * as ko from "knockout";
import ArrayDataProvider = require("ojs/ojarraydataprovider");

import "oj-c/collapsible";
import "ojs/ojtable";
import 'oj-c/list-view';
import 'oj-c/list-item-layout';
import 'oj-c/form-layout';
import 'oj-c/select-single';
import 'oj-c/text-area';
import "oj-c/button";
import 'oj-c/dialog';
import 'oj-c/tab-bar';
import 'oj-c/input-text'
import "oj-c/input-number";

type ClavePresupuestalItemView = {
    id: number;
    clavePresupuestal: string;
    partidaEspecifica: string;
    montoAsignado: number;
};

type BienContratoItemView = {
    id: number;
    lote: number;
    partida: number;
    unidadMedida: string;
    descripcion: string;
    cantidad: number;
    precioUnitario: number;
    subtotal: number;
};

class NuevoContratoViewModel {

    private router;
    public modo = ko.observable<"NUEVO" | "EDICION">("NUEVO");
    public tituloPantalla = ko.pureComputed(() => {
        return this.modo() === "EDICION" ? "Editar Contrato" : "Nuevo Contrato";
    });

    constructor(params: any) {
        this.router = params.router;
        this.cargarClaves();
        this.cargarUnidadesMedida();
    }

    public goToInicio = () => {
        this.router.go({ path: 'dashboard' });
    }



    // ================================================================
    // CONTRATO
    // ================================================================

    public contratoId = ko.observable<number | null>(null);
    // info contrato
    public formNumeroContrato = ko.observable("");
    public formAdquisicion = ko.observable("");
    public formCotizacion = ko.observable("");

    // ================================================================
    // COMPRADOR
    // ================================================================

    // titular
    public formTitularDependencia = ko.observable("");
    public formTitularNombre = ko.observable("");
    public formTitularCaracter = ko.observable("");

    // administrador
    public formAdministradorDependencia = ko.observable("");
    public formAdministradorNombre = ko.observable("");
    public formAdministradorCaracter = ko.observable("");


    // ================================================================
    // PROVEEDOR
    // ================================================================

    // proveedor
    public formProveedorEmpresa = ko.observable("");
    public formProveedorDomicilioFiscal = ko.observable("");
    public formProveedorRepresentante = ko.observable("");
    public formProveedorCaracter = ko.observable("");

    // ================================================================
    // PAGO 
    // ================================================================

    public formMontoSinImpuestos = ko.observable<number>(0);
    public formImpuestos = ko.observable<number>(0);
    public formMontoTotal = ko.pureComputed(() => {
        return Number(this.formMontoSinImpuestos() || 0) + Number(this.formImpuestos() || 0);
    });

    // ================================================================
    // CLAVES PRESUPUESTALES
    // ================================================================

    public formClavePresupuestal = ko.observable<string>("-seleccionar-");
    public formPartidaEspecifica = ko.observable<string>("");
    public formMontoAsignado = ko.observable<number>(0);

    public listaClavesPresupuestales = ko.observableArray<ClavePresupuestalItemView>([]);
    public clavesDataProvider = new ArrayDataProvider(this.listaClavesPresupuestales, { keyAttributes: "id" })

    public optionsClavesPresupuestales = ko.observableArray();
    private clavePresupuestalIdSequence = 1;
    
    public hayClaves = ko.pureComputed(() => {
        return this.listaClavesPresupuestales().length > 0;
    });

    public agregarClavePresupuestal = (): void => {
        if (!this.formClavePresupuestal() 
                || !this.formPartidaEspecifica() 
                    || this.formMontoAsignado() < 1) {
            alert("Faltan Datos");
            return;
        }
        const item: ClavePresupuestalItemView = {
            id: this.clavePresupuestalIdSequence++,
            clavePresupuestal: this.formClavePresupuestal(),
            partidaEspecifica: this.formPartidaEspecifica(),
            montoAsignado: Number(this.formMontoAsignado() || 0)
        }
        this.listaClavesPresupuestales.push(item);
        this.limpiarFormularioClavesPresupuestales();
    };

    public eliminarClavePresupuestal = (clave: ClavePresupuestalItemView): void => {
        this.listaClavesPresupuestales.remove(clave);
    }

    private limpiarFormularioClavesPresupuestales(): void {
        this.formClavePresupuestal("");
        this.formPartidaEspecifica(""),
        this.formMontoAsignado(0);
    }

    public async cargarClaves(): Promise<void> {
        try {
            const response = await fetch("http://localhost:8080/api/claves");
            if (!response.ok) {
                throw new Error("Error al obtener claves");
            }
            const data = await response.json();
            data.unshift({clavePresupuestal: "", partidaEspecifica: ""});
            console.log("Claves:", data);
            this.optionsClavesPresupuestales(data);
        } catch (error) {
            console.error("Error:", error);
        }
    }

    public valueChangedHandler = (event: CustomEvent) => {

        if (event.detail.updatedFrom === "external") {
            return;
        }
      //console.log(event.detail);
      const claveEncontrada = this.optionsClavesPresupuestales().find(p => p.clavePresupuestal === event.detail.value);
      console.log(claveEncontrada);
      this.formPartidaEspecifica(claveEncontrada.partidaEspecifica)
    };


    readonly clavesPresupuestalesDP = new ArrayDataProvider(this.optionsClavesPresupuestales, {
      keyAttributes: 'clavePresupuestal',
    });

    // ================================================================
    // TAB BENEFICIARIOS   
    // ================================================================
    public beneficiarios = ko.observable();


    // ================================================================
    // BIENES / PRODUCTOS  
    // ================================================================

    public listaBienes = ko.observableArray<BienContratoItemView>([]);

    public isBienDescripcionDialogOpened = ko.observable(false);
    public optionsUnidadesMedida = ko.observableArray();

    public bienLote = ko.observable<number>(0);
    public bienPartida = ko.observable<number>(0);
    public bienUnidadMedida = ko.observable<string>("");
    public bienDescripcion = ko.observable<string>("");
    public bienCantidad = ko.observable<number>(0);
    public bienPrecioUnitario = ko.observable<number>(0);
    public bienSubtotal = ko.pureComputed(() => {
        return Number(this.bienCantidad() || 0) * Number(this.bienPrecioUnitario() || 0);
    });

    public bienesDataProvider = new ArrayDataProvider(this.listaBienes, {
        keyAttributes: "id"
    });

    private bienIdSequence = 1;
    public descripcionBienSeleccionada = ko.observable<string>("");

    public hayBienes = ko.pureComputed(() => {
        return this.listaBienes().length > 0;
    });

    public agregarBien = (): void => {
        const bien: BienContratoItemView = {
            id: this.bienIdSequence++,
            lote: Number(this.bienLote() || 0),
            partida: Number(this.bienPartida() || 0),
            unidadMedida: this.bienUnidadMedida(),
            descripcion: this.bienDescripcion(),
            cantidad: Number(this.bienCantidad() || 0),
            precioUnitario: Number(this.bienPrecioUnitario() || 0),
            subtotal: Number(this.bienSubtotal() || 0)
        };

        this.listaBienes.push(bien);
        this.limpiarFormularioBienes();
    };

    public eliminarBien = (bien: BienContratoItemView): void => {
        this.listaBienes.remove(bien);
    };

    public verDescripcionBien = (bien: BienContratoItemView): void => {
        this.descripcionBienSeleccionada(bien.descripcion);
        this.openBienDescripcionDialog()
    };

    public openBienDescripcionDialog = (): void => {
        this.isBienDescripcionDialogOpened(true)
    }

    public closeBienDescripcionDialog = (): void => {
        this.isBienDescripcionDialogOpened(false)
    }

    private limpiarFormularioBienes(): void {
        this.bienLote(0);
        this.bienPartida(0);
        this.bienUnidadMedida("");
        this.bienDescripcion("");
        this.bienCantidad(0);
        this.bienPrecioUnitario(0);
    }

    public async cargarUnidadesMedida(): Promise<void> {
        try {
            const response = await fetch("http://localhost:8080/api/unidadesMedida");
            if (!response.ok) {
                throw new Error("Error al obtener claves");
            }
            const data = await response.json();
            data.unshift({clave: "", descripcion: ""});
            console.log("Unidades Medida:", data);
            this.optionsUnidadesMedida(data);
        } catch (error) {
            console.error("Error:", error);
        }
    }

    public valueChangedHandlerUM = (event: CustomEvent) => {

        if (event.detail.updatedFrom === "external") {
            return;
        }
      const unidadEncontrada = this.optionsUnidadesMedida().find(p => p.clave === event.detail.value);
      console.log(unidadEncontrada);
      this.formPartidaEspecifica(unidadEncontrada.clave)
    };


    readonly unidadesMedidaDP = new ArrayDataProvider(this.optionsUnidadesMedida, {
      keyAttributes: 'clave',
    });

    // ================================================================
    // ================================================================

    private count = 1;
    public guardarBorrador = (): void => {
        this.count++;
        const payload = this.crearPayloadContrato();

        console.log(this.count + " Guardar borrador >>", payload);

        // if (!this.contratoId()) {
        //   this.contratoId(1);
        //   this.formNumeroContrato(this.formNumeroContrato() || "CNT-BORRADOR");
        //   this.modo("EDICION");
        // }
    };

    private crearPayloadContrato() {
        return {
            idContrato: this.contratoId(),
            numeroContrato: this.formNumeroContrato(),
            descripcionAdquisicion: this.formAdquisicion(),
            numeroCotizacion: this.formCotizacion(),
            titularDependencia: {
                dependencia: this.formTitularDependencia(),
                nombre: this.formTitularNombre(),
                caracter: this.formTitularCaracter()
            },
            administradorContrato: {
                dependencia: this.formAdministradorDependencia(),
                nombre: this.formAdministradorNombre(),
                caracter: this.formAdministradorCaracter()
            },
            proveedor: {
                empresa: this.formProveedorEmpresa(),
                representante: this.formProveedorRepresentante(),
                domicilio: this.formProveedorDomicilioFiscal(),
                caracter: this.formProveedorCaracter()
            },
            pago: {
                montoSinImpuestos: this.formMontoSinImpuestos(),
                impuestos: this.formImpuestos(),
                montoTotal: this.formMontoTotal(),
                clavesPresupuestales: this.listaClavesPresupuestales().map(item => ({
                    clavePresupuestal: item.clavePresupuestal,
                    partidaEspecifica: item.partidaEspecifica,
                    montoAsignado: item.montoAsignado
                }))
            },
            beneficiariosTexto: this.beneficiarios(),
            bienes: this.listaBienes()
        };
    }



    // ================================================================
    /**
     * Optional ViewModel method invoked after the View is inserted into the
     * document DOM.  The application can put logic that requires the DOM being
     * attached here.
     * This method might be called multiple times - after the View is created
     * and inserted into the DOM and after the View is reconnected
     * after being disconnected.
     */
    connected(): void {
        AccUtils.announce("Nuevo Contrato  page loaded.");
        document.title = "Nuevo Contrato";
        // implement further logic if needed
    }

    /**
     * Optional ViewModel method invoked after the View is disconnected from the DOM.
     */
    disconnected(): void {
        // implement if needed
    }

    /**
     * Optional ViewModel method invoked after transition to the new View is complete.
     * That includes any possible animation between the old and the new View.
     */
    transitionCompleted(): void {
        // implement if needed
    }
}

export = NuevoContratoViewModel;