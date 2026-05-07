package com.sesesp.almacen.domain.repository;

import com.sesesp.almacen.domain.entity.ProveedorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProveedorRepository extends JpaRepository<ProveedorEntity, Integer> {

    ProveedorEntity save(ProveedorEntity entity);
}
