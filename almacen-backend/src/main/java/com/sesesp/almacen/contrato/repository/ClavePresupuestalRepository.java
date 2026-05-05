package com.sesesp.almacen.contrato.repository;

import com.sesesp.almacen.contrato.entity.ClavePresupuestalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClavePresupuestalRepository extends JpaRepository<ClavePresupuestalEntity, Integer> {

    /**
     * @return all items
     */
    List<ClavePresupuestalEntity> findByActivoTrue();

    /**
     * @return one item that matchs ID and is Activo
     */
    Optional<ClavePresupuestalEntity> findByIdClavePresupuestalAndActivoTrue(Integer id);

    /**
     * Save the entity
     * @param entity
     * @return
     */
    ClavePresupuestalEntity save(ClavePresupuestalEntity entity);
}
