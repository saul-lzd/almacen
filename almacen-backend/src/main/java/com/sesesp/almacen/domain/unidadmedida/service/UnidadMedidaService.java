package com.sesesp.almacen.domain.unidadmedida.service;

import com.sesesp.almacen.common.dto.CatalogoOptionDto;
import com.sesesp.almacen.domain.clavepresupuestal.entity.ClavePresupuestalEntity;
import com.sesesp.almacen.domain.clavepresupuestal.mapper.ClavePresupuestalMapper;
import com.sesesp.almacen.domain.unidadmedida.dto.UnidadMedidaResponse;
import com.sesesp.almacen.domain.unidadmedida.entity.UnidadMedidaEntity;
import com.sesesp.almacen.domain.unidadmedida.mapper.UnidadMedidaMapper;
import com.sesesp.almacen.domain.unidadmedida.repository.UnidadMedidaRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UnidadMedidaService {

    private final UnidadMedidaRepository repository;

    public UnidadMedidaService(UnidadMedidaRepository repository) {
        this.repository = repository;
    }

    public List<CatalogoOptionDto> getCatalogOptions() {
        List<UnidadMedidaEntity> listEntity = this.repository.findByActivoTrue();
        List<CatalogoOptionDto> options = new ArrayList<>();

        for (UnidadMedidaEntity entity : listEntity) {
            options.add(UnidadMedidaMapper.toOption(entity));
        }
        return options;
    }
}
