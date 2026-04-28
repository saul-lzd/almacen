package com.sesesp.almacen.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ContratoNoEncontradoException.class)
    public ResponseEntity<String> manejarContratoNoEncontrado(ContratoNoEncontradoException ex) {
        return ResponseEntity.notFound().build();
    }
}