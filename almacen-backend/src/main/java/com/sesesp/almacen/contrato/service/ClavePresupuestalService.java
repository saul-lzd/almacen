package com.sesesp.almacen.contrato.service;

import com.sesesp.almacen.contrato.dto.ClavePresupuestalResponse;
import com.sesesp.almacen.contrato.entity.ClavePresupuestalEntity;
import com.sesesp.almacen.contrato.mapper.ClavePresupuestalMapper;
import com.sesesp.almacen.contrato.repository.ClavePresupuestalRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ClavePresupuestalService {

    private final ClavePresupuestalRepository repository;

    public ClavePresupuestalService(ClavePresupuestalRepository repository) {
        this.repository = repository;
    }

    public List<ClavePresupuestalResponse> getAll() {
        List<ClavePresupuestalEntity> listEntity = this.repository.findByActivoTrue();
        List<ClavePresupuestalResponse> listResponse = new ArrayList<>();

        for (ClavePresupuestalEntity  entity : listEntity) {
            listResponse.add(ClavePresupuestalMapper.toResponse(entity));
        }

        return listResponse;
    }


}
