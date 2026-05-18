package com.sesesp.almacen.domain.repository;

import com.sesesp.almacen.domain.entity.SalidaAlmacenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalidaAlmacenRepository extends JpaRepository<SalidaAlmacenEntity, Integer> {

    /** Lista todas las salidas de un contrato — para ver el historial de entregas */
    List<SalidaAlmacenEntity> findByContratoIdContratoAndActivoTrue(Integer idContrato);
}
