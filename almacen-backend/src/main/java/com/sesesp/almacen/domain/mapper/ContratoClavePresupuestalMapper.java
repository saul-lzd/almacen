package com.sesesp.almacen.domain.mapper;

import com.sesesp.almacen.domain.dto.ClavePresupuestalDto;
import com.sesesp.almacen.domain.entity.ClavePresupuestalEntity;
import com.sesesp.almacen.domain.entity.ContratoClavePresupuestalEntity;
import com.sesesp.almacen.domain.entity.ContratoEntity;
import org.springframework.stereotype.Component;

@Component
public class ContratoClavePresupuestalMapper {

    public ContratoClavePresupuestalEntity toEntity(
            ContratoEntity contrato,
            ClavePresupuestalEntity clavePresupuestal,
            Double montoAsignado) {

        ContratoClavePresupuestalEntity entity = new ContratoClavePresupuestalEntity();

        entity.setContrato(contrato);
        entity.setClavePresupuestal(clavePresupuestal);
        entity.setMontoAsignado(montoAsignado);

        return entity;
    }
}