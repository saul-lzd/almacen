package com.sesesp.almacen.contrato.controller;

import com.sesesp.almacen.contrato.entity.Contrato;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/")
public class Default {

    @GetMapping
    public ResponseEntity<String> defaultResponse() {
        return ResponseEntity.ok("OK");
    }
}
