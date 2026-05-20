package com.sesesp.almacen.domain.controller;

import com.sesesp.almacen.domain.dto.FuncionarioDto;
import com.sesesp.almacen.domain.service.FuncionarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/funcionarios")
@RequiredArgsConstructor
public class FuncionarioController {

    private final FuncionarioService funcionarioService;

    /**
     * Lista todos los funcionarios activos para los dropdowns
     * de comprador y administrador del contrato en la UI.
     */
    @GetMapping
    public ResponseEntity<List<FuncionarioDto>> findAll() {
        return ResponseEntity.ok(funcionarioService.findOptions());
    }
}

