package com.sesesp.almacen.domain.mapper;

import com.sesesp.almacen.domain.dto.*;
import com.sesesp.almacen.domain.entity.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

//@Component
//public class ContratoMapper_BAK {
//
//    private final ProveedorMapper proveedorMapper;
//    private final FuncionarioMapper servidorPublicoMapper;
//
//    public ContratoMapper_BAK(ProveedorMapper proveedorMapper,
//                              FuncionarioMapper servidorPublicoMapper) {
//        this.proveedorMapper = proveedorMapper;
//        this.servidorPublicoMapper = servidorPublicoMapper;
//    }

    /**
     *
     * @param dto
     * @return
     */
//    public ContratoEntity toEntity(ContratoCreateRequestDto dto) {
//        if (dto == null) return null;
//
//        ContratoEntity entity = new ContratoEntity();
//        entity.setIdContrato(dto.getIdContrato());
//        entity.setIdentificadorContrato(dto.getNumeroContrato());
//        entity.setAdquisicion(dto.getAdquisicion());
//        entity.setFolioOrigen(dto.getFolioOrigen());
//        entity.setFechaOrigen(dto.getFechaOrigen());
//        entity.setFechaTentativaLlegada(dto.getFechaTentativaLlegada());
//
//        // Mapeo de detalles del pago
//        mapDetallesPago(entity, dto.getDetallesPago());
//
//        return entity;
//    }

    /**
     *
     * @param entity
     * @return
     */
//    public ContratoCreateResponseDto toResponse(ContratoEntity entity) {
//        if (entity == null) return null;
//
//        ContratoCreateResponseDto response = new ContratoCreateResponseDto();
//
//        response.setIdContrato(entity.getIdContrato());
//        response.setNumeroContrato(entity.getNumeroContrato());
//        response.setAdquisicion(entity.getAdquisicion());
//
//        // Mapeo de detalles de pago
//        entity.setMontoSinImpuestos(entity.getMontoSinImpuestos());
//        entity.setImpuestos(entity.getImpuestos());
//        entity.setMontoTotal(entity.getMontoTotal());
//
//        // Estatus
//        if (entity.getEstatusContrato() != null) {
//            response.setEstatusContrato(entity.getEstatusContrato().getDescripcion());
//        }
//
//        // Proveedor
//        if (entity.getProveedor() != null) {
//            response.setProveedor(proveedorMapper.toResponse(entity.getProveedor()));
//        }
//
//        // Comprador
//        if (entity.getComprador() != null) {
//            response.setComprador(servidorPublicoMapper.toResponse(entity.getComprador()));
//        }
//
//        // Administrador del contrato
//        if (entity.getAdministradorContrato() != null) {
//            response.setAdministradorContrato(servidorPublicoMapper.toResponse(entity.getAdministradorContrato()));
//        }
//
//        // Claves Presupuestales
//        if (entity.getClavesPresupuestales() != null) {
//            List<ClavePresupuestalDto> claves = entity.getClavesPresupuestales()
//                    .stream()
//                    .map(this::toClaveDto)
//                    .toList();
//            response.setClavesPresupuestales(claves);
//        }
//
//        // Beneficiarios
//        if (entity.getBeneficiarios() != null) {
//            String nombresBeneficiarios = entity.getBeneficiarios()
//                    .stream()
//                    .map(cb -> cb.getBeneficiario().getNombre())
//                    .collect(Collectors.joining(", "));
//            response.setBeneficiarios(nombresBeneficiarios);
//        }
//
//        // Productos
//        if (entity.getProductos() != null) {
//            List<ContratoBienDto> productos = entity.getProductos()
//                    .stream()
//                    .map(this::toProductoDto)
//                    .toList();
//            response.setProductos(productos);
//        }
//
//        return response;
//    }

    // --- MÉTODOS PRIVADOS DE APOYO (HELPERS) ---

//    public void mapDetallesPago(ContratoEntity entity, DetallesPagoDto detallesPago) {
//        if (detallesPago == null) {
//            return;
//        }
//        entity.setMontoSinImpuestos(detallesPago.getMontoSinImpuestos());
//        entity.setImpuestos(detallesPago.getImpuestos());
//        entity.setMontoTotal(detallesPago.getMontoTotal());
//        entity.setTieneAnticipo(detallesPago.getTieneAnticipo());
//        entity.setPorcentajeAnticipo(detallesPago.getPorcentajeAnticipo());
//        entity.setMontoAnticipo(detallesPago.getMontoAnticipo());
//        entity.setNumeroExhibiciones(detallesPago.getNumeroExhibiciones());
//        entity.setTieneFiniquito(detallesPago.getTieneFiniquito());
//        entity.setPorcentajeFiniquito(detallesPago.getPorcentajeFiniquito());
//        entity.setMontoFiniquito(detallesPago.getMontoFiniquito());
//    }

//    private DetallesPagoDto toDetallesPagoDto(ContratoEntity entity) {
//        return DetallesPagoDto.builder()
//                .montoSinImpuestos(entity.getMontoSinImpuestos())
//                .impuestos(entity.getImpuestos())
//                .montoTotal(entity.getMontoTotal())
//                .build();
//    }

//    private ClavePresupuestalDto toClaveDto(ContratoClavePresupuestalEntity entity) {
//        return ClavePresupuestalDto.builder()
//                .clave(entity.getClavePresupuestal().getClave())
//                .partidaEspecifica(entity.getClavePresupuestal().getPartidaEspecifica())
//                .montoAsignado(entity.getMontoAsignado())
//                .build();
//    }
//
//    private ContratoBienDto toProductoDto(ContratoBienEntity entity) {
//        return ContratoBienDto.builder()
//                .lote(entity.getLote())
//                .partida(entity.getPartida())
//                .descripcionTecnica(entity.getDescripcionTecnica())
//                .unidadMedida(entity.getUnidadMedida().getNombre())
//                .cantidad(entity.getCantidad())
//                .precioUnitario(entity.getPrecioUnitario())
//                .subtotal(entity.getSubtotal())
//                .build();
//    }

//}