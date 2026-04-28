package com.sesesp.almacen.contrato.service;

import com.sesesp.almacen.common.exception.ContratoNoEncontradoException;
import com.sesesp.almacen.contrato.dto.ContratoRequest;
import com.sesesp.almacen.contrato.repository.ContratoRepository;
import com.sesesp.almacen.contrato.entity.Contrato;
import com.sesesp.almacen.contrato.dto.ContratoResponse;
import com.sesesp.almacen.contrato.mapper.ContratoMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContratoService {

    private final ContratoRepository repository;

    public ContratoService(ContratoRepository contratoRepository) {
        this.repository = contratoRepository;
    }

    public List<Contrato> getAll() {
        return repository.findByActivoTrue();
    }

    public ContratoResponse getById(Long idContrato) {
        Contrato contrato = repository.findByIdContratoAndActivoTrue(idContrato)
                .orElseThrow(() -> new ContratoNoEncontradoException(idContrato));

        return ContratoMapper.toResponse(contrato);
    }

    public ContratoResponse save(ContratoRequest request) {
        request.setIdAdministradorContrato(1L);
        request.setIdComprador(1L);
        request.setIdProveedor(1L);
        request.setIdTipoOrigenContrato(1L);
        request.setIdEstatusContrato(1L);
        request.setNumeroExhibiciones(2);


        Contrato entity = ContratoMapper.toEntity(request);
        Contrato contratoGuardado = repository.save(entity);
        return ContratoMapper.toResponse(contratoGuardado);
    }




}
