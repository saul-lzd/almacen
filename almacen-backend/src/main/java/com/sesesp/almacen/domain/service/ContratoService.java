package com.sesesp.almacen.domain.service;

import com.sesesp.almacen.common.types.EstatusContrato;
import com.sesesp.almacen.domain.dto.*;
import com.sesesp.almacen.domain.entity.*;
import com.sesesp.almacen.domain.mapper.ContratoMapper;
import com.sesesp.almacen.domain.repository.ContratoRepository;
import com.sesesp.almacen.domain.repository.EstatusContratoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ContratoService {

    Logger logger = LoggerFactory.getLogger(ContratoService.class);

    private final ContratoRepository contratoRepository;
    private final ContratoMapper contratoMapper;
    private final ContratoClavePresupuestalService contratoClavePresupuestalService;
    private final ContratoBeneficiarioService contratoBeneficiarioService;
    private final ProveedorService proveedorService;
    private final ServidorPublicoService servidorPublicoService;
    private final EstatusContratoRepository estatusContratoRepository;
    private final ProductoService productoService;
    private final ClavePresupuestalService clavePresupuestalService;

    public ContratoService(ContratoRepository contratoRepository,
                           ContratoMapper contratoMapper,
                           ContratoClavePresupuestalService contratoClavePresupuestalService,
                           ContratoBeneficiarioService contratoBeneficiarioService,
                           ProveedorService proveedorService,
                           ServidorPublicoService servidorPublicoService,
                           EstatusContratoRepository estatusContratoRepository,
                           ProductoService productoService,
                           ClavePresupuestalService clavePresupuestalService) {
        this.contratoRepository = contratoRepository;
        this.contratoMapper = contratoMapper;
        this.contratoClavePresupuestalService = contratoClavePresupuestalService;
        this.contratoBeneficiarioService = contratoBeneficiarioService;
        this.proveedorService = proveedorService;
        this.servidorPublicoService = servidorPublicoService;
        this.estatusContratoRepository = estatusContratoRepository;
        this.productoService = productoService;
        this.clavePresupuestalService = clavePresupuestalService;
    }

    public List<ContratoCreateResponseDto> findAllContratos() {
        List<ContratoEntity> contratos = contratoRepository.findAll();
        return contratos.stream().map(c -> contratoMapper.toResponse(c)).toList();
    }

    public ContratoCreateResponseDto findContratoById(Long idContrato) {
        Optional<ContratoEntity> entity =  contratoRepository.findById(idContrato);
        return contratoMapper.toResponse(entity.get());
    }

    @Transactional
    public ContratoCreateResponseDto createContrato(ContratoCreateRequestDto request) {
        logger.info("Starting creation of Contrato: {}", request.getNumeroContrato());

        // 1. Mapeo inicial
        ContratoEntity contrato = contratoMapper.toEntity(request);

        // 2. Seteo de estatus seguro
        EstatusContrato statusEnum = EstatusContrato.EN_CAPTURA;
        contrato.setEstatusContrato(
                    estatusContratoRepository
                        .findById(statusEnum.getId())
                        .orElseThrow(() -> new EntityNotFoundException("Estatus inicial no encontrado en DB")));

        // 3. Procesar entidades relacionadas (delegar responsabilidad a servicios)
        contrato.setProveedor(proveedorService.createProveedorFromContrato(request));
        contrato.setComprador(servidorPublicoService.createCompradorFromContrato(request));
        contrato.setAdministradorContrato(servidorPublicoService.createAdministradorDelContratoFromContrato(request));

        // 4. Guardar contrato
        // Lo guardamos aquí para tener el ID generado necesario para las relaciones hijas
        ContratoEntity contratoGuardado = contratoRepository.save(contrato);

        // 5. Relaciones de detalle

        // 5.1 Claves Presupuestales
        if (request.getClavesPresupuestales() != null && !request.getClavesPresupuestales().isEmpty()) {
            logger.info("Procesando {} llaves presupuestales", request.getClavesPresupuestales().size());
            var claves = contratoClavePresupuestalService
                    .createClavesPresupuestales(contratoGuardado, request.getClavesPresupuestales());
            contratoGuardado.setClavesPresupuestales(claves);
        }

        // 5.2 Beneficiarios
        if (request.getBeneficiarios() != null && !request.getBeneficiarios().equals("")) {
            logger.info("Procesando Beneficiarios");

            var beneficiarios = contratoBeneficiarioService
                    .createBeneficiarios(contratoGuardado, request.getBeneficiarios());
            contratoGuardado.setBeneficiarios(beneficiarios);
        }

        // 5.3 Productos
        if (request.getProductos() != null && !request.getProductos().isEmpty()) {
            logger.info("Procesando {} Productos", request.getProductos().size());

            var productos = productoService
                    .createProductos(contratoGuardado, request.getProductos());
            contratoGuardado.setProductos(productos);
        }

        return contratoMapper.toResponse(contratoGuardado);
    }

    @Transactional
    public ContratoCreateResponseDto updateContrato(
            Long idContrato,
            ContratoCreateRequestDto request) {

        ContratoEntity contratoFound = contratoRepository.findById(idContrato)
                .orElseThrow(() -> new EntityNotFoundException("Contrato no encontrado"));

        // map general information
        ContratoEntity contratoToUpdate = contratoMapper.toEntity(request);

        contratoToUpdate.setEstatusContrato(contratoFound.getEstatusContrato());
        contratoToUpdate.setBeneficiarios(contratoFound.getBeneficiarios());
        contratoToUpdate.setProveedor(contratoFound.getProveedor());

        // map auditoria fields
        contratoToUpdate.setUsuarioCreacion(contratoFound.getUsuarioCreacion());
        contratoToUpdate.setFechaModificacion(contratoFound.getFechaCreacion());
        contratoToUpdate.setActivo(contratoFound.getActivo());

        ContratoEntity contratoEntity = contratoRepository.save(contratoToUpdate);
        return contratoMapper.toResponse(contratoEntity);
    }

    private void actualizarDatosBasicos(
            ContratoEntity contrato,
            ContratoCreateRequestDto request) {

        contrato.setIdentificadorContrato(request.getNumeroContrato());
        contrato.setAdquisicion(request.getAdquisicion());
        contrato.setFolioOrigen(request.getFolioOrigen());

    }

    private void actualizarProveedor(
            ContratoEntity contrato,
            ProveedorContratoDto proveedor ) {

        contrato.getProveedor().setRazonSocial(proveedor.getRazonSocial());
        contrato.getProveedor().setRepresentanteEmpresa(proveedor.getRepresentanteEmpresa());
        contrato.getProveedor().setCaracterRepresentante(proveedor.getCaracterRepresentante());
        contrato.getProveedor().setDireccion(proveedor.getDireccion());

    }

    private void actualizarServidorPublico(
            ContratoEntity contrato,
            ServidorPublicoDto servidorPublicoDto
            ) {

        // comprador y administrador del contrato
        contrato.getComprador().setDependencia(servidorPublicoDto.getDependencia());
        contrato.getComprador().setNombre(servidorPublicoDto.getNombre());
        contrato.getComprador().setCargo(servidorPublicoDto.getCargo());
    }

    private void actualizarDetallesPago(
            ContratoEntity contrato,
            DetallesPagoDto detallesPago ) {

        if (detallesPago == null) {
            return;
        }
        contrato.setMontoSinImpuestos(detallesPago.getMontoSinImpuestos());
        contrato.setImpuestos(detallesPago.getImpuestos());
        contrato.setMontoTotal(detallesPago.getMontoTotal());
        contrato.setTieneAnticipo(detallesPago.getTieneAnticipo());
        contrato.setPorcentajeAnticipo(detallesPago.getPorcentajeAnticipo());
        contrato.setMontoAnticipo(detallesPago.getMontoAnticipo());
        contrato.setNumeroExhibiciones(detallesPago.getNumeroExhibiciones());
        contrato.setTieneFiniquito(detallesPago.getTieneFiniquito());
        contrato.setPorcentajeFiniquito(detallesPago.getPorcentajeFiniquito());
        contrato.setMontoFiniquito(detallesPago.getMontoFiniquito());
    }

    private void actualizarBeneficiarios(
            ContratoEntity contrato,
            String beneficiarios) {



    }

    private void actualizarClavesPresupuestales(
            ContratoEntity contrato,
            List<ClavePresupuestalDto> clavesPresupuestales) {

        if (clavesPresupuestales == null) {
            return;
        }

        List<ContratoClavePresupuestalEntity> claves = clavesPresupuestales.stream()
                .map(dto -> crearContratoClavePresupuestal(contrato, dto))
                .toList();

        contrato.setClavesPresupuestales(claves);
    }

    private ContratoClavePresupuestalEntity crearContratoClavePresupuestal(
            ContratoEntity contrato,
            ClavePresupuestalDto dto) {

        ClavePresupuestalEntity clave = clavePresupuestalService
                .findByClavePresupuestal(dto.getClavePresupuestal());

        return ContratoClavePresupuestalEntity.builder()
                .contrato(contrato)
                .clavePresupuestal(clave)
                .montoAsignado(dto.getMontoAsignado())
                .build();
    }


}



