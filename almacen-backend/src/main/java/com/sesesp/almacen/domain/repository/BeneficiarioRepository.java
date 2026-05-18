package com.sesesp.almacen.domain.repository;

import com.sesesp.almacen.domain.entity.BeneficiarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BeneficiarioRepository extends JpaRepository<BeneficiarioEntity, Integer> {

    /**
     * Busca un beneficiario por nombre exacto (activo).
     * Se usa en el autocomplete al capturar beneficiarios del contrato:
     * si existe se reutiliza, si no se crea uno nuevo.
     */
    Optional<BeneficiarioEntity> findByNombreAndActivoTrue(String nombre);
}
