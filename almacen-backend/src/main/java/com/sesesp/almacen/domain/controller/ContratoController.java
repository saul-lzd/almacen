package com.sesesp.almacen.domain.controller;

import com.sesesp.almacen.domain.dto.ContratoCreateRequestDto;
import com.sesesp.almacen.domain.dto.ContratoDto;
import com.sesesp.almacen.domain.service.ContratoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contratos")
@RequiredArgsConstructor
public class ContratoController {

    private final ContratoService contratoService;

    @GetMapping
    public ResponseEntity<List<ContratoDto>> findAll() {
        return ResponseEntity.ok(contratoService.findAllContratos());
    }

    @GetMapping("/{idContrato}")
    public ResponseEntity<ContratoDto> findById(@PathVariable Integer idContrato) {
        return ResponseEntity.ok(contratoService.findContratoById(idContrato));
    }

    @PostMapping
    public ResponseEntity<ContratoDto> create(@RequestBody ContratoCreateRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contratoService.createContrato(request));
    }

    @PutMapping("/{idContrato}")
    public ResponseEntity<ContratoDto> update(
            @PathVariable Integer idContrato,
            @RequestBody ContratoCreateRequestDto request) {
        return ResponseEntity.ok(contratoService.updateContrato(idContrato, request));
    }
}


//@RestController
//@RequestMapping("/api/contratos")
//@RequiredArgsConstructor
//public class ContratoController {
//
//    private final ContratoService contratoService;
//
//
//    @GetMapping
//    public ResponseEntity<List<ContratoDto>> findAll() {
//        return ResponseEntity.ok(contratoService.findAllContratos());
//    }
//
//    @GetMapping("/{idContrato}")
//    public ResponseEntity<ContratoDto> findContratoById(@PathVariable Long idContrato) {
//        return ResponseEntity.ok(contratoService.findContratoById(idContrato));
//    }
//
//    @PostMapping
//    public ResponseEntity<ContratoDto> create(@RequestBody ContratoCreateRequestDto request) {
//        ContratoCreateResponseDto response = contratoService.createContrato(request);
//        return ResponseEntity.status(HttpStatus.CREATED).body(response);
//    }
//
//    @PutMapping("{idContrato}")
//    public ResponseEntity<ContratoDto> update(@PathVariable Long idContrato, @RequestBody ContratoCreateRequestDto request) {
//        ContratoCreateResponseDto response = contratoService.updateContrato(idContrato, request);
//        return ResponseEntity.status(HttpStatus.CREATED).body(response);
//    }
//}
