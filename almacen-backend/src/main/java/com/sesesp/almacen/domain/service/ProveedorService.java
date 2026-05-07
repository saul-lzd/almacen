package com.sesesp.almacen.domain.service;

import com.sesesp.almacen.domain.dto.ContratoCreateRequestDto;
import com.sesesp.almacen.domain.dto.ProveedorContratoDto;
import com.sesesp.almacen.domain.entity.ProveedorEntity;
import com.sesesp.almacen.domain.mapper.ProveedorMapper;
import com.sesesp.almacen.domain.repository.ProveedorRepository;
import org.springframework.stereotype.Service;

@Service
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;
    private final ProveedorMapper proveedorMapper;

    public ProveedorService(ProveedorRepository proveedorRepository, ProveedorMapper proveedorMapper) {
        this.proveedorRepository = proveedorRepository;
        this.proveedorMapper = proveedorMapper;
    }

    public ProveedorEntity resolveProveedor(ProveedorContratoDto proveedor) {
        if (proveedor == null) {
            return null;
        }

        ProveedorEntity proveedorEntity = proveedorMapper.toEntity(proveedor);
        return proveedorRepository.save(proveedorEntity);
    }


}
