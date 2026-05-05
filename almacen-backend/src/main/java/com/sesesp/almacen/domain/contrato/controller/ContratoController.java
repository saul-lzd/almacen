package com.sesesp.almacen.domain.contrato.controller;

import com.sesesp.almacen.domain.contrato.dto.ContratoRequest;
import com.sesesp.almacen.domain.contrato.service.ContratoService;
import com.sesesp.almacen.domain.contrato.entity.Contrato;
import com.sesesp.almacen.domain.contrato.dto.ContratoResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contratos")
public class ContratoController {

    private final ContratoService contratoService;

    public ContratoController(ContratoService contratoService) {
        this.contratoService = contratoService;
    }

    @GetMapping
    public ResponseEntity<List<Contrato>> obtenerTodos() {
        return ResponseEntity.ok(contratoService.getAll());
    }

    @GetMapping("/{idContrato}")
    public ResponseEntity<ContratoResponse> obtenerPorId(@PathVariable Long idContrato) {
        return ResponseEntity.ok(contratoService.getById(idContrato));
    }

    @PostMapping
    public ResponseEntity<ContratoResponse> guardar(@RequestBody ContratoRequest contratoRequest) {
        ContratoResponse response = contratoService.save(contratoRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
