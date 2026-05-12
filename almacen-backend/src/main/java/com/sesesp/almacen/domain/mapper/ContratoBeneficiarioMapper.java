package com.sesesp.almacen.domain.mapper;

import com.sesesp.almacen.domain.entity.BeneficiarioEntity;
import com.sesesp.almacen.domain.entity.ContratoBeneficiarioEntity;
import com.sesesp.almacen.domain.entity.ContratoEntity;
import lombok.Builder;
import org.springframework.stereotype.Component;

@Component
public class ContratoBeneficiarioMapper {

    public ContratoBeneficiarioEntity toEntity(
            ContratoEntity contrato,
            BeneficiarioEntity beneficiario,
            String observaciones ) {

        return ContratoBeneficiarioEntity.builder()
                .contrato(contrato)
                .beneficiario(beneficiario)
                .observaciones(observaciones)
                .build();
    }
}
