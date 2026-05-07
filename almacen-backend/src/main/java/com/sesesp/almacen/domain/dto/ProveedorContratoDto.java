package com.sesesp.almacen.domain.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProveedorContratoDto {

    private String razonSocial;
    private String direccion;
    private String representanteEmpresa;
    private String caracterRepresentante;
    private String telefono;
    private String correo;

}
