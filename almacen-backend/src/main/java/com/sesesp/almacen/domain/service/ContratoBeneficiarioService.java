package com.sesesp.almacen.domain.service;

import com.sesesp.almacen.domain.dto.ClavePresupuestalDto;
import com.sesesp.almacen.domain.entity.BeneficiarioEntity;
import com.sesesp.almacen.domain.entity.ContratoBeneficiarioEntity;
import com.sesesp.almacen.domain.entity.ContratoEntity;
import com.sesesp.almacen.domain.mapper.BeneficiarioMapper;
import com.sesesp.almacen.domain.mapper.ContratoBeneficiarioMapper;
import com.sesesp.almacen.domain.repository.BeneficiarioRepository;
import com.sesesp.almacen.domain.repository.ContratoBeneficiarioRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContratoBeneficiarioService {

    private static final Logger logger = LoggerFactory.getLogger(ContratoBeneficiarioService.class);

    private final ContratoBeneficiarioRepository contratoBeneficiarioRepository;
    private final ContratoBeneficiarioMapper contratoBeneficiarioMapper;
    private final BeneficiarioRepository beneficiarioRepository;
    private final BeneficiarioMapper beneficiarioMapper;


    @Transactional
    public List<ContratoBeneficiarioEntity> createBeneficiarios(
            ContratoEntity contrato,
            String beneficiarios) {

        if (beneficiarios == null || beneficiarios.isBlank()) {
            return List.of();
        }

        List<String> nombresBeneficiarios = getListOfBeneficiarios(beneficiarios);

        logger.info("Saving {} beneficiarios en la base de datos", nombresBeneficiarios.size());

        List<ContratoBeneficiarioEntity> relacionesBeneficiarios =
                nombresBeneficiarios
                        .stream()
                        .map(nombreBeneficiario -> {
                            BeneficiarioEntity beneficiario = findOrCreateBeneficiario(nombreBeneficiario);
                            return contratoBeneficiarioMapper.toEntity(contrato, beneficiario, "");
                        })
                        .toList();

        return contratoBeneficiarioRepository.saveAll(relacionesBeneficiarios);
    }


//    @Transactional
//    public List<ContratoBeneficiarioEntity> syncBeneficiarios(
//            ContratoEntity contrato,
//            String beneficiarios) {
//
//        if (beneficiarios == null) {
//            logger.info("No se sincronizan beneficiarios porque el request viene en null. Contrato ID: {}", contrato.getIdContrato());
//
//            return findAllRelacionesByContrato(contrato.getIdContrato());
//        }
//
//        // nuevos beneficiarios desde el request
//        List<String> nombresBeneficiariosRequest = getListOfBeneficiarios(beneficiarios);
//
//        // relaciones actuales que estan en BD
//        List<ContratoBeneficiarioEntity> relacionesActuales = findAllRelacionesByContrato(contrato.getIdContrato());
//
//
//        // Si no hay nuevos beneficiarios, desactivar todos los existentes
//        if (nombresBeneficiariosRequest.isEmpty()) {
//            logger.info("Desactivando todos los beneficiarios del contrato ID: {}", contrato.getIdContrato());
//            relacionesActuales.forEach(relacion -> relacion.setActivo(false));
//            return contratoBeneficiarioRepository.saveAll(relacionesActuales);
//        }
//
//        Map<String, ContratoBeneficiarioEntity> relacionesPorNombreNormalizado =
//                mapRelacionesByNombreBeneficiario(relacionesActuales);
//
//        Set<String> nombresNormalizadosRequest =
//                nombresBeneficiariosRequest
//                        .stream()
//                        .map(this::normalizar)
//                        .collect(Collectors.toSet());
//
//        desactivarRelacionesEliminadas(relacionesActuales, nombresNormalizadosRequest);
//
//        List<ContratoBeneficiarioEntity> relacionesActualizadas = new ArrayList<>(relacionesActuales);
//
//        for (String nombreBeneficiario : nombresBeneficiariosRequest) {
//            String nombreNormalizado = normalizar(nombreBeneficiario);
//
//            ContratoBeneficiarioEntity relacionExistente =
//                    relacionesPorNombreNormalizado.get(nombreNormalizado);
//
//            if (relacionExistente != null) {
//                relacionExistente.setActivo(true);
//                continue;
//            }
//
//            BeneficiarioEntity beneficiario = findOrCreateBeneficiario(nombreBeneficiario);
//
//            ContratoBeneficiarioEntity nuevaRelacion =
//                    contratoBeneficiarioMapper.toEntity(contrato, beneficiario, "");
//
//            nuevaRelacion.setActivo(true);
//
//            relacionesActualizadas.add(nuevaRelacion);
//        }
//
//        logger.info(
//                "Sincronizando {} relaciones de beneficiarios para contrato ID: {}",
//                relacionesActualizadas.size(),
//                contrato.getIdContrato()
//        );
//
//        return contratoBeneficiarioRepository.saveAll(relacionesActualizadas);
//    }



    // ------------------------------------------------------------------------
    // Helpers

    private BeneficiarioEntity findOrCreateBeneficiario(String nombreBeneficiario) {
        return beneficiarioRepository
                .findByNombreAndActivoTrue(nombreBeneficiario)
                .orElseGet(() ->
                        beneficiarioRepository.save(beneficiarioMapper.toEntity(nombreBeneficiario))
                );
    }

//    private List<ContratoBeneficiarioEntity> findAllRelacionesByContrato(Integer idContrato) {
//        return contratoBeneficiarioRepository.findByContrato_IdContrato(idContrato);
//    }


    private List<String> getListOfBeneficiarios(String beneficiarios) {
        if (beneficiarios == null || beneficiarios.isBlank()) {
            return List.of();
        }

        return Arrays.stream(beneficiarios.split(","))
                .map(String::trim)
                .filter(nombre -> !nombre.isBlank())
                .distinct()
                .toList();
    }

    private Map<String, ContratoBeneficiarioEntity> mapRelacionesByNombreBeneficiario(
            List<ContratoBeneficiarioEntity> relacionesBeneficiarios
    ) {
        return relacionesBeneficiarios.stream()
                .collect(Collectors.toMap(
                        relacion -> normalizar(
                                relacion.getBeneficiario().getNombre()
                        ),
                        Function.identity(),
                        (relacionExistente, relacionDuplicada) -> relacionExistente
                ));
    }

    private void desactivarRelacionesEliminadas(
            List<ContratoBeneficiarioEntity> relacionesActuales,
            Set<String> nombresNormalizadosRequest
    ) {
        relacionesActuales
                .stream()
                .filter(relacion -> !nombresNormalizadosRequest.contains(
                        normalizar(relacion.getBeneficiario().getNombre())
                ))
                .forEach(relacion -> relacion.setActivo(false));
    }

    private String normalizar(String value) {
        return value == null
                ? ""
                : value.trim().toLowerCase();
    }







//
//    private List<ContratoBeneficiarioEntity> findBeneficiariosFromContrato(Integer idContrato){
//        return contratoBeneficiarioRepository.findByContrato_IdContratoAndActivoTrue(idContrato);
//    }
//
//
//
//    private Set<String> normalizarBeneficiariosFromRequest(List<String> beneficiarios) {
//        return beneficiarios.stream()
//                .map(this::normalizar)
//                .collect(Collectors.toSet());
//    }
//




}
