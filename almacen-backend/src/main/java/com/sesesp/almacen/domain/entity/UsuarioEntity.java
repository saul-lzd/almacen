package com.sesesp.almacen.domain.entity;

import com.sesesp.almacen.common.entity.AuditoriaEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Usuarios del sistema.
 * El rol define qué puede hacer cada usuario:
 *   SISTEMA       — acciones automatizadas internas
 *   ADMINISTRADOR — autoriza entregas, supervisa el proceso
 *   ALMACEN       — registra recepciones, procesa bienes, registra entregas
 *   CAPTURA       — captura y edita contratos
 */
@Entity
@Table(name = "usuario")
@Getter
@Setter
public class UsuarioEntity extends AuditoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "correo", length = 150)
    private String correo;

    @Column(name = "usuario_sistema", nullable = false)
    private Boolean usuarioSistema = false;

    /**
     * Rol del usuario dentro del sistema.
     * Valores válidos: SISTEMA, ADMINISTRADOR, ALMACEN, CAPTURA
     */
    @Column(name = "rol", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private RolUsuario rol = RolUsuario.CAPTURA;

    public enum RolUsuario {
        SISTEMA, ADMINISTRADOR, ALMACEN, CAPTURA
    }
}
