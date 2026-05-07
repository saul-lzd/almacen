package com.sesesp.almacen.domain.mapper;

import com.sesesp.almacen.common.dto.CatalogOptionDto;
import com.sesesp.almacen.domain.dto.ClavePresupuestalDto;
import com.sesesp.almacen.domain.entity.ClavePresupuestalEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ClavePresupuestalMapper {

    public CatalogOptionDto toOption(ClavePresupuestalEntity entity) {

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("partida_especifica", entity.getPartidaEspecifica());

        CatalogOptionDto dto = new CatalogOptionDto();
        dto.setValue(entity.getClavePresupuestal());
        dto.setLabel(entity.getClavePresupuestal());
        dto.setMetadata(metadata);

        return dto;
    }

}
