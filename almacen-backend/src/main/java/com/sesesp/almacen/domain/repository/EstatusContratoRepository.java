package com.sesesp.almacen.domain.repository;

import com.sesesp.almacen.domain.entity.EstatusContratoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstatusContratoRepository extends JpaRepository<EstatusContratoEntity, Integer> {

    Optional<EstatusContratoEntity> findByClave(String clave);
}
