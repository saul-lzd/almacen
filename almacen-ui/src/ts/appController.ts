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

interface CoreRouterDetail {
  label: string;
  iconClass: string;
};

class RootViewModel {
  manner: ko.Observable<string>;
  message: ko.Observable<string|undefined>;
  smScreen: ko.Observable<boolean>|undefined;
  moduleAdapter: ModuleRouterAdapter<CoreRouterDetail>;
  navDataProvider: ojNavigationList<string, CoreRouter.CoreRouterState<CoreRouterDetail>>["data"];
  appName: ko.Observable<string>;
  userLogin: ko.Observable<string>;
  footerLinks: Array<object>;
  selection: KnockoutRouterAdapter<CoreRouterDetail>;

  constructor() {
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
      { path: "recepcion",          detail: { label: "Recepción",      iconClass: "oj-ux-ico-package" } },
      { path: "procesamiento",      detail: { label: "Procesamiento",  iconClass: "oj-ux-ico-process" } },
      // Rutas de desarrollo (temporales)
      { path: "dashboard_demo", detail: { label: "Dashboard Demo", iconClass: "oj-ux-ico-data-document" } },
      { path: "dashboard_OG",   detail: { label: "Dashboard OG",   iconClass: "oj-ux-ico-data-document" } },
      { path: "contrato_BAK",   detail: { label: "Contratos BAK",  iconClass: "oj-ux-ico-data-document" } },
      { path: "contrato_old",   detail: { label: "Contratos OLD",  iconClass: "oj-ux-ico-data-document" } },
    ];

    // Rutas visibles en el tab bar (excluye sub-rutas y rutas de desarrollo)
    const navItems = allRoutes.filter(r =>
      r.path !== "" && r.path !== "recepcion" && r.path !== "procesamiento" &&
      !r.path.endsWith("_demo") && !r.path.endsWith("_OG") &&
      !r.path.endsWith("_BAK") && !r.path.endsWith("_old")
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

    // application Name used in Branding Area
    this.appName = ko.observable("Gestion de Almacen");

    // user Info used in Global Navigation area
    this.userLogin = ko.observable("usuario@sesesp.com");

    // footer
    this.footerLinks = [
      {name: 'About Oracle', linkId: 'aboutOracle', linkTarget:'http://www.oracle.com/us/corporate/index.html#menu-about'},
      { name: "Contact Us", id: "contactUs", linkTarget: "http://www.oracle.com/us/corporate/contact/index.html" },
      { name: "Legal Notices", id: "legalNotices", linkTarget: "http://www.oracle.com/us/legal/index.html" },
      { name: "Terms Of Use", id: "termsOfUse", linkTarget: "http://www.oracle.com/us/legal/terms/index.html" },
      { name: "Your Privacy Rights", id: "yourPrivacyRights", linkTarget: "http://www.oracle.com/us/legal/privacy/index.html" },
    ];
    
    // release the application bootstrap busy state
    Context.getPageContext().getBusyContext().applicationBootstrapComplete();        
  }

  announcementHandler = (event: any): void => {
      this.message(event.detail.message);
      this.manner(event.detail.manner);
  }
}

export default new RootViewModel();
