package com.sesesp.almacen.domain.clavepresupuestal.service;

import com.sesesp.almacen.common.dto.CatalogoOptionDto;
import com.sesesp.almacen.domain.clavepresupuestal.entity.ClavePresupuestalEntity;
import com.sesesp.almacen.domain.clavepresupuestal.mapper.ClavePresupuestalMapper;
import com.sesesp.almacen.domain.clavepresupuestal.repository.ClavePresupuestalRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ClavePresupuestalService {

    private final ClavePresupuestalRepository repository;

    public ClavePresupuestalService(ClavePresupuestalRepository repository) {
        this.repository = repository;
    }

    public List<CatalogoOptionDto> getCatalogOptions() {
        List<ClavePresupuestalEntity> listEntity = this.repository.findByActivoTrue();
        List<CatalogoOptionDto> options = new ArrayList<>();

        for (ClavePresupuestalEntity entity : listEntity) {
            options.add(ClavePresupuestalMapper.toOption(entity));
        }
        return options;
    }
}
