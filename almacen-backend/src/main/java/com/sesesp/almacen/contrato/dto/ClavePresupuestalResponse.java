package com.sesesp.almacen.contrato.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ClavePresupuestalResponse {

    private String clavePresupuestal;
    private String partidaEspecifica;
}
