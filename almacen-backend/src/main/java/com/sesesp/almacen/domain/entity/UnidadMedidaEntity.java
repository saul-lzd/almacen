package com.sesesp.almacen.domain.entity;

import com.sesesp.almacen.common.entity.AuditoriaEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Catálogo de unidades de medida.
 * Precargado en BD. Se usa el nombre tal como aparece en el contrato físico.
 * No tiene clave abreviada — el id es el identificador en los dropdowns.
 *
 * DDL v5: tabla unidad_medida
 *   nombre  (ej: "Vehículo", "Litro", "Kilogramo", "Pieza", "Unidad"...)
 */
@Entity
@Table(name = "unidad_medida")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UnidadMedidaEntity extends AuditoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_unidad_medida")
    private Integer idUnidadMedida;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;
}
