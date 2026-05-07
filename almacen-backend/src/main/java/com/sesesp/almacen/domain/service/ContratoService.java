package com.sesesp.almacen.domain.service;

import com.sesesp.almacen.domain.dto.ContratoCreateRequestDto;
import com.sesesp.almacen.domain.dto.ContratoCreateResponseDto;
import com.sesesp.almacen.domain.entity.*;
import com.sesesp.almacen.domain.repository.ContratoRepository;
import com.sesesp.almacen.domain.mapper.ContratoMapper;
import com.sesesp.almacen.domain.repository.EstatusContratoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ContratoService {

    private final ContratoRepository contratoRepository;
    private final ContratoMapper contratoMapper;
    private final ProveedorService proveedorService;
    private final ServidorPublicoService servidorPublicoService;
    private final EstatusContratoRepository estatusContratoRepository;
    private final ContratoClavePresupuestalService contratoClavePresupuestalService;

    public ContratoService(ContratoRepository contratoRepository, ContratoMapper contratoMapper, ProveedorService proveedorService, ServidorPublicoService servidorPublicoService, EstatusContratoRepository estatusContratoRepository, ContratoClavePresupuestalService contratoClavePresupuestalService) {

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

        ProveedorEntity proveedor = proveedorService.resolveProveedor(request.getProveedor());
        ServidorPublicoEntity comprador = servidorPublicoService.resolveServidorPublico(request.getComprador());
        ServidorPublicoEntity administradorContrato = servidorPublicoService.resolveServidorPublico(request.getAdministradorContrato());
        EstatusContratoEntity estatusContrato = estatusContratoRepository.findById(request.getIdEstatusContrato()).orElseThrow(() -> new RuntimeException("Estatus de contrato no encontrado"));

        ContratoEntity contrato;
        contrato = contratoMapper.mapGeneralInformation(request);
        contrato.setProveedor(proveedor);
        contrato.setComprador(comprador);
        contrato.setAdministradorContrato(administradorContrato);
        contrato.setEstatusContrato(estatusContrato);

        ContratoEntity contratoGuardado = contratoRepository.save(contrato);
        List<ContratoClavePresupuestalEntity> relationContratosClaves = contratoClavePresupuestalService.createClavesPresupuestales(contratoGuardado, request.getClavesPresupuestales());
        contratoGuardado.setClavesPresupuestales(relationContratosClaves);
        return contratoMapper.toResponse(contratoGuardado);
    }
}



