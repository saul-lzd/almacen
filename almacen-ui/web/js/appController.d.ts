import * as ko from "knockout";
import CoreRouter = require("ojs/ojcorerouter");
import ModuleRouterAdapter = require("ojs/ojmodulerouter-adapter");
import KnockoutRouterAdapter = require("ojs/ojknockoutrouteradapter");
import "ojs/ojknockout";
import "ojs/ojmodule-element";
import { ojNavigationList } from "ojs/ojnavigationlist";
interface CoreRouterDetail {
    label: string;
    iconClass: string;
}
declare class RootViewModel {
    manner: ko.Observable<string>;
    message: ko.Observable<string | undefined>;
    smScreen: ko.Observable<boolean> | undefined;
    moduleAdapter: ModuleRouterAdapter<CoreRouterDetail>;
    navDataProvider: ojNavigationList<string, CoreRouter.CoreRouterState<CoreRouterDetail>>["data"];
    appName: ko.Observable<string>;
    userLogin: ko.Observable<string>;
    footerLinks: Array<object>;
    selection: KnockoutRouterAdapter<CoreRouterDetail>;
    constructor();
    announcementHandler: (event: any) => void;
}
declare const _default: RootViewModel;
export default _default;
