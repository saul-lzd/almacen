package com.sesesp.almacen.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AlmacenBienDto {
    private Integer idAlmacenBien;
    private String codigoInterno;
    private String estatus;
    private String folioRecepcion;
    private LocalDateTime fechaRecepcion;
    private String numeroSerie;
    private String numeroMotor;
    private String marca;
    private String modelo;
    private String descripcionComplementaria;
}
