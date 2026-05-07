package com.sesesp.almacen.domain.mapper;

import com.sesesp.almacen.domain.dto.*;
import com.sesesp.almacen.domain.entity.ContratoClavePresupuestalEntity;
import com.sesesp.almacen.domain.entity.ContratoEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ContratoMapper {

    private final ProveedorMapper proveedorMapper;
    private final ServidorPublicoMapper servidorPublicoMapper;
    private final ClavePresupuestalMapper clavePresupuestalMapper;

    public ContratoMapper(
            ProveedorMapper proveedorMapper,
            ServidorPublicoMapper servidorPublicoMapper,
            ClavePresupuestalMapper clavePresupuestalMapper) {

        this.proveedorMapper = proveedorMapper;
        this.servidorPublicoMapper = servidorPublicoMapper;
        this.clavePresupuestalMapper = clavePresupuestalMapper;
    }

    /**
     * Maps all the general attributes even payment.
     * @param dto
     * @return
     */
    public ContratoEntity mapGeneralInformation(ContratoCreateRequestDto dto) {
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
        return contratoResponse;
    }

    private ClavePresupuestalDto mapClave(ContratoClavePresupuestalEntity cc) {
        ClavePresupuestalDto dto = new ClavePresupuestalDto();
        dto.setClavePresupuestal(cc.getClavePresupuestal().getClavePresupuestal());
        dto.setPartidaEspecifica(cc.getClavePresupuestal().getPartidaEspecifica());
        dto.setMontoAsignado(cc.getMontoAsignado());
        return dto;
    }

    private void mapDetallesPago(ContratoEntity entity, DetallesPagoDto detallesPago) {

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