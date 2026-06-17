package com.sesesp.almacen.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResumenBienesDto {
    /** Suma de ContratoBien.cantidad — total de unidades pactadas en el contrato. */
    private long totalContratados;
    /** Unidades que han llegado físicamente al almacén (cualquier estatus de AlmacenBien). */
    private long totalRecibidos;
    /** RECIBIDO + EN_PROCESO: llegaron pero aún no terminaron el procesamiento. */
    private long enProceso;
    /** PROCESADO: datos completos, esperando autorización del admin. */
    private long procesados;
    /** PROCESADO estrictamente: pendientes de autorización por el admin. */
    private long pendientesAutorizar;
    /** LISTO_PARA_ENTREGAR: autorizados por el admin, pendientes de entrega física. */
    private long listos;
    /** ENTREGADO: entregados al beneficiario. Ciclo completado. */
    private long entregados;
}
