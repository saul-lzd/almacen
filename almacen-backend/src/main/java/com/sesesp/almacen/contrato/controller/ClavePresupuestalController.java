package com.sesesp.almacen.contrato.controller;

import com.sesesp.almacen.contrato.dto.ClavePresupuestalResponse;
import com.sesesp.almacen.contrato.entity.Contrato;
import com.sesesp.almacen.contrato.service.ClavePresupuestalService;
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
    public ResponseEntity<List<ClavePresupuestalResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }
}
