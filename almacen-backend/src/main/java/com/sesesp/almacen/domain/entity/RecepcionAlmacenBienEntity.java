package com.sesesp.almacen.domain.entity;

import com.sesesp.almacen.common.entity.AuditoriaEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Detalle de bienes recibidos en una recepción.
 * Registra cuántas unidades de cada bien del contrato llegaron en este evento.
 *
 * cantidad_recibida + cantidad_rechazada <= contrato_bien.cantidad pendiente
 *
 * tabla recepcion_almacen_bien
 */
@Entity
@Table(name = "recepcion_almacen_bien")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecepcionAlmacenBienEntity extends AuditoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_recepcion_almacen_bien")
    private Integer idRecepcionAlmacenBien;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_recepcion_almacen", nullable = false)
    private RecepcionAlmacenEntity recepcionAlmacen;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_contrato_bien", nullable = false)
    private ContratoBienEntity contratoBien;

    @Column(name = "cantidad_recibida", nullable = false, precision = 15, scale = 2)
    private BigDecimal cantidadRecibida;

    @Column(name = "cantidad_rechazada", nullable = false, precision = 15, scale = 2)
    //private BigDecimal cantidadRechazada = BigDecimal.ZERO;
    private BigDecimal cantidadRechazada;

    @Column(name = "comentarios", length = 500)
    private String comentarios;

    @Column(name = "motivo_rechazo", length = 500)
    private String motivoRechazo;
}
