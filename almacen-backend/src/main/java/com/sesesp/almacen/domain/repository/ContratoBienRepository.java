package com.sesesp.almacen.domain.repository;

import com.sesesp.almacen.domain.entity.ContratoBienEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContratoBienRepository extends JpaRepository<ContratoBienEntity, Integer> {

    List<ContratoBienEntity> findByContratoIdContratoAndActivoTrue(Integer idContrato);
}