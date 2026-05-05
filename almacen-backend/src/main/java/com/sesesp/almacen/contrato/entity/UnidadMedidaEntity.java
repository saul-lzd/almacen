package com.sesesp.almacen.contrato.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "unidad_medida")
@Data
public class UnidadMedidaEntity extends Auditoria{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_unidad_medida")
    private Integer idUnidadMedida;

    @Column(name = "clave")
    private String clave;

    @Column(name = "descripcion")
    private String descripcion;


}
