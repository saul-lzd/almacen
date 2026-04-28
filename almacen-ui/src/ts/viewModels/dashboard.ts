/**
 * @license
 * Copyright (c) 2014, 2026, Oracle and/or its affiliates.
 * Licensed under The Universal Permissive License (UPL), Version 1.0
 * as shown at https://oss.oracle.com/licenses/upl/
 * @ignore
 */
import * as AccUtils from "../accUtils";
import * as ko from "knockout";
import "ojs/ojtable";
import 'oj-c/form-layout';
import "oj-c/button";
import 'oj-c/dialog';
import 'oj-c/tab-bar';
import 'oj-c/input-text'
import "oj-c/input-number";


import { TabData } from 'oj-c/tab-bar';

const data: TabData<string>[] = [
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

class DashboardViewModel {

  public isDialogOpened = ko.observable(false);
  public isPanelContratoVisible = ko.observable(false);
  public isPanelCompradorVisible = ko.observable(false);

  edgeOptions = [
    { label: 'Top', value: 'top' },
    { label: 'Bottom', value: 'bottom' }];

  layoutOptions = [{ label: 'Stretch', value: 'stretch' },
  { label: 'Condense', value: 'condense' }];

  displayOptions = [{ label: 'Standard', value: 'standard' },
  { label: 'Icons', value: 'icons' },
  { label: 'Stacked', value: 'stacked' }
  ];

  data = ko.observableArray(data);
  selectedItem = ko.observable(data[0].itemKey);
  edge = ko.observable('top');
  layout = ko.observable('stretch');
  display = ko.observable('standard');

  montoSinImpuestos = ko.observable(0.0);
  impuestos = ko.observable(0.0);
  montoTotal = ko.pureComputed(() => {
    return this.montoSinImpuestos() + this.impuestos();
  });


  /*
  public onFormSectionChanged = (event: ojcTabBar.currentItemChanged<string>) => {
      const newTab = event.detail.value;
      alert("section changed " + selectedItem);
  }
  */


  public openDialog = (): void => {
    this.isDialogOpened(true)
  }

  public closeDialog = (): void => {
    this.isDialogOpened(false)
  }

  connected(): void {
    //AccUtils.announce("Vista de viajes cargada.");
    //document.title = "Viajes";
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

export = DashboardViewModel;

/*
interface Viaje {
  autobus_id: number;
  origen: string;
  destino: string;
  hora_salida: string;
  precio: number;
  fecha: string;
  tipo_autobus: string;
}

interface Boleto {
  id: number;
  pasajero_id: number;
  viaje_id: number;
  asiento: string;
  estatus: string;
  precio_pagado: number;
  metodo_pago: string;
  fecha_emision: string;
  vencimiento: string ;
}

 viajes = ko.observableArray([]);
  allBoletos: Boleto[] = [];
  boletosDelViaje = ko.observableArray<Boleto | null>([]);
  viajeSeleccionado = ko.observable<Viaje | null>(null);
  selected = ko.observable();

  constructor() {
    this.loadViajes();
    this.loadBoletos();
  }

 async loadViajes() {
    const data = await this.loadJSON("assets/viajes.json");
    this.viajes(data.slice(0, 6));
  }

  async loadBoletos() {
    const data = await this.loadJSON("assets/boleto.json");
    this.allBoletos = data;
  }

  mostrarBoletosViaje(viajeId : number) {
    console.log("loading tickets for trip: " + viajeId);
    const filteredList = this.allBoletos.filter(b => b.viaje_id === viajeId);
    this.boletosDelViaje(filteredList);
  }

  async loadJSON(path: string) {
    const response = await fetch(path);
    const data = await response.json();
    return data;
  }

  seleccionarViaje = (event: Event, element: any) => {

    if (this.selected()) {
      this.selected().classList.remove("item-viaje-selected");
    }

    const idViaje = element.data.precio;
    this.viajeSeleccionado(element.data);
    this.mostrarBoletosViaje(element.data.viaje_id);

    // set css to selected panel
    const panel = event.currentTarget as HTMLElement;
    this.selected(panel);
    panel.classList.add("item-viaje-selected");
  }
*/