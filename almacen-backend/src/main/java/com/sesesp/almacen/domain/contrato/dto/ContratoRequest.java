package com.sesesp.almacen.domain.contrato.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ContratoRequest {

    private String identificadorContrato;

    private Integer idProveedor;
    private Integer idComprador;
    private Integer idAdministradorContrato;
    private Integer idTipoOrigenContrato;

    private String folioOrigen;
    private LocalDateTime fechaOrigen;

    private Double montoSinImpuestos;
    private Double impuestos;
    private Double montoTotal;

    private Boolean tieneAnticipo;
    private BigDecimal porcentajeAnticipo;
    private Double montoAnticipo;

    private Integer numeroExhibiciones;

    private Boolean tieneFiniquito;
    private BigDecimal porcentajeFiniquito;
    private Double montoFiniquito;

    private LocalDateTime fechaTentativaLlegada;
    private Integer idEstatusContrato;

    public String getIdentificadorContrato() {
        return identificadorContrato;
    }

    public void setIdentificadorContrato(String identificadorContrato) {
        this.identificadorContrato = identificadorContrato;
    }

    public Integer getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(Integer idProveedor) {
        this.idProveedor = idProveedor;
    }

    public Integer getIdComprador() {
        return idComprador;
    }

    public void setIdComprador(Integer idComprador) {
        this.idComprador = idComprador;
    }

    public Integer getIdAdministradorContrato() {
        return idAdministradorContrato;
    }

    public void setIdAdministradorContrato(Integer idAdministradorContrato) {
        this.idAdministradorContrato = idAdministradorContrato;
    }

    public Integer getIdTipoOrigenContrato() {
        return idTipoOrigenContrato;
    }

    public void setIdTipoOrigenContrato(Integer idTipoOrigenContrato) {
        this.idTipoOrigenContrato = idTipoOrigenContrato;
    }

    public String getFolioOrigen() {
        return folioOrigen;
    }

    public void setFolioOrigen(String folioOrigen) {
        this.folioOrigen = folioOrigen;
    }

    public LocalDateTime getFechaOrigen() {
        return fechaOrigen;
    }

    public void setFechaOrigen(LocalDateTime fechaOrigen) {
        this.fechaOrigen = fechaOrigen;
    }

    public Double getMontoSinImpuestos() {
        return montoSinImpuestos;
    }

    public void setMontoSinImpuestos(Double montoSinImpuestos) {
        this.montoSinImpuestos = montoSinImpuestos;
    }

    public Double getImpuestos() {
        return impuestos;
    }

    public void setImpuestos(Double impuestos) {
        this.impuestos = impuestos;
    }

    public Double getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(Double montoTotal) {
        this.montoTotal = montoTotal;
    }

    public Boolean getTieneAnticipo() {
        return tieneAnticipo;
    }

    public void setTieneAnticipo(Boolean tieneAnticipo) {
        this.tieneAnticipo = tieneAnticipo;
    }

    public BigDecimal getPorcentajeAnticipo() {
        return porcentajeAnticipo;
    }

    public void setPorcentajeAnticipo(BigDecimal porcentajeAnticipo) {
        this.porcentajeAnticipo = porcentajeAnticipo;
    }

    public Double getMontoAnticipo() {
        return montoAnticipo;
    }

    public void setMontoAnticipo(Double montoAnticipo) {
        this.montoAnticipo = montoAnticipo;
    }

    public Integer getNumeroExhibiciones() {
        return numeroExhibiciones;
    }

    public void setNumeroExhibiciones(Integer numeroExhibiciones) {
        this.numeroExhibiciones = numeroExhibiciones;
    }

    public Boolean getTieneFiniquito() {
        return tieneFiniquito;
    }

    public void setTieneFiniquito(Boolean tieneFiniquito) {
        this.tieneFiniquito = tieneFiniquito;
    }

    public BigDecimal getPorcentajeFiniquito() {
        return porcentajeFiniquito;
    }

    public void setPorcentajeFiniquito(BigDecimal porcentajeFiniquito) {
        this.porcentajeFiniquito = porcentajeFiniquito;
    }

    public Double getMontoFiniquito() {
        return montoFiniquito;
    }

    public void setMontoFiniquito(Double montoFiniquito) {
        this.montoFiniquito = montoFiniquito;
    }

    public LocalDateTime getFechaTentativaLlegada() {
        return fechaTentativaLlegada;
    }

    public void setFechaTentativaLlegada(LocalDateTime fechaTentativaLlegada) {
        this.fechaTentativaLlegada = fechaTentativaLlegada;
    }

    public Integer getIdEstatusContrato() {
        return idEstatusContrato;
    }

    public void setIdEstatusContrato(Integer idEstatusContrato) {
        this.idEstatusContrato = idEstatusContrato;
    }
}
