package com.sesesp.almacen.domain.entity;

import com.sesesp.almacen.common.entity.AuditoriaEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "estatus_contrato")
@Data
@EqualsAndHashCode(callSuper = true)
public class EstatusContratoEntity extends AuditoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estatus_contrato")
    private Integer idEstatusContrato;

    @Column(name = "clave", nullable = false, length = 50)
    private String clave;

    @Column(name = "descripcion", nullable = false, length = 200)
    private String descripcion;

    @Column(name = "orden", nullable = false)
    private Integer orden;
}
