package com.sesesp.almacen.common.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ContratoNoEncontradoException.class)
    public ResponseEntity<String> manejarContratoNoEncontrado(ContratoNoEncontradoException ex) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(ContratoValidacionException.class)
    public ResponseEntity<Map<String, Object>> manejarValidacionContrato(
            ContratoValidacionException ex) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("mensaje", "El contrato no puede ser enviado al almacén");
        body.put("errores", ex.getErrores());

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> manejarEntidadNoEncontrada(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}