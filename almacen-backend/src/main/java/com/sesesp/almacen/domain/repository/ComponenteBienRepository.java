package com.sesesp.almacen.domain.repository;

import com.sesesp.almacen.domain.entity.ComponenteBienEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ComponenteBienRepository extends JpaRepository<ComponenteBienEntity, Integer> {

    List<ComponenteBienEntity> findByAlmacenBienIdAlmacenBienAndActivoTrue(Integer idAlmacenBien);

    /** Para validar unicidad global de número de serie antes de guardar. */
    List<ComponenteBienEntity> findByNumeroSerieInAndActivoTrue(Collection<String> numerosSerie);
}
