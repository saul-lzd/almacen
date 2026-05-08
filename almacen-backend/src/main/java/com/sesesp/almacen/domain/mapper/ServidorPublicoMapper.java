package com.sesesp.almacen.domain.mapper;

import com.sesesp.almacen.domain.dto.ServidorPublicoDto;
import com.sesesp.almacen.domain.entity.ServidorPublicoEntity;
import org.springframework.stereotype.Component;

@Component
public class ServidorPublicoMapper {

    public ServidorPublicoEntity toEntity(ServidorPublicoDto servidorPublico) {
        if (servidorPublico == null) {
            return null;
        }
        ServidorPublicoEntity entity = new ServidorPublicoEntity();
        entity.setDependencia(servidorPublico.getDependencia());
        entity.setNombre(servidorPublico.getNombre());
        entity.setCargo(servidorPublico.getCargo());
        return entity;
    }

    public ServidorPublicoDto toResponse(ServidorPublicoEntity entity) {
        if (entity == null) {
            return null;
        }
        ServidorPublicoDto servidorPublicoDto = new ServidorPublicoDto();
        servidorPublicoDto.setDependencia(entity.getDependencia());
        servidorPublicoDto.setNombre(entity.getNombre());
        servidorPublicoDto.setCargo(entity.getCargo());
        return servidorPublicoDto;
    }

}
