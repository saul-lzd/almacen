package com.sesesp.almacen.domain.service;

import com.sesesp.almacen.domain.dto.ClavePresupuestalDto;
import com.sesesp.almacen.domain.entity.ClavePresupuestalEntity;
import com.sesesp.almacen.domain.entity.ContratoClavePresupuestalEntity;
import com.sesesp.almacen.domain.entity.ContratoEntity;
import com.sesesp.almacen.domain.mapper.ContratoClavePresupuestalMapper;
import com.sesesp.almacen.domain.repository.ClavePresupuestalRepository;
import com.sesesp.almacen.domain.repository.ContratoClavePresupuestalRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContratoClavePresupuestalService {

    private final ContratoClavePresupuestalRepository contratoClavePresupuestalRepository;
    private final ClavePresupuestalRepository clavePresupuestalRepository;
    private final ContratoClavePresupuestalMapper contratoClavePresupuestalMapper;

    public ContratoClavePresupuestalService(
            ContratoClavePresupuestalRepository contratoClavePresupuestalRepository,
            ClavePresupuestalRepository clavePresupuestalRepository,
            ContratoClavePresupuestalMapper contratoClavePresupuestalMapper) {

        this.contratoClavePresupuestalRepository = contratoClavePresupuestalRepository;
        this.clavePresupuestalRepository = clavePresupuestalRepository;
        this.contratoClavePresupuestalMapper = contratoClavePresupuestalMapper;
    }

    public List<ContratoClavePresupuestalEntity> createClavesPresupuestales(
            ContratoEntity contrato,
            List<ClavePresupuestalDto> clavesPresupuestales) {

        if (clavesPresupuestales == null || clavesPresupuestales.isEmpty()) {
            return null;
        }

        List<ContratoClavePresupuestalEntity> entities = clavesPresupuestales.stream()
                .map(element -> {
                    ClavePresupuestalEntity clave = clavePresupuestalRepository
                            .findByClavePresupuestal(element.getClavePresupuestal())
                            .orElseThrow(() -> new RuntimeException(
                                    "Clave presupuestal no encontrada: " + element.getClavePresupuestal()
                            ));

                    return contratoClavePresupuestalMapper.toEntity(contrato, clave, element.getMontoAsignado());
                })
                .toList();

        return contratoClavePresupuestalRepository.saveAll(entities);
    }
}
