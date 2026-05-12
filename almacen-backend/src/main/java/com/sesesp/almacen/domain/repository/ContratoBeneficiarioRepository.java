package com.sesesp.almacen.domain.repository;

import com.sesesp.almacen.domain.entity.ContratoBeneficiarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContratoBeneficiarioRepository extends JpaRepository<ContratoBeneficiarioEntity, Integer> {

}
