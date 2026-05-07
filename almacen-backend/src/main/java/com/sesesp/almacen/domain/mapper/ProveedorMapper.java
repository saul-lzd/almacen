package com.sesesp.almacen.domain.mapper;

import com.sesesp.almacen.domain.dto.ContratoCreateRequestDto;
import com.sesesp.almacen.domain.dto.ProveedorContratoDto;
import com.sesesp.almacen.domain.entity.ProveedorEntity;
import org.springframework.stereotype.Component;

@Component
public class ProveedorMapper {

    public ProveedorEntity toEntity(ProveedorContratoDto dto) {
        ProveedorEntity entity = new ProveedorEntity();
        entity.setRazonSocial(dto.getRazonSocial());
        entity.setDireccion(dto.getDireccion());
        entity.setRepresentanteEmpresa(dto.getRepresentanteEmpresa());
        entity.setCaracterRepresentante(dto.getCaracterRepresentante());
        return entity;
    }

    public ProveedorContratoDto toResponse(ProveedorEntity entity) {
        ProveedorContratoDto response = new ProveedorContratoDto();
        response.setRazonSocial(entity.getRazonSocial());
        response.setDireccion(entity.getDireccion());
        response.setRepresentanteEmpresa(entity.getRepresentanteEmpresa());
        response.setCaracterRepresentante(entity.getCaracterRepresentante());
        return response;
    }

}
