package com.sesesp.almacen.domain.entity;

import com.sesesp.almacen.common.entity.AuditoriaEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Relación N:M entre Contrato y ClavePresupuestal.
 * Cada clave aporta un monto_asignado al financiamiento del contrato.
 *
 * Validación financiera:
 *   SUM(monto_asignado) de todas las claves del contrato = contrato.monto_total
 *
 * tabla contrato_clave_presupuestal
 */
@Entity
@Table(name = "contrato_clave_presupuestal",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_contrato_clave_presupuestal",
                columnNames = {"id_contrato", "id_clave_presupuestal"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContratoClavePresupuestalEntity extends AuditoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contrato_clave_presupuestal")
    private Integer idContratoClavePresupuestal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_contrato", nullable = false)
    private ContratoEntity contrato;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_clave_presupuestal", nullable = false)
    private ClavePresupuestalEntity clavePresupuestal;

    /**
     * Monto de esta clave que financia al contrato.
     * SUM de todos los montos = contrato.monto_total
     */
    @Column(name = "monto_asignado", precision = 15, scale = 2)
    private BigDecimal montoAsignado;
}