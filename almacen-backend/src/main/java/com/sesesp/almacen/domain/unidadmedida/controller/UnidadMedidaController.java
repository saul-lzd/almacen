package com.sesesp.almacen.domain.unidadmedida.controller;

import com.sesesp.almacen.common.dto.CatalogoOptionDto;
import com.sesesp.almacen.domain.unidadmedida.dto.UnidadMedidaResponse;
import com.sesesp.almacen.domain.unidadmedida.service.UnidadMedidaService;
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
    public ResponseEntity<List<CatalogoOptionDto>> getAll() {
        return ResponseEntity.ok(service.getCatalogOptions());
    }
}
