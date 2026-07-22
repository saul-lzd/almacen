package com.sesesp.almacen.domain.controller;

import com.sesesp.almacen.domain.service.AlmacenBienService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Catálogo de nombres de componente para bienes "conjunto" — autocompletado en la UI de procesamiento. */
@RestController
@RequestMapping("/api/catalogo-componentes")
@RequiredArgsConstructor
public class CatalogoComponenteController {

    private final AlmacenBienService almacenBienService;

    @GetMapping
    public ResponseEntity<List<String>> listar() {
        return ResponseEntity.ok(almacenBienService.getCatalogoComponentes());
    }
}
