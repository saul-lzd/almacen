package com.sesesp.almacen.domain.repository;

import com.sesesp.almacen.domain.entity.AlmacenBienEntity;
import com.sesesp.almacen.domain.entity.AlmacenBienEntity.EstatusBien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface AlmacenBienRepository extends JpaRepository<AlmacenBienEntity, Integer> {

    List<AlmacenBienEntity> findByContratoIdContratoAndEstatusAndActivoTrue(
            Integer idContrato, EstatusBien estatus);

    List<AlmacenBienEntity> findByContratoIdContratoAndActivoTrue(Integer idContrato);

    long countByContratoIdContratoAndEstatusAndActivoTrue(Integer idContrato, EstatusBien estatus);

    List<AlmacenBienEntity> findByIdAlmacenBienInAndActivoTrue(List<Integer> ids);

    /** Para validar unicidad global de número de serie antes de guardar. */
    List<AlmacenBienEntity> findByNumeroSerieInAndActivoTrue(Collection<String> numerosSerie);

    /**
     * Para validar unicidad global de número de motor — trae también el contrato
     * para poder informar en qué contrato ya está registrado cada duplicado.
     */
    @Query("SELECT ab FROM AlmacenBienEntity ab " +
           "JOIN FETCH ab.contrato " +
           "WHERE ab.numeroMotor IN :numerosMotor AND ab.activo = true")
    List<AlmacenBienEntity> findByNumeroMotorInWithContrato(@Param("numerosMotor") Collection<String> numerosMotor);

    /**
     * Carga bienes de un contrato con JOIN FETCH para evitar N+1:
     * resuelve contratoBien→unidadMedida y recepcionAlmacenBien→recepcionAlmacen en una sola query.
     */
    /**
     * Devuelve [idContrato, estatus, count] para todos los contratos indicados en una sola query.
     * Usado para construir el resumenBienes del listado sin N+1.
     */
    @Query("SELECT ab.contrato.idContrato, ab.estatus, COUNT(ab) " +
           "FROM AlmacenBienEntity ab " +
           "WHERE ab.contrato.idContrato IN :ids AND ab.activo = true " +
           "GROUP BY ab.contrato.idContrato, ab.estatus")
    List<Object[]> countByContratosGroupByEstatus(@Param("ids") List<Integer> ids);

    /** Total de bienes activos de una recepción (sin importar estatus). */
    long countByRecepcionAlmacenBienRecepcionAlmacenIdRecepcionAlmacenAndActivoTrue(
            Integer idRecepcionAlmacen);

    /** Bienes de una recepción que están en alguno de los estatus indicados. */
    long countByRecepcionAlmacenBienRecepcionAlmacenIdRecepcionAlmacenAndEstatusInAndActivoTrue(
            Integer idRecepcionAlmacen, Collection<EstatusBien> estatuses);

    /** Todos los bienes activos de un contrato (para verificar cierre). */
    long countByContratoIdContratoAndActivoTrue(Integer idContrato);

    /** Bienes activos de un conjunto de recepciones, filtrados por estatus. */
    @Query("SELECT ab FROM AlmacenBienEntity ab " +
           "WHERE ab.recepcionAlmacenBien.recepcionAlmacen.idRecepcionAlmacen IN :idRecepciones " +
           "AND ab.estatus = :estatus " +
           "AND ab.activo = true")
    List<AlmacenBienEntity> findByRecepcionesAndEstatus(
            @Param("idRecepciones") List<Integer> idRecepciones,
            @Param("estatus") EstatusBien estatus);

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

    @Query("SELECT ab FROM AlmacenBienEntity ab " +
           "JOIN FETCH ab.contratoBien cb " +
           "JOIN FETCH cb.unidadMedida " +
           "JOIN FETCH ab.recepcionAlmacenBien rab " +
           "JOIN FETCH rab.recepcionAlmacen ra " +
           "WHERE ab.contrato.idContrato = :idContrato " +
           "AND ra.idRecepcionAlmacen = :idRecepcion " +
           "AND ab.activo = true " +
           "AND ab.estatus IN :estatuses")
    List<AlmacenBienEntity> findByContratoAndRecepcionWithFetch(
            @Param("idContrato") Integer idContrato,
            @Param("idRecepcion") Integer idRecepcion,
            @Param("estatuses") List<EstatusBien> estatuses);

    /** Todos los bienes de una recepción sin filtro de estatus (para vista de detalle). */
    @Query("SELECT ab FROM AlmacenBienEntity ab " +
           "JOIN FETCH ab.contratoBien cb " +
           "JOIN FETCH cb.unidadMedida " +
           "JOIN FETCH ab.recepcionAlmacenBien rab " +
           "JOIN FETCH rab.recepcionAlmacen ra " +
           "WHERE ab.contrato.idContrato = :idContrato " +
           "AND ra.idRecepcionAlmacen = :idRecepcion " +
           "AND ab.activo = true")
    List<AlmacenBienEntity> findAllByContratoAndRecepcion(
            @Param("idContrato") Integer idContrato,
            @Param("idRecepcion") Integer idRecepcion);
}