package com.sesesp.almacen.domain.mapper;

import com.sesesp.almacen.domain.dto.ProveedorContratoDto;
import com.sesesp.almacen.domain.entity.ProveedorEntity;
import org.springframework.stereotype.Component;

@Component
public class ProveedorMapper {

    public ProveedorEntity toEntity(ProveedorContratoDto dto) {
        if (dto == null) {
            return null;
        }

        ProveedorEntity entity = new ProveedorEntity();
        entity.setRazonSocial(dto.getRazonSocial());
        entity.setDomicilioFiscal(dto.getDomicilioFiscal());
        entity.setRepresentante(dto.getRepresentante());
        entity.setCaracter(dto.getCaracter());
        return entity;
    }

    public ProveedorContratoDto toResponse(ProveedorEntity entity) {
        if (entity == null) {
            return null;
        }

        ProveedorContratoDto response = new ProveedorContratoDto();
        response.setRazonSocial(entity.getRazonSocial());
        response.setDomicilioFiscal(entity.getDomicilioFiscal());
        response.setRepresentante(entity.getRepresentante());
        response.setCaracter(entity.getCaracter());
        return response;
    }

}


