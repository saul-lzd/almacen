package com.sesesp.almacen.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Clase base para auditoría. Todos los campos de creación y modificación
 * se heredan de aquí. No se expone como tabla — es solo un mapeo compartido.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class AuditoriaEntity {

    @Column(name = "usuario_creacion", nullable = false, updatable = false)
    private Integer usuarioCreacion;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "usuario_modificacion")
    private Integer usuarioModificacion;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @PrePersist
    protected void onCreate() {
        // Usuario sistema por defecto hasta que se implemente autenticación
        if (usuarioCreacion == null) {
            usuarioCreacion = 1;
        }
        fechaCreacion = LocalDateTime.now();
        activo = true;
    }

    @PreUpdate
    protected void onUpdate() {
        fechaModificacion = LocalDateTime.now();
    }
}
