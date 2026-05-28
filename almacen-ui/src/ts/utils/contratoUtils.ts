/**
 * Utilidades compartidas para contratos.
 * Usado en cualquier vista que muestre estatus.
 */
export const ESTATUS_LABELS: Record<string, string> = {
    CAPTURA:             "En captura",
    POR_RECIBIR:         "Pendiente de recibir",
    RECEPCION_PARCIAL:   "Recepción parcial",
    EN_ALMACEN:          "En almacén",
    LISTO_PARA_ENTREGAR: "Listo para entregar",
    ENTREGA_PARCIAL:     "Entrega parcial",
    ENTREGADO:           "Entregado",
    CERRADO:             "Cerrado"
};

export const ESTATUS_BADGE: Record<string, string> = {
    CAPTURA:             "",
    POR_RECIBIR:         "oj-badge-info",
    RECEPCION_PARCIAL:   "oj-badge-info",
    EN_ALMACEN:          "oj-badge-warning",
    LISTO_PARA_ENTREGAR: "oj-badge-success",
    ENTREGA_PARCIAL:     "oj-badge-warning",
    ENTREGADO:           "oj-badge-success",
    CERRADO:             ""
};

/**
   * Convierte el enum del backend a etiqueta en español para la UI.
   */
export function mapEstatusToLabel(estatus: string): string {
    return ESTATUS_LABELS[estatus] || estatus;
}

export function mapEstatusToBadge(estatus: string): string {
    return ESTATUS_BADGE[estatus] || "oj-badge-neutral";
}

export function removeAccents(str: string): string {
    return str.normalize("NFD").replace(/[\u0300-\u036f]/g, "");
};