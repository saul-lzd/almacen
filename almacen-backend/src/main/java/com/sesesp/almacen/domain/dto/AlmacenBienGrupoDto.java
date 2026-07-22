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

    /** NINGUNO | SIMPLE | CONJUNTO — determina qué se captura por unidad. */
    private String tipoCapturaSerie;

    /** Nombres de componente esperados cuando tipoCapturaSerie = CONJUNTO, en orden. */
    private List<String> componentesEsperados;

    /** Fotos "de catálogo" ya subidas para este grupo (no aplica a Vehículo). */
    private int totalEvidenciasGrupo;
}
