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
import { TabData } from 'oj-c/tab-bar';

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


const tabData: TabData<string>[] = [
    {
        label: 'Contrato', itemKey: 'contrato',
        icon: {
            type: 'class',
            class: 'oj-ux-ico-document-gear'
        }
    },
    {
        label: 'Comprador', itemKey: 'comprador',
        icon: {
            type: 'class',
            class: 'oj-ux-ico-building'
        }
    },
    {
        label: 'Proveedor', itemKey: 'proveedor',
        icon: {
            type: 'class',
            class: 'oj-ux-ico-truck'
        }
    },
    {
        label: 'Pago', itemKey: 'pago',
        icon: {
            type: 'class',
            class: 'oj-ux-ico-vendor-payment'
        }
    },
    {
        label: 'Beneficiarios', itemKey: 'beneficiarios',
        icon: {
            type: 'class',
            class: 'oj-ux-ico-contact-group'
        }
    },
    {
        label: 'Bienes', itemKey: 'bienes',
        icon: {
            type: 'class',
            class: 'oj-ux-ico-box'
        }
    }
];

type ClavePresupuestaria = {
    clave: ko.Observable<string>;
    partida: ko.Observable<string>;
    monto: ko.Observable<number>;
    puedeEliminar: ko.PureComputed<boolean>;
    eliminar: () => void;
};


// type ProductoContrato = {
//     lote: ko.Observable<number>;
//     partida: ko.Observable<number>;
//     unidadMedida: ko.Observable<string>;
//     descripcion: ko.Observable<string>;
//     cantidad: ko.Observable<number>;
//     precioUnitario: ko.Observable<number>;
//     subtotal: ko.PureComputed<number>;
//     //puedeEliminar: ko.PureComputed<boolean>;
//     //eliminar: () => void;
// };

type BienContrato = {
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
        this.agregarClave();
        //this.agregarProducto();
    }

    // TAB
    public tabDataSource = ko.observableArray(tabData);
    public selectedTab = ko.observable(tabData[0].itemKey);


    // ================================================================
    // TAB PAGO   
    // ================================================================
    public montoSinImpuestos = ko.observable(0.0);
    public impuestos = ko.observable(0.0);
    public montoTotal = ko.pureComputed(() => {
        return this.montoSinImpuestos() + this.impuestos();
    });

    public listaClaves = ko.observableArray<ClavePresupuestaria>([]);

    agregarClave = () => {
        const item = {} as ClavePresupuestaria;

        item.clave = ko.observable("");
        item.partida = ko.observable("");
        item.monto = ko.observable(0.0);

        item.puedeEliminar = ko.pureComputed(() => {
            return this.listaClaves().length > 1;
        });

        item.eliminar = () => {
            if (this.listaClaves().length <= 1) {
                return;
            }
            this.listaClaves.remove(item);
        };
        this.listaClaves.push(item);
    };


    eliminarClave = (item: ClavePresupuestaria) => {

        if (this.listaClaves.length == 1) {
            alert("Debe existir al menos una clave");
        }
        console.log("remove: " + item.clave);
        this.listaClaves.remove(item);
    };

    guardarClaves = () => {
        const payload = this.listaClaves().map(item => ({
            clave: item.clave(),
            partida: item.partida(),
            monto: item.monto()
        }));

        console.log(payload);
    };

    // ================================================================
    // TAB BENEFICIARIOS   
    // ================================================================
    public beneficiarios = ko.observable();


    // ================================================================
    // TAB BIENES   
    // ================================================================
    // public listaProductos = ko.observableArray<ProductoContrato>([]);
    public listaBienes = ko.observableArray<BienContrato>([]);

    public lote = ko.observable<number>(0);
    public partida = ko.observable<number>(0);
    public unidadMedida = ko.observable<string>("");
    public descripcion = ko.observable<string>("");
    public cantidad = ko.observable<number>(0);
    public precioUnitario = ko.observable<number>(0);
    public subtotal = ko.pureComputed(() => {
        return this.cantidad() * this.precioUnitario();
    });

    public bienesDataProvider = new ArrayDataProvider(this.listaBienes, {
        keyAttributes: "id"
    });

    private bienIdSequence = 1;
    public descripcionSeleccionada = ko.observable<string>("");

    agregarBien = () => {

        if (this.lote() == 0 && this.partida() == 0 && this.cantidad() == 0 && this.descripcion() == "") {
            alert("Ingresa datos ");
            return;
        }
        const nuevoBien: BienContrato = {
            id: this.bienIdSequence++,
            lote: Number(this.lote()),
            partida: Number(this.partida()),
            unidadMedida: this.unidadMedida(),
            descripcion: this.descripcion(),
            cantidad: Number(this.cantidad()),
            precioUnitario: Number(this.precioUnitario()),
            subtotal: Number(this.subtotal())
        };

        this.listaBienes.push(nuevoBien);
        this.limpiarFormulario();
    }

    public eliminarBien = (bien: BienContrato): void => {
        this.listaBienes.remove(bien);
    };

    public verDescripcion = (bien: BienContrato): void => {
        this.descripcionSeleccionada(bien.descripcion);
        this.openDescripcionDialog();
    };

    public isDescripcionDialogOpened = ko.observable(false);

    public openDescripcionDialog = (): void => {
        this.isDescripcionDialogOpened(true)
    }

    public closeDescripcionDialog = (): void => {
        this.isDescripcionDialogOpened(false)
    }

    private limpiarFormulario = (): void => {
        this.lote(0);
        this.partida(0);
        this.unidadMedida("");
        this.descripcion("");
        this.cantidad(0);
        this.precioUnitario(0);
    };

    // agregarProducto = () => {
    //     const item = {} as ProductoContrato;

    //     item.lote = ko.observable(0);
    //     item.partida = ko.observable(0);
    //     item.unidadMedida = ko.observable("");
    //     item.descripcion = ko.observable("");
    //     item.cantidad = ko.observable(1);
    //     item.precioUnitario = ko.observable(0.0);
    //     item.subtotal = ko.pureComputed(() => {
    //         return item.cantidad() * item.precioUnitario();
    //     });

    //     this.listaProductos.push(item);
    // }

    /**
     * Optional ViewModel method invoked after the View is inserted into the
     * document DOM.  The application can put logic that requires the DOM being
     * attached here.
     * This method might be called multiple times - after the View is created
     * and inserted into the DOM and after the View is reconnected
     * after being disconnected.
     */
    connected(): void {
        AccUtils.announce("Nuevo Contrato page loaded.");
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