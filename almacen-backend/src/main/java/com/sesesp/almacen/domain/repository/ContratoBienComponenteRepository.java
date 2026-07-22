package com.sesesp.almacen.domain.repository;

import com.sesesp.almacen.domain.entity.ContratoBienComponenteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContratoBienComponenteRepository extends JpaRepository<ContratoBienComponenteEntity, Integer> {

    List<ContratoBienComponenteEntity> findByContratoBienIdContratoBienAndActivoTrueOrderByOrdenAsc(
            Integer idContratoBien);
}
