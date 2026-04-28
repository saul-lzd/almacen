package com.sesesp.almacen.common.exception;

public class ContratoNoEncontradoException extends RuntimeException {

    public ContratoNoEncontradoException(Long id) {
        super("No se encontró el contrato con id: " + id);
    }
}