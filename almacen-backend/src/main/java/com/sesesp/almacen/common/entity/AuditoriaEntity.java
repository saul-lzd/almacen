package com.sesesp.almacen.common.entity;


import com.sesesp.almacen.common.types.DefaultUser;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@MappedSuperclass
public abstract class AuditoriaEntity {

    @Column(name = "usuario_creacion", nullable = false)
    private Integer usuarioCreacion;

    @Column(name = "usuario_modificacion")
    private Integer usuarioModificacion;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @PrePersist
    protected void onCreate() {
        usuarioCreacion = DefaultUser.SISTEMA.ID;
        fechaCreacion = LocalDateTime.now();
        activo = true;
    }

    @PreUpdate
    protected void onUpdate() {
        fechaModificacion = LocalDateTime.now();
    }

    // getters and setter auto-generated
}
