package com.sesesp.almacen.domain.controller;

import com.sesesp.almacen.domain.dto.DefinirCapturaSerieRequestDto;
import com.sesesp.almacen.domain.service.AlmacenBienService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/contrato-bienes")
@RequiredArgsConstructor
public class ContratoBienController {

    private final AlmacenBienService almacenBienService;

    /**
     * Define qué se captura por unidad en este grupo (Ninguno/Simple/Conjunto) y,
     * si es Conjunto, la plantilla de nombres de componente esperados.
     */
    @PatchMapping("/{id}/captura-serie")
    public ResponseEntity<?> definirCapturaSerie(
            @PathVariable Integer id,
            @RequestBody DefinirCapturaSerieRequestDto request) {
        almacenBienService.definirCapturaSerie(id, request);
        return ResponseEntity.ok().build();
    }

    /**
     * Sube fotos "de catálogo" del tipo de bien (mínimo 5). Acumulativo: se
     * puede llamar varias veces antes de llegar al mínimo.
     */
    @PostMapping(value = "/{id}/evidencias", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> subirEvidenciaGrupo(
            @PathVariable Integer id,
            @RequestPart("evidencias") List<MultipartFile> evidencias) {
        almacenBienService.subirEvidenciaGrupo(id, evidencias);
        return ResponseEntity.ok().build();
    }
}
