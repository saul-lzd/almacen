package com.sesesp.almacen.domain.entity;

import com.sesesp.almacen.common.entity.AuditoriaEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Proveedor que realiza la entrega de los bienes al almacén.
 * Los datos se toman directamente del contrato físico.
 *
 * tabla proveedor
 *   razon_social, domicilio_fiscal, representante, caracter
 */
@Entity
@Table(name = "proveedor")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProveedorEntity extends AuditoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_proveedor")
    private Integer idProveedor;

    /** Nombre fiscal de la empresa proveedora */
    @Column(name = "razon_social", nullable = false, length = 255)
    private String razonSocial;

    /** Domicilio fiscal tal como aparece en el contrato */
    @Column(name = "domicilio_fiscal", length = 500)
    private String domicilioFiscal;

    /** Persona con facultades para firmar el contrato */
    @Column(name = "representante", length = 255)
    private String representante;

    /** Puesto o rol que avala al representante (ej: "Apoderado Legal") */
    @Column(name = "caracter", length = 150)
    private String caracter;
}
