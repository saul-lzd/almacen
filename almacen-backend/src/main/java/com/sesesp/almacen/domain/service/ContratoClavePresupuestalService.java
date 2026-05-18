package com.sesesp.almacen.domain.service;

import com.sesesp.almacen.domain.dto.ClavePresupuestalDto;
import com.sesesp.almacen.domain.entity.BeneficiarioEntity;
import com.sesesp.almacen.domain.entity.ClavePresupuestalEntity;
import com.sesesp.almacen.domain.entity.ContratoClavePresupuestalEntity;
import com.sesesp.almacen.domain.entity.ContratoEntity;
import com.sesesp.almacen.domain.mapper.ContratoClavePresupuestalMapper;
import com.sesesp.almacen.domain.repository.ClavePresupuestalRepository;
import com.sesesp.almacen.domain.repository.ContratoClavePresupuestalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContratoClavePresupuestalService {

    private final ContratoClavePresupuestalRepository contratoClavePresupuestalRepository;
    private final ContratoClavePresupuestalMapper contratoClavePresupuestalMapper;
    private final ClavePresupuestalRepository clavePresupuestalRepository;

//    public List<ContratoClavePresupuestalEntity> createClavesPresupuestales(
//            ContratoEntity contrato,
//            List<ClavePresupuestalDto> clavesPresupuestales) {
//
//        if (clavesPresupuestales == null || clavesPresupuestales.isEmpty()) {
//            return null;
//        }
//
//        List<ContratoClavePresupuestalEntity> entities = clavesPresupuestales
//                .stream()
//                .map(element -> {
//                    ClavePresupuestalEntity clave = clavePresupuestalRepository
//                            .findByClavePresupuestalAndActivoTrue(element.getClavePresupuestal())
//                            .orElseThrow(() -> new RuntimeException(
//                                    "Clave presupuestal no encontrada: " + element.getClavePresupuestal()
//                            ));
//
//                    return contratoClavePresupuestalMapper.toEntity(contrato, clave, element.getMontoAsignado());
//                })
//                .toList();
//
//        return contratoClavePresupuestalRepository.saveAll(entities);
//    }
//
//    public void syncClavesPresupuestales(
//            ContratoEntity contrato,
//            List<ClavePresupuestalDto> clavesPresupuestales) {
//
//    }

}
