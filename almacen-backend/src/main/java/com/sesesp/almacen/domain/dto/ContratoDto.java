package com.sesesp.almacen.domain.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContratoDto {

    private Integer idContrato;
    private String numeroContrato;
    private String adquisicion;
    private LocalDateTime fechaTentativaLlegada;
    private BigDecimal montoSinImpuestos;
    private BigDecimal impuestos;
    private BigDecimal montoTotal;
    private String estatus;

    private String beneficiarios;

    private ProveedorContratoDto proveedor;
    private FuncionarioDto comprador;
    private FuncionarioDto administradorContrato;

    private List<ClavePresupuestalDto> clavesPresupuestales;
    private List<ContratoBienDto> bienes;

}