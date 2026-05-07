package com.sesesp.almacen.domain.service;

import com.sesesp.almacen.common.dto.CatalogOptionDto;
import com.sesesp.almacen.domain.mapper.UnidadMedidaMapper;
import com.sesesp.almacen.domain.repository.UnidadMedidaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UnidadMedidaService {

    private final UnidadMedidaRepository repository;
    private final UnidadMedidaMapper mapper;

    public UnidadMedidaService(UnidadMedidaRepository repository, UnidadMedidaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<CatalogOptionDto> findOptions() {
        return this.repository.findByActivoTrue()
                .stream()
                .map(this.mapper::toOption)
                .toList();
    }

}
