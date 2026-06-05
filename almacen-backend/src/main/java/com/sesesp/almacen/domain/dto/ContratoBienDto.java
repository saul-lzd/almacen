package com.sesesp.almacen.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContratoBienDto {

    private Integer idContratoBien;
    private Short lote;
    private Short partida;
    private String descripcionTecnica;
    private String descripcionCorta;
    private Integer idUnidadMedida;
    private String unidadMedida;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;

    /** Total acumulado recibido en todas las recepciones previas. Null si no aplica. */
    private BigDecimal cantidadRecibidaTotal;

    /** Unidades ya procesadas (PROCESADO, LISTO_PARA_ENTREGAR o ENTREGADO). */
    private Long cantidadProcesadaTotal;

    /** Unidades ya entregadas al beneficiario. */
    private Long cantidadEntregadaTotal;

}
