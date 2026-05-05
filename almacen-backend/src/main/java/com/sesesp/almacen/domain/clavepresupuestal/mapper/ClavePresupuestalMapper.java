package com.sesesp.almacen.domain.clavepresupuestal.mapper;

import com.sesesp.almacen.common.dto.CatalogoOptionDto;
import com.sesesp.almacen.domain.clavepresupuestal.entity.ClavePresupuestalEntity;

import java.util.HashMap;
import java.util.Map;

public class ClavePresupuestalMapper {

    public static CatalogoOptionDto toOption(ClavePresupuestalEntity entity) {

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("partida-especifica", entity.getPartidaEspecifica());

        CatalogoOptionDto dto = new CatalogoOptionDto();
        dto.setValue(entity.getClavePresupuestal());
        dto.setLabel(entity.getClavePresupuestal());
        dto.setMetadata(metadata);

        return dto;
    }


}
