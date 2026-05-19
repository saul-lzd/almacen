package com.sesesp.almacen.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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
    private String estatus;
    private LocalDateTime fechaTentativaLlegada;

    private BigDecimal montoSinImpuestos;
    private BigDecimal impuestos;
    private BigDecimal montoTotal;

    private String beneficiarios;
    private ProveedorContratoDto proveedor;
    private FuncionarioDto comprador;
    private FuncionarioDto administradorContrato;

    private List<ClavePresupuestalDto> clavesPresupuestales;
    private List<ContratoBienDto> bienes;

}
