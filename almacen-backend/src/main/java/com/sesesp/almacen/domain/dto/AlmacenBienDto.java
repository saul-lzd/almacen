package com.sesesp.almacen.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

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

    /** Fotos ya subidas para esta unidad — Vehículo (min 5) o etiqueta de serie simple (min 1). */
    private int totalEvidencias;

    /** Componentes ya capturados (nombre + número de serie + si ya tiene foto) — solo grupos CONJUNTO. */
    private List<ComponenteBienDto> componentes;
}
