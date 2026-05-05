package com.sesesp.almacen.domain.unidadmedida.repository;

import com.sesesp.almacen.domain.unidadmedida.entity.UnidadMedidaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UnidadMedidaRepository extends JpaRepository<UnidadMedidaEntity, Integer> {

    /**
     * @return all items
     */
    List<UnidadMedidaEntity> findByActivoTrue();

    /**
     * @return one item that matchs ID and is Activo
     */
    //Optional<UnidadMedidaEntity> findByIdClavePresupuestalAndActivoTrue(Integer id);

    /**
     * Save the entity
     * @param entity
     * @return
     */
    //UnidadMedidaEntity save(UnidadMedidaEntity entity);

}
