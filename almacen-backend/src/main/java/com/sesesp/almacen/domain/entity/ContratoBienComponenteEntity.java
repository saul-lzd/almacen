package com.sesesp.almacen.domain.entity;

import com.sesesp.almacen.common.entity.AuditoriaEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Plantilla de componentes esperados para un ContratoBien marcado como
 * "conjunto" (tipoCapturaSerie = CONJUNTO) — ej. para una partida de
 * "Workstation" se define una sola vez: Monitor, CPU, Teclado. Cada unidad
 * física (AlmacenBienEntity) de ese grupo captura después un número de
 * serie + foto por cada uno de estos componentes (ver ComponenteBienEntity).
 *
 * tabla contrato_bien_componente
 */
@Entity
@Table(name = "contrato_bien_componente")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContratoBienComponenteEntity extends AuditoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contrato_bien_componente")
    private Integer idContratoBienComponente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_contrato_bien", nullable = false)
    private ContratoBienEntity contratoBien;

    @Column(name = "nombre_componente", nullable = false, length = 150)
    private String nombreComponente;

    /** Orden de captura/despliegue en la UI — no tiene otro significado de negocio. */
    @Column(name = "orden", nullable = false)
    private Short orden;
}
