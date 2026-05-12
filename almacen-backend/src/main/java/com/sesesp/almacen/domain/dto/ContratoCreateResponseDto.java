package com.sesesp.almacen.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ContratoCreateResponseDto {

    private Integer idContrato;
    private String numeroContrato;
    private String adquisicion;
    private String folioOrigen;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaOrigen;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaTentativaLlegada;
    private Integer idEstatusContrato;
    private String estatusContrato;
    private ProveedorContratoDto proveedor;
    private ServidorPublicoDto comprador;
    private ServidorPublicoDto administradorContrato;
    private DetallesPagoDto detallesPago;
    private List<ClavePresupuestalDto> clavesPresupuestales;
    private List<BeneficiarioDto> beneficiarios;
    private List<ProductoDto> productos;

}