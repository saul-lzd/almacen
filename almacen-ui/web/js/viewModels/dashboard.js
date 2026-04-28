define(["require", "exports", "knockout", "ojs/ojtable", "oj-c/form-layout", "oj-c/button", "oj-c/dialog", "oj-c/tab-bar", "oj-c/input-text", "oj-c/input-number"], function (require, exports, ko) {
    "use strict";
    const data = [
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
        constructor() {
            this.isDialogOpened = ko.observable(false);
            this.isPanelContratoVisible = ko.observable(false);
            this.isPanelCompradorVisible = ko.observable(false);
            this.edgeOptions = [
                { label: 'Top', value: 'top' },
                { label: 'Bottom', value: 'bottom' }
            ];
            this.layoutOptions = [{ label: 'Stretch', value: 'stretch' },
                { label: 'Condense', value: 'condense' }];
            this.displayOptions = [{ label: 'Standard', value: 'standard' },
                { label: 'Icons', value: 'icons' },
                { label: 'Stacked', value: 'stacked' }
            ];
            this.data = ko.observableArray(data);
            this.selectedItem = ko.observable(data[0].itemKey);
            this.edge = ko.observable('top');
            this.layout = ko.observable('stretch');
            this.display = ko.observable('standard');
            this.montoSinImpuestos = ko.observable(0.0);
            this.impuestos = ko.observable(0.0);
            this.montoTotal = ko.pureComputed(() => {
                return this.montoSinImpuestos() + this.impuestos();
            });
            this.openDialog = () => {
                this.isDialogOpened(true);
            };
            this.closeDialog = () => {
                this.isDialogOpened(false);
            };
        }
        connected() {
        }
        disconnected() {
        }
        transitionCompleted() {
        }
    }
    return DashboardViewModel;
});
//# sourceMappingURL=dashboard.js.map