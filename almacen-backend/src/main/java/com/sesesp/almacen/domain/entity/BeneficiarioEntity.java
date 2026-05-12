package com.sesesp.almacen.domain.entity;

import com.sesesp.almacen.common.entity.AuditoriaEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "beneficiario")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BeneficiarioEntity extends AuditoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_beneficiario")
    private Integer idBeneficiario;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "contacto")
    private String contacto;

    @Column(name = "direccion")
    private String direccion;

}
