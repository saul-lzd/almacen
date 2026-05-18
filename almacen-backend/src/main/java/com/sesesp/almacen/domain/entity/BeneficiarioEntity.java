package com.sesesp.almacen.domain.entity;

import com.sesesp.almacen.common.entity.AuditoriaEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Municipio o dependencia de gobierno que recibe los bienes.
 * Funciona como catálogo: se busca por nombre antes de crear uno nuevo.
 *
 * La persona que físicamente recibe en el momento de la entrega
 * se registra en SalidaAlmacenEntity.nombreRecibeBeneficiario,
 * no aquí — puede ser diferente en cada entrega.
 *
 * tabla beneficiario
 *   solo nombre
 */
@Entity
@Table(name = "beneficiario")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BeneficiarioEntity extends AuditoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_beneficiario")
    private Integer idBeneficiario;

    @Column(name = "nombre", nullable = false, length = 255)
    private String nombre;
}
