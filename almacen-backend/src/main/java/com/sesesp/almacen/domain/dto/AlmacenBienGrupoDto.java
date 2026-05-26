package com.sesesp.almacen.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AlmacenBienGrupoDto {
    private Integer idContratoBien;
    private Short lote;
    private Short partida;
    private String descripcion;
    private String unidadMedida;
    private int totalUnidades;
    private List<AlmacenBienDto> unidades;
}
