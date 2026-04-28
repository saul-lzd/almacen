package com.sesesp.almacen.contrato.repository;

import com.sesesp.almacen.contrato.entity.Contrato;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContratoRepository extends JpaRepository<Contrato, Long> {

    List<Contrato> findByActivoTrue();

    Optional<Contrato> findByIdContratoAndActivoTrue(Long id);

    Contrato save(Contrato contrato);


}