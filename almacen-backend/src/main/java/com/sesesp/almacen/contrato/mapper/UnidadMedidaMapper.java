package com.sesesp.almacen.contrato.mapper;

import com.sesesp.almacen.contrato.dto.UnidadMedidaResponse;
import com.sesesp.almacen.contrato.entity.UnidadMedidaEntity;

public class UnidadMedidaMapper {

    public static UnidadMedidaResponse toResponse(UnidadMedidaEntity entity) {
        UnidadMedidaResponse response = new UnidadMedidaResponse();
        response.setClave(entity.getClave());
        response.setDescripcion(entity.getDescripcion());
        return response;
    }
}
