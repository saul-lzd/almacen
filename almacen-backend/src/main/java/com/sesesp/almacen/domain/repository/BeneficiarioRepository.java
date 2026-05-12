package com.sesesp.almacen.domain.repository;

import com.sesesp.almacen.domain.entity.BeneficiarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BeneficiarioRepository extends JpaRepository<BeneficiarioEntity, Integer> {

    Optional<BeneficiarioEntity> findByNombreAndActivoTrue(String nombreBeneficiario);
}
