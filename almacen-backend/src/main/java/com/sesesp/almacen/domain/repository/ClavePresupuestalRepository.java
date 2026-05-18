package com.sesesp.almacen.domain.repository;

import com.sesesp.almacen.domain.entity.ClavePresupuestalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClavePresupuestalRepository extends JpaRepository<ClavePresupuestalEntity, Integer> {

    /** Lista todas las claves activas para el dropdown del contrato */
    List<ClavePresupuestalEntity> findByActivoTrue();

    /**
     * Busca una clave por su identificador alfanumérico.
     * Se usa al guardar las claves del contrato para obtener
     * la entidad completa a partir del valor seleccionado en el dropdown.
     */
    Optional<ClavePresupuestalEntity> findByClaveAndActivoTrue(String clave);
}