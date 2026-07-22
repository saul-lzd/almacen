package com.sesesp.almacen.domain.repository;

import com.sesesp.almacen.domain.entity.CatalogoComponenteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CatalogoComponenteRepository extends JpaRepository<CatalogoComponenteEntity, Integer> {

    List<CatalogoComponenteEntity> findByActivoTrueOrderByNombreAsc();

    Optional<CatalogoComponenteEntity> findByNombreIgnoreCase(String nombre);
}
