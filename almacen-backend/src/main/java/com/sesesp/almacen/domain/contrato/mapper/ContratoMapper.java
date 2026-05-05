package com.sesesp.almacen.domain.contrato.mapper;

import com.sesesp.almacen.domain.contrato.dto.ContratoRequest;
import com.sesesp.almacen.domain.contrato.entity.Contrato;
import com.sesesp.almacen.domain.contrato.dto.ContratoResponse;

import java.time.LocalDateTime;

public class ContratoMapper {

    private ContratoMapper() {
    }


    public static Contrato toEntity(ContratoRequest request) {
        Contrato contrato = new Contrato();

        contrato.setIdentificadorContrato(request.getIdentificadorContrato());
        contrato.setIdProveedor(request.getIdProveedor());
        contrato.setIdComprador(request.getIdComprador());
        contrato.setIdAdministradorContrato(request.getIdAdministradorContrato());
        contrato.setIdTipoOrigenContrato(request.getIdTipoOrigenContrato());

        contrato.setFolioOrigen(request.getFolioOrigen());
        contrato.setFechaOrigen(request.getFechaOrigen());

        contrato.setMontoSinImpuestos(request.getMontoSinImpuestos());
        contrato.setImpuestos(request.getImpuestos());
        contrato.setMontoTotal(request.getMontoTotal());

        contrato.setTieneAnticipo(request.getTieneAnticipo());
        contrato.setPorcentajeAnticipo(request.getPorcentajeAnticipo());
        contrato.setMontoAnticipo(request.getMontoAnticipo());

        contrato.setNumeroExhibiciones(request.getNumeroExhibiciones());

        contrato.setTieneFiniquito(request.getTieneFiniquito());
        contrato.setPorcentajeFiniquito(request.getPorcentajeFiniquito());
        contrato.setMontoFiniquito(request.getMontoFiniquito());

        contrato.setFechaTentativaLlegada(request.getFechaTentativaLlegada());
        contrato.setIdEstatusContrato(request.getIdEstatusContrato());

        contrato.setFechaCreacion(LocalDateTime.now());
        contrato.setActivo(true);

        return contrato;
    }


    /**
     * Convierte un objeto de la capa de Base de Datos a un objeto de la Capa de servicio
     * @param contrato
     * @return
     */
    public static ContratoResponse toResponse(Contrato contrato) {
        ContratoResponse dto = new ContratoResponse();

        dto.setIdContrato(contrato.getIdContrato());
        dto.setIdentificadorContrato(contrato.getIdentificadorContrato());

        dto.setIdProveedor(contrato.getIdProveedor());
        dto.setIdComprador(contrato.getIdComprador());
        dto.setIdAdministradorContrato(contrato.getIdAdministradorContrato());
        dto.setIdTipoOrigenContrato(contrato.getIdTipoOrigenContrato());

        dto.setFolioOrigen(contrato.getFolioOrigen());
        dto.setFechaOrigen(contrato.getFechaOrigen());

        dto.setMontoSinImpuestos(contrato.getMontoSinImpuestos());
        dto.setImpuestos(contrato.getImpuestos());
        dto.setMontoTotal(contrato.getMontoTotal());

        dto.setTieneAnticipo(contrato.getTieneAnticipo());
        dto.setPorcentajeAnticipo(contrato.getPorcentajeAnticipo());
        dto.setMontoAnticipo(contrato.getMontoAnticipo());

        dto.setNumeroExhibiciones(contrato.getNumeroExhibiciones());

        dto.setTieneFiniquito(contrato.getTieneFiniquito());
        dto.setPorcentajeFiniquito(contrato.getPorcentajeFiniquito());
        dto.setMontoFiniquito(contrato.getMontoFiniquito());

        dto.setFechaTentativaLlegada(contrato.getFechaTentativaLlegada());
        dto.setIdEstatusContrato(contrato.getIdEstatusContrato());

        return dto;
    }
}