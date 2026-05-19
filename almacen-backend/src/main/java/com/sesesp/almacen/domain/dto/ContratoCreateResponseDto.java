//package com.sesesp.almacen.domain.dto;
//
//import com.fasterxml.jackson.annotation.JsonFormat;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Data
//@Builder
//@AllArgsConstructor
//@NoArgsConstructor
//public class ContratoCreateResponseDto {
//
//    private Integer idContrato;
//    private String numeroContrato;
//    private String adquisicion;
//    private String folioOrigen;
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    private LocalDateTime fechaOrigen;
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    private LocalDateTime fechaTentativaLlegada;
//    private Integer idEstatusContrato;
//    private String estatusContrato;
//    private ProveedorContratoDto proveedor;
//    private FuncionarioDto comprador;
//    private FuncionarioDto administradorContrato;
//    //private DetallesPagoDto detallesPago;
//    private List<ClavePresupuestalDto> clavesPresupuestales;
//    private String beneficiarios;
//    private List<ContratoBienDto> productos;
//
//}