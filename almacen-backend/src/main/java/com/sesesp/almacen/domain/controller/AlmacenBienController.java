package com.sesesp.almacen.domain.controller;

import com.sesesp.almacen.domain.dto.ProcesarBienRequestDto;
import com.sesesp.almacen.domain.dto.ProcesarBloqueRequestDto;
import com.sesesp.almacen.domain.service.AlmacenBienService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/almacen-bienes")
@RequiredArgsConstructor
public class AlmacenBienController {

    private final AlmacenBienService almacenBienService;

    @PatchMapping("/{id}/procesar")
    public ResponseEntity<?> procesarBien(
            @PathVariable Integer id,
            @RequestBody ProcesarBienRequestDto request) {
        almacenBienService.procesarBien(id, request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/procesar-bloque")
    public ResponseEntity<?> procesarBloque(
            @RequestBody ProcesarBloqueRequestDto request) {
        almacenBienService.procesarBloque(request);
        return ResponseEntity.ok().build();
    }
}
