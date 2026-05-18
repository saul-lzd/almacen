package com.sesesp.almacen.domain.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FuncionarioDto {

    private Integer id;
    private String nombre;
    private String dependencia;
    private String caracter;
}
