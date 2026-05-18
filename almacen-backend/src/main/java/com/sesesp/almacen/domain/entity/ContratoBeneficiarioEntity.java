package com.sesesp.almacen.domain.entity;

import com.sesesp.almacen.common.entity.AuditoriaEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Relación N:M entre Contrato y Beneficiario.
 * Un contrato puede tener múltiples beneficiarios (municipios/dependencias).
 * Un beneficiario puede aparecer en múltiples contratos.
 *
 * tabla contrato_beneficiario
 */
@Entity
@Table(name = "contrato_beneficiario",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_contrato_beneficiario",
                columnNames = {"id_contrato", "id_beneficiario"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContratoBeneficiarioEntity extends AuditoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contrato_beneficiario")
    private Integer idContratoBeneficiario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_contrato", nullable = false)
    private ContratoEntity contrato;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_beneficiario", nullable = false)
    private BeneficiarioEntity beneficiario;

    @Column(name = "observaciones", length = 500)
    private String observaciones;
}