package com.sesesp.almacen.domain.service;

import com.sesesp.almacen.domain.dto.ContratoBienDto;
import com.sesesp.almacen.domain.entity.ContratoBienEntity;
import com.sesesp.almacen.domain.entity.ContratoEntity;
import com.sesesp.almacen.domain.entity.UnidadMedidaEntity;
import com.sesesp.almacen.domain.repository.UnidadMedidaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContratoBienService {

    private static final Logger logger = LoggerFactory.getLogger(ContratoBienService.class);

    private final UnidadMedidaRepository unidadMedidaRepository;

    /**
     * Crea los bienes del contrato a partir del DTO.
     * El subtotal se calcula en el backend — no se confía en el valor del cliente.
     * Se usa en createContrato.
     */
    public List<ContratoBienEntity> crear(ContratoEntity contrato, List<ContratoBienDto> bienesDto) {
        return bienesDto.stream()
                .map(dto -> construirBien(contrato, dto))
                .toList();
    }

    /**
     * Sincroniza los bienes del contrato.
     *
     * null               → no se toca nada
     * bien con ID        → actualiza el existente
     * bien sin ID        → crea uno nuevo
     * bien no en request → se desactiva
     *
     * Se usa en updateContrato.
     */
    public void sincronizar(ContratoEntity contrato, List<ContratoBienDto> bienesRequest) {
        if (bienesRequest == null) return;

        // IDs que vienen en el request (solo existentes)
        List<Integer> idsEnRequest = bienesRequest.stream()
                .filter(b -> b.getIdContratoBien() != null)
                .map(ContratoBienDto::getIdContratoBien)
                .toList();

        // Desactivar los que ya no están en el request
        contrato.getBienes().forEach(bien ->
                bien.setActivo(idsEnRequest.contains(bien.getIdContratoBien()))
        );

        // Mapa de bienes existentes por ID para acceso rápido
        Map<Integer, ContratoBienEntity> bienesPorId = contrato.getBienes().stream()
                .filter(b -> b.getIdContratoBien() != null)
                .collect(Collectors.toMap(
                        ContratoBienEntity::getIdContratoBien,
                        b -> b,
                        (a, b) -> a
                ));

        for (ContratoBienDto dto : bienesRequest) {
            if (dto.getIdContratoBien() != null && bienesPorId.containsKey(dto.getIdContratoBien())) {
                // Actualizar bien existente
                actualizarBien(bienesPorId.get(dto.getIdContratoBien()), dto);
            } else {
                // Bien nuevo
                contrato.getBienes().add(construirBien(contrato, dto));
            }
        }

        logger.info("Bienes sincronizados para contrato ID: {}", contrato.getIdContrato());
    }

    // ─── helpers ───────────────────────────────────────────────

    private ContratoBienEntity construirBien(ContratoEntity contrato, ContratoBienDto dto) {
        UnidadMedidaEntity unidad = buscarUnidadMedida(dto.getIdUnidadMedida());
        BigDecimal subtotal = dto.getPrecioUnitario()
                .multiply(BigDecimal.valueOf(dto.getCantidad()));

        String sanitizarDescricion = Jsoup.clean(dto.getDescripcionTecnica(), Safelist.basic());

        return ContratoBienEntity.builder()
                .contrato(contrato)
                .lote(dto.getLote())
                .partida(dto.getPartida())
                .descripcionTecnica(sanitizarDescricion)
                .unidadMedida(unidad)
                .cantidad(dto.getCantidad())
                .precioUnitario(dto.getPrecioUnitario())
                .subtotal(subtotal)
                .build();
    }

    private void actualizarBien(ContratoBienEntity bien, ContratoBienDto dto) {
        bien.setLote(dto.getLote());
        bien.setPartida(dto.getPartida());
        bien.setDescripcionTecnica(Jsoup.clean(dto.getDescripcionTecnica(), Safelist.basic()));
        bien.setCantidad(dto.getCantidad());
        bien.setPrecioUnitario(dto.getPrecioUnitario());
        bien.setSubtotal(dto.getPrecioUnitario().multiply(BigDecimal.valueOf(dto.getCantidad())));
        bien.setActivo(true);

        if (dto.getIdUnidadMedida() != null) {
            bien.setUnidadMedida(buscarUnidadMedida(dto.getIdUnidadMedida()));
        }
    }

    private UnidadMedidaEntity buscarUnidadMedida(Integer idUnidadMedida) {
        return unidadMedidaRepository.findById(idUnidadMedida)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Unidad de medida no encontrada con ID: " + idUnidadMedida));
    }
}
