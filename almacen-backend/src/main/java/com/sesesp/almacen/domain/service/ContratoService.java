package com.sesesp.almacen.domain.service;

import com.sesesp.almacen.domain.dto.ClavePresupuestalDto;
import com.sesesp.almacen.domain.dto.ContratoBienDto;
import com.sesesp.almacen.domain.dto.ContratoCreateRequestDto;
import com.sesesp.almacen.domain.dto.ContratoDto;
import com.sesesp.almacen.domain.entity.*;
import com.sesesp.almacen.domain.mapper.ContratoMapper;
import com.sesesp.almacen.domain.mapper.FuncionarioMapper;
import com.sesesp.almacen.domain.mapper.ProveedorMapper;
import com.sesesp.almacen.domain.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContratoService {

    private static final Logger logger = LoggerFactory.getLogger(ContratoService.class);


    private final ContratoRepository contratoRepository;
    private final ProveedorRepository proveedorRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final BeneficiarioRepository beneficiarioRepository;
    private final ClavePresupuestalRepository clavePresupuestalRepository;
    private final UnidadMedidaRepository unidadMedidaRepository;

    private final ContratoMapper contratoMapper;
    private final ProveedorMapper proveedorMapper;
    private final FuncionarioMapper funcionarioMapper;


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
        ContratoEntity contrato = findContratoOrThrow(idContrato);
        return contratoMapper.toResponse(contrato);
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
                .estatus(ContratoEntity.EstatusContrato.CAPTURA)
                .build();

        // 2. Asignar proveedor (se crea nuevo por cada contrato — los datos vienen del contrato físico)
        if (request.getProveedor() != null) {
            ProveedorEntity proveedor = proveedorMapper.toEntity(request.getProveedor());
            contrato.setProveedor(proveedorRepository.save(proveedor));
        }

        // 3. Asignar comprador (se busca por ID — viene precargado del catálogo de funcionarios)
        if (request.getComprador() != null && request.getComprador().getId() != null) {
            contrato.setComprador(
                    funcionarioRepository.findById(request.getComprador().getId())
                            .orElseThrow(() -> new EntityNotFoundException(
                                    "Comprador no encontrado con ID: " + request.getComprador().getId()))
            );
        }

        // 4. Asignar administrador del contrato (mismo criterio que comprador)
        if (request.getAdministradorContrato() != null && request.getAdministradorContrato().getId() != null) {
            contrato.setAdministradorContrato(
                    funcionarioRepository.findById(request.getAdministradorContrato().getId())
                            .orElseThrow(() -> new EntityNotFoundException(
                                    "Administrador no encontrado con ID: " + request.getAdministradorContrato().getId()))
            );
        }

        // 5. Guardar contrato para obtener el ID generado
        ContratoEntity contratoGuardado = contratoRepository.save(contrato);

        // 6. Beneficiarios — buscar o crear por nombre, luego crear la relación
        if (request.getBeneficiarios() != null && !request.getBeneficiarios().isBlank()) {
            List<ContratoBeneficiarioEntity> relaciones = crearBeneficiarios(contratoGuardado, request.getBeneficiarios());
            contratoGuardado.getBeneficiarios().addAll(relaciones);
        }

        // 7. Claves presupuestales — se buscan del catálogo por clave alfanumérica
        if (request.getClavesPresupuestales() != null && !request.getClavesPresupuestales().isEmpty()) {
            List<ContratoClavePresupuestalEntity> claves = crearClavesPresupuestales(contratoGuardado, request.getClavesPresupuestales());
            contratoGuardado.getClavesPresupuestales().addAll(claves);
        }

        // 8. Bienes — se crea uno por cada bien del request
        if (request.getBienes() != null && !request.getBienes().isEmpty()) {
            List<ContratoBienEntity> bienes = crearBienes(contratoGuardado, request.getBienes());
            contratoGuardado.getBienes().addAll(bienes);
        }

        // Un solo save persiste todo — el cascade propaga a las tablas hijas
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

        // 1. Buscar el contrato existente
        ContratoEntity contrato = findContratoOrThrow(idContrato);

        // Forzar la carga de las listas lazy ANTES de cualquier modificación
        // Esto garantiza que JPA trabaja con los objetos correctos en memoria
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
        actualizarProveedor(contrato, request);

        // 4. Actualizar comprador — buscar por ID en el catálogo
        actualizarFuncionario(contrato, request);

        // 5. Sync de relaciones hijas
        syncBeneficiarios(contrato, request.getBeneficiarios());
        syncClavesPresupuestales(contrato, request.getClavesPresupuestales());
        syncBienes(contrato, request.getBienes());

        // 6. El @Transactional hace flush automático al terminar el método.
        //    El save explícito garantiza que JPA procese todos los cambios
        //    incluyendo las listas con orphanRemoval.
        contratoRepository.save(contrato);

        return contratoMapper.toResponse(contrato);
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
     * Procesa el string de beneficiarios separados por coma.
     * Si el beneficiario ya existe en la BD (por nombre exacto) lo reutiliza.
     * Si no existe, lo crea.
     */
    private List<ContratoBeneficiarioEntity> crearBeneficiarios(ContratoEntity contrato, String beneficiariosStr) {
        List<ContratoBeneficiarioEntity> relaciones = new ArrayList<>();

        String[] nombres = beneficiariosStr.split(",");
        for (String nombreRaw : nombres) {
            String nombre = nombreRaw.trim();
            if (nombre.isBlank()) continue;

            BeneficiarioEntity beneficiario = beneficiarioRepository
                    .findByNombreAndActivoTrue(nombre)
                    .orElseGet(() -> {
                        BeneficiarioEntity nuevo = BeneficiarioEntity.builder()
                                .nombre(nombre)
                                .build();
                        return beneficiarioRepository.save(nuevo);
                    });

            ContratoBeneficiarioEntity relacion = ContratoBeneficiarioEntity.builder()
                    .contrato(contrato)
                    .beneficiario(beneficiario)
                    .build();

            relaciones.add(relacion);
        }

        return relaciones;
    }

    /**
     * Asigna claves presupuestales al contrato.
     * Las claves vienen del catálogo — se buscan por su clave alfanumérica.
     * Lanza excepción si una clave no existe en el catálogo.
     */
    private List<ContratoClavePresupuestalEntity> crearClavesPresupuestales(
            ContratoEntity contrato,
            List<ClavePresupuestalDto> clavesDto) {

        List<ContratoClavePresupuestalEntity> entidades = new ArrayList<>();

        for (ClavePresupuestalDto dto : clavesDto) {
            ClavePresupuestalEntity clave = clavePresupuestalRepository
                    .findByClaveAndActivoTrue(dto.getClave())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Clave presupuestal no encontrada: " + dto.getClave()));

            ContratoClavePresupuestalEntity entidad = ContratoClavePresupuestalEntity.builder()
                    .contrato(contrato)
                    .clavePresupuestal(clave)
                    .montoAsignado(dto.getMontoAsignado())
                    .build();

            entidades.add(entidad);
        }

        return entidades;
    }

    /**
     * Crea los bienes del contrato.
     * La unidad de medida se busca por ID en el catálogo.
     * El subtotal se calcula automáticamente si no viene en el request.
     */
    private List<ContratoBienEntity> crearBienes(ContratoEntity contrato, List<ContratoBienDto> bienesDto) {

        List<ContratoBienEntity> entidades = new ArrayList<>();

        for (ContratoBienDto dto : bienesDto) {
            UnidadMedidaEntity unidadMedida = unidadMedidaRepository
                    .findById(dto.getIdUnidadMedida())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Unidad de medida no encontrada con ID: " + dto.getIdUnidadMedida()));

            // Calcular subtotal en el backend — no confiamos en el valor que manda el cliente
            BigDecimal subtotal = dto.getPrecioUnitario()
                    .multiply(BigDecimal.valueOf(dto.getCantidad()));

            ContratoBienEntity bien = ContratoBienEntity.builder()
                    .contrato(contrato)
                    .lote(dto.getLote())
                    .partida(dto.getPartida())
                    .descripcionTecnica(dto.getDescripcionTecnica())
                    .unidadMedida(unidadMedida)
                    .cantidad(dto.getCantidad())
                    .precioUnitario(dto.getPrecioUnitario())
                    .subtotal(subtotal)
                    .build();

            entidades.add(bien);
        }

        return entidades;
    }

    /**
     * Actualiza los campos del proveedor existente.
     * No crea un nuevo proveedor — muta el que ya está asociado al contrato.
     * Si el contrato no tenía proveedor, crea uno nuevo.
     */
    private void actualizarProveedor(ContratoEntity contrato, ContratoCreateRequestDto request) {
        if (request.getProveedor() == null) return;

        if (contrato.getProveedor() != null) {
            // Mutar el proveedor existente — JPA detecta los cambios automáticamente
            contrato.getProveedor().setRazonSocial(request.getProveedor().getRazonSocial());
            contrato.getProveedor().setDomicilioFiscal(request.getProveedor().getDomicilioFiscal());
            contrato.getProveedor().setRepresentante(request.getProveedor().getRepresentante());
            contrato.getProveedor().setCaracter(request.getProveedor().getCaracter());
        } else {
            // El contrato no tenía proveedor aún (captura progresiva)
            ProveedorEntity nuevo = proveedorMapper.toEntity(request.getProveedor());
            contrato.setProveedor(proveedorRepository.save(nuevo));
        }
    }

    /**
     * Reasigna comprador y administrador del contrato buscando por ID.
     * El ID viene del dropdown de funcionarios en la UI.
     */
    private void actualizarFuncionario(ContratoEntity contrato, ContratoCreateRequestDto request) {
        // Comprador
        if (request.getComprador() != null && request.getComprador().getId() != null) {
            contrato.setComprador(
                    funcionarioRepository.findById(request.getComprador().getId())
                            .orElseThrow(() -> new EntityNotFoundException(
                                    "Comprador no encontrado con ID: " + request.getComprador().getId()))
            );
        }

        // Administrador del contrato
        if (request.getAdministradorContrato() != null && request.getAdministradorContrato().getId() != null) {
            contrato.setAdministradorContrato(
                    funcionarioRepository.findById(request.getAdministradorContrato().getId())
                            .orElseThrow(() -> new EntityNotFoundException(
                                    "Administrador no encontrado con ID: " + request.getAdministradorContrato().getId()))
            );
        }
    }


    /**
     * Sincroniza los beneficiarios del contrato.
     *
     * Si el request trae null → no se toca nada (el usuario no modificó la sección)
     * Si el request trae "" o lista vacía → se desactivan todos los beneficiarios
     * Si el request trae nombres → se agregan los nuevos, se desactivan los que no están
     *
     * La comparación se hace normalizando los nombres (trim + lowercase)
     * para tolerar diferencias de espacios y capitalización.
     */
    private void syncBeneficiarios(ContratoEntity contrato, String beneficiariosStr) {
        if (beneficiariosStr == null) return;

        // Parsear nombres del request
        List<String> nombresRequest = parsearNombres(beneficiariosStr);

        // Normalizar para comparación
        List<String> nombresNormalizados = nombresRequest.stream()
                .map(this::normalizar)
                .toList();

        // Buscar beneficiarios en el contrato de BD y desactivar los que ya no están en el request
        contrato.getBeneficiarios().forEach(relacion -> {
            String nombreActual = normalizar(relacion.getBeneficiario().getNombre());
            if (!nombresNormalizados.contains(nombreActual)) {
                relacion.setActivo(false);
            } else {
                relacion.setActivo(true); // reactivar si estaba desactivado
            }
        });

        // Identificar nombres ya existentes en el contrato (activos o inactivos)
        List<String> nombresExistentes = contrato.getBeneficiarios()
                .stream()
                .map(r -> normalizar(r.getBeneficiario().getNombre()))
                .toList();

        // Agregar los que son nuevos
        for (String nombre : nombresRequest) {
            if (nombresExistentes.contains(normalizar(nombre))) continue;

            BeneficiarioEntity beneficiario = beneficiarioRepository
                    .findByNombreAndActivoTrue(nombre)
                    .orElseGet(() -> beneficiarioRepository.save(
                            BeneficiarioEntity.builder().nombre(nombre).build()
                    ));

            contrato.getBeneficiarios()
                    .add(
                            ContratoBeneficiarioEntity.builder()
                                    .contrato(contrato)
                                    .beneficiario(beneficiario)
                                    .build()
                    );
        }
    }


    /**
     * Sincroniza las claves presupuestales del contrato.
     *
     * Si el request trae null → no se toca nada
     * Si existe → agregar nuevas, actualizar monto de existentes, desactivar eliminadas
     *
     * La clave alfanumérica es el identificador — no el ID de la BD.
     */
    private void syncClavesPresupuestales(ContratoEntity contrato, List<ClavePresupuestalDto> clavesRequest) {
        if (clavesRequest == null) return;

        // Claves que vienen en el request (por clave alfanumérica)
        List<String> clavesEnRequest = clavesRequest.stream()
                .map(ClavePresupuestalDto::getClave)
                .toList();

        // Desactivar las que ya no están en el request
        contrato.getClavesPresupuestales().forEach(relacion -> {
            String claveActual = relacion.getClavePresupuestal().getClave();
            if (!clavesEnRequest.contains(claveActual)) {
                relacion.setActivo(false);
            } else {
                relacion.setActivo(true);
            }
        });

        // Construir mapa de relaciones existentes por clave alfanumérica
        Map<String, ContratoClavePresupuestalEntity> relacionesPorClave =
                contrato.getClavesPresupuestales().stream()
                        .collect(Collectors.toMap(
                                r -> r.getClavePresupuestal().getClave(),
                                r -> r,
                                (a, b) -> a // en caso de duplicados, quedarse con el primero
                        ));

        for (ClavePresupuestalDto dto : clavesRequest) {
            ContratoClavePresupuestalEntity relacionExistente = relacionesPorClave.get(dto.getClave());

            if (relacionExistente != null) {
                // Actualizar el monto de la clave existente
                relacionExistente.setMontoAsignado(dto.getMontoAsignado());
                relacionExistente.setActivo(true);
            } else {
                // Agregar clave nueva desde el catálogo
                ClavePresupuestalEntity clave = clavePresupuestalRepository
                        .findByClaveAndActivoTrue(dto.getClave())
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Clave presupuestal no encontrada: " + dto.getClave()));

                contrato.getClavesPresupuestales().add(
                        ContratoClavePresupuestalEntity.builder()
                                .contrato(contrato)
                                .clavePresupuestal(clave)
                                .montoAsignado(dto.getMontoAsignado())
                                .build()
                );
            }
        }
    }


    /**
     * Sincroniza los bienes del contrato.
     *
     * Si el request trae null → no se toca nada
     * Si un bien trae idContratoBien → se actualiza el existente
     * Si un bien no trae idContratoBien → es nuevo, se crea
     * Si un bien existente no aparece en el request → se desactiva
     */
    private void syncBienes(ContratoEntity contrato, List<ContratoBienDto> bienesRequest) {
        if (bienesRequest == null) return;

        // IDs que vienen en el request (solo los que ya existen)
        List<Integer> idsEnRequest = bienesRequest
                .stream()
                .filter(b -> b.getIdContratoBien() != null)
                .map(ContratoBienDto::getIdContratoBien)
                .toList();

        // Desactivar los bienes que ya no están en el request
        contrato.getBienes().forEach(bien -> {
            if (!idsEnRequest.contains(bien.getIdContratoBien())) {
                bien.setActivo(false);
            }
        });

        // Construir mapa de bienes existentes por ID
        Map<Integer, ContratoBienEntity> bienesPorId =
                contrato.getBienes().stream()
                        .filter(b -> b.getIdContratoBien() != null)
                        .collect(Collectors.toMap(
                                ContratoBienEntity::getIdContratoBien,
                                b -> b,
                                (a, b) -> a
                        ));

        for (ContratoBienDto dto : bienesRequest) {
            if (dto.getIdContratoBien() != null && bienesPorId.containsKey(dto.getIdContratoBien())) {
                // Actualizar bien existente
                ContratoBienEntity bien = bienesPorId.get(dto.getIdContratoBien());
                bien.setLote(dto.getLote());
                bien.setPartida(dto.getPartida());
                bien.setDescripcionTecnica(dto.getDescripcionTecnica());
                bien.setCantidad(dto.getCantidad());
                bien.setPrecioUnitario(dto.getPrecioUnitario());
                bien.setSubtotal(dto.getPrecioUnitario().multiply(BigDecimal.valueOf(dto.getCantidad())));
                bien.setActivo(true);

                // Actualizar unidad de medida si cambió
                if (dto.getIdUnidadMedida() != null) {
                    UnidadMedidaEntity unidad = unidadMedidaRepository
                            .findById(dto.getIdUnidadMedida())
                            .orElseThrow(() -> new EntityNotFoundException(
                                    "Unidad de medida no encontrada con ID: " + dto.getIdUnidadMedida()));
                    bien.setUnidadMedida(unidad);
                }

            } else {
                // Bien nuevo — no tiene idContratoBien
                UnidadMedidaEntity unidad = unidadMedidaRepository
                        .findById(dto.getIdUnidadMedida())
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Unidad de medida no encontrada con ID: " + dto.getIdUnidadMedida()));

                BigDecimal subtotal = dto.getPrecioUnitario()
                        .multiply(BigDecimal.valueOf(dto.getCantidad()));

                contrato.getBienes().add(
                        ContratoBienEntity.builder()
                                .contrato(contrato)
                                .lote(dto.getLote())
                                .partida(dto.getPartida())
                                .descripcionTecnica(dto.getDescripcionTecnica())
                                .unidadMedida(unidad)
                                .cantidad(dto.getCantidad())
                                .precioUnitario(dto.getPrecioUnitario())
                                .subtotal(subtotal)
                                .build()
                );
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Utilidades
    // ─────────────────────────────────────────────────────────────

    private List<String> parsearNombres(String beneficiariosStr) {
        if (beneficiariosStr == null || beneficiariosStr.isBlank()) return List.of();
        return java.util.Arrays.stream(beneficiariosStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .toList();
    }

    private String normalizar(String valor) {
        return valor == null ? "" : valor.trim().toLowerCase();
    }




//    private final ContratoRepository contratoRepository;
//
//    private final ProductoService productoService;
//    private final ProveedorService proveedorService;
//    private final ServidorPublicoService servidorPublicoService;
//    private final ContratoBeneficiarioService contratoBeneficiarioService;
//    private final ContratoClavePresupuestalService contratoClavePresupuestalService;
//
//    private final ContratoMapper contratoMapper;
//
//    // ------------------------------------------------------------------------------------------
//
//    @Transactional
//    public ContratoDto createContrato(ContratoCreateRequestDto request) {
//        logger.info("Creando contrato: {}", request.getNumeroContrato());
//
//        // 1. Mapeo inicial de atributos
//        ContratoEntity contrato = contratoMapper.toEntity(request);
//
//        // 2. Asignacion de estatus
//        setEstatus(contrato, EstatusContrato.EN_CAPTURA);
//
//        // 3. Asignar relaciones de Proveedor, Comprador y Administrador del Contrato
//        updateRelacionesPrincipales(contrato, request);
//
//        // 4. Guardar contrato en la BBDD
//        ContratoEntity contratoPersistido = contratoRepository.save(contrato);
//
//        // 5. Asignar informacion de Beneficiarios, Claves Presupuestales y Productos
//        createRelacionesHijas(contratoPersistido, request);
//
//        return contratoMapper.toResponse(contratoPersistido);
//    }
//
//    @Transactional
//    public ContratoDto updateContrato(Long idContrato, ContratoCreateRequestDto request) {
//        logger.info("Actualizando contrato ID: {}", idContrato);
//
//        // 1. Busca contrato en la BB DD
//        ContratoEntity contrato = findContratoOrThrow(idContrato);
//
//        // 2. Asigna informacion inicial
//        contratoMapper.updateEntityFromDto(request, contrato);
//
//        // 3. Actualiza relaciones de Proveedor, Comprador y Administrador del Contrato
//        updateRelacionesPrincipales(contrato, request);
//
//        // 4. Gestiona los cambios en Beneficiarios, Claves Presupuestales y Productos
//        syncRelacionesHijas(contrato, request);
//
//        return contratoMapper.toResponse(contrato);
//    }


    // ------------------------------------------------------------------------
    // Helpers

//    private ContratoEntity findContratoOrThrow(Long idContrato) {
//        return contratoRepository.findById(idContrato)
//                .orElseThrow(() -> new EntityNotFoundException(
//                        "Contrato no encontrado con ID: " + idContrato
//                ));
//    }
//
//    private void setEstatus(ContratoEntity contrato, EstatusContrato estatus) {
//        contrato.setEstatusContrato(
//                estatusContratoRepository.findById(estatus.getId())
//                .orElseThrow(() -> new EntityNotFoundException(
//                        "Estatus no válido: " + estatus)));
//    }
//
//    private void updateRelacionesPrincipales(ContratoEntity contrato, ContratoCreateRequestDto request) {
//        contrato.setProveedor(proveedorService.createProveedorFromContrato(request));
//        contrato.setComprador(servidorPublicoService.createCompradorFromContrato(request));
//        contrato.setAdministradorContrato(servidorPublicoService.createAdministradorDelContratoFromContrato(request));
//    }

//    private void createRelacionesHijas(ContratoEntity contrato, ContratoCreateRequestDto request) {
//        createClavesPresupuestales(contrato, request);
//        createBeneficiarios(contrato, request);
//        createProductos(contrato, request);
//    }

//    private void createClavesPresupuestales(ContratoEntity contrato, ContratoCreateRequestDto request) {
//
//        if (request.getClavesPresupuestales() == null || request.getClavesPresupuestales().isEmpty()) {
//            return;
//        }
//
//        logger.info("Creando {} claves presupuestales para contrato ID: {}", request.getClavesPresupuestales().size(), contrato.getIdContrato());
//
//        contrato.setClavesPresupuestales(
//                contratoClavePresupuestalService.createClavesPresupuestales(
//                        contrato,
//                        request.getClavesPresupuestales()
//                )
//        );
//    }

//    private void createBeneficiarios(ContratoEntity contrato, ContratoCreateRequestDto request) {
//
//        if (request.getBeneficiarios() == null || request.getBeneficiarios().isBlank()) {
//            return;
//        }
//
//        logger.info("Creando beneficiarios para contrato ID: {}", contrato.getIdContrato());
//
//        contrato.setBeneficiarios(
//                contratoBeneficiarioService.createBeneficiarios(
//                        contrato,
//                        request.getBeneficiarios()
//                )
//        );
//    }

//    private void createProductos(ContratoEntity contrato, ContratoCreateRequestDto request) {
//
//        if (request.getProductos() == null || request.getProductos().isEmpty()) {
//            return;
//        }
//
//        logger.info("Creando {} productos para contrato ID: {}", request.getProductos().size(), contrato.getIdContrato());
//
//        contrato.setProductos(
//                productoService.createProductos(
//                        contrato,
//                        request.getProductos()
//                )
//        );
//    }

//    private void syncRelacionesHijas(ContratoEntity contrato, ContratoCreateRequestDto request) {
//        syncClavesPresupuestales(contrato, request);
//        syncBeneficiarios(contrato, request);
//        syncProductos(contrato, request);
//    }
//
//    private void syncClavesPresupuestales(ContratoEntity contrato, ContratoCreateRequestDto request) {
//        if (request.getClavesPresupuestales() == null) {
//            return;
//        }
//
//        logger.info("Sincronizando claves presupuestales para contrato ID: {}", contrato.getIdContrato());
//
//        contratoClavePresupuestalService.syncClavesPresupuestales(
//                contrato,
//                request.getClavesPresupuestales()
//        );
//    }
//
//    private void syncBeneficiarios(ContratoEntity contrato, ContratoCreateRequestDto request) {
//        if (request.getBeneficiarios() == null) {
//            return;
//        }
//
//        logger.info("Sincronizando beneficiarios para contrato ID: {}", contrato.getIdContrato());
//
//        contratoBeneficiarioService.syncBeneficiarios(
//                contrato,
//                request.getBeneficiarios()
//        );
//    }
//
//    private void syncProductos(ContratoEntity contrato, ContratoCreateRequestDto request) {
//        if (request.getProductos() == null) {
//            return;
//        }
//
//        logger.info("Sincronizando productos para contrato ID: {}", contrato.getIdContrato());
//
//        productoService.syncProductos(
//                contrato,
//                request.getProductos()
//        );
//    }
}
