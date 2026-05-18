package com.sesesp.almacen.domain.mapper;

import com.sesesp.almacen.domain.dto.ContratoBienDto;
import com.sesesp.almacen.domain.dto.ClavePresupuestalDto;
import com.sesesp.almacen.domain.dto.ContratoDto;
import com.sesesp.almacen.domain.entity.ContratoBeneficiarioEntity;
import com.sesesp.almacen.domain.entity.ContratoBienEntity;
import com.sesesp.almacen.domain.entity.ContratoClavePresupuestalEntity;
import com.sesesp.almacen.domain.entity.ContratoEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ContratoMapper {

    private final ProveedorMapper proveedorMapper;
    private final FuncionarioMapper funcionarioMapper;


    public ContratoDto toResponse(ContratoEntity entity) {

        if (entity == null) return null;

       return ContratoDto.builder()
               // generales
               .idContrato(entity.getIdContrato())
               .numeroContrato(entity.getNumeroContrato())
               .adquisicion(entity.getAdquisicion())
               .estatus(entity.getEstatus().name())

               // detalles de pago
               .montoSinImpuestos(entity.getMontoSinImpuestos())
               .impuestos(entity.getImpuestos())
               .montoTotal(entity.getMontoTotal())

               // interesados
               .proveedor(proveedorMapper.toResponse(entity.getProveedor()))
               .comprador(funcionarioMapper.toResponse(entity.getComprador()))
               .administradorContrato(funcionarioMapper.toResponse(entity.getAdministradorContrato()))

               // relaciones
               .clavesPresupuestales(convertClaves(entity.getClavesPresupuestales()))
               .beneficiarios(joinNames(entity.getBeneficiarios()))
               .bienes(convertBienes(entity.getBienes()))
               .build();
    }


    private String joinNames(List<ContratoBeneficiarioEntity> beneficiarios) {
        return beneficiarios.stream()
                .map(cb -> cb.getBeneficiario().getNombre())
                .collect(Collectors.joining(", "));
    }

    private List<ClavePresupuestalDto> convertClaves(List<ContratoClavePresupuestalEntity> clavesPresupuestales ) {
        return clavesPresupuestales.stream()
                .map(this::toClaveDto)
                .toList();
    }

    private List<ContratoBienDto> convertBienes(List<ContratoBienEntity> bienes) {
        return bienes.stream()
                .map(this::toProductoDto)
                .toList();
    }

    private ClavePresupuestalDto toClaveDto(ContratoClavePresupuestalEntity entity) {
        return ClavePresupuestalDto.builder()
                .clave(entity.getClavePresupuestal().getClave())
                .partidaEspecifica(entity.getClavePresupuestal().getPartidaEspecifica())
                .montoAsignado(entity.getMontoAsignado())
                .build();
    }

    private ContratoBienDto toProductoDto(ContratoBienEntity entity) {
        return ContratoBienDto.builder()
                .lote(entity.getLote())
                .partida(entity.getPartida())
                .descripcionTecnica(entity.getDescripcionTecnica())
                .unidadMedida(entity.getUnidadMedida().getNombre())
                .cantidad(entity.getCantidad())
                .precioUnitario(entity.getPrecioUnitario())
                .subtotal(entity.getSubtotal())
                .build();
    }

}
