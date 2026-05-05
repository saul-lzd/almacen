package com.sesesp.almacen.domain.unidadmedida.mapper;

import com.sesesp.almacen.common.dto.CatalogoOptionDto;
import com.sesesp.almacen.domain.unidadmedida.dto.UnidadMedidaResponse;
import com.sesesp.almacen.domain.unidadmedida.entity.UnidadMedidaEntity;

public class UnidadMedidaMapper {

    public static CatalogoOptionDto toOption(UnidadMedidaEntity entity) {
        CatalogoOptionDto dto = new CatalogoOptionDto();
        dto.setValue(entity.getClave());
        dto.setLabel(entity.getDescripcion());
        return dto;
    }
}
