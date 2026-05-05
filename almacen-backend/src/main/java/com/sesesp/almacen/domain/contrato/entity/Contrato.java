package com.sesesp.almacen.domain.contrato.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "contrato")
public class Contrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contrato")
    private Integer idContrato;

    @Column(name = "identificador_contrato", nullable = true)
    private String identificadorContrato;

    @Column(name = "id_proveedor", nullable = false)
    private Integer idProveedor;

    @Column(name = "id_comprador", nullable = false)
    private Integer idComprador;

    @Column(name = "id_administrador_contrato", nullable = false)
    private Integer idAdministradorContrato;

    @Column(name = "id_tipo_origen_contrato")
    private Integer idTipoOrigenContrato;

    @Column(name = "folio_origen")
    private String folioOrigen;

    @Column(name = "fecha_origen")
    private LocalDateTime fechaOrigen;

    @Column(name = "monto_sin_impuestos")
    private Double montoSinImpuestos;

    @Column(name = "impuestos")
    private Double impuestos;

    @Column(name = "monto_total")
    private Double montoTotal;

    @Column(name = "tiene_anticipo")
    private Boolean tieneAnticipo;

    @Column(name = "porcentaje_anticipo")
    private BigDecimal porcentajeAnticipo;

    @Column(name = "monto_anticipo")
    private Double montoAnticipo;

    @Column(name = "numero_exhibiciones")
    private Integer numeroExhibiciones;

    @Column(name = "tiene_finiquito")
    private Boolean tieneFiniquito;

    @Column(name = "porcentaje_finiquito")
    private BigDecimal porcentajeFiniquito;

    @Column(name = "monto_finiquito")
    private Double montoFiniquito;

    @Column(name = "fecha_tentativa_llegada")
    private LocalDateTime fechaTentativaLlegada;

    @Column(name = "id_estatus_contrato", nullable = false)
    private Integer idEstatusContrato;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "usuario_creacion")
    private Integer usuarioCreacion;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @Column(name = "usuario_modificacion")
    private Integer usuarioModificacion;

    @Column(name = "activo")
    private Boolean activo;

    public Integer getIdContrato() {
        return idContrato;
    }

    public void setIdContrato(Integer idContrato) {
        this.idContrato = idContrato;
    }

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

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Integer getUsuarioCreacion() {
        return usuarioCreacion;
    }

    public void setUsuarioCreacion(Integer usuarioCreacion) {
        this.usuarioCreacion = usuarioCreacion;
    }

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(LocalDateTime fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    public Integer getUsuarioModificacion() {
        return usuarioModificacion;
    }

    public void setUsuarioModificacion(Integer usuarioModificacion) {
        this.usuarioModificacion = usuarioModificacion;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}