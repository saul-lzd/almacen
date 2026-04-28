import * as ko from "knockout";
import "ojs/ojtable";
import 'oj-c/form-layout';
import "oj-c/button";
import 'oj-c/dialog';
import 'oj-c/tab-bar';
import 'oj-c/input-text';
import "oj-c/input-number";
import { TabData } from 'oj-c/tab-bar';
declare class DashboardViewModel {
    isDialogOpened: ko.Observable<boolean>;
    isPanelContratoVisible: ko.Observable<boolean>;
    isPanelCompradorVisible: ko.Observable<boolean>;
    edgeOptions: {
        label: string;
        value: string;
    }[];
    layoutOptions: {
        label: string;
        value: string;
    }[];
    displayOptions: {
        label: string;
        value: string;
    }[];
    data: ko.ObservableArray<TabData<string>>;
    selectedItem: ko.Observable<string>;
    edge: ko.Observable<string>;
    layout: ko.Observable<string>;
    display: ko.Observable<string>;
    montoSinImpuestos: ko.Observable<number>;
    impuestos: ko.Observable<number>;
    montoTotal: ko.PureComputed<number>;
    openDialog: () => void;
    closeDialog: () => void;
    connected(): void;
    disconnected(): void;
    transitionCompleted(): void;
}
export = DashboardViewModel;
