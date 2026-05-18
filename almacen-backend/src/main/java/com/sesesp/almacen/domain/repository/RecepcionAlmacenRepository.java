package com.sesesp.almacen.domain.repository;

import com.sesesp.almacen.domain.entity.RecepcionAlmacenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecepcionAlmacenRepository extends JpaRepository<RecepcionAlmacenEntity, Integer> {

    /** Lista todas las recepciones de un contrato — para ver el historial */
    List<RecepcionAlmacenEntity> findByContratoIdContratoAndActivoTrue(Integer idContrato);
}