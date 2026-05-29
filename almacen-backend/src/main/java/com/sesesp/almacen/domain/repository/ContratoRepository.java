package com.sesesp.almacen.domain.repository;

import com.sesesp.almacen.domain.entity.ContratoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContratoRepository extends JpaRepository<ContratoEntity, Integer> {

    /** Lista contratos activos filtrados por estatus — usado en las vistas por rol */
    List<ContratoEntity> findByEstatusAndActivoTrue(ContratoEntity.EstatusContrato estatus);

    /** Lista todos los contratos activos */
    List<ContratoEntity> findByActivoTrue();

    /** Validación de número único al crear */
    boolean existsByNumeroContratoAndActivoTrue(String numeroContrato);

    /** Validación de número único al editar — excluye el propio contrato */
    boolean existsByNumeroContratoAndActivoTrueAndIdContratoNot(String numeroContrato, Integer idContrato);
}
