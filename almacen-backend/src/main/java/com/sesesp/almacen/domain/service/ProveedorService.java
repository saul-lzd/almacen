package com.sesesp.almacen.domain.service;


import com.sesesp.almacen.domain.dto.ProveedorContratoDto;
import com.sesesp.almacen.domain.entity.ProveedorEntity;
import com.sesesp.almacen.domain.mapper.ProveedorMapper;
import com.sesesp.almacen.domain.repository.ProveedorRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProveedorService {

    Logger logger = LoggerFactory.getLogger(ProveedorService.class);

    private final ProveedorRepository proveedorRepository;
    private final ProveedorMapper proveedorMapper;


    /**
     * Crea un nuevo proveedor a partir del DTO.
     * Se usa en createContrato — cada contrato tiene su propia copia
     * del proveedor tal como aparece en el documento físico.
     */
    public ProveedorEntity crear(ProveedorContratoDto dto) {
        if (dto == null) return null;
        return proveedorRepository.save(proveedorMapper.toEntity(dto));
    }

    /**
     * Actualiza los campos del proveedor existente.
     * Se usa en updateContrato — no crea un registro nuevo,
     * muta el que ya está asociado al contrato.
     * Si el contrato no tenía proveedor, crea uno nuevo.
     */
    public ProveedorEntity actualizar(ProveedorEntity proveedorActual, ProveedorContratoDto dto) {
        if (dto == null) return proveedorActual;

        if (proveedorActual != null) {
            proveedorActual.setRazonSocial(dto.getRazonSocial());
            proveedorActual.setDomicilioFiscal(dto.getDomicilioFiscal());
            proveedorActual.setRepresentante(dto.getRepresentante());
            proveedorActual.setCaracter(dto.getCaracter());
            return proveedorActual;
        }

        // El contrato no tenía proveedor aún (captura progresiva)
        return crear(dto);
    }

}
