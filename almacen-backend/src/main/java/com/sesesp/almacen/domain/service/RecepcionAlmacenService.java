package com.sesesp.almacen.domain.service;

import com.sesesp.almacen.common.exception.ContratoValidacionException;
import com.sesesp.almacen.common.util.SecurityUtils;
import com.sesesp.almacen.domain.dto.RecepcionAlmacenRequestDto;
import com.sesesp.almacen.domain.dto.RecepcionAlmacenRequestDto.BienRecepcionDto;
import com.sesesp.almacen.domain.dto.RecepcionAlmacenResponseDto;
import com.sesesp.almacen.domain.dto.RecepcionDetalleDto;
import com.sesesp.almacen.domain.entity.AlmacenBienEntity;
import com.sesesp.almacen.domain.entity.AlmacenBienEntity.EstatusBien;
import com.sesesp.almacen.domain.entity.ContratoBienEntity;
import com.sesesp.almacen.domain.entity.ContratoEntity;
import com.sesesp.almacen.domain.entity.ContratoEntity.EstatusContrato;
import com.sesesp.almacen.domain.entity.EvidenciaEntradaEntity;
import com.sesesp.almacen.domain.entity.RecepcionAlmacenBienEntity;
import com.sesesp.almacen.domain.entity.RecepcionAlmacenEntity;
import com.sesesp.almacen.domain.entity.RecepcionAlmacenEntity.EstatusRecepcion;
import com.sesesp.almacen.domain.repository.AlmacenBienRepository;
import com.sesesp.almacen.domain.repository.ContratoBienRepository;
import com.sesesp.almacen.domain.repository.ContratoRepository;
import com.sesesp.almacen.domain.repository.EvidenciaEntradaRepository;
import com.sesesp.almacen.domain.repository.RecepcionAlmacenBienRepository;
import com.sesesp.almacen.domain.repository.RecepcionAlmacenRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecepcionAlmacenService {

    private static final Logger logger = LoggerFactory.getLogger(RecepcionAlmacenService.class);

    private final ContratoRepository contratoRepository;
    private final ContratoBienRepository contratoBienRepository;
    private final RecepcionAlmacenRepository recepcionAlmacenRepository;
    private final RecepcionAlmacenBienRepository recepcionAlmacenBienRepository;
    private final AlmacenBienRepository almacenBienRepository;
    private final EvidenciaEntradaRepository evidenciaEntradaRepository;
    private final S3StorageService s3StorageService;

    private static final int MIN_EVIDENCIAS = 5;
    private static final int MAX_EVIDENCIAS = 10;

    /**
     * Registra la recepción de bienes cuando llega el proveedor.
     *
     * Validaciones:
     *   - El contrato debe estar en estatus POR_RECIBIR
     *   - El nombre del transportista es obligatorio
     *   - Todos los bienes recibidos deben pertenecer al contrato
     *   - La cantidad recibida no puede superar la cantidad del contrato
     *
     * Efecto:
     *   - Crea el registro RecepcionAlmacen con su folio
     *   - Crea el detalle por bien (RecepcionAlmacenBien)
     *   - Si todas las cantidades son completas → EN_ALMACEN
     *   - Si alguna cantidad es menor a la esperada → RECEPCION_PARCIAL
     */
    @Transactional
    public void recibirBienes(Integer idContrato, RecepcionAlmacenRequestDto request, List<MultipartFile> evidencias) {
        logger.info("Registrando recepción para contrato ID: {}", idContrato);

        validarEvidencias(evidencias);

        // 1. Cargar y validar el contrato
        ContratoEntity contrato = contratoRepository.findById(idContrato)
                .orElseThrow(() -> new EntityNotFoundException("Contrato no encontrado: " + idContrato));

        if (contrato.getEstatus() != EstatusContrato.POR_RECIBIR) {
            throw new ContratoValidacionException(List.of(
                    "El contrato no puede recibirse porque su estatus es: "
                            + contrato.getEstatus().name()
                            + ". Solo se pueden recibir contratos en estatus POR_RECIBIR."
            ));
        }

        if (contrato.isContratoCerrado()) {
            throw new ContratoValidacionException(List.of(
                    "El contrato ya está cerrado — todos los bienes han sido entregados."
            ));
        }

        if (contrato.isTodosLosBienesRecibidos()) {
            throw new ContratoValidacionException(List.of(
                    "Todos los bienes de este contrato ya fueron recibidos."
            ));
        }

        // Totales ya recibidos en recepciones anteriores (vacío para POR_RECIBIR)
        Map<Integer, BigDecimal> yaRecibidoPorBien = buildTotalesRecibidos(idContrato);

        // 2. Validar el payload del request
        validarRequest(request);

        // 3. Cargar los bienes del contrato indicados en el request
        Map<Integer, BigDecimal> cantidadesPorBien = request.getBienes().stream()
                .collect(Collectors.toMap(
                        BienRecepcionDto::getIdContratoBien,
                        BienRecepcionDto::getCantidadRecibida
                ));

        List<ContratoBienEntity> bienesEncontrados =
                contratoBienRepository.findAllById(cantidadesPorBien.keySet());

        // 4. Validar pertenencia al contrato y que las cantidades no excedan lo esperado
        List<String> errores = new ArrayList<>();

        for (ContratoBienEntity bien : bienesEncontrados) {
            if (!bien.getContrato().getIdContrato().equals(idContrato)) {
                errores.add("El bien ID " + bien.getIdContratoBien()
                        + " no pertenece a este contrato.");
            }
            BigDecimal recibida = cantidadesPorBien.get(bien.getIdContratoBien());
            BigDecimal esperada = BigDecimal.valueOf(bien.getCantidad());
            BigDecimal yaRecibida = yaRecibidoPorBien.getOrDefault(bien.getIdContratoBien(), BigDecimal.ZERO);
            BigDecimal pendiente = esperada.subtract(yaRecibida).max(BigDecimal.ZERO);
            if (recibida.compareTo(pendiente) > 0) {
                errores.add("El bien L" + bien.getLote() + "/P" + bien.getPartida()
                        + " tiene cantidad recibida (" + recibida
                        + ") mayor a la pendiente (" + pendiente + ").");
            }
        }

        if (bienesEncontrados.size() < cantidadesPorBien.size()) {
            errores.add("Uno o más bienes indicados no existen.");
        }

        if (!errores.isEmpty()) {
            throw new ContratoValidacionException(errores);
        }

        // 5. Construir la entidad de recepción
        RecepcionAlmacenEntity recepcion = RecepcionAlmacenEntity.builder()
                .contrato(contrato)
                .folioEntradaAlmacen(generarFolio())
                .fechaRecepcion(LocalDateTime.now())
                .proveedor(contrato.getProveedor())
                .nombreEntrega(request.getTransportista())
                .observaciones(request.getObservaciones())
                .nombreRecibe("Almacén SESESP")
                .estatus(EstatusRecepcion.INICIADA)
                .build();

        // 6. Agregar el detalle por bien
        for (ContratoBienEntity bien : bienesEncontrados) {
            recepcion.getBienes().add(
                    RecepcionAlmacenBienEntity.builder()
                            .recepcionAlmacen(recepcion)
                            .contratoBien(bien)
                            .cantidadRecibida(cantidadesPorBien.get(bien.getIdContratoBien()))
                            .cantidadRechazada(BigDecimal.ZERO)
                            .build()
            );
        }

        // Determinar si el contrato queda completamente recibido considerando TODOS los bienes
        // y la suma acumulada de recepciones anteriores + la actual
        Map<Integer, BigDecimal> totalesActualizados = new HashMap<>(yaRecibidoPorBien);
        cantidadesPorBien.forEach((id, nueva) ->
                totalesActualizados.merge(id, nueva, BigDecimal::add));

        List<ContratoBienEntity> todosLosBienes =
                contratoBienRepository.findByContratoIdContratoAndActivoTrue(idContrato);

        boolean recepcionCompleta = todosLosBienes.stream().allMatch(b -> {
            BigDecimal totalRecibido = totalesActualizados.getOrDefault(b.getIdContratoBien(), BigDecimal.ZERO);
            return totalRecibido.compareTo(BigDecimal.valueOf(b.getCantidad())) >= 0;
        });

        // 7. Persistir la recepción (cascade guarda el detalle de bienes)
        recepcionAlmacenRepository.save(recepcion);

        // 7.1 Crear una AlmacenBienEntity por cada unidad física recibida
        crearUnidadesAlmacen(contrato, recepcion);

        // 7.2 Subir evidencias fotográficas a S3 y guardar su referencia
        subirEvidencias(recepcion, evidencias);

        // 8. Actualizar checkpoints del contrato
        if (!contrato.isPrimeraRecepcionRegistrada()) {
            contrato.setPrimeraRecepcionRegistrada(true);
        }
        if (recepcionCompleta && !contrato.isTodosLosBienesRecibidos()) {
            contrato.setTodosLosBienesRecibidos(true);
        }
        contratoRepository.save(contrato);

        logger.info("Recepción registrada. Folio: {}. Contrato ID: {}. Completa: {}",
                recepcion.getFolioEntradaAlmacen(), idContrato, recepcionCompleta);
    }

    // ─────────────────────────────────────────────────────────────
    // GET — lista de todas las recepciones de un contrato
    // ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<RecepcionDetalleDto> listarRecepciones(Integer idContrato) {
        return recepcionAlmacenRepository.findByContratoIdContratoAndActivoTrue(idContrato)
                .stream()
                .sorted(java.util.Comparator.comparing(RecepcionAlmacenEntity::getFechaRecepcion).reversed())
                .map(this::mapToDetalle)
                .collect(Collectors.toList());
    }

    private RecepcionDetalleDto mapToDetalle(RecepcionAlmacenEntity r) {
        List<RecepcionDetalleDto.BienDetalleDto> bienes = r.getBienes().stream()
                .map(b -> {
                    ContratoBienEntity cb = b.getContratoBien();
                    return new RecepcionDetalleDto.BienDetalleDto(
                            cb.getIdContratoBien(),
                            cb.getLote(),
                            cb.getPartida(),
                            descripcionCorta(cb.getDescripcionTecnica()),
                            cb.getUnidadMedida().getNombre(),
                            cb.getCantidad(),
                            b.getCantidadRecibida(),
                            b.getCantidadRechazada(),
                            b.getComentarios()
                    );
                })
                .collect(Collectors.toList());

        int totalBienes = (int) almacenBienRepository
                .countByRecepcionAlmacenBienRecepcionAlmacenIdRecepcionAlmacenAndActivoTrue(r.getIdRecepcionAlmacen());
        int totalProcesados = (int) almacenBienRepository
                .countByRecepcionAlmacenBienRecepcionAlmacenIdRecepcionAlmacenAndEstatusInAndActivoTrue(
                        r.getIdRecepcionAlmacen(),
                        List.of(EstatusBien.PROCESADO, EstatusBien.LISTO_PARA_ENTREGAR, EstatusBien.ENTREGADO));
        int totalPendientesAutorizar = (int) almacenBienRepository
                .countByRecepcionAlmacenBienRecepcionAlmacenIdRecepcionAlmacenAndEstatusInAndActivoTrue(
                        r.getIdRecepcionAlmacen(),
                        List.of(EstatusBien.PROCESADO));
        int totalListos = (int) almacenBienRepository
                .countByRecepcionAlmacenBienRecepcionAlmacenIdRecepcionAlmacenAndEstatusInAndActivoTrue(
                        r.getIdRecepcionAlmacen(),
                        List.of(EstatusBien.LISTO_PARA_ENTREGAR));
        int totalEntregados = (int) almacenBienRepository
                .countByRecepcionAlmacenBienRecepcionAlmacenIdRecepcionAlmacenAndEstatusInAndActivoTrue(
                        r.getIdRecepcionAlmacen(),
                        List.of(EstatusBien.ENTREGADO));

        return new RecepcionDetalleDto(
                r.getIdRecepcionAlmacen(),
                r.getFolioEntradaAlmacen(),
                r.getFechaRecepcion(),
                r.getNombreEntrega(),
                r.getNombreRecibe(),
                r.getObservaciones(),
                r.getEstatus().name(),
                totalBienes,
                totalProcesados,
                totalPendientesAutorizar,
                totalListos,
                totalEntregados,
                bienes
        );
    }

    private static String descripcionCorta(String html) {
        if (html == null || html.isBlank()) return "";
        String stripped = html.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").strip();
        return stripped.length() > 100 ? stripped.substring(0, 97) + "..." : stripped;
    }

    // ─────────────────────────────────────────────────────────────
    // GET — recepción de un contrato (legado — devuelve la más reciente)
    // ─────────────────────────────────────────────────────────────

    public RecepcionAlmacenResponseDto findByContrato(Integer idContrato) {
        List<RecepcionAlmacenEntity> recepciones =
                recepcionAlmacenRepository.findByContratoIdContratoAndActivoTrue(idContrato);

        if (recepciones.isEmpty()) {
            throw new EntityNotFoundException(
                    "No hay recepción registrada para el contrato: " + idContrato);
        }

        // La más reciente primero en caso de recepciones parciales futuras
        RecepcionAlmacenEntity recepcion = recepciones.stream()
                .max(java.util.Comparator.comparing(RecepcionAlmacenEntity::getFechaRecepcion))
                .orElseThrow();

        return new RecepcionAlmacenResponseDto(
                recepcion.getFolioEntradaAlmacen(),
                recepcion.getFechaRecepcion(),
                recepcion.getNombreEntrega(),
                recepcion.getObservaciones()
        );
    }

    // ─────────────────────────────────────────────────────────────
    // Helpers privados
    // ─────────────────────────────────────────────────────────────

    private void validarEvidencias(List<MultipartFile> evidencias) {
        int total = evidencias == null ? 0 : evidencias.size();
        if (total < MIN_EVIDENCIAS || total > MAX_EVIDENCIAS) {
            throw new ContratoValidacionException(List.of(
                    "Debes adjuntar entre " + MIN_EVIDENCIAS + " y " + MAX_EVIDENCIAS
                            + " fotos de evidencia (se recibieron " + total + ")."));
        }
    }

    /**
     * Sube cada foto a S3 dentro de la carpeta del contrato (todas las recepciones
     * del mismo contrato comparten esa carpeta — sin subcarpeta por recepción) y
     * guarda su referencia en evidencia_entrada. Se llama después de persistir la
     * recepción porque necesita su folio (ya asignado) para nombrar cada archivo.
     *
     * Nombre de archivo: {fecha}_{folio}_{numeroProgresivo}{extension}, ej.
     * "2026-07-06_EA-2026-0009_1.jpg" — el folio ya es único por recepción, así
     * que distingue las fotos de esta recepción de las de cualquier otra recepción
     * del mismo contrato dentro de la carpeta compartida.
     */
    private void subirEvidencias(RecepcionAlmacenEntity recepcion, List<MultipartFile> evidencias) {
        String folderContrato = recepcion.getContrato().getNumeroContrato().replaceAll("/", "_");
        String folio = recepcion.getFolioEntradaAlmacen().replaceAll("-","_");

        List<EvidenciaEntradaEntity> entidades = new ArrayList<>();
        int numeroProgresivo = 1;
        for (MultipartFile file : evidencias) {
            String nombreEnS3 = folio + "_IMG_" + numeroProgresivo + extraerExtension(file.getOriginalFilename());
            String key = s3StorageService.getPrefixEvidencias() + "/recepcion/" + folderContrato + "/" + nombreEnS3;
            String url = s3StorageService.uploadEvidencia(file, key);
            entidades.add(EvidenciaEntradaEntity.builder()
                    .recepcionAlmacen(recepcion)
                    .url(url)
                    .nombreArchivo(file.getOriginalFilename())
                    .fechaCaptura(LocalDateTime.now())
                    .usuarioCaptura(SecurityUtils.getCurrentUserId())
                    .build());
            numeroProgresivo++;
        }

        evidenciaEntradaRepository.saveAll(entidades);
    }

    private static String extraerExtension(String originalFilename) {
        if (originalFilename == null) return "";
        int dot = originalFilename.lastIndexOf('.');
        return dot >= 0 ? originalFilename.substring(dot) : "";
    }

    private void validarRequest(RecepcionAlmacenRequestDto request) {
        List<String> errores = new ArrayList<>();

        if (request.getTransportista() == null || request.getTransportista().isBlank()) {
            errores.add("El nombre del transportista es obligatorio.");
        }
        if (request.getBienes() == null || request.getBienes().isEmpty()) {
            errores.add("Debe incluir al menos un bien en la recepción.");
        } else {
            for (BienRecepcionDto bien : request.getBienes()) {
                if (bien.getCantidadRecibida() == null
                        || bien.getCantidadRecibida().compareTo(BigDecimal.ZERO) < 0) {
                    errores.add("La cantidad recibida del bien ID "
                            + bien.getIdContratoBien() + " no es válida.");
                }
            }
        }

        if (!errores.isEmpty()) {
            throw new ContratoValidacionException(errores);
        }
    }

    /**
     * Crea una AlmacenBienEntity por cada unidad física recibida.
     * Se llama después de persistir la recepción, cuando los RecepcionAlmacenBien
     * ya tienen ID asignado por la BD.
     *
     * Nota: cantidades fraccionarias se truncan a entero — los bienes medidos en
     * unidades continuas (litros, metros) generan 0 registros individuales si
     * cantidad < 1. El registro de RecepcionAlmacenBien preserva la cantidad exacta.
     */
    private void crearUnidadesAlmacen(ContratoEntity contrato, RecepcionAlmacenEntity recepcion) {
        String anio = recepcion.getFechaRecepcion().format(DateTimeFormatter.ofPattern("yyyy"));
        long baseSecuencial = almacenBienRepository.count();
        int indiceGlobal = 0;

        List<AlmacenBienEntity> unidades = new ArrayList<>();

        for (RecepcionAlmacenBienEntity recepcionBien : recepcion.getBienes()) {
            int cantidad = recepcionBien.getCantidadRecibida().intValue();
            for (int i = 1; i <= cantidad; i++) {
                String codigoInterno = String.format("AB-%s-%05d", anio, baseSecuencial + (++indiceGlobal));
                unidades.add(AlmacenBienEntity.builder()
                        .contrato(contrato)
                        .contratoBien(recepcionBien.getContratoBien())
                        .recepcionAlmacenBien(recepcionBien)
                        .codigoInterno(codigoInterno)
                        .estatus(EstatusBien.RECIBIDO)
                        .fechaRecepcion(recepcion.getFechaRecepcion())
                        .build());
            }
        }

        if (!unidades.isEmpty()) {
            almacenBienRepository.saveAll(unidades);
            logger.info("Creadas {} unidades en almacén para contrato ID: {}",
                    unidades.size(), contrato.getIdContrato());
        }
    }

    /** Suma de cantidades recibidas por bien en todas las recepciones activas del contrato. */
    private Map<Integer, BigDecimal> buildTotalesRecibidos(Integer idContrato) {
        return recepcionAlmacenBienRepository.sumCantidadRecibidaByContrato(idContrato)
                .stream()
                .collect(Collectors.toMap(
                        arr -> (Integer) arr[0],
                        arr -> (BigDecimal) arr[1]
                ));
    }

    /** Genera folio único: EA-{secuencial 4 dígitos}-{YYYY-MM-DD} */
    private String generarFolio() {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        long secuencial = recepcionAlmacenRepository.count() + 1;
        return String.format("EA-%04d-%s", secuencial, fecha);
    }
}
