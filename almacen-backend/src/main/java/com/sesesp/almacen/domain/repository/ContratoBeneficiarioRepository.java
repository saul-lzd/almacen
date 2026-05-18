package com.sesesp.almacen.domain.repository;

import com.sesesp.almacen.domain.entity.ContratoBeneficiarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContratoBeneficiarioRepository extends JpaRepository<ContratoBeneficiarioEntity, Integer> {
    // Las operaciones de beneficiarios del contrato se manejan
    // via cascade desde ContratoEntity — no se necesitan métodos adicionales en v1
}