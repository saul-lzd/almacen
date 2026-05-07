package com.sesesp.almacen.domain.dto;

import lombok.Data;

@Data
public class DetallesPagoDto {

    private Double montoSinImpuestos;
    private Double impuestos;
    private Double montoTotal;
    private Boolean tieneAnticipo;
    private Double porcentajeAnticipo;
    private Double montoAnticipo;
    private Integer numeroExhibiciones;
    private Boolean tieneFiniquito;
    private Double porcentajeFiniquito;
    private Double montoFiniquito;
}
