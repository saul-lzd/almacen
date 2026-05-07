package com.sesesp.almacen.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ContratoCreateRequestDto {

    private String numeroContrato;
    private String adquisicion;
    private String folioOrigen;
    private LocalDateTime fechaOrigen;
    private LocalDateTime fechaTentativaLlegada;
    private Integer idEstatusContrato;
    private ProveedorContratoDto proveedor;
    private ServidorPublicoDto comprador;
    private ServidorPublicoDto administradorContrato;
    private DetallesPagoDto detallesPago;
    private List<ClavePresupuestalDto> clavesPresupuestales;

}
