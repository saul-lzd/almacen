/**
 * Utilidades compartidas para contratos.
 */

// ──────────────────────────────────────────────────────────────────
// Estatus efectivo — derivado de estatus + checkpoints booleanos
//
// El backend persiste CAPTURA como estatus formal.
// El resto se deriva de los checkpoints booleanos irreversibles.
// ──────────────────────────────────────────────────────────────────

export type EstatusEfectivo =
    | "CAPTURA"
    | "EN_ALMACEN"
    | "LISTO_ENTREGAR"
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
    if (c.estatus === "CAPTURA")    return "CAPTURA";
    if (c.contratoCerrado)          return "CERRADO";
    if (c.primeraEntregaAutorizada) return "LISTO_ENTREGAR";
    return "EN_ALMACEN";
}

// ──────────────────────────────────────────────────────────────────
// Etiquetas y badges para el estatus efectivo
// ──────────────────────────────────────────────────────────────────

const ESTATUS_LABELS: Record<EstatusEfectivo, string> = {
    CAPTURA:        "En captura",
    EN_ALMACEN:     "En almacén",
    LISTO_ENTREGAR: "Listo para entregar",
    CERRADO:        "Cerrado",
};

const ESTATUS_BADGE: Record<EstatusEfectivo, string> = {
    CAPTURA:        "",
    EN_ALMACEN:     "oj-badge-info",
    LISTO_ENTREGAR: "oj-badge-warning",
    CERRADO:        "oj-badge-success",
};

// Compatibilidad con valores de estatus crudos del backend (usados en lógica de negocio)
const ESTATUS_LABELS_RAW: Record<string, string> = {
    POR_RECIBIR:       "En almacén",
    RECEPCION_PARCIAL: "En almacén",
    RECIBIDO:          "En almacén",
    EN_ENTREGA:        "Listo para entregar",
};

const ESTATUS_BADGE_RAW: Record<string, string> = {
    POR_RECIBIR:       "oj-badge-info",
    RECEPCION_PARCIAL: "oj-badge-info",
    RECIBIDO:          "oj-badge-info",
    EN_ENTREGA:        "oj-badge-warning",
};

export function mapEstatusToLabel(estatus: string): string {
    return (ESTATUS_LABELS as Record<string, string>)[estatus]
        ?? ESTATUS_LABELS_RAW[estatus]
        ?? estatus;
}

export function mapEstatusToBadge(estatus: string): string {
    return (ESTATUS_BADGE as Record<string, string>)[estatus]
        ?? ESTATUS_BADGE_RAW[estatus]
        ?? "oj-badge-neutral";
}

// ──────────────────────────────────────────────────────────────────
// Helpers generales
// ──────────────────────────────────────────────────────────────────

export function removeAccents(str: string): string {
    return str.normalize("NFD").replace(/[̀-ͯ]/g, "");
}
