package com.sesesp.almacen.domain.repository;

import com.sesesp.almacen.domain.entity.UnidadMedidaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UnidadMedidaRepository extends JpaRepository<UnidadMedidaEntity, Integer> {

    /** Lista todas las unidades activas para el dropdown de bienes */
    List<UnidadMedidaEntity> findByActivoTrue();

    /**
     * Busca una unidad por nombre exacto.
     * Se usa al guardar bienes del contrato para obtener la entidad
     * a partir del nombre seleccionado en el dropdown.
     */
    Optional<UnidadMedidaEntity> findByNombreAndActivoTrue(String nombre);
}