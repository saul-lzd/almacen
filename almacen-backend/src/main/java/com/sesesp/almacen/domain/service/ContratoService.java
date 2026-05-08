package com.sesesp.almacen.domain.service;

import com.sesesp.almacen.common.types.EstatusContrato;
import com.sesesp.almacen.domain.dto.ContratoCreateRequestDto;
import com.sesesp.almacen.domain.dto.ContratoCreateResponseDto;
import com.sesesp.almacen.domain.entity.*;
import com.sesesp.almacen.domain.repository.ContratoRepository;
import com.sesesp.almacen.domain.mapper.ContratoMapper;
import com.sesesp.almacen.domain.repository.EstatusContratoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

@Service
public class ContratoService {

    Logger logger = LoggerFactory.getLogger(ContratoService.class);

    private final ContratoRepository contratoRepository;
    private final ContratoMapper contratoMapper;
    private final ProveedorService proveedorService;
    private final ServidorPublicoService servidorPublicoService;
    private final EstatusContratoRepository estatusContratoRepository;
    private final ContratoClavePresupuestalService contratoClavePresupuestalService;

    public ContratoService(ContratoRepository contratoRepository,
                           ContratoMapper contratoMapper,
                           ProveedorService proveedorService,
                           ServidorPublicoService servidorPublicoService,
                           EstatusContratoRepository estatusContratoRepository,
                           ContratoClavePresupuestalService contratoClavePresupuestalService) {

        this.contratoRepository = contratoRepository;
        this.contratoMapper = contratoMapper;
        this.proveedorService = proveedorService;
        this.servidorPublicoService = servidorPublicoService;
        this.estatusContratoRepository = estatusContratoRepository;
        this.contratoClavePresupuestalService = contratoClavePresupuestalService;
    }

    public List<ContratoCreateResponseDto> findAllContratos() {
        List<ContratoEntity> contratos = contratoRepository.findAll();
        return contratos.stream().map(c -> contratoMapper.toResponse(c)).toList();
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
        if (request.getClavesPresupuestales() != null && !request.getClavesPresupuestales().isEmpty()) {
            logger.info("Procesando {} llaves presupuestales", request.getClavesPresupuestales().size());
            var claves = contratoClavePresupuestalService.createClavesPresupuestales(contratoGuardado, request.getClavesPresupuestales());
            contratoGuardado.setClavesPresupuestales(claves);
        }

        return contratoMapper.toResponse(contratoGuardado);
    }

}



