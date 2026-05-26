package com.sesesp.almacen.domain.repository;

import com.sesesp.almacen.domain.entity.RecepcionAlmacenBienEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface RecepcionAlmacenBienRepository extends JpaRepository<RecepcionAlmacenBienEntity, Integer> {

    /**
     * Suma las cantidades recibidas por bien para un contrato, considerando solo
     * recepciones activas. Retorna pares [idContratoBien, totalRecibido].
     */
    @Query("SELECT b.contratoBien.idContratoBien, SUM(b.cantidadRecibida) " +
           "FROM RecepcionAlmacenBienEntity b " +
           "WHERE b.contratoBien.contrato.idContrato = :idContrato " +
           "AND b.recepcionAlmacen.activo = true " +
           "GROUP BY b.contratoBien.idContratoBien")
    List<Object[]> sumCantidadRecibidaByContrato(@Param("idContrato") Integer idContrato);
}