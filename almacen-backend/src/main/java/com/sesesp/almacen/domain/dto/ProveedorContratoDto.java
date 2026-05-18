package com.sesesp.almacen.domain.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProveedorContratoDto {

    private String razonSocial;
    private String domicilioFiscal;
    private String representante;
    private String caracter;

}
