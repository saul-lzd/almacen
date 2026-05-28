package com.sesesp.almacen.domain.entity;

import com.sesesp.almacen.common.entity.AuditoriaEntity;
import com.sesesp.almacen.common.types.TipoFuncionario;
import jakarta.persistence.*;
import lombok.*;

/**
 * Funcionario público que participa en el contrato.
 * Puede ser el comprador (titular de la dependencia) o el
 * administrador del contrato (director operativo).
 *
 * El rol dentro de cada contrato se define en ContratoEntity
 * mediante id_comprador e id_administrador_contrato — no aquí.
 *
 * tabla funcionario
 *   nombre, dependencia, caracter
 */
@Entity
@Table(name = "funcionario")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FuncionarioEntity extends AuditoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_funcionario")
    private Integer idFuncionario;

    /** Nombre completo del funcionario */
    @Column(name = "nombre", nullable = false, length = 255)
    private String nombre;

    /** Departamento gubernamental al que pertenece */
    @Column(name = "dependencia", nullable = false, length = 255)
    private String dependencia;

    /**
     * Puesto o función que lo avala para participar en el contrato.
     * Mismo concepto que en ProveedorEntity.caracter.
     * Ej: "Titular de la Dependencia", "Director Operativo"
     */
    @Column(name = "caracter", length = 150)
    private String caracter;

    /** Rol del funcionario en el sistema: TITULAR (comprador) o ADMINISTRADOR */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_funcionario", nullable = false, length = 20)
    private TipoFuncionario tipoFuncionario;

    /** El funcionario esta activo en su rol*/
    @Column(name = "es_activo_en_rol", nullable = false)
    private Boolean esActivoEnRol;
}
