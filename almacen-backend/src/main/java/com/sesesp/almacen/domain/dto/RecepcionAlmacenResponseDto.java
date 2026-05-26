package com.sesesp.almacen.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Datos de la recepción guardada para un contrato.
 * Usado en la vista de solo lectura del almacén.
 */
@Data
@AllArgsConstructor
public class RecepcionAlmacenResponseDto {

    private String folio;
    private LocalDateTime fechaRecepcion;

    /** Nombre del transportista que entregó físicamente */
    private String nombreEntrega;

    private String observaciones;
}
