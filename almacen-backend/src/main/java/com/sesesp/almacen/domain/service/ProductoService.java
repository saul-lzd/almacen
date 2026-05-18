package com.sesesp.almacen.domain.service;

//import com.sesesp.almacen.domain.entity.ProductoEntity;
//import com.sesesp.almacen.domain.mapper.ProductoMapper;
//import com.sesesp.almacen.domain.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductoService {

//    private final ProductoRepository productoRepository;
//    private final ProductoMapper productoMapper;


//    public List<ProductoEntity> createProductos(
//            ContratoEntity contrato,
//            List<ContratoBienDto> productos ) {
//
//        if (productos == null || productos.isEmpty()) {
//            return null;
//        }
//
//        List<ProductoEntity> entities = productos.stream()
//                .map(element -> {
//                    return productoMapper.toEntity(contrato, element);
//                })
//                .map(productoRepository::save)
//                .toList();
//
//        return productoRepository.saveAll(entities);
//    }
//
//    public void syncProductos(
//            ContratoEntity contrato,
//            List<ContratoBienDto> productos) {
//
//    }

}
