package com.sesesp.almacen.contrato.controller;

import com.sesesp.almacen.contrato.dto.UnidadMedidaResponse;
import com.sesesp.almacen.contrato.service.UnidadMedidaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/unidadesMedida")
public class UnidadMedidaController {

    private final UnidadMedidaService service;

    public UnidadMedidaController(UnidadMedidaService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<UnidadMedidaResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }
}
