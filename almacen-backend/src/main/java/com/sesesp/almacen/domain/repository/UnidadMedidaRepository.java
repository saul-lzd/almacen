package com.sesesp.almacen.domain.repository;

import com.sesesp.almacen.domain.entity.UnidadMedidaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UnidadMedidaRepository extends JpaRepository<UnidadMedidaEntity, Integer> {

    List<UnidadMedidaEntity> findByActivoTrue();
    //Optional<UnidadMedidaEntity> findByIdClavePresupuestalAndActivoTrue(Integer id);
    //UnidadMedidaEntity save(UnidadMedidaEntity entity);

}
