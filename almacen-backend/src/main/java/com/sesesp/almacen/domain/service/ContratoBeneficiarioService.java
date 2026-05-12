package com.sesesp.almacen.domain.service;

import com.sesesp.almacen.domain.entity.BeneficiarioEntity;
import com.sesesp.almacen.domain.entity.ContratoBeneficiarioEntity;
import com.sesesp.almacen.domain.entity.ContratoEntity;
import com.sesesp.almacen.domain.mapper.BeneficiarioMapper;
import com.sesesp.almacen.domain.mapper.ContratoBeneficiarioMapper;
import com.sesesp.almacen.domain.repository.BeneficiarioRepository;
import com.sesesp.almacen.domain.repository.ContratoBeneficiarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class ContratoBeneficiarioService {

    Logger logger = LoggerFactory.getLogger(ContratoBeneficiarioService.class);

    private final ContratoBeneficiarioRepository contratoBeneficiarioRepository;
    private final ContratoBeneficiarioMapper contratoBeneficiarioMapper;
    private final BeneficiarioRepository beneficiarioRepository;
    private final BeneficiarioMapper beneficiarioMapper;

    public ContratoBeneficiarioService(ContratoBeneficiarioRepository contratoBeneficiarioRepository,
                                       ContratoBeneficiarioMapper contratoBeneficiarioMapper,
                                       BeneficiarioRepository beneficiarioRepository,
                                       BeneficiarioMapper beneficiarioMapper) {
        this.contratoBeneficiarioRepository = contratoBeneficiarioRepository;
        this.contratoBeneficiarioMapper = contratoBeneficiarioMapper;
        this.beneficiarioRepository = beneficiarioRepository;
        this.beneficiarioMapper = beneficiarioMapper;
    }

    public List<ContratoBeneficiarioEntity> createBeneficiarios(
            ContratoEntity contrato,
            String beneficiarios) {

        if (beneficiarios == null || beneficiarios.isBlank()) {
            return List.of();
        }

        List<String> nombresBeneficiarios = Arrays.stream(beneficiarios.split(","))
                .map(String::trim)
                .filter(nombre -> !nombre.isBlank())
                .distinct()
                .toList();

        logger.info("Saving {} beneficiarios en la base de datos", nombresBeneficiarios.size());

        List<ContratoBeneficiarioEntity> contratoBeneficiarios = nombresBeneficiarios.stream()
                .map(nombre -> {
                    BeneficiarioEntity beneficiario = beneficiarioRepository
                            .findByNombreAndActivoTrue(nombre)
                            .orElseGet(() -> beneficiarioRepository.save(
                                    beneficiarioMapper.toEntity(nombre)
                            ));

                    return contratoBeneficiarioMapper.toEntity(contrato, beneficiario, "");
                })
                .toList();

        return contratoBeneficiarioRepository.saveAll(contratoBeneficiarios);
    }


}
