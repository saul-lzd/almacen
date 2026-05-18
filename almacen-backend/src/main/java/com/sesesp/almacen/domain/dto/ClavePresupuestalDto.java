package com.sesesp.almacen.domain.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClavePresupuestalDto {

     private String clave;
     private String partidaEspecifica;
     private BigDecimal montoAsignado;
}
