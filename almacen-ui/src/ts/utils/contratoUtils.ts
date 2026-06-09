/**
 * Utilidades compartidas para contratos.
 */

// ──────────────────────────────────────────────────────────────────
// Estatus efectivo — derivado de estatus + 4 checkpoints
//
// El backend persiste CAPTURA como estatus formal.
// El resto se deriva de los checkpoints booleanos irreversibles.
// ──────────────────────────────────────────────────────────────────

export type EstatusEfectivo =
    | "CAPTURA"
    | "POR_RECIBIR"
    | "RECEPCION_PARCIAL"
    | "RECIBIDO"
    | "EN_ENTREGA"
    | "CERRADO";

export type ContratoCheckpoints = {
    estatus: string;
    primeraRecepcionRegistrada: boolean;
    primeraEntregaAutorizada:   boolean;
    todosLosBienesRecibidos:    boolean;
    contratoCerrado:            boolean;
};

/**
 * Calcula el estado visual del contrato a partir de sus checkpoints.
 * Orden de prioridad descendente.
 */
export function calcEstatusEfectivo(c: ContratoCheckpoints): EstatusEfectivo {
    if (c.estatus === "CAPTURA")        return "CAPTURA";
    if (c.contratoCerrado)              return "CERRADO";
    if (c.primeraEntregaAutorizada)     return "EN_ENTREGA";
    if (c.todosLosBienesRecibidos)      return "RECIBIDO";
    if (c.primeraRecepcionRegistrada)   return "RECEPCION_PARCIAL";
    return "POR_RECIBIR";
}

// ──────────────────────────────────────────────────────────────────
// Etiquetas y badges para el estatus efectivo
// ──────────────────────────────────────────────────────────────────

const ESTATUS_LABELS: Record<EstatusEfectivo, string> = {
    CAPTURA:           "En captura",
    POR_RECIBIR:       "Por recibir",
    RECEPCION_PARCIAL: "Recepción parcial",
    RECIBIDO:          "Recibido",
    EN_ENTREGA:        "En entrega",
    CERRADO:           "Cerrado",
};

const ESTATUS_BADGE: Record<EstatusEfectivo, string> = {
    CAPTURA:           "",
    POR_RECIBIR:       "oj-badge-info",
    RECEPCION_PARCIAL: "oj-badge-info",
    RECIBIDO:          "oj-badge-warning",
    EN_ENTREGA:        "oj-badge-warning",
    CERRADO:           "oj-badge-success",
};

export function mapEstatusToLabel(estatus: string): string {
    return (ESTATUS_LABELS as Record<string, string>)[estatus] ?? estatus;
}

export function mapEstatusToBadge(estatus: string): string {
    return (ESTATUS_BADGE as Record<string, string>)[estatus] ?? "oj-badge-neutral";
}

// ──────────────────────────────────────────────────────────────────
// Helpers generales
// ──────────────────────────────────────────────────────────────────

export function removeAccents(str: string): string {
    return str.normalize("NFD").replace(/[̀-ͯ]/g, "");
}
