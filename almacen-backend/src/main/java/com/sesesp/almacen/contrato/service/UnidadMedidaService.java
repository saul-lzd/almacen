package com.sesesp.almacen.contrato.service;

import com.sesesp.almacen.contrato.dto.ClavePresupuestalResponse;
import com.sesesp.almacen.contrato.dto.UnidadMedidaResponse;
import com.sesesp.almacen.contrato.entity.ClavePresupuestalEntity;
import com.sesesp.almacen.contrato.entity.UnidadMedidaEntity;
import com.sesesp.almacen.contrato.mapper.ClavePresupuestalMapper;
import com.sesesp.almacen.contrato.mapper.UnidadMedidaMapper;
import com.sesesp.almacen.contrato.repository.UnidadMedidaRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UnidadMedidaService {

    private final UnidadMedidaRepository repository;

    public UnidadMedidaService(UnidadMedidaRepository repository) {
        this.repository = repository;
    }

    public List<UnidadMedidaResponse> getAll() {
        List<UnidadMedidaEntity> listEntity = this.repository.findByActivoTrue();
        List<UnidadMedidaResponse> listResponse = new ArrayList<>();

        for (UnidadMedidaEntity entity : listEntity) {
            listResponse.add(UnidadMedidaMapper.toResponse(entity));
        }

        return listResponse;
    }
}
