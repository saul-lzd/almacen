package com.sesesp.almacen.domain.service;

//import com.sesesp.almacen.common.types.EstatusContrato;
import com.sesesp.almacen.domain.dto.ContratoCreateRequestDto;
import com.sesesp.almacen.domain.dto.ContratoCreateResponseDto;
import com.sesesp.almacen.domain.dto.ContratoDto;
import com.sesesp.almacen.domain.entity.ContratoEntity;
import com.sesesp.almacen.domain.mapper.ContratoMapper;
import com.sesesp.almacen.domain.repository.ContratoRepository;
//import com.sesesp.almacen.domain.repository.EstatusContratoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
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
//    private final EstatusContratoRepository estatusContratoRepository;

    private final ProductoService productoService;
    private final ProveedorService proveedorService;
    private final ServidorPublicoService servidorPublicoService;
    private final ContratoBeneficiarioService contratoBeneficiarioService;
    private final ContratoClavePresupuestalService contratoClavePresupuestalService;

    private final ContratoMapper contratoMapper;

    // ------------------------------------------------------------------------------------------

    public List<ContratoDto> findAllContratos() {
        return contratoRepository.findAll()
                .stream()
                .map(contratoMapper::toResponse)
                .toList();
    }

//    public ContratoDto findContratoById(Long idContrato) {
//        ContratoEntity contrato = findContratoOrThrow(idContrato);
//        return contratoMapper.toResponse(contrato);
//    }
//
//    @Transactional
//    public ContratoCreateResponseDto createContrato(ContratoCreateRequestDto request) {
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
//    public ContratoCreateResponseDto updateContrato(Long idContrato, ContratoCreateRequestDto request) {
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
