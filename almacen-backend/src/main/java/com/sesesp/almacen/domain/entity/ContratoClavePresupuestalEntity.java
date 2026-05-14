package com.sesesp.almacen.domain.entity;

import com.sesesp.almacen.common.entity.AuditoriaEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "contrato_clave_presupuestal")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContratoClavePresupuestalEntity extends AuditoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contrato_clave_presupuestal")
    private Integer idContratoClavePresupuestal;

    /**
     * Contrato al que pertenece la clave presupuestal.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_contrato", nullable = false)
    private ContratoEntity contrato;

    /**
    /**
     * Clave presupuestal del catálogo.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_clave_presupuestal", nullable = false)
    private ClavePresupuestalEntity clavePresupuestal;

    /**
     * Monto asignado a esta clave presupuestal dentro del contrato.
     */
    @Column(name = "monto_asignado", precision = 15)
    private Double montoAsignado;
}
