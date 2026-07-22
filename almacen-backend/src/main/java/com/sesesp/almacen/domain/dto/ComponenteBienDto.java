package com.sesesp.almacen.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ComponenteBienDto {
    private Integer idComponenteBien;
    private String nombreComponente;
    private String numeroSerie;
    private boolean tieneFoto;
}
