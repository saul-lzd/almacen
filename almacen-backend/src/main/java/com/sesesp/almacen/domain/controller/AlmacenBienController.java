package com.sesesp.almacen.domain.controller;

import com.sesesp.almacen.domain.dto.ActualizarDatosBienRequestDto;
import com.sesesp.almacen.domain.dto.ComponenteBienRequestDto;
import com.sesesp.almacen.domain.dto.ProcesarBienRequestDto;
import com.sesesp.almacen.domain.dto.ProcesarBloqueRequestDto;
import com.sesesp.almacen.domain.service.AlmacenBienService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/almacen-bienes")
@RequiredArgsConstructor
public class AlmacenBienController {

    private final AlmacenBienService almacenBienService;

    @PatchMapping("/{id}/datos")
    public ResponseEntity<?> actualizarDatos(
            @PathVariable Integer id,
            @RequestBody ActualizarDatosBienRequestDto request) {
        almacenBienService.actualizarDatos(id, request);
        return ResponseEntity.ok().build();
    }

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

    /**
     * Sube fotos de una unidad individual — Vehículo (mínimo 5) o etiqueta del
     * número de serie/motor (mínimo 1). Acumulativo: se puede llamar varias veces.
     */
    @PostMapping(value = "/{id}/evidencias", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> subirEvidenciaUnidad(
            @PathVariable Integer id,
            @RequestPart("evidencias") List<MultipartFile> evidencias) {
        almacenBienService.subirEvidenciaUnidad(id, evidencias);
        return ResponseEntity.ok().build();
    }

    /**
     * Guarda los componentes (número de serie + foto) de una unidad "conjunto".
     * multipart/form-data: la parte "request" con el JSON de List&lt;ComponenteBienRequestDto&gt;
     * y la parte "evidencias" con una foto por componente, en el mismo orden.
     */
    @PostMapping(value = "/{id}/componentes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> guardarComponentesUnidad(
            @PathVariable Integer id,
            @RequestPart("request") List<ComponenteBienRequestDto> componentes,
            @RequestPart("evidencias") List<MultipartFile> evidencias) {
        almacenBienService.guardarComponentesUnidad(id, componentes, evidencias);
        return ResponseEntity.ok().build();
    }
}
