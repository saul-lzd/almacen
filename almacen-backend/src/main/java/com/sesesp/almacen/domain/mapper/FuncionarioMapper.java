package com.sesesp.almacen.domain.mapper;

import com.sesesp.almacen.domain.dto.FuncionarioDto;
import com.sesesp.almacen.domain.entity.FuncionarioEntity;
import org.springframework.stereotype.Component;

@Component
public class FuncionarioMapper {

    public FuncionarioEntity toEntity(FuncionarioDto dto) {
        if (dto == null) {
            return null;
        }

        return FuncionarioEntity.builder()
                .nombre(dto.getNombre())
                .dependencia(dto.getDependencia())
                .caracter(dto.getCaracter())
                .build();
    }

    public FuncionarioDto toResponse(FuncionarioEntity entity) {
        if (entity == null) {
            return null;
        }

        return FuncionarioDto.builder()
                .nombre(entity.getNombre())
                .dependencia(entity.getDependencia())
                .caracter(entity.getCaracter())
                .build();
    }

}
