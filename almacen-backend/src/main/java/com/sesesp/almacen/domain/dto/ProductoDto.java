package com.sesesp.almacen.domain.dto;

import com.sesesp.almacen.domain.entity.UnidadMedidaEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductoDto {

    private Integer lote;
    private Integer partida;
    private String descripcionTecnica;
    private String codigoUnidadMedida;
    private Integer cantidad;
    private Double precioUnitario;
    private Double subtotal;

}
