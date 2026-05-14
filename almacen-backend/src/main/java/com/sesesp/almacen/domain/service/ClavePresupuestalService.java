package com.sesesp.almacen.domain.service;

import com.sesesp.almacen.common.dto.CatalogOptionDto;
import com.sesesp.almacen.domain.entity.ClavePresupuestalEntity;
import com.sesesp.almacen.domain.mapper.ClavePresupuestalMapper;
import com.sesesp.almacen.domain.repository.ClavePresupuestalRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClavePresupuestalService {

    private final ClavePresupuestalRepository repository;
    private final ClavePresupuestalMapper mapper;

    public ClavePresupuestalService(ClavePresupuestalRepository repository, ClavePresupuestalMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<CatalogOptionDto> findOptions() {
        return this.repository.findByActivoTrue()
                .stream()
                .map(this.mapper::toOption)
                .toList();
    }

    public ClavePresupuestalEntity findByClavePresupuestal(String clavePresupuestal) {
        return this.repository
                .findByClavePresupuestalAndActivoTrue(clavePresupuestal)
                .orElseGet(null);
    }
}
