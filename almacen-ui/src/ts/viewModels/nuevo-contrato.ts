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
import 'oj-c/text-area';
import "oj-c/button";
import 'oj-c/dialog';
import 'oj-c/tab-bar';
import 'oj-c/input-text'
import "oj-c/input-number";

type ClavePresupuestal = {
    clave: ko.Observable<string>;
    partida: ko.Observable<string>;
    monto: ko.Observable<number>;
    puedeEliminar: ko.PureComputed<boolean>;
    eliminar: () => void;
};

type ClavePresupuestalItemView = {
    id: number;
    clave: string;
    partidaEspecifica: string;
    montoClave: number;
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

    constructor() {
        //this.agregarClavePresupuestal();
    }

    // ================================================================
    // CONTRATO
    // ================================================================


    // ================================================================
    // COMPRADOR
    // ================================================================

    // ================================================================
    // PROVEEDOR
    // ================================================================

    // ================================================================
    // PAGO / CLAVES PRESUPUESTALES
    // ================================================================
    public listaClaves = ko.observableArray<ClavePresupuestalItemView>([]);

    //public listaClaves = ko.observableArray<ClavePresupuestal>([]);

    public montoSinImpuestos = ko.observable<number>(0);
    public impuestos = ko.observable<number>(0);
    public montoTotal = ko.pureComputed(() => {
        return Number(this.montoSinImpuestos() || 0) + Number(this.impuestos() || 0);
    });

    public clave = ko.observable<string>("127001-106");
    public partidaEspecifica = ko.observable<string>("");
    public montoClave = ko.observable<number>(0);

    public clavesDataProvider = new ArrayDataProvider(this.listaClaves, {
        keyAttributes: "id"
    })

    private clavePresupuestalIdSequence = 1;
    public hayClaves = ko.pureComputed( () => {
        return this.listaClaves().length > 0;
    });

    public agregarClavePresupuestal = (): void => {

        if(this.montoClave() < 1) {
            alert("agregar clave p");
            return;
        }
        const item : ClavePresupuestalItemView = {
            id : this.clavePresupuestalIdSequence++,
            clave: this.clave(),
            partidaEspecifica: this.partidaEspecifica(),
            montoClave: Number(this.montoClave() || 0)
        }

        this.listaClaves.push(item);
        this.limpiarFormularioClavesPresupuestales();
    };

    public eliminarClavePresupuestal = (clave: ClavePresupuestalItemView): void => {
        this.listaClaves.remove(clave);
    } 

    private limpiarFormularioClavesPresupuestales(): void {
        this.clave("");
        this.partidaEspecifica(""),
        this.montoClave(0);
    }

    // ================================================================
    // TAB BENEFICIARIOS   
    // ================================================================
    public beneficiarios = ko.observable();


    // ================================================================
    // BIENES / PRODUCTOS  
    // ================================================================

    public listaBienes = ko.observableArray<BienContratoItemView>([]);

    public isBienDescripcionDialogOpened = ko.observable(false);

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