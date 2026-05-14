package com.sesesp.almacen.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContratoCreateRequestDto {

    private Integer idContrato;
    private String numeroContrato;
    private String adquisicion;
    private String folioOrigen;
    private LocalDateTime fechaOrigen;
    private LocalDateTime fechaTentativaLlegada;
    private Integer idEstatusContrato;
    private String beneficiarios;
    private ProveedorContratoDto proveedor;
    private ServidorPublicoDto comprador;
    private ServidorPublicoDto administradorContrato;
    private DetallesPagoDto detallesPago;
    private List<ClavePresupuestalDto> clavesPresupuestales;
    private List<ProductoDto> productos;

}
