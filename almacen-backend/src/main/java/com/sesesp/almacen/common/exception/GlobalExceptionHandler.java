package com.sesesp.almacen.common.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
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
        body.put("mensaje", ex.getErrores().isEmpty() ? "Error de validación" : ex.getErrores().get(0));
        body.put("errores", ex.getErrores());

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> manejarEntidadNoEncontrada(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> manejarIntegridad(DataIntegrityViolationException ex) {
        String mensaje = ex.getMessage() != null && ex.getMessage().contains("numero_contrato")
                ? "El número de contrato ya existe."
                : "Operación no permitida: violación de integridad de datos.";

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("mensaje", mensaje);
        body.put("errores", java.util.List.of(mensaje));

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }
}