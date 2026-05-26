package com.sesesp.almacen.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProcesarBloqueRequestDto {
    private List<Integer> ids;
    private String marca;
    private String modelo;
    private String descripcionComplementaria;
}
