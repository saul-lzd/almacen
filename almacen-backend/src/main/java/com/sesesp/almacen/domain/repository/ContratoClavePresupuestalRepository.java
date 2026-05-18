package com.sesesp.almacen.domain.repository;

import com.sesesp.almacen.domain.entity.ContratoClavePresupuestalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContratoClavePresupuestalRepository extends JpaRepository<ContratoClavePresupuestalEntity, Integer> {
    // Las operaciones de claves del contrato se manejan
    // via cascade desde ContratoEntity — no se necesitan métodos adicionales en v1
}
