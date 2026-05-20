package com.sesesp.almacen.domain.controller;

import com.sesesp.almacen.domain.dto.ContratoCreateRequestDto;
import com.sesesp.almacen.domain.dto.ContratoDto;
import com.sesesp.almacen.domain.service.ContratoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contratos")
@RequiredArgsConstructor
public class ContratoController {

    private final ContratoService contratoService;

    @GetMapping
    public ResponseEntity<List<ContratoDto>> findAll() {
        return ResponseEntity
                .ok(contratoService.findAllContratos());
    }

    @GetMapping("/{idContrato}")
    public ResponseEntity<ContratoDto> findById(
            @PathVariable Integer idContrato) {
        return ResponseEntity.
                ok(contratoService.findContratoById(idContrato));
    }

    @PostMapping
    public ResponseEntity<ContratoDto> create(
            @RequestBody ContratoCreateRequestDto request) {
        return ResponseEntity.
                status(HttpStatus.CREATED)
                .body(contratoService.createContrato(request));
    }

    @PutMapping("/{idContrato}")
    public ResponseEntity<ContratoDto> update(
            @PathVariable Integer idContrato,
            @RequestBody ContratoCreateRequestDto request) {
        return ResponseEntity
                .ok(contratoService.updateContrato(idContrato, request));
    }

    /**
     * Envía el contrato al almacén.
     * Cambia el estatus de CAPTURA a POR_RECIBIR y bloquea la edición.
     * No recibe body — la acción se ejecuta sobre el contrato existente.
     * Regresa 400 con la lista de errores si el contrato está incompleto.
     */
    @PatchMapping("/{idContrato}/enviar-almacen")
    public ResponseEntity<?> enviarAlmacen(@PathVariable Integer idContrato) {
        contratoService.enviarAlmacen(idContrato);
        return ResponseEntity
                .ok().build();
    }
}