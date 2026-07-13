package com.sesesp.almacen.common.entity;

import com.sesesp.almacen.common.util.SecurityUtils;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

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
        if (usuarioCreacion == null) {
            usuarioCreacion = SecurityUtils.getCurrentUserId();
        }
        fechaCreacion = LocalDateTime.now();
        activo = true;
    }

    @PreUpdate
    protected void onUpdate() {
        usuarioModificacion = SecurityUtils.getCurrentUserId();
        fechaModificacion = LocalDateTime.now();
    }
}
