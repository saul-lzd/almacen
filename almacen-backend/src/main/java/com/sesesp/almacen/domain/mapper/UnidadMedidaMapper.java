package com.sesesp.almacen.domain.mapper;

import com.sesesp.almacen.common.dto.CatalogOptionDto;
import com.sesesp.almacen.domain.entity.UnidadMedidaEntity;
import org.springframework.stereotype.Component;

@Component
public class UnidadMedidaMapper {

    public CatalogOptionDto toOption(UnidadMedidaEntity entity) {
        CatalogOptionDto dto = new CatalogOptionDto();
        dto.setValue(String.valueOf(entity.getIdUnidadMedida()));
        dto.setLabel(entity.getNombre());
        return dto;
    }
}
