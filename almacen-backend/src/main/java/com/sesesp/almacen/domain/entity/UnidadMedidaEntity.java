package com.sesesp.almacen.domain.entity;

import com.sesesp.almacen.common.entity.AuditoriaEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "unidad_medida")
@Data
@EqualsAndHashCode(callSuper = true)
public class UnidadMedidaEntity extends AuditoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_unidad_medida")
    private Integer idUnidadMedida;

    @Column(name = "clave")
    private String clave;

    @Column(name = "descripcion")
    private String descripcion;

    // getters and setter auto-generated
}
