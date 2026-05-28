package com.sesesp.almacen.domain.service;

import com.sesesp.almacen.common.SESESP_UTILS;
import com.sesesp.almacen.common.exception.ContratoValidacionException;
import com.sesesp.almacen.domain.dto.AlmacenBienDto;
import com.sesesp.almacen.domain.dto.AlmacenBienGrupoDto;
import com.sesesp.almacen.domain.dto.ActualizarDatosBienRequestDto;
import com.sesesp.almacen.domain.dto.ProcesarBienRequestDto;
import com.sesesp.almacen.domain.dto.ProcesarBloqueRequestDto;
import com.sesesp.almacen.domain.entity.AlmacenBienEntity;
import com.sesesp.almacen.domain.entity.AlmacenBienEntity.EstatusBien;
import com.sesesp.almacen.domain.entity.RecepcionAlmacenBienEntity;
import com.sesesp.almacen.domain.entity.RecepcionAlmacenEntity;
import com.sesesp.almacen.domain.repository.AlmacenBienRepository;
import com.sesesp.almacen.domain.repository.ContratoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlmacenBienService {

    private static final Logger logger = LoggerFactory.getLogger(AlmacenBienService.class);

    private final AlmacenBienRepository almacenBienRepository;
    private final ContratoRepository contratoRepository;

    @Transactional(readOnly = true)
    public List<AlmacenBienGrupoDto> getBienesAgrupados(Integer idContrato) {
        contratoRepository.findById(idContrato)
                .orElseThrow(() -> new EntityNotFoundException("Contrato no encontrado: " + idContrato));

        return agrupar(almacenBienRepository.findByContratoWithFetch(
                idContrato, List.of(EstatusBien.RECIBIDO, EstatusBien.EN_PROCESO, EstatusBien.PROCESADO)));
    }

    @Transactional(readOnly = true)
    public List<AlmacenBienGrupoDto> getBienesListosParaEntregar(Integer idContrato) {
        contratoRepository.findById(idContrato)
                .orElseThrow(() -> new EntityNotFoundException("Contrato no encontrado: " + idContrato));

        return agrupar(almacenBienRepository.findByContratoWithFetch(
                idContrato, List.of(EstatusBien.LISTO_PARA_ENTREGAR)));
    }

    private List<AlmacenBienGrupoDto> agrupar(List<AlmacenBienEntity> bienes) {
        Map<Integer, List<AlmacenBienEntity>> porContratoBien = bienes.stream()
                .collect(Collectors.groupingBy(ab -> ab.getContratoBien().getIdContratoBien()));

        return porContratoBien.entrySet().stream()
                .map(entry -> {
                    var cb = entry.getValue().get(0).getContratoBien();
                    List<AlmacenBienDto> unidades = entry.getValue().stream()
                            .sorted(Comparator.comparing(AlmacenBienEntity::getCodigoInterno))
                            .map(this::toDto)
                            .collect(Collectors.toList());
                    return AlmacenBienGrupoDto.builder()
                            .idContratoBien(cb.getIdContratoBien())
                            .lote(cb.getLote())
                            .partida(cb.getPartida())
                            .descripcion(stripHtml(cb.getDescripcionTecnica(), SESESP_UTILS.DESCRIPCION_CORTA_BIEN_MAX_LENGTH))
                            .unidadMedida(cb.getUnidadMedida().getNombre())
                            .totalUnidades(unidades.size())
                            .unidades(unidades)
                            .build();
                })
                .sorted(Comparator
                        .comparingInt((AlmacenBienGrupoDto g) -> g.getLote() == null ? Integer.MAX_VALUE : g.getLote().intValue())
                        .thenComparingInt(g -> g.getPartida() == null ? Integer.MAX_VALUE : g.getPartida().intValue()))
                .collect(Collectors.toList());
    }

    /**
     * Guarda los datos del bien (serie, marca, etc.) sin cambiar el estatus a PROCESADO.
     * - RECIBIDO     → EN_PROCESO  (primera edición)
     * - EN_PROCESO   → EN_PROCESO  (actualización de datos)
     * - PROCESADO    → EN_PROCESO  (re-edición; requiere nueva confirmación explícita)
     */
    @Transactional
    public void actualizarDatos(Integer id, ActualizarDatosBienRequestDto request) {
        AlmacenBienEntity bien = almacenBienRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Bien no encontrado: " + id));

        if (bien.getEstatus() == EstatusBien.LISTO_PARA_ENTREGAR
                || bien.getEstatus() == EstatusBien.ENTREGADO) {
            throw new ContratoValidacionException(List.of(
                    "El bien " + bien.getCodigoInterno() + " no puede editarse en estatus " + bien.getEstatus() + "."));
        }

        Optional.ofNullable(request.getNumeroSerie()).ifPresent(bien::setNumeroSerie);
        Optional.ofNullable(request.getNumeroMotor()).ifPresent(bien::setNumeroMotor);
        Optional.ofNullable(request.getMarca()).ifPresent(bien::setMarca);
        Optional.ofNullable(request.getModelo()).ifPresent(bien::setModelo);
        Optional.ofNullable(request.getDescripcionComplementaria()).ifPresent(bien::setDescripcionComplementaria);

        if (bien.getEstatus() != EstatusBien.EN_PROCESO) {
            bien.setEstatus(EstatusBien.EN_PROCESO);
        }

        almacenBienRepository.save(bien);
    }

    /**
     * Marca el bien como PROCESADO de forma explícita.
     * Solo acepta bienes en estatus EN_PROCESO.
     */
    @Transactional
    public void procesarBien(Integer id, ProcesarBienRequestDto request) {
        AlmacenBienEntity bien = almacenBienRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Bien no encontrado: " + id));

        if (bien.getEstatus() != EstatusBien.EN_PROCESO) {
            throw new ContratoValidacionException(List.of(
                    "El bien " + bien.getCodigoInterno()
                            + " debe estar EN_PROCESO para poder marcarse como procesado (estatus actual: "
                            + bien.getEstatus() + ")."));
        }

        Optional.ofNullable(request.getNumeroSerie()).ifPresent(bien::setNumeroSerie);
        Optional.ofNullable(request.getNumeroMotor()).ifPresent(bien::setNumeroMotor);
        Optional.ofNullable(request.getMarca()).ifPresent(bien::setMarca);
        Optional.ofNullable(request.getModelo()).ifPresent(bien::setModelo);
        Optional.ofNullable(request.getDescripcionComplementaria()).ifPresent(bien::setDescripcionComplementaria);
        bien.setEstatus(EstatusBien.PROCESADO);
        bien.setFechaProcesamiento(LocalDateTime.now());

        almacenBienRepository.save(bien);

        long pendientes = almacenBienRepository.countByContratoIdContratoAndEstatusAndActivoTrue(
                bien.getContrato().getIdContrato(), EstatusBien.RECIBIDO)
                + almacenBienRepository.countByContratoIdContratoAndEstatusAndActivoTrue(
                bien.getContrato().getIdContrato(), EstatusBien.EN_PROCESO);
        if (pendientes == 0) {
            logger.info("Todos los bienes del contrato {} fueron procesados — notificar al admin.",
                    bien.getContrato().getIdContrato());
        }
    }

    @Transactional
    public void procesarBloque(ProcesarBloqueRequestDto request) {
        if (request.getIds() == null || request.getIds().isEmpty()) {
            throw new ContratoValidacionException(List.of("Debe indicar al menos un bien."));
        }

        List<AlmacenBienEntity> bienes = almacenBienRepository
                .findByIdAlmacenBienInAndActivoTrue(request.getIds());

        if (bienes.size() != request.getIds().size()) {
            throw new ContratoValidacionException(List.of("Uno o más bienes no encontrados."));
        }

        List<String> errores = bienes.stream()
                .filter(b -> b.getEstatus() != EstatusBien.EN_PROCESO)
                .map(b -> "El bien " + b.getCodigoInterno() + " no está EN_PROCESO (estatus: " + b.getEstatus() + ").")
                .collect(Collectors.toList());
        if (!errores.isEmpty()) throw new ContratoValidacionException(errores);

        long grupos = bienes.stream()
                .map(b -> b.getContratoBien().getIdContratoBien())
                .distinct().count();
        if (grupos > 1) {
            throw new ContratoValidacionException(List.of(
                    "El procesamiento por lote solo aplica a bienes del mismo tipo de contrato."));
        }

        LocalDateTime ahora = LocalDateTime.now();
        for (AlmacenBienEntity b : bienes) {
            Optional.ofNullable(request.getMarca()).ifPresent(b::setMarca);
            Optional.ofNullable(request.getModelo()).ifPresent(b::setModelo);
            Optional.ofNullable(request.getDescripcionComplementaria()).ifPresent(b::setDescripcionComplementaria);
            b.setEstatus(EstatusBien.PROCESADO);
            b.setFechaProcesamiento(ahora);
        }

        almacenBienRepository.saveAll(bienes);

        Integer idContrato = bienes.get(0).getContrato().getIdContrato();
        long pendientes = almacenBienRepository.countByContratoIdContratoAndEstatusAndActivoTrue(
                idContrato, EstatusBien.RECIBIDO);
        if (pendientes == 0) {
            logger.info("Todos los bienes del contrato {} fueron procesados — notificar al admin.", idContrato);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────

    private AlmacenBienDto toDto(AlmacenBienEntity ab) {
        String folio = Optional.ofNullable(ab.getRecepcionAlmacenBien())
                .map(RecepcionAlmacenBienEntity::getRecepcionAlmacen)
                .map(RecepcionAlmacenEntity::getFolioEntradaAlmacen)
                .orElse("—");

        return AlmacenBienDto.builder()
                .idAlmacenBien(ab.getIdAlmacenBien())
                .codigoInterno(ab.getCodigoInterno())
                .estatus(ab.getEstatus().name())
                .folioRecepcion(folio)
                .fechaRecepcion(ab.getFechaRecepcion())
                .numeroSerie(ab.getNumeroSerie())
                .numeroMotor(ab.getNumeroMotor())
                .marca(ab.getMarca())
                .modelo(ab.getModelo())
                .descripcionComplementaria(ab.getDescripcionComplementaria())
                .build();
    }

    private String stripHtml(String html, int maxLen) {
        if (html == null) return "";

        // Jsoup parsea el HTML y .text() extrae el texto plano eliminando etiquetas
        // y convirtiendo entidades como &nbsp; o &amp; en caracteres normales
        String text = Jsoup.parse(html).text().trim();

        // Validamos el recorte de longitud
        return text.length() > maxLen ? text.substring(0, maxLen) + "…" : text;
    }
}
