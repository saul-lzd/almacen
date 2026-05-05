package com.sesesp.almacen.domain.clavepresupuestal.controller;

import com.sesesp.almacen.common.dto.CatalogoOptionDto;
import com.sesesp.almacen.domain.clavepresupuestal.service.ClavePresupuestalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/claves")
public class ClavePresupuestalController {

    private final ClavePresupuestalService service;

    public ClavePresupuestalController(ClavePresupuestalService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<CatalogoOptionDto>> getOptions() {
        return ResponseEntity.ok(service.getCatalogOptions());
    }
}
