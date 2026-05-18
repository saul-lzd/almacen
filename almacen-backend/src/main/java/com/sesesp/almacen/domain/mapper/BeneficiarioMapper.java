package com.sesesp.almacen.domain.mapper;

import com.sesesp.almacen.domain.dto.BeneficiarioDto;
import com.sesesp.almacen.domain.entity.BeneficiarioEntity;
import com.sesesp.almacen.domain.entity.ContratoBeneficiarioEntity;
import org.springframework.stereotype.Component;

@Component
public class BeneficiarioMapper {

    public BeneficiarioEntity toEntity(BeneficiarioDto dto) {
        return BeneficiarioEntity.builder()
                .nombre(dto.getNombre())
                .build();
    }

    public BeneficiarioEntity toEntity(String nombreBeneficiario) {
        return BeneficiarioEntity.builder()
                .nombre(nombreBeneficiario)
                .build();
    }


//    public BeneficiarioDto toResponse(BeneficiarioEntity entity) {
//        return BeneficiarioDto.builder()
//                .nombre(entity.getNombre())
//                .contacto(entity.getContacto())
//                .direccion(entity.getDireccion())
//                .build();
//    }
//
//    private BeneficiarioDto mapBeneficiario(ContratoBeneficiarioEntity entity) {
//        return BeneficiarioDto.builder()
//                .nombre(entity.getBeneficiario().getNombre())
//                .contacto(entity.getBeneficiario().getContacto())
//                .direccion(entity.getBeneficiario().getDireccion())
//                .build();
//    }

}
