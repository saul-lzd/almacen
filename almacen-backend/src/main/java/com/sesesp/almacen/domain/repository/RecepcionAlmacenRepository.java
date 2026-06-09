package com.sesesp.almacen.domain.repository;

import com.sesesp.almacen.domain.entity.RecepcionAlmacenEntity;
import com.sesesp.almacen.domain.entity.RecepcionAlmacenEntity.EstatusRecepcion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecepcionAlmacenRepository extends JpaRepository<RecepcionAlmacenEntity, Integer> {

    /** Lista todas las recepciones de un contrato — para ver el historial */
    List<RecepcionAlmacenEntity> findByContratoIdContratoAndActivoTrue(Integer idContrato);

    /** Recepciones de un contrato que están en el estatus indicado */
    List<RecepcionAlmacenEntity> findByContratoIdContratoAndEstatusAndActivoTrue(
            Integer idContrato, EstatusRecepcion estatus);
}