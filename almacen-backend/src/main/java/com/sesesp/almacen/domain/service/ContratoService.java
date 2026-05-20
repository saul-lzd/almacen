package com.sesesp.almacen.domain.service;

import com.sesesp.almacen.common.exception.ContratoValidacionException;
import com.sesesp.almacen.domain.dto.ContratoCreateRequestDto;
import com.sesesp.almacen.domain.dto.ContratoDto;
import com.sesesp.almacen.domain.entity.ContratoEntity;
import com.sesesp.almacen.domain.entity.ContratoEntity.EstatusContrato;
import com.sesesp.almacen.domain.mapper.ContratoMapper;
import com.sesesp.almacen.domain.repository.ContratoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContratoService {

    private static final Logger logger = LoggerFactory.getLogger(ContratoService.class);

    private final ContratoRepository contratoRepository;
    private final ContratoMapper contratoMapper;

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

    public List<ContratoDto> findAllContratos() {
        return contratoRepository.findByActivoTrue()
                .stream()
                .map(contratoMapper::toResponse)
                .toList();
    }


    // ─────────────────────────────────────────────────────────
    // GET BY ID
    // ─────────────────────────────────────────────────────────

    public ContratoDto findContratoById(Integer idContrato) {
        return contratoMapper.toResponse(findContratoOrThrow(idContrato));
    }

    // ─────────────────────────────────────────────────────────
    // POST — Crear contrato
    // ─────────────────────────────────────────────────────────

    @Transactional
    public ContratoDto createContrato(ContratoCreateRequestDto request) {
        logger.info("Creando contrato: {}", request.getNumeroContrato());

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
    // Helpers privados
    // ─────────────────────────────────────────────────────────────

    private ContratoEntity findContratoOrThrow(Integer idContrato) {
        return contratoRepository.findById(idContrato)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Contrato no encontrado con ID: " + idContrato));
    }
}
