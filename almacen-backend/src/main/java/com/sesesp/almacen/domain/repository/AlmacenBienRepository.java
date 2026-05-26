package com.sesesp.almacen.domain.repository;

import com.sesesp.almacen.domain.entity.AlmacenBienEntity;
import com.sesesp.almacen.domain.entity.AlmacenBienEntity.EstatusBien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AlmacenBienRepository extends JpaRepository<AlmacenBienEntity, Integer> {

    List<AlmacenBienEntity> findByContratoIdContratoAndEstatusAndActivoTrue(
            Integer idContrato, EstatusBien estatus);

    List<AlmacenBienEntity> findByContratoIdContratoAndActivoTrue(Integer idContrato);

    long countByContratoIdContratoAndEstatusAndActivoTrue(Integer idContrato, EstatusBien estatus);

    List<AlmacenBienEntity> findByIdAlmacenBienInAndActivoTrue(List<Integer> ids);

    /**
     * Carga bienes de un contrato con JOIN FETCH para evitar N+1:
     * resuelve contratoBien→unidadMedida y recepcionAlmacenBien→recepcionAlmacen en una sola query.
     */
    @Query("SELECT ab FROM AlmacenBienEntity ab " +
           "JOIN FETCH ab.contratoBien cb " +
           "JOIN FETCH cb.unidadMedida " +
           "LEFT JOIN FETCH ab.recepcionAlmacenBien rab " +
           "LEFT JOIN FETCH rab.recepcionAlmacen " +
           "WHERE ab.contrato.idContrato = :idContrato " +
           "AND ab.activo = true " +
           "AND ab.estatus IN :estatuses")
    List<AlmacenBienEntity> findByContratoWithFetch(
            @Param("idContrato") Integer idContrato,
            @Param("estatuses") List<EstatusBien> estatuses);
}