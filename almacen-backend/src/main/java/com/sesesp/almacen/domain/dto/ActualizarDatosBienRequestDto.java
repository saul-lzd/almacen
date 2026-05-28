package com.sesesp.almacen.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActualizarDatosBienRequestDto {
    private String numeroSerie;
    private String numeroMotor;
    private String marca;
    private String modelo;
    private String descripcionComplementaria;
}
