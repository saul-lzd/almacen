package com.sesesp.almacen.domain.entity;

import com.sesesp.almacen.common.entity.AuditoriaEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Bienes incluidos en una salida de almacén.
 * Cada registro vincula un bien físico (AlmacenBienEntity) con una salida.
 * Un bien solo puede aparecer en una salida — no se puede entregar dos veces.
 *
 * DDL v5: tabla salida_almacen_bien
 */
@Entity
@Table(name = "salida_almacen_bien")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalidaAlmacenBienEntity extends AuditoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_salida_almacen_bien")
    private Integer idSalidaAlmacenBien;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_salida_almacen", nullable = false)
    private SalidaAlmacenEntity salidaAlmacen;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_almacen_bien", nullable = false)
    private AlmacenBienEntity almacenBien;

    @Column(name = "observaciones", length = 500)
    private String observaciones;
}