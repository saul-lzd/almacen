package com.sesesp.almacen.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActualizarFechaTentativaRequestDto {
    private LocalDateTime fechaTentativaLlegada;
}
