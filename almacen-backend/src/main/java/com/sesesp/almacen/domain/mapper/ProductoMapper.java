package com.sesesp.almacen.domain.mapper;

import com.sesesp.almacen.domain.dto.ProductoDto;
import com.sesesp.almacen.domain.entity.ContratoEntity;
import com.sesesp.almacen.domain.entity.ProductoEntity;
import com.sesesp.almacen.domain.entity.UnidadMedidaEntity;
import com.sesesp.almacen.domain.repository.UnidadMedidaRepository;
import org.springframework.stereotype.Component;

@Component
public class ProductoMapper {

    private final UnidadMedidaRepository repository;

    public ProductoMapper(UnidadMedidaRepository repository) {
        this.repository = repository;
    }

    public ProductoEntity toEntity(
            ContratoEntity entity,
            ProductoDto producto) {

        UnidadMedidaEntity unidadMedida = repository
                .findByClaveAndActivoTrue(producto.getCodigoUnidadMedida())
                .orElseThrow(() -> new RuntimeException("Unidad de medida no encontrada"));

        return ProductoEntity.builder()
                .contrato(entity)
                .lote(producto.getLote())
                .partida(producto.getPartida())
                .descripcionTecnica(producto.getDescripcionTecnica())
                .unidadMedida(unidadMedida)
                .cantidad(producto.getCantidad())
                .precioUnitario(producto.getPrecioUnitario())
                .subtotal(producto.getSubtotal())
                .build();
    }
}
