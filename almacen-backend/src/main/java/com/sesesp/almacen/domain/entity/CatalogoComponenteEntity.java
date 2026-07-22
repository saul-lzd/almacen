package com.sesesp.almacen.domain.entity;

import com.sesesp.almacen.common.entity.AuditoriaEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Catálogo de nombres de componente para bienes "conjunto" (ej. workstation
 * = Monitor + CPU + Teclado). Crece de forma orgánica: cuando el almacenista
 * escribe un nombre nuevo al definir los componentes de un grupo, se agrega
 * aquí para autocompletado en capturas futuras.
 *
 * tabla catalogo_componente
 */
@Entity
@Table(name = "catalogo_componente")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatalogoComponenteEntity extends AuditoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_catalogo_componente")
    private Integer idCatalogoComponente;

    @Column(name = "nombre", nullable = false, length = 150, unique = true)
    private String nombre;
}
