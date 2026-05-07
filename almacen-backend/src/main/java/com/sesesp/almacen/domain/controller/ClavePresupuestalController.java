package com.sesesp.almacen.domain.controller;

import com.sesesp.almacen.common.dto.CatalogOptionDto;
import com.sesesp.almacen.domain.service.ClavePresupuestalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/claves-presupuestales")
public class ClavePresupuestalController {

    private final ClavePresupuestalService service;

    public ClavePresupuestalController(ClavePresupuestalService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<CatalogOptionDto>> findOptions() {
        return ResponseEntity.ok(service.findOptions());
    }
}
