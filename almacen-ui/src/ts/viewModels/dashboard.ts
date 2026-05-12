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

import "oj-c/list-view";
import "oj-c/button";
import 'oj-c/dialog';
import "oj-c/avatar";


type ContratoListaItem = {
  idContrato: number;
  numeroContrato: string;
  adquisicion: string;
  proveedor: string;
  estatusContrato: string;
};

class DashboardViewModel {
  // ================================================================
  // ROUTER / UI STATE
  // ================================================================
  private router;
  //public isDialogOpened = ko.observable(false);

  // ================================================================
  // CONTRATO - TABLE
  // ================================================================

  public listContratos = ko.observableArray<ContratoListaItem>([]);
  public dpContratos = new ArrayDataProvider(this.listContratos, {
      keyAttributes: "idContrato",
    });

  public getStatusBadgeClass(status: string): string {

    let badgeClass = "oj-badge oj-badge-subtle ";

    switch (status) {

      case "Contrato en captura progresiva":
        badgeClass += "oj-badge-success";
        break;

      case "Pendiente de firma":
        badgeClass += "oj-badge-warning";
        break;

      case "Contrato finalizado":
        badgeClass += "oj-badge-danger";
        break;

      case "Todos los productos fueron entregados":
        badgeClass += "oj-badge-info";
        break;

      default:
        badgeClass += "oj-badge-neutral";
        break;
    }

    return badgeClass;
  }


  // ================================================================
  // CONSTRUCTOR / LIFECYCLE
  // ================================================================
  constructor(params: any) {
    this.router = params.router;
    this.cargarContratos();
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

  // ================================================================
  // LOAD - API / BACKEND
  // ================================================================
  private async cargarContratos(): Promise<void> {
    const response = await fetch("http://localhost:8080/api/contratos");
    
    if (!response.ok) {
      throw new Error("Error al obtener el listado de contratos");
    }
    
    const data = await response.json();
    const contratosArray = Array.isArray(data) ? data : [data];

    const items: ContratoListaItem[] = contratosArray.map((contrato: any) => ({
      idContrato: contrato.idContrato,
      numeroContrato: contrato.numeroContrato,
      adquisicion: contrato.adquisicion,
      proveedor: contrato.proveedor?.razonSocial ?? "Pendiente de asignar",
      estatusContrato: contrato.estatusContrato,
    }));

    this.listContratos(items);
  }

  public verBeneficiarios = (event: Event, context: any): void => {
    const contrato = context.item.data as ContratoListaItem;

    console.log("Ver beneficiarios del contrato:", contrato.idContrato);

    // Aquí podrías abrir un dialog, navegar a otra vista,
    // o consumir /api/contratos/{id}/beneficiarios
  };

  // ================================================================
  // COMMANDS - NAVEGACION
  // ================================================================
  public cmdGoToCreateContrato = () => {
    this.router?.go({ path: 'nuevo-contrato' });
  }

  public cmdEditarContrato = (event: Event, context: any): void => {
     const idContrato = context.item.data.idContrato;
     console.log("Editar contrato >>", idContrato);
     // Pendiente: implementar navegación a pantalla de edición con contratoId.
      this.router?.go({ path: 'nuevo-contrato/{idContrato}' });
      
     // Implementar pantalla de edición reutilizando componentes de captura.
  }
}

export = DashboardViewModel;