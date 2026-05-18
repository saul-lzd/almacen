package com.sesesp.almacen.domain.mapper;

import com.sesesp.almacen.domain.dto.ClavePresupuestalDto;
import com.sesesp.almacen.domain.entity.ClavePresupuestalEntity;
import com.sesesp.almacen.domain.entity.ContratoClavePresupuestalEntity;
import com.sesesp.almacen.domain.entity.ContratoEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ContratoClavePresupuestalMapper {

    public ContratoClavePresupuestalEntity toEntity(
            ContratoEntity contrato,
            ClavePresupuestalEntity clavePresupuestal,
            BigDecimal montoAsignado) {

        return ContratoClavePresupuestalEntity.builder()
                .contrato(contrato)
                .clavePresupuestal(clavePresupuestal)
                .montoAsignado(montoAsignado)
                .build();

    }
}