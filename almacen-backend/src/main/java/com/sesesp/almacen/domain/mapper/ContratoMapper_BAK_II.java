//package com.sesesp.almacen.domain.mapper;
//
//
//import com.sesesp.almacen.domain.dto.ContratoCreateRequestDto;
//import com.sesesp.almacen.domain.dto.ContratoCreateResponseDto;
//import com.sesesp.almacen.domain.entity.BeneficiarioEntity;
//import com.sesesp.almacen.domain.entity.ContratoBeneficiarioEntity;
//import com.sesesp.almacen.domain.entity.ContratoEntity;
//import org.mapstruct.Mapper;
//import org.mapstruct.Mapping;
//import org.mapstruct.MappingTarget;
//import org.mapstruct.ReportingPolicy;
//
//import java.util.List;
//import java.util.Objects;
//import java.util.stream.Collectors;
//
//@Mapper (
//        componentModel = "spring",
//        unmappedTargetPolicy = ReportingPolicy.IGNORE,
//        uses = {
//                ProveedorMapper.class,
//                FuncionarioMapper.class,
//                ClavePresupuestalMapper.class,
//                BeneficiarioMapper.class }
//)
//public interface ContratoMapper_BAK_II {
//
//    @Mapping(target = "identificadorContrato", source = "numeroContrato")
//    @Mapping(target = "estatusContrato", ignore = true)
//    @Mapping(target = "proveedor", ignore = true)
//    @Mapping(target = "comprador", ignore = true)
//    @Mapping(target = "administradorContrato", ignore = true)
//    @Mapping(target = "clavesPresupuestales", ignore = true)
//    @Mapping(target = "beneficiarios", ignore = true)
//    @Mapping(target = "productos", ignore = true)
//    ContratoEntity toEntity(ContratoCreateRequestDto dto);
//
//
//    @Mapping(target = "numeroContrato", source = "identificadorContrato")
//    @Mapping(target = "beneficiarios", expression = "java(mapBeneficiariosToString(entity.getBeneficiarios()))")
//    @Mapping(target = "estatusContrato", source = "entity.estatusContrato.descripcion")
//    ContratoCreateResponseDto toResponse(ContratoEntity entity);
//
//
////    @Mapping(target = "clavePresupuestal", source = "entity.clavePresupuestal.clavePresupuestal")
////    @Mapping(target = "partidaEspecifica", source = "entity.clavePresupuestal.partidaEspecifica")
////    @Mapping(target = "montoAsignado", source = "entity.montoAsignado")
////    ClavePresupuestalDto toClaveDto(ContratoClavePresupuestalEntity entity);
//
//
//    @Mapping(target = "idContrato", ignore = true)
//    @Mapping(target = "identificadorContrato", source = "numeroContrato")
//    @Mapping(target = "estatusContrato", ignore = true)
//    @Mapping(target = "clavesPresupuestales", ignore = true)
//    @Mapping(target = "productos", ignore = true)
//    @Mapping(target = "beneficiarios", ignore = true)
//    void updateEntityFromDto(ContratoCreateRequestDto dto, @MappingTarget ContratoEntity entity);
//
//
//
//    // --- HELPERS ---
//
//    default String mapBeneficiariosToString(List<ContratoBeneficiarioEntity> beneficiarios) {
//        if (beneficiarios == null || beneficiarios.isEmpty()) {
//            return "";
//        }
//
//        return beneficiarios
//                .stream()
//                .filter(cb -> Boolean.TRUE.equals(cb.getActivo()))
//                .map(ContratoBeneficiarioEntity::getBeneficiario)
//                .filter(Objects::nonNull)
//                .map(BeneficiarioEntity::getNombre)
//                .collect(Collectors.joining(", "));
//    }
//
//}
