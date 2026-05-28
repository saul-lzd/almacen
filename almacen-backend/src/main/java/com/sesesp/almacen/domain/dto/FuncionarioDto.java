package com.sesesp.almacen.domain.dto;

import com.sesesp.almacen.common.types.TipoFuncionario;
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
    private TipoFuncionario tipoFuncionario;
}
