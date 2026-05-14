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
    private final ContratoClavePresupuestalMapper contratoClavePresupuestalMapper;
    private final ClavePresupuestalRepository clavePresupuestalRepository;

    public ContratoClavePresupuestalService(ContratoClavePresupuestalRepository contratoClavePresupuestalRepository,
                                            ContratoClavePresupuestalMapper contratoClavePresupuestalMapper,
                                            ClavePresupuestalRepository clavePresupuestalRepository) {
        this.contratoClavePresupuestalRepository = contratoClavePresupuestalRepository;
        this.contratoClavePresupuestalMapper = contratoClavePresupuestalMapper;
        this.clavePresupuestalRepository = clavePresupuestalRepository;
    }

    public List<ContratoClavePresupuestalEntity> createClavesPresupuestales(
            ContratoEntity contrato,
            List<ClavePresupuestalDto> clavesPresupuestales) {

        if (clavesPresupuestales == null || clavesPresupuestales.isEmpty()) {
            return null;
        }

        List<ContratoClavePresupuestalEntity> entities = clavesPresupuestales
                .stream()
                .map(element -> {
                    ClavePresupuestalEntity clave = clavePresupuestalRepository
                            .findByClavePresupuestalAndActivoTrue(element.getClavePresupuestal())
                            .orElseThrow(() -> new RuntimeException(
                                    "Clave presupuestal no encontrada: " + element.getClavePresupuestal()
                            ));

                    return contratoClavePresupuestalMapper.toEntity(contrato, clave, element.getMontoAsignado());
                })
                .toList();

        return contratoClavePresupuestalRepository.saveAll(entities);
    }
}
