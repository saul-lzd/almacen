package com.sesesp.almacen.contrato.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "clave_presupuestal")
public class ClavePresupuestalEntity extends Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_clave_presupuestal")
    private Integer idClavePresupuestal;

    @Column(name = "clave_presupuestal")
    private String clavePresupuestal;

    @Column(name = "partida_especifica")
    private String partidaEspecifica;

    // getters and setter auto-generated

}
