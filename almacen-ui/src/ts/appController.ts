/**
 * @license
 * Copyright (c) 2014, 2026, Oracle and/or its affiliates.
 * Licensed under The Universal Permissive License (UPL), Version 1.0
 * as shown at https://oss.oracle.com/licenses/upl/
 * @ignore
 */
import * as ko from "knockout";
import * as ModuleUtils from "ojs/ojmodule-element-utils";
import * as ResponsiveUtils from "ojs/ojresponsiveutils";
import * as  ResponsiveKnockoutUtils from "ojs/ojresponsiveknockoututils";
import CoreRouter = require("ojs/ojcorerouter");
import ModuleRouterAdapter = require("ojs/ojmodulerouter-adapter");
import KnockoutRouterAdapter = require("ojs/ojknockoutrouteradapter");
import UrlParamAdapter = require("ojs/ojurlparamadapter");
import ArrayDataProvider = require("ojs/ojarraydataprovider");
import "ojs/ojknockout";
import "ojs/ojmodule-element";
import { ojNavigationList } from "ojs/ojnavigationlist";
import Context = require("ojs/ojcontext");
import { isAuthenticated, redirectToLogin, getRole, getNombre, clearSession } from "./utils/auth";

interface CoreRouterDetail {
  label: string;
  iconClass: string;
};

class RootViewModel {
  manner!: ko.Observable<string>;
  message!: ko.Observable<string|undefined>;
  smScreen: ko.Observable<boolean>|undefined;
  moduleAdapter!: ModuleRouterAdapter<CoreRouterDetail>;
  navDataProvider!: ojNavigationList<string, CoreRouter.CoreRouterState<CoreRouterDetail>>["data"];
  appName!: ko.Observable<string>;
  userLogin!: ko.Observable<string>;
  footerLinks!: Array<object>;
  selection!: KnockoutRouterAdapter<CoreRouterDetail>;

  constructor() {
    // Redirigir a login si no hay sesión válida
    if (!isAuthenticated()) {
      redirectToLogin();
      return;
    }

    // handle announcements sent when pages change, for Accessibility.
    this.manner = ko.observable("polite");
    this.message = ko.observable();

    let globalBodyElement: HTMLElement = document.getElementById("globalBody") as HTMLElement;
    globalBodyElement.addEventListener("announce", this.announcementHandler, false);

    // media queries for responsive layouts
    let smQuery: string | null = ResponsiveUtils.getFrameworkQuery("sm-only");
    if (smQuery){
      this.smScreen = ResponsiveKnockoutUtils.createMediaQueryObservable(smQuery);
    }

    // Todas las rutas registradas en el router (incluyendo sub-rutas sin nav)
    const allRoutes = [
      { path: "", redirect: "dashboard" },
      { path: "dashboard",          detail: { label: "Inicio",    iconClass: "oj-ux-ico-home" } },
      { path: "contrato",           detail: { label: "Contratos", iconClass: "oj-ux-ico-data-document" } },
      { path: "almacen",            detail: { label: "Almacén",   iconClass: "oj-ux-ico-warehouse" } },
      // Sub-rutas: registradas en el router pero no aparecen en el nav
      { path: "contrato-detalle",   detail: { label: "Detalle",       iconClass: "oj-ux-ico-data-document" } },
      { path: "procesamiento",      detail: { label: "Procesamiento", iconClass: "oj-ux-ico-process" } },
      { path: "recepcion",          detail: { label: "Recepción",     iconClass: "oj-ux-ico-package" } },
      { path: "entrega",            detail: { label: "Entrega",       iconClass: "oj-ux-ico-export" } },
    ];

    // Rutas visibles en el tab bar
    const navItems = allRoutes.filter(r =>
      r.path !== "" &&
      r.path !== "almacen" &&
      r.path !== "contrato-detalle" && r.path !== "procesamiento" &&
      r.path !== "recepcion" && r.path !== "entrega"
    );

    // router setup
    const router = new CoreRouter(allRoutes, {
      urlAdapter: new UrlParamAdapter()
    });
    router.sync();

    // module config
    this.moduleAdapter = new ModuleRouterAdapter(router);

    this.selection = new KnockoutRouterAdapter(router);

    this.navDataProvider = new ArrayDataProvider(navItems, {keyAttributes: "path"});

    // header
    this.appName = ko.observable("Gestion de Almacen");

    const nombre = getNombre();
    const rol = getRole();
    const rolLabel = rol === "ADMINISTRADOR" ? "Administrador" : "Almacén";
    this.userLogin = ko.observable(nombre ? `${nombre} (${rolLabel})` : rolLabel);

    this.footerLinks = [];

    // release the application bootstrap busy state
    Context.getPageContext().getBusyContext().applicationBootstrapComplete();
  }

  announcementHandler = (event: any): void => {
      this.message(event.detail.message);
      this.manner(event.detail.manner);
  }

  cmdMenuAction = (event: any): void => {
    console.log("Menu action:", event?.detail?.selectedValue);
    if (event?.detail?.selectedValue === "out") {
      clearSession();
      redirectToLogin();
    }
  }
}

export default new RootViewModel();
