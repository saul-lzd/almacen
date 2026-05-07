package com.sesesp.almacen.domain.repository;

import com.sesesp.almacen.domain.entity.ClavePresupuestalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClavePresupuestalRepository extends JpaRepository<ClavePresupuestalEntity, Integer> {

    Optional<ClavePresupuestalEntity> findByClavePresupuestal(String clavePresupuestal);
    List<ClavePresupuestalEntity> findByActivoTrue();
}
