package com.sesesp.almacen.domain.entity;

import com.sesesp.almacen.common.entity.AuditoriaEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "contrato_beneficiario")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
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

    @Column(name = "observaciones")
    private String observaciones;

}
