package com.sesesp.almacen.domain.mapper;

import com.sesesp.almacen.domain.dto.*;
import com.sesesp.almacen.domain.entity.ContratoBeneficiarioEntity;
import com.sesesp.almacen.domain.entity.ContratoClavePresupuestalEntity;
import com.sesesp.almacen.domain.entity.ContratoEntity;
import com.sesesp.almacen.domain.entity.ProductoEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ContratoMapper {

    private final ProveedorMapper proveedorMapper;
    private final ServidorPublicoMapper servidorPublicoMapper;

    public ContratoMapper(ProveedorMapper proveedorMapper,
                          ServidorPublicoMapper servidorPublicoMapper) {
        this.proveedorMapper = proveedorMapper;
        this.servidorPublicoMapper = servidorPublicoMapper;
    }

    public ContratoEntity toEntity (ContratoCreateRequestDto dto) {
        ContratoEntity entity = new ContratoEntity();
        entity.setIdentificadorContrato(dto.getNumeroContrato());
        entity.setAdquisicion(dto.getAdquisicion());
        entity.setFolioOrigen(dto.getFolioOrigen());
        entity.setFechaOrigen(dto.getFechaOrigen());
        entity.setFechaTentativaLlegada(dto.getFechaTentativaLlegada());
        mapDetallesPago(entity, dto.getDetallesPago());
        return entity;
    }

    public ContratoCreateResponseDto toResponse(ContratoEntity contratoEntity) {

        ContratoCreateResponseDto contratoResponse = new ContratoCreateResponseDto();

        contratoResponse.setIdContrato(contratoEntity.getIdContrato());
        contratoResponse.setNumeroContrato(contratoEntity.getIdentificadorContrato());
        contratoResponse.setAdquisicion(contratoEntity.getAdquisicion());
        contratoResponse.setDetallesPago(mapDetallesPagoResponse(contratoEntity));


        if (contratoEntity.getEstatusContrato() != null) {
            contratoResponse.setEstatusContrato(contratoEntity.getEstatusContrato().getDescripcion());
        }

        if (contratoEntity.getProveedor() != null) {
            ProveedorContratoDto proveedorContrato = proveedorMapper.toResponse(contratoEntity.getProveedor());
            contratoResponse.setProveedor(proveedorContrato);
        }

        if (contratoEntity.getComprador() != null) {
            ServidorPublicoDto comprador = servidorPublicoMapper.toResponse(contratoEntity.getComprador());
            contratoResponse.setComprador(comprador);
        }

        if (contratoEntity.getAdministradorContrato() != null) {
            ServidorPublicoDto administradorContrato = servidorPublicoMapper.toResponse(contratoEntity.getAdministradorContrato());
            contratoResponse.setAdministradorContrato(administradorContrato);
        }

        if (contratoEntity.getClavesPresupuestales() != null) {
            List<ClavePresupuestalDto> claves = contratoEntity.getClavesPresupuestales()
                    .stream()
                    .map(this::mapClave)
                    .toList();
            contratoResponse.setClavesPresupuestales(claves);
        }

        if(contratoEntity.getBeneficiarios() != null) {
            List<BeneficiarioDto> beneficiarios = contratoEntity.getBeneficiarios()
                    .stream()
                    .map(this::mapBeneficiario)
                    .toList();
            contratoResponse.setBeneficiarios(beneficiarios);
        }

        if(contratoEntity.getProductos() != null) {
            List<ProductoDto> productos = contratoEntity.getProductos()
                    .stream()
                    .map(this::mapProducto)
                    .toList();
            contratoResponse.setProductos(productos);
        }

        return contratoResponse;
    }

    private ClavePresupuestalDto mapClave(ContratoClavePresupuestalEntity entity) {
        return ClavePresupuestalDto.builder()
                .clavePresupuestal(entity.getClavePresupuestal().getClavePresupuestal())
                .partidaEspecifica(entity.getClavePresupuestal().getPartidaEspecifica())
                .montoAsignado(entity.getMontoAsignado())
                .build();
    }

    private BeneficiarioDto mapBeneficiario(ContratoBeneficiarioEntity entity) {
        return BeneficiarioDto.builder()
                .nombre(entity.getBeneficiario().getNombre())
                .direccion(entity.getBeneficiario().getDireccion())
                .contacto(entity.getBeneficiario().getContacto())
                .build();
    }

    private ProductoDto mapProducto(ProductoEntity entity) {
        return ProductoDto.builder()
                .lote(entity.getLote())
                .partida(entity.getPartida())
                .descripcionTecnica(entity.getDescripcionTecnica())
                .codigoUnidadMedida(entity.getUnidadMedida().getClave())
                .cantidad(entity.getCantidad())
                .precioUnitario(entity.getPrecioUnitario())
                .subtotal(entity.getSubtotal())
                .build();
    }

    public void mapDetallesPago(ContratoEntity entity, DetallesPagoDto detallesPago) {
        if (detallesPago == null) {
            return;
        }
        entity.setMontoSinImpuestos(detallesPago.getMontoSinImpuestos());
        entity.setImpuestos(detallesPago.getImpuestos());
        entity.setMontoTotal(detallesPago.getMontoTotal());
        entity.setTieneAnticipo(detallesPago.getTieneAnticipo());
        entity.setPorcentajeAnticipo(detallesPago.getPorcentajeAnticipo());
        entity.setMontoAnticipo(detallesPago.getMontoAnticipo());
        entity.setNumeroExhibiciones(detallesPago.getNumeroExhibiciones());
        entity.setTieneFiniquito(detallesPago.getTieneFiniquito());
        entity.setPorcentajeFiniquito(detallesPago.getPorcentajeFiniquito());
        entity.setMontoFiniquito(detallesPago.getMontoFiniquito());
    }


    private DetallesPagoDto mapDetallesPagoResponse(ContratoEntity contratoEntity) {
        DetallesPagoDto detallesPago = new DetallesPagoDto();
        detallesPago.setMontoSinImpuestos(contratoEntity.getMontoSinImpuestos());
        detallesPago.setImpuestos(contratoEntity.getImpuestos());
        detallesPago.setMontoTotal(contratoEntity.getMontoTotal());
        detallesPago.setTieneAnticipo(contratoEntity.getTieneAnticipo());
        detallesPago.setPorcentajeAnticipo(contratoEntity.getPorcentajeAnticipo());
        detallesPago.setMontoAnticipo(contratoEntity.getMontoAnticipo());
        detallesPago.setNumeroExhibiciones(contratoEntity.getNumeroExhibiciones());
        detallesPago.setTieneFiniquito(contratoEntity.getTieneFiniquito());
        detallesPago.setPorcentajeFiniquito(contratoEntity.getPorcentajeFiniquito());
        detallesPago.setMontoFiniquito(contratoEntity.getMontoFiniquito());
        return detallesPago;
    }


}