package com.sesesp.almacen.domain.service;

import com.sesesp.almacen.common.SESESP_UTILS;
import com.sesesp.almacen.common.exception.ContratoValidacionException;
import com.sesesp.almacen.common.util.SecurityUtils;
import com.sesesp.almacen.domain.dto.EntregaRequestDto;
import com.sesesp.almacen.domain.entity.AlmacenBienEntity;
import com.sesesp.almacen.domain.entity.AlmacenBienEntity.EstatusBien;
import com.sesesp.almacen.domain.entity.BeneficiarioEntity;
import com.sesesp.almacen.domain.entity.ContratoEntity;
import com.sesesp.almacen.domain.entity.ContratoEntity.EstatusContrato;
import com.sesesp.almacen.domain.entity.EvidenciaSalidaEntity;
import com.sesesp.almacen.domain.entity.RecepcionAlmacenEntity;
import com.sesesp.almacen.domain.entity.RecepcionAlmacenEntity.EstatusRecepcion;
import com.sesesp.almacen.domain.entity.SalidaAlmacenBienEntity;
import com.sesesp.almacen.domain.entity.SalidaAlmacenEntity;
import com.sesesp.almacen.domain.repository.AlmacenBienRepository;
import com.sesesp.almacen.domain.repository.BeneficiarioRepository;
import com.sesesp.almacen.domain.repository.ContratoRepository;
import com.sesesp.almacen.domain.repository.EvidenciaSalidaRepository;
import com.sesesp.almacen.domain.repository.RecepcionAlmacenRepository;
import com.sesesp.almacen.domain.repository.SalidaAlmacenRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalidaAlmacenService {

    private static final Logger logger = LoggerFactory.getLogger(SalidaAlmacenService.class);

    private final ContratoRepository contratoRepository;
    private final BeneficiarioRepository beneficiarioRepository;
    private final AlmacenBienRepository almacenBienRepository;
    private final SalidaAlmacenRepository salidaAlmacenRepository;
    private final RecepcionAlmacenRepository recepcionAlmacenRepository;
    private final EvidenciaSalidaRepository evidenciaSalidaRepository;
    private final S3StorageService s3StorageService;

    /**
     * Registra la entrega de bienes a un beneficiario.
     *
     * Validaciones:
     *   - Contrato en POR_RECIBIR y no cerrado
     *   - El beneficiario existe
     *   - Todos los bienes indicados están en LISTO_PARA_ENTREGAR y pertenecen al contrato
     *
     * Efecto:
     *   - Crea SalidaAlmacenEntity con su folio
     *   - Marca los bienes como ENTREGADO
     *   - Actualiza checkpoints del contrato (primeraEntregaAutorizada, contratoCerrado)
     *   - Marca la recepción como ENTREGADA si todos sus bienes están en ENTREGADO
     */
    @Transactional
    public void registrarEntrega(Integer idContrato, EntregaRequestDto request, List<MultipartFile> evidencias) {
        logger.info("Registrando entrega para contrato ID: {}", idContrato);

        validarEvidencias(evidencias);

        // 1. Validar contrato
        ContratoEntity contrato = contratoRepository.findById(idContrato)
                .orElseThrow(() -> new EntityNotFoundException("Contrato no encontrado: " + idContrato));

        if (contrato.getEstatus() != EstatusContrato.POR_RECIBIR) {
            throw new ContratoValidacionException(List.of(
                    "El contrato no puede entregarse porque su estatus es: "
                            + contrato.getEstatus().name() + "."));
        }

        if (contrato.isContratoCerrado()) {
            throw new ContratoValidacionException(List.of(
                    "El contrato ya está cerrado — todos los bienes han sido entregados."));
        }

        // 2. Validar request
        List<String> errores = new ArrayList<>();
        if (request.getNombreEntregaAlmacen() == null || request.getNombreEntregaAlmacen().isBlank())
            errores.add("El nombre de quien entrega es obligatorio.");
        if (request.getNombreRecibeBeneficiario() == null || request.getNombreRecibeBeneficiario().isBlank())
            errores.add("El nombre de quien recibe es obligatorio.");
        if (request.getIdsAlmacenBien() == null || request.getIdsAlmacenBien().isEmpty())
            errores.add("Debe seleccionar al menos un bien para entregar.");
        if (!errores.isEmpty()) throw new ContratoValidacionException(errores);

        // 3. Cargar beneficiario
        BeneficiarioEntity beneficiario = beneficiarioRepository.findById(request.getIdBeneficiario())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Beneficiario no encontrado: " + request.getIdBeneficiario()));

        // 4. Cargar y validar bienes
        List<AlmacenBienEntity> bienes = almacenBienRepository
                .findByIdAlmacenBienInAndActivoTrue(request.getIdsAlmacenBien());

        if (bienes.size() != request.getIdsAlmacenBien().size()) {
            throw new ContratoValidacionException(List.of("Uno o más bienes no encontrados."));
        }

        List<String> erroresBienes = bienes.stream()
                .filter(b -> !b.getContrato().getIdContrato().equals(idContrato))
                .map(b -> "El bien " + b.getCodigoInterno() + " no pertenece a este contrato.")
                .collect(Collectors.toList());

        bienes.stream()
                .filter(b -> b.getEstatus() != EstatusBien.LISTO_PARA_ENTREGAR)
                .forEach(b -> erroresBienes.add(
                        "El bien " + b.getCodigoInterno() + " no está listo para entregar (estatus: " + b.getEstatus() + ")."));

        if (!erroresBienes.isEmpty()) throw new ContratoValidacionException(erroresBienes);

        // 5. Crear la salida
        LocalDateTime ahora = LocalDateTime.now();
        SalidaAlmacenEntity salida = SalidaAlmacenEntity.builder()
                .contrato(contrato)
                .beneficiario(beneficiario)
                .folioSalidaAlmacen(generarFolio())
                .fechaSalida(ahora)
                .nombreEntregaAlmacen(request.getNombreEntregaAlmacen().trim())
                .nombreRecibeBeneficiario(request.getNombreRecibeBeneficiario().trim())
                .beneficiarioFirma(Boolean.TRUE.equals(request.getBeneficiarioFirma()))
                .observaciones(request.getObservaciones())
                .esEntregaTotal(false) // se actualiza abajo
                .build();

        // 6. Vincular bienes y marcarlos como ENTREGADO
        for (AlmacenBienEntity b : bienes) {
            salida.getBienes().add(SalidaAlmacenBienEntity.builder()
                    .salidaAlmacen(salida)
                    .almacenBien(b)
                    .build());
            b.setEstatus(EstatusBien.ENTREGADO);
            b.setFechaEntrega(ahora);
        }

        salidaAlmacenRepository.save(salida);
        almacenBienRepository.saveAll(bienes);

        // 6.1 Subir evidencias fotográficas a S3 y guardar su referencia
        subirEvidencias(salida, evidencias);

        // 7. Marcar checkpoint de primera entrega
        if (!contrato.isPrimeraEntregaAutorizada()) {
            contrato.setPrimeraEntregaAutorizada(true);
        }

        // 8. Verificar si todos los bienes del contrato están entregados → cerrar contrato
        long totalBienes    = almacenBienRepository.countByContratoIdContratoAndActivoTrue(idContrato);
        long totalEntregados = almacenBienRepository.countByContratoIdContratoAndEstatusAndActivoTrue(
                idContrato, EstatusBien.ENTREGADO);
        boolean contratoCompleto = totalBienes > 0 && totalEntregados == totalBienes;

        salida.setEsEntregaTotal(contratoCompleto);

        if (contratoCompleto) {
            contrato.setContratoCerrado(true);
        }
        contratoRepository.save(contrato);

        // 9. Verificar recepciones afectadas → marcar ENTREGADA si todos sus bienes están entregados
        Set<Integer> recepcionesAfectadas = bienes.stream()
                .filter(b -> b.getRecepcionAlmacenBien() != null)
                .map(b -> b.getRecepcionAlmacenBien().getRecepcionAlmacen().getIdRecepcionAlmacen())
                .collect(Collectors.toSet());

        for (Integer idRecepcion : recepcionesAfectadas) {
            long totalRec    = almacenBienRepository
                    .countByRecepcionAlmacenBienRecepcionAlmacenIdRecepcionAlmacenAndActivoTrue(idRecepcion);
            long entregadosRec = almacenBienRepository
                    .countByRecepcionAlmacenBienRecepcionAlmacenIdRecepcionAlmacenAndEstatusInAndActivoTrue(
                            idRecepcion, List.of(EstatusBien.ENTREGADO));

            if (totalRec > 0 && entregadosRec == totalRec) {
                recepcionAlmacenRepository.findById(idRecepcion).ifPresent(rec -> {
                    rec.setEstatus(EstatusRecepcion.ENTREGADA);
                    recepcionAlmacenRepository.save(rec);
                    logger.info("Recepción {} → ENTREGADA", rec.getFolioEntradaAlmacen());
                });
            }
        }

        logger.info("Entrega registrada. Folio: {}. Contrato ID: {}. Cerrado: {}",
                salida.getFolioSalidaAlmacen(), idContrato, contratoCompleto);
    }

    private String generarFolio() {
        String anio = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy"));
        long secuencial = salidaAlmacenRepository.count() + 1;
        return String.format("%s-%s-%0" + SESESP_UTILS.DIGITOS_CONSECUTIVO_FOLIO + "d",
                SESESP_UTILS.PREFIJO_FOLIO_ENTREGA, anio, secuencial);
    }

    private void validarEvidencias(List<MultipartFile> evidencias) {
        int total = evidencias == null ? 0 : evidencias.size();
        if (total < SESESP_UTILS.MIN_EVIDENCIAS_ENTREGA || total > SESESP_UTILS.MAX_EVIDENCIAS_ENTREGA) {
            throw new ContratoValidacionException(List.of(
                    "Debes adjuntar entre " + SESESP_UTILS.MIN_EVIDENCIAS_ENTREGA + " y " + SESESP_UTILS.MAX_EVIDENCIAS_ENTREGA
                            + " fotos de evidencia (se recibieron " + total + ")."));
        }
    }

    /**
     * Sube cada foto a S3 dentro de la carpeta del contrato y guarda su referencia
     * en evidencia_salida. Se llama después de persistir la salida porque necesita
     * su folio (ya asignado) para nombrar cada archivo.
     *
     * Estructura en S3: {prefixEvidencias}/{numeroContrato}/evidencia/entrega/{nombreArchivo}
     * — misma raíz que usan las evidencias de recepción ("recepcion/") y de bienes
     * procesados ("bienes/").
     *
     * Nombre de archivo: SA_{consecutivo}_{fecha}_IMG_{numeroProgresivo}{extension}, ej.
     * "SA_0001_2026_07_20_IMG_1.jpg" — el consecutivo es el mismo secuencial usado
     * en el folio (SA-{año}-{consecutivo}), pero aquí se combina con la fecha completa
     * de la salida (no solo el año) para mantener la misma convención que recepción.
     */
    private void subirEvidencias(SalidaAlmacenEntity salida, List<MultipartFile> evidencias) {
        String folderContrato = salida.getContrato().getNumeroContrato().replaceAll("/", "_");

        String[] folioParts = salida.getFolioSalidaAlmacen().split("-");
        String consecutivo = folioParts[folioParts.length - 1];
        String fecha = salida.getFechaSalida().format(DateTimeFormatter.ofPattern("yyyy_MM_dd"));
        String base = SESESP_UTILS.PREFIJO_FOLIO_ENTREGA + "_" + consecutivo + "_" + fecha;

        List<EvidenciaSalidaEntity> entidades = new ArrayList<>();
        int numeroProgresivo = 1;
        for (MultipartFile file : evidencias) {
            String nombreEnS3 = base + "_IMG_" + numeroProgresivo + extraerExtension(file.getOriginalFilename());
            String key = s3StorageService.getPrefixEvidencias() + "/" + folderContrato
                    + "/evidencia/entrega/" + nombreEnS3;

            String url = s3StorageService.uploadEvidencia(file, key);
            entidades.add(EvidenciaSalidaEntity.builder()
                    .salidaAlmacen(salida)
                    .url(url)
                    .nombreArchivo(file.getOriginalFilename())
                    .fechaCaptura(LocalDateTime.now())
                    .usuarioCaptura(SecurityUtils.getCurrentUserId())
                    .build());
            numeroProgresivo++;
        }

        evidenciaSalidaRepository.saveAll(entidades);
    }

    private static String extraerExtension(String originalFilename) {
        if (originalFilename == null) return "";
        int dot = originalFilename.lastIndexOf('.');
        return dot >= 0 ? originalFilename.substring(dot) : "";
    }
}
