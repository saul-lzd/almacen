package com.sesesp.almacen.domain.entity;

import com.sesesp.almacen.common.entity.AuditoriaEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Catálogo de claves presupuestales.
 * Precargado en BD — el usuario selecciona de este catálogo al capturar
 * el contrato. No se crean registros nuevos desde la UI.
 *
 * El monto asignado por contrato vive en ContratoClave PresupuestalEntity.
 *
 * tabla clave_presupuestal
 *   clave, partida_especifica
 *
 * Ejemplo:
 *   clave:             127001-10601012013-211201AEAAA0226
 *   partida_especifica: 211-MATERIALES, ÚTILES Y EQUIPOS MENORES DE OFICINA
 */
@Entity
@Table(name = "clave_presupuestal")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClavePresupuestalEntity extends AuditoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_clave_presupuestal")
    private Integer idClavePresupuestal;

    /** Identificador alfanumérico de la clave */
    @Column(name = "clave", nullable = false, length = 100)
    private String clave;

    /** Descripción de la partida (ej: "211-MATERIALES...") */
    @Column(name = "partida_especifica", nullable = false, length = 255)
    private String partidaEspecifica;
}
