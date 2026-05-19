package com.sesesp.almacen.domain.service;

import com.sesesp.almacen.common.dto.CatalogOptionDto;
import com.sesesp.almacen.domain.dto.ClavePresupuestalDto;
import com.sesesp.almacen.domain.entity.ClavePresupuestalEntity;
import com.sesesp.almacen.domain.entity.ContratoClavePresupuestalEntity;
import com.sesesp.almacen.domain.entity.ContratoEntity;
import com.sesesp.almacen.domain.mapper.ClavePresupuestalMapper;
import com.sesesp.almacen.domain.repository.ClavePresupuestalRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClavePresupuestalService {

    private static final Logger logger = LoggerFactory.getLogger(ClavePresupuestalService.class);

    private final ClavePresupuestalRepository clavePresupuestalRepository;
    private final ClavePresupuestalMapper clavePresupuestalMapper;


    /**
     * Lista todas las claves activas para el dropdown del contrato.
     */
    public List<CatalogOptionDto> findOptions() {
        return clavePresupuestalRepository.findByActivoTrue()
                .stream()
                .map(clavePresupuestalMapper::toOption)
                .toList();
    }

    /**
     * Crea las relaciones contrato-clave a partir del DTO.
     * Las claves deben existir en el catálogo — lanza excepción si no.
     * Se usa en createContrato.
     */
    public List<ContratoClavePresupuestalEntity> crear(
            ContratoEntity contrato,
            List<ClavePresupuestalDto> clavesDto) {

        return clavesDto.stream()
                .map(dto -> {
                    ClavePresupuestalEntity clave = buscarClavePorCodigo(dto.getClave());
                    return ContratoClavePresupuestalEntity.builder()
                            .contrato(contrato)
                            .clavePresupuestal(clave)
                            .montoAsignado(dto.getMontoAsignado())
                            .build();
                })
                .toList();
    }

    /**
     * Sincroniza las claves presupuestales del contrato.
     *
     * null  → no se toca nada
     * lista → agrega nuevas, actualiza monto de existentes, desactiva eliminadas
     *
     * Se usa en updateContrato.
     */
    public void sincronizar(ContratoEntity contrato, List<ClavePresupuestalDto> clavesRequest) {
        if (clavesRequest == null) return;

        List<String> codigosEnRequest = clavesRequest.stream()
                .map(ClavePresupuestalDto::getClave)
                .toList();

        // Desactivar o reactivar según presencia en el request
        contrato.getClavesPresupuestales().forEach(relacion -> {
            String codigoActual = relacion.getClavePresupuestal().getClave();
            relacion.setActivo(codigosEnRequest.contains(codigoActual));
        });

        // Mapa de relaciones existentes por código para acceso rápido
        Map<String, ContratoClavePresupuestalEntity> relacionesPorCodigo =
                contrato.getClavesPresupuestales().stream()
                        .collect(Collectors.toMap(
                                r -> r.getClavePresupuestal().getClave(),
                                r -> r,
                                (a, b) -> a
                        ));

        for (ClavePresupuestalDto dto : clavesRequest) {
            ContratoClavePresupuestalEntity relacionExistente =
                    relacionesPorCodigo.get(dto.getClave());

            if (relacionExistente != null) {
                // Actualizar monto de la clave existente
                relacionExistente.setMontoAsignado(dto.getMontoAsignado());
                relacionExistente.setActivo(true);
            } else {
                // Agregar clave nueva desde el catálogo
                ClavePresupuestalEntity clave = buscarClavePorCodigo(dto.getClave());
                contrato.getClavesPresupuestales().add(
                        ContratoClavePresupuestalEntity.builder()
                                .contrato(contrato)
                                .clavePresupuestal(clave)
                                .montoAsignado(dto.getMontoAsignado())
                                .build()
                );
            }
        }

        logger.info("Claves presupuestales sincronizadas para contrato ID: {}", contrato.getIdContrato());
    }

    // ─── helpers ───────────────────────────────────────────────

    private ClavePresupuestalEntity buscarClavePorCodigo(String codigo) {
        return clavePresupuestalRepository
                .findByClaveAndActivoTrue(codigo)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Clave presupuestal no encontrada: " + codigo));
    }
}
