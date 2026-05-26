package com.sesesp.almacen.domain.dto;

import lombok.Data;

@Data
public class ProcesarBienRequestDto {
    private String numeroSerie;
    private String numeroMotor;
    private String marca;
    private String modelo;
    private String descripcionComplementaria;
}
