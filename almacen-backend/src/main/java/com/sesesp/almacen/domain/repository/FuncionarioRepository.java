package com.sesesp.almacen.domain.repository;

import com.sesesp.almacen.domain.entity.FuncionarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FuncionarioRepository extends JpaRepository<FuncionarioEntity, Integer> {

    /**
     * Lista los funcionarios activos para los dropdowns de comprador
     * y administrador del contrato en la UI.
     */
    List<FuncionarioEntity> findByActivoTrue();
}
