package com.sesesp.almacen.common.exception;

public class ContratoNoGuardadoException extends RuntimeException {

    public ContratoNoGuardadoException(String identificador) {
        super("No se pudo guardar: " + identificador);
    }
}