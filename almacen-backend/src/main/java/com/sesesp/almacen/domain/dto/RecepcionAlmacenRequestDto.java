package com.sesesp.almacen.domain.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Payload para registrar la recepción de bienes de un contrato.
 * Enviado por el almacenista cuando llega el proveedor.
 */
@Data
public class RecepcionAlmacenRequestDto {

    /** Nombre del transportista del proveedor que entrega físicamente */
    private String transportista;

    /** Observaciones que hacer la persona que recibe */
    private String observaciones;

    /** Lista de bienes recibidos con sus cantidades */
    private List<BienRecepcionDto> bienes;

    @Data
    public static class BienRecepcionDto {
        private Integer idContratoBien;
        private BigDecimal cantidadRecibida;
    }
}
