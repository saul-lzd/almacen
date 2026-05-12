package com.sesesp.almacen.domain.controller;

import com.sesesp.almacen.domain.dto.ContratoCreateRequestDto;
import com.sesesp.almacen.domain.dto.ContratoCreateResponseDto;
import com.sesesp.almacen.domain.service.ContratoService;
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
    public ResponseEntity<List<ContratoCreateResponseDto>> findAll() {
        return ResponseEntity.ok(contratoService.findAllContratos());
    }

    @GetMapping("/{idContrato}")
    public ResponseEntity<ContratoCreateResponseDto> findContratoById(@PathVariable Long idContrato) {
        return ResponseEntity.ok(contratoService.findContratoById(idContrato));
    }

    @PostMapping
    public ResponseEntity<ContratoCreateResponseDto> create(@RequestBody ContratoCreateRequestDto request) {
        ContratoCreateResponseDto response = contratoService.createContrato(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
