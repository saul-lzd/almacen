package com.sesesp.almacen.domain.service;

import com.sesesp.almacen.common.SESESP_UTILS;
import com.sesesp.almacen.common.exception.ContratoValidacionException;
import com.sesesp.almacen.common.util.SecurityUtils;
import com.sesesp.almacen.domain.dto.AlmacenBienDto;
import com.sesesp.almacen.domain.dto.AlmacenBienGrupoDto;
import com.sesesp.almacen.domain.dto.ActualizarDatosBienRequestDto;
import com.sesesp.almacen.domain.dto.ComponenteBienDto;
import com.sesesp.almacen.domain.dto.ComponenteBienRequestDto;
import com.sesesp.almacen.domain.dto.DefinirCapturaSerieRequestDto;
import com.sesesp.almacen.domain.dto.ProcesarBienRequestDto;
import com.sesesp.almacen.domain.dto.ProcesarBloqueRequestDto;
import com.sesesp.almacen.domain.entity.AlmacenBienEntity;
import com.sesesp.almacen.domain.entity.AlmacenBienEntity.EstatusBien;
import com.sesesp.almacen.domain.entity.CatalogoComponenteEntity;
import com.sesesp.almacen.domain.entity.ComponenteBienEntity;
import com.sesesp.almacen.domain.entity.ContratoBienComponenteEntity;
import com.sesesp.almacen.domain.entity.ContratoBienEntity;
import com.sesesp.almacen.domain.entity.EvidenciaBienEntity;
import com.sesesp.almacen.domain.entity.EvidenciaContratoBienEntity;
import com.sesesp.almacen.domain.entity.RecepcionAlmacenBienEntity;
import com.sesesp.almacen.domain.entity.RecepcionAlmacenEntity;
import com.sesesp.almacen.domain.entity.RecepcionAlmacenEntity.EstatusRecepcion;
import com.sesesp.almacen.domain.repository.AlmacenBienRepository;
import com.sesesp.almacen.domain.repository.CatalogoComponenteRepository;
import com.sesesp.almacen.domain.repository.ComponenteBienRepository;
import com.sesesp.almacen.domain.repository.ContratoBienComponenteRepository;
import com.sesesp.almacen.domain.repository.ContratoBienRepository;
import com.sesesp.almacen.domain.repository.ContratoRepository;
import com.sesesp.almacen.domain.repository.EvidenciaBienRepository;
import com.sesesp.almacen.domain.repository.EvidenciaContratoBienRepository;
import com.sesesp.almacen.domain.repository.RecepcionAlmacenRepository;
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
    private final RecepcionAlmacenRepository recepcionAlmacenRepository;
    private final ContratoBienRepository contratoBienRepository;
    private final ContratoBienComponenteRepository contratoBienComponenteRepository;
    private final ComponenteBienRepository componenteBienRepository;
    private final EvidenciaBienRepository evidenciaBienRepository;
    private final EvidenciaContratoBienRepository evidenciaContratoBienRepository;
    private final CatalogoComponenteRepository catalogoComponenteRepository;
    private final S3StorageService s3StorageService;

    @Transactional(readOnly = true)
    public List<AlmacenBienGrupoDto> getBienesAgrupados(Integer idContrato) {
        contratoRepository.findById(idContrato)
                .orElseThrow(() -> new EntityNotFoundException("Contrato no encontrado: " + idContrato));

        return agrupar(almacenBienRepository.findByContratoWithFetch(
                idContrato, List.of(EstatusBien.RECIBIDO, EstatusBien.EN_PROCESO)));
    }

    @Transactional(readOnly = true)
    public List<AlmacenBienGrupoDto> getBienesAgrupadosPorRecepcion(Integer idContrato, Integer idRecepcion) {
        contratoRepository.findById(idContrato)
                .orElseThrow(() -> new EntityNotFoundException("Contrato no encontrado: " + idContrato));

        return agrupar(almacenBienRepository.findByContratoAndRecepcionWithFetch(
                idContrato, idRecepcion,
                List.of(EstatusBien.RECIBIDO, EstatusBien.EN_PROCESO)));
    }

    @Transactional(readOnly = true)
    public List<AlmacenBienGrupoDto> getBienesDetalleRecepcion(Integer idContrato, Integer idRecepcion) {
        contratoRepository.findById(idContrato)
                .orElseThrow(() -> new EntityNotFoundException("Contrato no encontrado: " + idContrato));

        return agrupar(almacenBienRepository.findAllByContratoAndRecepcion(idContrato, idRecepcion));
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
                            .descripcion(SESESP_UTILS.stripHtml(cb.getDescripcionTecnica(), SESESP_UTILS.DESCRIPCION_CORTA_BIEN_MAX_LENGTH))
                            .unidadMedida(cb.getUnidadMedida().getNombre())
                            .totalUnidades(unidades.size())
                            .unidades(unidades)
                            .tipoCapturaSerie(cb.getTipoCapturaSerie().name())
                            .componentesEsperados(cb.getComponentesEsperados().stream()
                                    .sorted(Comparator.comparing(ContratoBienComponenteEntity::getOrden))
                                    .map(ContratoBienComponenteEntity::getNombreComponente)
                                    .toList())
                            .totalEvidenciasGrupo(cb.getEvidencias().size())
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

        if (request.getNumeroSerie() != null && !request.getNumeroSerie().isBlank()) {
            validarUnicidadSerie(List.of(request.getNumeroSerie().trim()), id);
            bien.setNumeroSerie(request.getNumeroSerie().trim());
        }
        if (request.getNumeroMotor() != null && !request.getNumeroMotor().isBlank()) {
            validarUnicidadMotor(List.of(request.getNumeroMotor().trim()), id);
            bien.setNumeroMotor(request.getNumeroMotor().trim());
        }
        Optional.ofNullable(request.getMarca()).ifPresent(bien::setMarca);
        Optional.ofNullable(request.getModelo()).ifPresent(bien::setModelo);
        Optional.ofNullable(request.getDescripcionComplementaria()).ifPresent(bien::setDescripcionComplementaria);

        if (bien.getEstatus() != EstatusBien.EN_PROCESO) {
            bien.setEstatus(EstatusBien.EN_PROCESO);
            avanzarRecepcionAEnProceso(bien);
        }

        almacenBienRepository.save(bien);
    }

    /**
     * Marca el bien como PROCESADO. Acepta RECIBIDO y EN_PROCESO.
     * RECIBIDO → PROCESADO (transición directa, sin pasar por EN_PROCESO explícito).
     */
    @Transactional
    public void procesarBien(Integer id, ProcesarBienRequestDto request) {
        AlmacenBienEntity bien = almacenBienRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Bien no encontrado: " + id));

        if (bien.getEstatus() != EstatusBien.EN_PROCESO && bien.getEstatus() != EstatusBien.RECIBIDO) {
            throw new ContratoValidacionException(List.of(
                    "El bien " + bien.getCodigoInterno()
                            + " debe estar RECIBIDO o EN_PROCESO para poder marcarse como procesado (estatus actual: "
                            + bien.getEstatus() + ")."));
        }

        if (request.getNumeroSerie() != null && !request.getNumeroSerie().isBlank()) {
            validarUnicidadSerie(List.of(request.getNumeroSerie().trim()), id);
            bien.setNumeroSerie(request.getNumeroSerie().trim());
        }
        if (request.getNumeroMotor() != null && !request.getNumeroMotor().isBlank()) {
            validarUnicidadMotor(List.of(request.getNumeroMotor().trim()), id);
            bien.setNumeroMotor(request.getNumeroMotor().trim());
        }
        Optional.ofNullable(request.getMarca()).ifPresent(bien::setMarca);
        Optional.ofNullable(request.getModelo()).ifPresent(bien::setModelo);
        Optional.ofNullable(request.getDescripcionComplementaria()).ifPresent(bien::setDescripcionComplementaria);
        avanzarRecepcionAEnProceso(bien);
        bien.setEstatus(EstatusBien.PROCESADO);
        bien.setFechaProcesamiento(LocalDateTime.now());

        almacenBienRepository.save(bien);

        verificarRecepcionProcesada(bien);
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
                .filter(b -> b.getEstatus() == EstatusBien.LISTO_PARA_ENTREGAR
                          || b.getEstatus() == EstatusBien.ENTREGADO)
                .map(b -> "El bien " + b.getCodigoInterno() + " no puede re-procesarse en estatus " + b.getEstatus() + ".")
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

        verificarRecepcionProcesada(bienes.get(0));
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
                .totalEvidencias(ab.getEvidencias().size())
                .componentes(ab.getComponentes().stream()
                        .map(c -> ComponenteBienDto.builder()
                                .idComponenteBien(c.getIdComponenteBien())
                                .nombreComponente(c.getNombreComponente())
                                .numeroSerie(c.getNumeroSerie())
                                .tieneFoto(c.getUrl() != null)
                                .build())
                        .toList())
                .build();
    }

    // ================================================================
    // Evidencias de bienes procesados — grupo (catálogo), unidad y componentes
    // ================================================================

    /**
     * Define qué se captura por unidad en este grupo (Ninguno/Simple/Conjunto)
     * y, si es Conjunto, la plantilla de nombres de componente esperados.
     * Reemplaza por completo la plantilla anterior — se llama cada vez que el
     * almacenista ajusta la configuración del grupo antes de procesar unidades.
     */
    @Transactional
    public void definirCapturaSerie(Integer idContratoBien, DefinirCapturaSerieRequestDto request) {
        ContratoBienEntity cb = contratoBienRepository.findById(idContratoBien)
                .orElseThrow(() -> new EntityNotFoundException("Bien de contrato no encontrado: " + idContratoBien));

        ContratoBienEntity.TipoCapturaSerie tipo;
        try {
            tipo = ContratoBienEntity.TipoCapturaSerie.valueOf(request.getTipoCapturaSerie());
        } catch (Exception e) {
            throw new ContratoValidacionException(List.of(
                    "Tipo de captura de serie inválido: " + request.getTipoCapturaSerie()));
        }

        cb.setTipoCapturaSerie(tipo);
        cb.getComponentesEsperados().clear();

        if (tipo == ContratoBienEntity.TipoCapturaSerie.CONJUNTO) {
            List<String> nombres = request.getComponentes();
            if (nombres == null || nombres.stream().noneMatch(n -> n != null && !n.isBlank())) {
                throw new ContratoValidacionException(List.of(
                        "Debes indicar al menos un componente para un bien tipo Conjunto."));
            }
            short orden = 0;
            for (String nombre : nombres) {
                if (nombre == null || nombre.isBlank()) continue;
                String limpio = nombre.trim();
                cb.getComponentesEsperados().add(ContratoBienComponenteEntity.builder()
                        .contratoBien(cb)
                        .nombreComponente(limpio)
                        .orden(orden++)
                        .build());
                registrarEnCatalogoComponentes(limpio);
            }
        }

        contratoBienRepository.save(cb);
    }

    @Transactional(readOnly = true)
    public List<String> getCatalogoComponentes() {
        return catalogoComponenteRepository.findByActivoTrueOrderByNombreAsc().stream()
                .map(CatalogoComponenteEntity::getNombre)
                .toList();
    }

    private void registrarEnCatalogoComponentes(String nombre) {
        if (catalogoComponenteRepository.findByNombreIgnoreCase(nombre).isEmpty()) {
            catalogoComponenteRepository.save(CatalogoComponenteEntity.builder().nombre(nombre).build());
        }
    }

    /**
     * Sube fotos "de catálogo" del tipo de bien (mínimo 5, acumulativo — se puede
     * volver a llamar para agregar más antes de llegar al mínimo).
     */
    @Transactional
    public void subirEvidenciaGrupo(Integer idContratoBien, List<MultipartFile> evidencias) {
        ContratoBienEntity cb = contratoBienRepository.findById(idContratoBien)
                .orElseThrow(() -> new EntityNotFoundException("Bien de contrato no encontrado: " + idContratoBien));

        if (evidencias == null || evidencias.isEmpty()) {
            throw new ContratoValidacionException(List.of("Debes adjuntar al menos una foto."));
        }
        int totalPrevio = cb.getEvidencias().size();
        if (totalPrevio + evidencias.size() > SESESP_UTILS.MAX_EVIDENCIAS_BIEN_GRUPO) {
            throw new ContratoValidacionException(List.of(
                    "No puedes superar " + SESESP_UTILS.MAX_EVIDENCIAS_BIEN_GRUPO + " evidencias por bien (ya hay " + totalPrevio + ")."));
        }

        String folderContrato = cb.getContrato().getNumeroContrato().replaceAll("/", "_");
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd"));
        String base = String.format("%s_%0" + SESESP_UTILS.DIGITOS_CONSECUTIVO_BIEN + "d_%s",
                SESESP_UTILS.PREFIJO_EVIDENCIA_BIEN_PROCESADO, idContratoBien, fecha);

        List<EvidenciaContratoBienEntity> nuevas = new ArrayList<>();
        int numeroProgresivo = totalPrevio + 1;
        for (MultipartFile file : evidencias) {
            String nombreEnS3 = base + "_IMG_" + numeroProgresivo + extraerExtension(file.getOriginalFilename());
            String key = s3StorageService.getPrefixEvidencias() + "/" + folderContrato + "/evidencia/bienes/" + nombreEnS3;
            String url = s3StorageService.uploadEvidencia(file, key);
            nuevas.add(EvidenciaContratoBienEntity.builder()
                    .contratoBien(cb)
                    .url(url)
                    .nombreArchivo(file.getOriginalFilename())
                    .fechaCaptura(LocalDateTime.now())
                    .usuarioCaptura(SecurityUtils.getCurrentUserId())
                    .build());
            numeroProgresivo++;
        }
        evidenciaContratoBienRepository.saveAll(nuevas);
    }

    /**
     * Sube fotos de una unidad individual — Vehículo (mínimo 5, fotos generales)
     * o Simple (mínimo 1, etiqueta del número de serie/motor). Acumulativo.
     */
    @Transactional
    public void subirEvidenciaUnidad(Integer idAlmacenBien, List<MultipartFile> evidencias) {
        AlmacenBienEntity bien = almacenBienRepository.findById(idAlmacenBien)
                .orElseThrow(() -> new EntityNotFoundException("Bien no encontrado: " + idAlmacenBien));

        if (evidencias == null || evidencias.isEmpty()) {
            throw new ContratoValidacionException(List.of("Debes adjuntar al menos una foto."));
        }
        int totalPrevio = bien.getEvidencias().size();
        if (totalPrevio + evidencias.size() > SESESP_UTILS.MAX_EVIDENCIAS_BIEN_UNIDAD) {
            throw new ContratoValidacionException(List.of(
                    "No puedes superar " + SESESP_UTILS.MAX_EVIDENCIAS_BIEN_UNIDAD + " evidencias por unidad (ya hay " + totalPrevio + ")."));
        }

        String folderContrato = bien.getContrato().getNumeroContrato().replaceAll("/", "_");
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd"));
        String base = String.format("%s_%0" + SESESP_UTILS.DIGITOS_CONSECUTIVO_BIEN + "d_%s",
                SESESP_UTILS.PREFIJO_EVIDENCIA_BIEN_PROCESADO, idAlmacenBien, fecha);

        List<EvidenciaBienEntity> nuevas = new ArrayList<>();
        int numeroProgresivo = totalPrevio + 1;
        for (MultipartFile file : evidencias) {
            String nombreEnS3 = base + "_IMG_" + numeroProgresivo + extraerExtension(file.getOriginalFilename());
            String key = s3StorageService.getPrefixEvidencias() + "/" + folderContrato + "/evidencia/bienes/" + nombreEnS3;
            String url = s3StorageService.uploadEvidencia(file, key);
            nuevas.add(EvidenciaBienEntity.builder()
                    .almacenBien(bien)
                    .url(url)
                    .nombreArchivo(file.getOriginalFilename())
                    .fechaCaptura(LocalDateTime.now())
                    .usuarioCaptura(SecurityUtils.getCurrentUserId())
                    .build());
            numeroProgresivo++;
        }
        evidenciaBienRepository.saveAll(nuevas);
    }

    /**
     * Guarda los componentes (número de serie + foto de etiqueta) de una unidad
     * "conjunto" — reemplaza por completo los componentes previos de esa unidad.
     * Requiere exactamente una foto por componente, en el mismo orden.
     */
    @Transactional
    public void guardarComponentesUnidad(Integer idAlmacenBien, List<ComponenteBienRequestDto> componentes,
                                          List<MultipartFile> evidencias) {
        AlmacenBienEntity bien = almacenBienRepository.findById(idAlmacenBien)
                .orElseThrow(() -> new EntityNotFoundException("Bien no encontrado: " + idAlmacenBien));

        if (componentes == null || componentes.isEmpty()) {
            throw new ContratoValidacionException(List.of("Debes capturar al menos un componente."));
        }
        int totalFotos = evidencias == null ? 0 : evidencias.size();
        if (totalFotos != componentes.size()) {
            throw new ContratoValidacionException(List.of(
                    "Debes adjuntar exactamente una foto por cada componente (" + componentes.size()
                            + " esperadas, " + totalFotos + " recibidas)."));
        }

        List<String> errores = new ArrayList<>();
        List<String> seriesNuevas = new ArrayList<>();
        for (ComponenteBienRequestDto c : componentes) {
            if (c.getNombreComponente() == null || c.getNombreComponente().isBlank()) {
                errores.add("Falta el nombre de un componente.");
            }
            if (c.getNumeroSerie() == null || c.getNumeroSerie().isBlank()) {
                errores.add("Falta el número de serie del componente "
                        + (c.getNombreComponente() == null ? "" : c.getNombreComponente()) + ".");
            } else {
                seriesNuevas.add(c.getNumeroSerie().trim());
            }
        }
        if (!errores.isEmpty()) throw new ContratoValidacionException(errores);

        validarUnicidadSerie(seriesNuevas, idAlmacenBien);

        // Reemplaza por completo los componentes previos de esta unidad — hard delete
        // (no soft-delete: el índice único de numero_serie debe liberarse de inmediato
        // para permitir re-guardar el mismo valor sin chocar contra el registro viejo).
        componenteBienRepository.deleteAll(
                componenteBienRepository.findByAlmacenBienIdAlmacenBienAndActivoTrue(idAlmacenBien));

        String folderContrato = bien.getContrato().getNumeroContrato().replaceAll("/", "_");
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd"));

        List<ComponenteBienEntity> nuevos = new ArrayList<>();
        for (int i = 0; i < componentes.size(); i++) {
            ComponenteBienRequestDto c = componentes.get(i);
            MultipartFile file = evidencias.get(i);
            String nombreComponenteLimpio = c.getNombreComponente().trim();
            String nombreArchivoBase = nombreComponenteLimpio.replaceAll("[^a-zA-Z0-9]+", "_");
            String nombreEnS3 = String.format("%s_%0" + SESESP_UTILS.DIGITOS_CONSECUTIVO_BIEN + "d_%s_%s%s",
                    SESESP_UTILS.PREFIJO_EVIDENCIA_BIEN_PROCESADO, idAlmacenBien, nombreArchivoBase, fecha,
                    extraerExtension(file.getOriginalFilename()));
            String key = s3StorageService.getPrefixEvidencias() + "/" + folderContrato + "/evidencia/bienes/" + nombreEnS3;
            String url = s3StorageService.uploadEvidencia(file, key);

            nuevos.add(ComponenteBienEntity.builder()
                    .almacenBien(bien)
                    .nombreComponente(nombreComponenteLimpio)
                    .numeroSerie(c.getNumeroSerie().trim())
                    .url(url)
                    .nombreArchivo(file.getOriginalFilename())
                    .fechaCaptura(LocalDateTime.now())
                    .usuarioCaptura(SecurityUtils.getCurrentUserId())
                    .build());

            registrarEnCatalogoComponentes(nombreComponenteLimpio);
        }
        componenteBienRepository.saveAll(nuevos);
    }

    /**
     * Valida que ninguno de los números de serie ya exista en otro bien —
     * ni como AlmacenBienEntity.numeroSerie (caso Simple) ni como
     * ComponenteBienEntity.numeroSerie (caso Conjunto). Excluye los registros
     * que ya pertenecen a idAlmacenBienExcluir, para permitir re-guardar sin
     * cambios el mismo valor que la unidad ya tenía.
     */
    private void validarUnicidadSerie(List<String> series, Integer idAlmacenBienExcluir) {
        if (series.isEmpty()) return;

        List<String> duplicados = new ArrayList<>();

        almacenBienRepository.findByNumeroSerieInAndActivoTrue(series).stream()
                .filter(ab -> !ab.getIdAlmacenBien().equals(idAlmacenBienExcluir))
                .forEach(ab -> duplicados.add(ab.getNumeroSerie()));

        componenteBienRepository.findByNumeroSerieInAndActivoTrue(series).stream()
                .filter(c -> !c.getAlmacenBien().getIdAlmacenBien().equals(idAlmacenBienExcluir))
                .forEach(c -> duplicados.add(c.getNumeroSerie()));

        if (!duplicados.isEmpty()) {
            throw new ContratoValidacionException(List.of(
                    "Los siguientes números de serie ya están registrados en otro bien: "
                            + String.join(", ", duplicados) + "."));
        }
    }

    /**
     * Valida que ninguno de los números de motor ya exista en otro vehículo,
     * indicando en qué contrato se encontró cada duplicado.
     */
    private void validarUnicidadMotor(List<String> motores, Integer idAlmacenBienExcluir) {
        if (motores.isEmpty()) return;

        List<String> duplicados = almacenBienRepository.findByNumeroMotorInWithContrato(motores).stream()
                .filter(ab -> !ab.getIdAlmacenBien().equals(idAlmacenBienExcluir))
                .map(ab -> ab.getNumeroMotor() + " (contrato " + ab.getContrato().getNumeroContrato() + ")")
                .toList();

        if (!duplicados.isEmpty()) {
            throw new ContratoValidacionException(List.of(
                    "Los siguientes números de motor ya están registrados: " + String.join(", ", duplicados) + "."));
        }
    }

    private static String extraerExtension(String originalFilename) {
        if (originalFilename == null) return "";
        int dot = originalFilename.lastIndexOf('.');
        return dot >= 0 ? originalFilename.substring(dot) : "";
    }

    /** Si la recepción está en INICIADA, la avanza a EN_PROCESO. */
    private void avanzarRecepcionAEnProceso(AlmacenBienEntity bien) {
        RecepcionAlmacenEntity recepcion = getRecepcion(bien);
        if (recepcion != null && recepcion.getEstatus() == EstatusRecepcion.INICIADA) {
            recepcion.setEstatus(EstatusRecepcion.EN_PROCESO);
            recepcionAlmacenRepository.save(recepcion);
            logger.info("Recepción {} → EN_PROCESO", recepcion.getFolioEntradaAlmacen());
        }
    }

    /** Si todos los bienes de la recepción están en PROCESADO, avanza a PROCESADA. */
    private void verificarRecepcionProcesada(AlmacenBienEntity bien) {
        RecepcionAlmacenEntity recepcion = getRecepcion(bien);
        if (recepcion == null || recepcion.getEstatus() == EstatusRecepcion.PROCESADA) return;

        almacenBienRepository.flush();

        Integer idRecepcion = recepcion.getIdRecepcionAlmacen();
        long total = almacenBienRepository
                .countByRecepcionAlmacenBienRecepcionAlmacenIdRecepcionAlmacenAndActivoTrue(idRecepcion);
        long sinProcesar = almacenBienRepository
                .countByRecepcionAlmacenBienRecepcionAlmacenIdRecepcionAlmacenAndEstatusInAndActivoTrue(
                        idRecepcion, List.of(EstatusBien.RECIBIDO, EstatusBien.EN_PROCESO));

        if (total > 0 && sinProcesar == 0) {
            recepcion.setEstatus(EstatusRecepcion.PROCESADA);
            recepcionAlmacenRepository.save(recepcion);
            logger.info("Recepción {} → PROCESADA", recepcion.getFolioEntradaAlmacen());
        }
    }

    private RecepcionAlmacenEntity getRecepcion(AlmacenBienEntity bien) {
        if (bien.getRecepcionAlmacenBien() == null) return null;
        return bien.getRecepcionAlmacenBien().getRecepcionAlmacen();
    }

}
