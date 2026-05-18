package com.sesesp.almacen.domain.repository;

import com.sesesp.almacen.domain.entity.AlmacenBienEntity;
import com.sesesp.almacen.domain.entity.AlmacenBienEntity.EstatusBien;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlmacenBienRepository extends JpaRepository<AlmacenBienEntity, Integer> {

    /**
     * Lista los bienes de un contrato filtrados por estatus.
     * Usado por el almacén para ver qué bienes están pendientes de procesar,
     * ya procesados, etc.
     */
    List<AlmacenBienEntity> findByContratoIdContratoAndEstatusAndActivoTrue(
            Integer idContrato, EstatusBien estatus);

    /** Lista todos los bienes de un contrato — para la vista completa del contrato */
    List<AlmacenBienEntity> findByContratoIdContratoAndActivoTrue(Integer idContrato);
}