//package com.sesesp.almacen.domain.service;
//
//import com.sesesp.almacen.common.types.EstatusContrato;
//import com.sesesp.almacen.domain.dto.ClavePresupuestalDto;
//import com.sesesp.almacen.domain.dto.ContratoCreateRequestDto;
//import com.sesesp.almacen.domain.dto.ContratoCreateResponseDto;
//import com.sesesp.almacen.domain.dto.ContratoBienDto;
//import com.sesesp.almacen.domain.entity.BeneficiarioEntity;
//import com.sesesp.almacen.domain.entity.ContratoBeneficiarioEntity;
//import com.sesesp.almacen.domain.entity.ContratoEntity;
//import com.sesesp.almacen.domain.mapper.ContratoBeneficiarioMapper;
//import com.sesesp.almacen.domain.mapper.ContratoMapper_BAK_II;
//import com.sesesp.almacen.domain.repository.BeneficiarioRepository;
//import com.sesesp.almacen.domain.repository.ContratoBeneficiarioRepository;
//import com.sesesp.almacen.domain.repository.ContratoRepository;
//import com.sesesp.almacen.domain.repository.EstatusContratoRepository;
//import jakarta.persistence.EntityNotFoundException;
//import lombok.RequiredArgsConstructor;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//@Service
//@RequiredArgsConstructor
//public class ContratoService_BAK {
//
//    Logger logger = LoggerFactory.getLogger(ContratoService_BAK.class);
//
//    // repositories
//    private final ContratoRepository contratoRepository;
//    private final BeneficiarioRepository beneficiarioRepository;
//    private final EstatusContratoRepository estatusContratoRepository;
//    private final ContratoBeneficiarioRepository contratoBeneficiarioRepository;
//
//    // services
//    private final ProductoService productoService;
//    private final ProveedorService proveedorService;
//    private final ServidorPublicoService servidorPublicoService;
//    private final ContratoBeneficiarioService contratoBeneficiarioService;
//    private final ContratoClavePresupuestalService contratoClavePresupuestalService;
//
//    // mappers
//    private final ContratoMapper_BAK_II contratoMapper;
//    private final ContratoBeneficiarioMapper contratoBeneficiarioMapper;
//
//
////    public List<ContratoCreateResponseDto> findAllContratos() {
////        return contratoRepository.findAll()
////                .stream()
////                .map(contratoMapper::toResponse)
////                .toList();
////    }
//
////    public ContratoCreateResponseDto findContratoById(Long id) {
////        return contratoRepository.findById(id)
////                .map(contratoMapper::toResponse)
////                .orElseThrow(() -> new EntityNotFoundException("Contrato no encontrado con ID: " + id));
////    }
//
////    @Transactional
////    public ContratoCreateResponseDto createContrato(ContratoCreateRequestDto request) {
////        logger.info("Starting creation of Contrato: {}", request.getNumeroContrato());
////
////        // Initial map
////        //ContratoEntity contrato = contratoMapper.toEntity(request);
////
////        //assignStatus(contrato, EstatusContrato.EN_CAPTURA);
////
////        // proveedor / comprador / administrador del contrato
////        //assignRelacionesPrincipales(contrato, request);
////
////        //ContratoEntity contratoGuardado = contratoRepository.save(contrato);
////
//////        assignClavesPresupuestales(contratoGuardado, request.getClavesPresupuestales());
//////        assignBeneficiarios(contratoGuardado, request.getBeneficiarios());
//////        assignBienes(contratoGuardado, request.getProductos());
////
////        return contratoMapper.toResponse(contratoGuardado);
////    }
//
//    @Transactional
//    public ContratoCreateResponseDto updateContrato(Long id, ContratoCreateRequestDto request) {
//        logger.info("Starting updating contrato contrato ID: {}", id);
//
//        // Buscar contrato existente
//        ContratoEntity contrato = loadContratoFromDB(id);
//
//        // Actualizar datos primitivos
//        contratoMapper.updateEntityFromDto(request, contrato);
//
//        updateBeneficiarios(contrato, request.getBeneficiarios());
//
//
//        return contratoMapper.toResponse(contrato);
//    }
//
//    // --- HELPERS ---
//
//    private ContratoEntity loadContratoFromDB(Long idContrato){
//        return contratoRepository.findById(idContrato)
//                .orElseThrow(() -> new EntityNotFoundException("No se puede actualizar: Contrato no encontrado"));
//    }
//
//    private void updateBeneficiarios(ContratoEntity contrato, String beneficiarios) {
//
//        // clean input and convert beneficiarios from Request
//        List<String> nombresBeneficiariosFromRequest = contratoBeneficiarioService.getListOfBeneficiarios(beneficiarios);
//
//        List<ContratoBeneficiarioEntity> beneficiariosFromDB = contratoBeneficiarioService.findBeneficiariosFromContrato(contrato.getIdContrato());
//
//        Map<String, ContratoBeneficiarioEntity> mapBeneficiariosFromDB = contratoBeneficiarioService.mapBeneficiarios(beneficiariosFromDB);
//
//        Set<String> beneficiariosNormalizadosRequest = contratoBeneficiarioService.normalizarBeneficiariosFromRequest(nombresBeneficiariosFromRequest);
//
//        beneficiariosFromDB.stream()
//                .filter( rel -> !beneficiariosNormalizadosRequest.contains(
//                        contratoBeneficiarioService.normalizar(rel.getBeneficiario().getNombre())
//                ))
//                .forEach(rel -> rel.setActivo(false)); // borrar registro
//
//        List<ContratoBeneficiarioEntity> relacionesParaGuardar = new ArrayList<>(beneficiariosFromDB);
//
//        for (String nombreBeneficiario : nombresBeneficiariosFromRequest) {
//            String nombreNormalizado = contratoBeneficiarioService.normalizar(nombreBeneficiario);
//
//            ContratoBeneficiarioEntity relacionExistente =
//                    mapBeneficiariosFromDB.get(nombreNormalizado);
//
//            if (relacionExistente != null) {
//                relacionExistente.setActivo(true);
//                continue;
//            }
//
//            BeneficiarioEntity beneficiario = beneficiarioRepository
//                    .findByNombreAndActivoTrue(nombreBeneficiario)
//                    .orElseGet(() -> {
//                        BeneficiarioEntity nuevo = new BeneficiarioEntity();
//                        nuevo.setNombre(nombreBeneficiario);
//                        nuevo.setActivo(true);
//                        return beneficiarioRepository.save(nuevo);
//                    });
//
//
//            ContratoBeneficiarioEntity nuevaRelacion = contratoBeneficiarioMapper.toEntity(contrato,beneficiario, "");
//            relacionesParaGuardar.add(nuevaRelacion);
//        }
//
//        contratoBeneficiarioRepository.saveAll(relacionesParaGuardar);
//    }
//
////    private void assignStatus(ContratoEntity contrato, EstatusContrato estatus) {
////        contrato.setEstatusContrato(estatusContratoRepository.findById(estatus.getId())
////                .orElseThrow(() -> new EntityNotFoundException("Estatus no válido: " + estatus)));
////    }
//
////    private void assignRelacionesPrincipales(ContratoEntity contrato, ContratoCreateRequestDto request) {
////        contrato.setProveedor(proveedorService.createProveedorFromContrato(request));
////        contrato.setComprador(servidorPublicoService.createCompradorFromContrato(request));
////        contrato.setAdministradorContrato(servidorPublicoService.createAdministradorDelContratoFromContrato(request));
////    }
//
////    private void assignClavesPresupuestales(ContratoEntity contrato, List<ClavePresupuestalDto> clavesPresupuestales ) {
////        logger.info("Procesando claves presupuestales");
////        if (clavesPresupuestales != null ) {
////            logger.info("Procesando {} claves presupuestales", clavesPresupuestales.size());
////            contrato.setClavesPresupuestales(
////                    contratoClavePresupuestalService.createClavesPresupuestales(contrato, clavesPresupuestales));
////        }
////    }
//
////    private void assignBeneficiarios(ContratoEntity contrato, String beneficiarios) {
////        logger.info("Procesando Beneficiarios");
////        if (beneficiarios != null && !beneficiarios.isBlank()) {
////            contrato.setBeneficiarios(
////                    contratoBeneficiarioService.createBeneficiarios(contrato, beneficiarios));
////        }
////    }
//
////    private void assignBienes(ContratoEntity contrato, List<ContratoBienDto> bienes) {
////        logger.info("Procesando bienes");
////        if (bienes != null) {
////            logger.info("Procesando {} Bienes", bienes.size());
////            contrato.setProductos(
////                    productoService.createProductos(contrato, bienes));
////        }
////    }
//
//}
//
//
//
