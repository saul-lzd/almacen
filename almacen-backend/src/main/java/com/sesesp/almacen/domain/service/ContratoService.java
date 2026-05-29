package com.sesesp.almacen.domain.service;

import com.sesesp.almacen.common.exception.ContratoValidacionException;
import com.sesesp.almacen.domain.dto.ActualizarFechaTentativaRequestDto;
import com.sesesp.almacen.domain.dto.ContratoCreateRequestDto;
import com.sesesp.almacen.domain.dto.ContratoDto;
import com.sesesp.almacen.domain.dto.ResumenBienesDto;
import com.sesesp.almacen.domain.entity.AlmacenBienEntity;
import com.sesesp.almacen.domain.entity.AlmacenBienEntity.EstatusBien;
import com.sesesp.almacen.domain.entity.ContratoEntity;
import com.sesesp.almacen.domain.entity.ContratoEntity.EstatusContrato;
import com.sesesp.almacen.domain.mapper.ContratoMapper;
import com.sesesp.almacen.domain.repository.AlmacenBienRepository;
import com.sesesp.almacen.domain.repository.ContratoRepository;
import com.sesesp.almacen.domain.repository.RecepcionAlmacenBienRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContratoService {

    private static final Logger logger = LoggerFactory.getLogger(ContratoService.class);

    private final ContratoRepository contratoRepository;
    private final ContratoMapper contratoMapper;
    private final RecepcionAlmacenBienRepository recepcionAlmacenBienRepository;
    private final AlmacenBienRepository almacenBienRepository;

    // Servicios delegados — cada uno es responsable de su propia entidad
    private final ProveedorService proveedorService;
    private final FuncionarioService funcionarioService;
    private final BeneficiarioService beneficiarioService;
    private final ClavePresupuestalService clavePresupuestalService;
    private final ContratoBienService contratoBienService;
    private final ContratoValidacionService contratoValidacionService;


    // ─────────────────────────────────────────────────────────
    // GET ALL
    // ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ContratoDto> findAllContratos() {
        List<ContratoEntity> contratos = contratoRepository.findByActivoTrue();
        List<ContratoDto> dtos = contratos.stream().map(contratoMapper::toResponse).toList();

        if (!contratos.isEmpty()) {
            List<Integer> ids = contratos.stream().map(ContratoEntity::getIdContrato).toList();
            Map<Integer, ResumenBienesDto> resumenes = buildResumenes(contratos, ids);
            dtos.forEach(dto -> dto.setResumenBienes(resumenes.get(dto.getIdContrato())));
        }
        return dtos;
    }


    // ─────────────────────────────────────────────────────────
    // GET BY ID
    // ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ContratoDto findContratoById(Integer idContrato) {
        ContratoEntity contrato = findContratoOrThrow(idContrato);
        ContratoDto dto = contratoMapper.toResponse(contrato);

        // Cantidades recibidas por bien (para recepciones parciales)
        Map<Integer, BigDecimal> totales = recepcionAlmacenBienRepository
                .sumCantidadRecibidaByContrato(idContrato)
                .stream()
                .collect(Collectors.toMap(arr -> (Integer) arr[0], arr -> (BigDecimal) arr[1]));
        if (!totales.isEmpty()) {
            dto.getBienes().forEach(b ->
                    b.setCantidadRecibidaTotal(totales.getOrDefault(b.getIdContratoBien(), BigDecimal.ZERO))
            );
        }

        // Resumen de bienes por estatus
        Map<Integer, ResumenBienesDto> resumenes = buildResumenes(List.of(contrato), List.of(idContrato));
        dto.setResumenBienes(resumenes.get(idContrato));

        return dto;
    }

    // ─────────────────────────────────────────────────────────
    // POST — Crear contrato
    // ─────────────────────────────────────────────────────────

    @Transactional
    public ContratoDto createContrato(ContratoCreateRequestDto request) {
        logger.info("Creando contrato: {}", request.getNumeroContrato());

        if (contratoRepository.existsByNumeroContratoAndActivoTrue(request.getNumeroContrato())) {
            throw new ContratoValidacionException(List.of(
                    "El número de contrato '" + request.getNumeroContrato() + "' ya existe."
            ));
        }

        // 1. Construir la entidad base del contrato
        ContratoEntity contrato = ContratoEntity.builder()
                .numeroContrato(request.getNumeroContrato())
                .adquisicion(request.getAdquisicion())
                .fechaTentativaLlegada(request.getFechaTentativaLlegada())
                .montoSinImpuestos(request.getMontoSinImpuestos())
                .impuestos(request.getImpuestos())
                .montoTotal(request.getMontoTotal())
                .estatus(EstatusContrato.CAPTURA)
                .build();

        // 2. Asignar proveedor
        contrato.setProveedor(proveedorService.crear(request.getProveedor()));


        // 3. Asignar comprador
        contrato.setComprador(funcionarioService.buscarPorId(
                request.getComprador() != null ? request.getComprador().getId() : null,
                "Comprador"));

        // 3. Asignar administrador
        contrato.setAdministradorContrato(funcionarioService.buscarPorId(
                request.getAdministradorContrato() != null ? request.getAdministradorContrato().getId() : null,
                "Administrador del contrato"));

        // 4. Guardar para obtener el ID generado
        ContratoEntity contratoGuardado = contratoRepository.save(contrato);

        // 5. Agregar relaciones hijas (beneficiarios, claves presupuestales y bienes)
        if (request.getBeneficiarios() != null && !request.getBeneficiarios().isBlank()) {
            contratoGuardado.getBeneficiarios()
                    .addAll(beneficiarioService.crear(contratoGuardado, request.getBeneficiarios()));
        }

        if (request.getClavesPresupuestales() != null && !request.getClavesPresupuestales().isEmpty()) {
            contratoGuardado.getClavesPresupuestales()
                    .addAll(clavePresupuestalService.crear(contratoGuardado, request.getClavesPresupuestales()));
        }

        if (request.getBienes() != null && !request.getBienes().isEmpty()) {
            contratoGuardado.getBienes()
                    .addAll(contratoBienService.crear(contratoGuardado, request.getBienes()));
        }

        // 6. Guardar con cascade para persistir las relaciones hijas
        contratoRepository.save(contratoGuardado);

        logger.info("Contrato creado con ID: {}", contratoGuardado.getIdContrato());
        return contratoMapper.toResponse(contratoGuardado);
    }

    // ─────────────────────────────────────────────────────────────
    // PUT — Actualizar contrato
    // ─────────────────────────────────────────────────────────────


    @Transactional
    public ContratoDto updateContrato(
            Integer idContrato,
            ContratoCreateRequestDto request) {
        logger.info("Actualizando contrato ID: {}", idContrato);

        // 1. Buscar el contrato existente y forzar carga de relaciones
        ContratoEntity contrato = findContratoOrThrow(idContrato);

        // Bloquear edición si el contrato ya fue enviado al almacén
        if (contrato.getEstatus() != ContratoEntity.EstatusContrato.CAPTURA) {
            throw new ContratoValidacionException(List.of(
                    "El contrato no puede editarse porque su estatus es: "
                            + contrato.getEstatus().name()
            ));
        }

        if (contratoRepository.existsByNumeroContratoAndActivoTrueAndIdContratoNot(
                request.getNumeroContrato(), idContrato)) {
            throw new ContratoValidacionException(List.of(
                    "El número de contrato '" + request.getNumeroContrato() + "' ya existe."
            ));
        }

        Hibernate.initialize(contrato.getBienes());
        Hibernate.initialize(contrato.getBeneficiarios());
        Hibernate.initialize(contrato.getClavesPresupuestales());

        // 2. Actualizar campos básicos directamente sobre el contrato encontrado
        contrato.setNumeroContrato(request.getNumeroContrato());
        contrato.setAdquisicion(request.getAdquisicion());
        contrato.setFechaTentativaLlegada(request.getFechaTentativaLlegada());
        contrato.setMontoSinImpuestos(request.getMontoSinImpuestos());
        contrato.setImpuestos(request.getImpuestos());
        contrato.setMontoTotal(request.getMontoTotal());

        // 3. Actualizar proveedor — mutamos el existente, no creamos uno nuevo
        contrato.setProveedor(
                proveedorService.actualizar(contrato.getProveedor(), request.getProveedor()));

        // 4. Actualizar comprador
        contrato.setComprador(funcionarioService.buscarPorId(
                request.getComprador() != null ? request.getComprador().getId() : null,
                "Comprador"));

        // 5. Actualizar administrador
        contrato.setAdministradorContrato(funcionarioService.buscarPorId(
                request.getAdministradorContrato() != null ? request.getAdministradorContrato().getId() : null,
                "Administrador del contrato"));

        // 6. Actualizar relaciones hijas (beneficiarios, claves presupuestales y bienes)
        beneficiarioService.sincronizar(contrato, request.getBeneficiarios());
        clavePresupuestalService.sincronizar(contrato, request.getClavesPresupuestales());
        contratoBienService.sincronizar(contrato, request.getBienes());

        // 7. Persistir todos los cambios
        contratoRepository.save(contrato);

        logger.info("Contrato actualizado: {}", idContrato);
        return contratoMapper.toResponse(contrato);
    }

    // ─────────────────────────────────────────────────────────────
    // PATCH — Enviar contrato a Almacen
    // ─────────────────────────────────────────────────────────────


    /**
     * Envía el contrato al almacén.
     *
     * Validaciones:
     *   - El contrato debe estar en estatus CAPTURA
     *   - Todos los campos obligatorios deben estar completos
     *
     * Efecto:
     *   - Estatus cambia a POR_RECIBIR
     *   - El contrato queda bloqueado para edición
     */
    @Transactional
    public void enviarAlmacen(Integer idContrato) {
        logger.info("Enviando contrato al almacén. ID: {}", idContrato);

        ContratoEntity contrato = findContratoOrThrow(idContrato);

        // Verificar que el contrato esté en estatus correcto para esta transición
        if (contrato.getEstatus() != ContratoEntity.EstatusContrato.CAPTURA) {
            throw new ContratoValidacionException(List.of(
                    "El contrato no puede enviarse al almacén porque su estatus es: "
                            + contrato.getEstatus().name()
                            + ". Solo se pueden enviar contratos en estatus CAPTURA."
            ));
        }

        // Forzar carga de listas lazy antes de validar
        Hibernate.initialize(contrato.getBeneficiarios());
        Hibernate.initialize(contrato.getClavesPresupuestales());
        Hibernate.initialize(contrato.getBienes());

        // Validar que todos los campos estén completos
        contratoValidacionService.validarParaEnviarAlmacen(contrato);

        // Cambiar estatus
        contrato.setEstatus(ContratoEntity.EstatusContrato.POR_RECIBIR);
        contratoRepository.save(contrato);

        logger.info("Contrato ID: {} enviado al almacén. Estatus: POR_RECIBIR", idContrato);
    }


    // ─────────────────────────────────────────────────────────────
    // PATCH — Autorizar entrega
    // ─────────────────────────────────────────────────────────────

    /**
     * Autoriza el contrato para entrega a beneficiarios.
     *
     * Validaciones:
     *   - El contrato debe estar en estatus EN_ALMACEN
     *   - Todos los bienes recibidos deben estar en estatus PROCESADO (ninguno en RECIBIDO)
     *
     * Efecto:
     *   - Estatus cambia a LISTO_PARA_ENTREGAR
     */
    @Transactional
    public void autorizarEntrega(Integer idContrato) {
        logger.info("Autorizando entrega del contrato ID: {}", idContrato);

        ContratoEntity contrato = findContratoOrThrow(idContrato);

        if (contrato.getEstatus() != EstatusContrato.EN_ALMACEN
                && contrato.getEstatus() != EstatusContrato.RECEPCION_PARCIAL) {
            throw new ContratoValidacionException(List.of(
                    "El contrato no puede autorizarse porque su estatus es: "
                            + contrato.getEstatus().name()
                            + ". Solo contratos EN_ALMACEN o RECEPCION_PARCIAL pueden autorizarse para entrega."
            ));
        }

        long sinProcesar = almacenBienRepository
                .countByContratoIdContratoAndEstatusAndActivoTrue(idContrato, EstatusBien.RECIBIDO)
                + almacenBienRepository
                .countByContratoIdContratoAndEstatusAndActivoTrue(idContrato, EstatusBien.EN_PROCESO);

        if (sinProcesar > 0) {
            throw new ContratoValidacionException(List.of(
                    "No se puede autorizar la entrega: quedan " + sinProcesar
                            + (sinProcesar == 1 ? " bien sin procesar." : " bienes sin procesar.")
            ));
        }

        long procesados = almacenBienRepository
                .countByContratoIdContratoAndEstatusAndActivoTrue(idContrato, EstatusBien.PROCESADO);

        if (procesados == 0) {
            throw new ContratoValidacionException(List.of(
                    "No hay bienes procesados disponibles para autorizar."
            ));
        }

        // Cambiar todos los PROCESADO → LISTO_PARA_ENTREGAR
        List<AlmacenBienEntity> bienes = almacenBienRepository
                .findByContratoIdContratoAndEstatusAndActivoTrue(idContrato, EstatusBien.PROCESADO);
        bienes.forEach(b -> b.setEstatus(EstatusBien.LISTO_PARA_ENTREGAR));
        almacenBienRepository.saveAll(bienes);

        // El contrato avanza a LISTO_PARA_ENTREGAR solo si ya se recibió todo (EN_ALMACEN).
        // Si aún hay recepciones pendientes (RECEPCION_PARCIAL), el estatus del contrato no cambia.
        if (contrato.getEstatus() == EstatusContrato.EN_ALMACEN) {
            contrato.setEstatus(EstatusContrato.LISTO_PARA_ENTREGAR);
            contratoRepository.save(contrato);
        }

        logger.info("Contrato ID: {} — {} bienes autorizados para entrega.", idContrato, procesados);
    }

    // ─────────────────────────────────────────────────────────────
    // PATCH — Actualizar fecha tentativa de llegada
    // ─────────────────────────────────────────────────────────────

    /**
     * Permite al admin corregir la fecha tentativa de llegada del proveedor.
     * Solo es posible cuando el contrato está en POR_RECIBIR —
     * una vez que llega el primer lote (RECEPCION_PARCIAL en adelante) queda bloqueado.
     */
    @Transactional
    public void actualizarFechaTentativa(Integer idContrato, ActualizarFechaTentativaRequestDto request) {
        ContratoEntity contrato = findContratoOrThrow(idContrato);

        if (contrato.getEstatus() != EstatusContrato.POR_RECIBIR) {
            throw new ContratoValidacionException(List.of(
                    "La fecha tentativa de llegada solo puede modificarse cuando el contrato está en estatus POR_RECIBIR."
            ));
        }

        contrato.setFechaTentativaLlegada(request.getFechaTentativaLlegada());
        contratoRepository.save(contrato);
        logger.info("Contrato ID: {} — fecha tentativa actualizada a {}", idContrato, request.getFechaTentativaLlegada());
    }

    // ─────────────────────────────────────────────────────────────
    // Helpers privados
    // ─────────────────────────────────────────────────────────────

    private ContratoEntity findContratoOrThrow(Integer idContrato) {
        return contratoRepository.findById(idContrato)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Contrato no encontrado con ID: " + idContrato));
    }

    /**
     * Construye el resumen de bienes por estatus para un conjunto de contratos.
     * Usa una sola query GROUP BY para evitar N+1.
     */
    private Map<Integer, ResumenBienesDto> buildResumenes(List<ContratoEntity> contratos, List<Integer> ids) {
        // totalContratados: suma de ContratoBien.cantidad por contrato
        Map<Integer, Long> totales = contratos.stream().collect(Collectors.toMap(
                ContratoEntity::getIdContrato,
                c -> c.getBienes().stream()
                        .filter(b -> Boolean.TRUE.equals(b.getActivo()))
                        .mapToLong(b -> b.getCantidad() != null ? b.getCantidad().longValue() : 0L)
                        .sum()
        ));

        // Conteos de AlmacenBien agrupados por contrato y estatus
        Map<Integer, Map<String, Long>> countMap = new HashMap<>();
        for (Object[] row : almacenBienRepository.countByContratosGroupByEstatus(ids)) {
            Integer idContrato = (Integer) row[0];
            String estatus     = ((EstatusBien) row[1]).name();
            Long   count       = (Long) row[2];
            countMap.computeIfAbsent(idContrato, k -> new HashMap<>()).put(estatus, count);
        }

        Map<Integer, ResumenBienesDto> result = new HashMap<>();
        for (ContratoEntity c : contratos) {
            Integer id = c.getIdContrato();
            Map<String, Long> s = countMap.getOrDefault(id, Map.of());
            long enProceso  = s.getOrDefault("RECIBIDO", 0L) + s.getOrDefault("EN_PROCESO", 0L);
            long procesados = s.getOrDefault("PROCESADO", 0L);
            long listos     = s.getOrDefault("LISTO_PARA_ENTREGAR", 0L);
            long entregados = s.getOrDefault("ENTREGADO", 0L);
            result.put(id, ResumenBienesDto.builder()
                    .totalContratados(totales.getOrDefault(id, 0L))
                    .totalRecibidos(enProceso + procesados + listos + entregados)
                    .enProceso(enProceso)
                    .procesados(procesados)
                    .listos(listos)
                    .entregados(entregados)
                    .build());
        }
        return result;
    }
}
