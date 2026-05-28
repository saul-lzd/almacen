package com.sesesp.almacen.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EntregaRequestDto {

    private Integer idBeneficiario;
    private String nombreEntregaAlmacen;
    private String nombreRecibeBeneficiario;
    private Boolean beneficiarioFirma;
    private String observaciones;

    /** IDs de AlmacenBienEntity a entregar en este evento */
    private List<Integer> idsAlmacenBien;
}
