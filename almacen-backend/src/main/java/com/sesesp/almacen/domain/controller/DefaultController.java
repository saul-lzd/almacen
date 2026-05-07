package com.sesesp.almacen.domain.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/")
public class DefaultController {

    @GetMapping
    public ResponseEntity<String> defaultResponse() {
        return ResponseEntity.ok("OK");
    }
}
