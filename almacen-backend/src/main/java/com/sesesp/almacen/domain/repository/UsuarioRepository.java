package com.sesesp.almacen.domain.repository;

import com.sesesp.almacen.domain.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Integer> {
}