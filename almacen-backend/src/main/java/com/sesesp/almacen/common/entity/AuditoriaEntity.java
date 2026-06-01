package com.sesesp.almacen.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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
            usuarioCreacion = getCurrentUserId();
        }
        fechaCreacion = LocalDateTime.now();
        activo = true;
    }

    @PreUpdate
    protected void onUpdate() {
        usuarioModificacion = getCurrentUserId();
        fechaModificacion = LocalDateTime.now();
    }

    private static Integer getCurrentUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getDetails() instanceof Integer id) {
                return id;
            }
        } catch (Exception ignored) {}
        return 1;
    }
}
