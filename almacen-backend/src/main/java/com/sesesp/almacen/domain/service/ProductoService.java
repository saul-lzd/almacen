package com.sesesp.almacen.domain.service;

import com.sesesp.almacen.domain.dto.ProductoDto;
import com.sesesp.almacen.domain.entity.ContratoEntity;
import com.sesesp.almacen.domain.entity.ProductoEntity;
import com.sesesp.almacen.domain.mapper.ProductoMapper;
import com.sesesp.almacen.domain.repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final ProductoMapper productoMapper;

    public ProductoService(ProductoRepository productoRepository,
                           ProductoMapper productoMapper) {
        this.productoRepository = productoRepository;
        this.productoMapper = productoMapper;
    }

    public List<ProductoEntity> createProductos(
            ContratoEntity contrato,
            List<ProductoDto> productos ) {

        if (productos == null || productos.isEmpty()) {
            return null;
        }

        List<ProductoEntity> entities = productos.stream()
                .map(element -> {
                    return productoMapper.toEntity(contrato, element);
                })
                .map(productoRepository::save)
                .toList();

        return productoRepository.saveAll(entities);
    }

}
