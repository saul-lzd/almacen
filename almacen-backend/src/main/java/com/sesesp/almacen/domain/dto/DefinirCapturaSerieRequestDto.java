package com.sesesp.almacen.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class DefinirCapturaSerieRequestDto {
    /** NINGUNO | SIMPLE | CONJUNTO */
    private String tipoCapturaSerie;

    /** Nombres de componente esperados, en orden — solo aplica cuando tipoCapturaSerie = CONJUNTO. */
    private List<String> componentes;
}
