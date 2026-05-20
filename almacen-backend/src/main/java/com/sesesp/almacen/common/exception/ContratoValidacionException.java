package com.sesesp.almacen.common.exception;

import java.util.List;

/**
 * Se lanza cuando un contrato no cumple las validaciones
 * necesarias para cambiar de estatus.
 * Lleva la lista de errores para mostrarlos en la UI.
 */
public class ContratoValidacionException extends RuntimeException {

    private final List<String> errores;

    public ContratoValidacionException(List<String> errores) {
        super("El contrato no cumple los requisitos: " + String.join(", ", errores));
        this.errores = errores;
    }

    public List<String> getErrores() {
        return errores;
    }
}