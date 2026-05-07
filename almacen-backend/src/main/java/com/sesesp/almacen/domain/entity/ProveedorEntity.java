package com.sesesp.almacen.domain.entity;

import com.sesesp.almacen.common.entity.AuditoriaEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "proveedor")
@Data
@EqualsAndHashCode(callSuper = true)
public class ProveedorEntity extends AuditoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_proveedor")
    private Integer idProveedor;

    @Column(name = "razon_social")
    private String razonSocial;

    @Column(name = "representante_empresa")
    private String representanteEmpresa;

    @Column(name = "caracter_representante")
    private String caracterRepresentante;

    @Column(name = "telefono")
    private String telefono;

    @Column(name = "correo")
    private String correo;

    @Column(name = "direccion")
    private String direccion;

    // getters and setter auto-generated
}
