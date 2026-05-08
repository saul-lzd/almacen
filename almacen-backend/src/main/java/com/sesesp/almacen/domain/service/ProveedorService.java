package com.sesesp.almacen.domain.service;

import com.sesesp.almacen.domain.dto.ContratoCreateRequestDto;
import com.sesesp.almacen.domain.dto.ProveedorContratoDto;
import com.sesesp.almacen.domain.entity.ProveedorEntity;
import com.sesesp.almacen.domain.mapper.ProveedorMapper;
import com.sesesp.almacen.domain.repository.ProveedorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProveedorService {

    Logger logger = LoggerFactory.getLogger(ProveedorService.class);

    private final ProveedorRepository proveedorRepository;
    private final ProveedorMapper proveedorMapper;

    public ProveedorService(ProveedorRepository proveedorRepository, ProveedorMapper proveedorMapper) {
        this.proveedorRepository = proveedorRepository;
        this.proveedorMapper = proveedorMapper;
    }

    public ProveedorEntity createProveedorFromContrato(ContratoCreateRequestDto requestContrato) {
        logger.info("Creating proveedor for Contrato {}", requestContrato.getNumeroContrato());

        return Optional.ofNullable(requestContrato.getProveedor())
                .map(proveedorMapper::toEntity)
                .map(proveedorRepository::save)
                .orElse(null);
    }

}
