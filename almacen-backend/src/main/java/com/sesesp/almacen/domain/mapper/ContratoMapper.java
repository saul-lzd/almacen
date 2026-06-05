package com.sesesp.almacen.domain.mapper;

import com.sesesp.almacen.common.SESESP_UTILS;
import com.sesesp.almacen.domain.dto.BeneficiarioDto;
import com.sesesp.almacen.domain.dto.ContratoBienDto;
import com.sesesp.almacen.domain.dto.ClavePresupuestalDto;
import com.sesesp.almacen.domain.dto.ContratoDto;
import com.sesesp.almacen.domain.entity.ContratoBeneficiarioEntity;
import com.sesesp.almacen.domain.entity.ContratoBienEntity;
import com.sesesp.almacen.domain.entity.ContratoClavePresupuestalEntity;
import com.sesesp.almacen.domain.entity.ContratoEntity;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ContratoMapper {

    private final ProveedorMapper proveedorMapper;
    private final FuncionarioMapper funcionarioMapper;


    public ContratoDto toResponse(ContratoEntity entity) {

        if (entity == null) return null;

       return ContratoDto.builder()
               // generales
               .idContrato(entity.getIdContrato())
               .numeroContrato(entity.getNumeroContrato())
               .adquisicion(entity.getAdquisicion())
               .estatus(entity.getEstatus().name())
               .fechaTentativaLlegada(entity.getFechaTentativaLlegada())

               // detalles de pago
               .montoSinImpuestos(entity.getMontoSinImpuestos())
               .impuestos(entity.getImpuestos())
               .montoTotal(entity.getMontoTotal())

               // interesados
               .proveedor(proveedorMapper.toResponse(entity.getProveedor()))
               .comprador(funcionarioMapper.toResponse(entity.getComprador()))
               .administradorContrato(funcionarioMapper.toResponse(entity.getAdministradorContrato()))

               // relaciones
               .clavesPresupuestales(convertClaves(entity.getClavesPresupuestales()))
               .beneficiarios(joinNames(entity.getBeneficiarios()))
               .listaBeneficiarios(convertBeneficiarios(entity.getBeneficiarios()))
               .bienes(convertBienes(entity.getBienes()))
               .build();
    }


    private String joinNames(List<ContratoBeneficiarioEntity> beneficiarios) {
        return beneficiarios.stream()
                .filter(cb -> Boolean.TRUE.equals(cb.getActivo()))
                .map(cb -> cb.getBeneficiario().getNombre())
                .collect(Collectors.joining(", "));
    }

    private List<BeneficiarioDto> convertBeneficiarios(List<ContratoBeneficiarioEntity> beneficiarios) {
        return beneficiarios.stream()
                .filter(cb -> Boolean.TRUE.equals(cb.getActivo()))
                .map(cb -> BeneficiarioDto.builder()
                        .idBeneficiario(cb.getBeneficiario().getIdBeneficiario())
                        .nombre(cb.getBeneficiario().getNombre())
                        .build())
                .toList();
    }

    private List<ClavePresupuestalDto> convertClaves(List<ContratoClavePresupuestalEntity> clavesPresupuestales ) {
        return clavesPresupuestales.stream()
                .filter(c -> Boolean.TRUE.equals(c.getActivo()))
                .map(this::toClaveDto)
                .toList();
    }

    private List<ContratoBienDto> convertBienes(List<ContratoBienEntity> bienes) {
        return bienes.stream()
                .filter(b -> Boolean.TRUE.equals(b.getActivo()))
                .map(this::toProductoDto)
                .toList();
    }

    private ClavePresupuestalDto toClaveDto(ContratoClavePresupuestalEntity entity) {
        return ClavePresupuestalDto.builder()
                .clave(entity.getClavePresupuestal().getClave())
                .partidaEspecifica(entity.getClavePresupuestal().getPartidaEspecifica())
                .montoAsignado(entity.getMontoAsignado())
                .build();
    }

    private ContratoBienDto toProductoDto(ContratoBienEntity entity) {
        return ContratoBienDto.builder()
                .idContratoBien(entity.getIdContratoBien())
                .lote(entity.getLote())
                .partida(entity.getPartida())
                .descripcionTecnica(entity.getDescripcionTecnica())
                .descripcionCorta(stripHtml(entity.getDescripcionTecnica(), SESESP_UTILS.DESCRIPCION_CORTA_BIEN_MAX_LENGTH))
                .idUnidadMedida(entity.getUnidadMedida().getIdUnidadMedida())
                .unidadMedida(entity.getUnidadMedida().getNombre())
                .cantidad(entity.getCantidad())
                .precioUnitario(entity.getPrecioUnitario())
                .subtotal(entity.getSubtotal())
                .cantidadProcesadaTotal(entity.getCantidadProcesadaTotal() != null ? entity.getCantidadProcesadaTotal() : 0L)
                .cantidadEntregadaTotal(entity.getCantidadEntregadaTotal() != null ? entity.getCantidadEntregadaTotal() : 0L)
                .build();
    }

    // Parse <html> string to "plain" text
    private String stripHtml(String html, int maxLen) {
        if (html == null || html.isBlank()) return "";
        String text = Jsoup.parse(html).text().trim();
        return text.length() > maxLen ? text.substring(0, maxLen) + "…" : text;
    }

}
