package com.sesesp.almacen.domain.mapper;


import com.sesesp.almacen.domain.dto.ClavePresupuestalDto;
import com.sesesp.almacen.domain.dto.ContratoCreateRequestDto;
import com.sesesp.almacen.domain.dto.ContratoCreateResponseDto;
import com.sesesp.almacen.domain.entity.ContratoBeneficiarioEntity;
import com.sesesp.almacen.domain.entity.ContratoClavePresupuestalEntity;
import com.sesesp.almacen.domain.entity.ContratoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.stream.Collectors;

@Mapper (
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {
                ProveedorMapper.class,
                ServidorPublicoMapper.class }
)
public interface ContratoMapper {

    @Mapping(target = "identificadorContrato", source = "numeroContrato")
    @Mapping(target = "proveedor", ignore = true)
    @Mapping(target = "comprador", ignore = true)
    @Mapping(target = "administradorContrato", ignore = true)
    @Mapping(target = "estatusContrato", ignore = true)
    @Mapping(target = "clavesPresupuestales", ignore = true)
    @Mapping(target = "productos", ignore = true)
    @Mapping(target = "beneficiarios", ignore = true)
    ContratoEntity toEntity(ContratoCreateRequestDto dto);


    @Mapping(target = "numeroContrato", source = "identificadorContrato")
    @Mapping(target = "beneficiarios", expression = "java(mapBeneficiariosToString(entity.getBeneficiarios()))")
    @Mapping(target = "estatusContrato", source = "entity.estatusContrato.descripcion")
    ContratoCreateResponseDto toResponse(ContratoEntity entity);

    default String mapBeneficiariosToString(List<ContratoBeneficiarioEntity> beneficiarios) {
        if (beneficiarios == null || beneficiarios.isEmpty()) {
            return "";
        }

        return beneficiarios
                .stream()
                .map(cb -> cb.getBeneficiario().getNombre())
                .collect(Collectors.joining(", "));
    }

    @Mapping(target = "clavePresupuestal", source = "entity.clavePresupuestal.clavePresupuestal")
    @Mapping(target = "partidaEspecifica", source = "entity.clavePresupuestal.partidaEspecifica")
    @Mapping(target = "montoAsignado", source = "entity.montoAsignado")
    ClavePresupuestalDto toClaveDto(ContratoClavePresupuestalEntity entity);

}
