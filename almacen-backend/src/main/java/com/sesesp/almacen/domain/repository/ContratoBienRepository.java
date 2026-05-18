package com.sesesp.almacen.domain.repository;

import com.sesesp.almacen.domain.entity.ContratoBienEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContratoBienRepository extends JpaRepository<ContratoBienEntity, Integer> {
    // Las operaciones de bienes del contrato se manejan
    // via cascade desde ContratoEntity — no se necesitan métodos adicionales en v1
}