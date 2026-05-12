package com.sesesp.almacen.domain.entity;

import com.sesesp.almacen.common.entity.AuditoriaEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "clave_presupuestal")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClavePresupuestalEntity extends AuditoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_clave_presupuestal")
    private Integer idClavePresupuestal;

    @Column(name = "clave_presupuestal")
    private String clavePresupuestal;

    @Column(name = "partida_especifica")
    private String partidaEspecifica;

}
