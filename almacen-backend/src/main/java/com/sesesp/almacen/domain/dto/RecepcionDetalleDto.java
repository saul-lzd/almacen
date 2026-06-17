package com.sesesp.almacen.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Detalle completo de una recepción de almacén.
 * Devuelto por GET /api/contratos/{id}/recepciones (lista de todas las recepciones del contrato).
 */
@Data
@AllArgsConstructor
public class RecepcionDetalleDto {

    private Integer     idRecepcionAlmacen;
    private String      folio;
    private LocalDateTime fechaRecepcion;
    private String      transportista;
    private String      almacenista;
    private String      observaciones;
    /** Valor del enum EstatusRecepcion como String: INICIADA | EN_PROCESO | PROCESADA | ENTREGADA */
    private String      estatus;
    private int         totalBienes;
    private int         totalProcesados;
    private int         totalPendientesAutorizar;
    private int         totalListos;
    private int         totalEntregados;
    private List<BienDetalleDto> bienes;

    @Data
    @AllArgsConstructor
    public static class BienDetalleDto {
        private Integer    idContratoBien;
        private Short      lote;
        private Short      partida;
        /** Texto plano derivado de descripcionTecnica, máx 100 caracteres */
        private String     descripcionCorta;
        private String     unidadMedida;
        private Integer    cantidadContratada;
        private BigDecimal cantidadRecibida;
        private BigDecimal cantidadRechazada;
        private String     comentarios;
    }
}
