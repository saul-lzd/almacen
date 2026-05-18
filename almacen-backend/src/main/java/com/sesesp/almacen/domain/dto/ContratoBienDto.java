package com.sesesp.almacen.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContratoBienDto {

    private Short lote;
    private Short partida;
    private String descripcionTecnica;
    private String unidadMedida;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;

}
