package com.sesesp.almacen.domain.service;

import com.sesesp.almacen.domain.dto.ContratoCreateRequestDto;
import com.sesesp.almacen.domain.dto.ServidorPublicoDto;
import com.sesesp.almacen.domain.entity.ServidorPublicoEntity;
import com.sesesp.almacen.domain.mapper.ServidorPublicoMapper;
import com.sesesp.almacen.domain.repository.ServidorPublicoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ServidorPublicoService {

    Logger logger = LoggerFactory.getLogger(ServidorPublicoService.class);

    private final ServidorPublicoRepository servidorPublicoRepository;
    private final ServidorPublicoMapper servidorPublicoMapper;

    public ServidorPublicoService(ServidorPublicoRepository servidorPublicoRepository, ServidorPublicoMapper servidorPublicoMapper) {
        this.servidorPublicoRepository = servidorPublicoRepository;
        this.servidorPublicoMapper = servidorPublicoMapper;
    }

    public ServidorPublicoEntity createCompradorFromContrato(ContratoCreateRequestDto requestContrato) {
        logger.info("Processing Comprador for Contrato {}", requestContrato.getNumeroContrato());
        return createServidorPublicoFromContrato(requestContrato.getComprador());
    }

    public ServidorPublicoEntity createAdministradorDelContratoFromContrato(ContratoCreateRequestDto requestContrato) {
        logger.info("Creating Administrador del Contrato for Contrato {}", requestContrato.getNumeroContrato());
        return createServidorPublicoFromContrato(requestContrato.getAdministradorContrato());
    }

    private ServidorPublicoEntity createServidorPublicoFromContrato(ServidorPublicoDto servidorPublicoDto) {
        // If validations are required place them here.
        if (servidorPublicoDto == null ) {
            logger.info("Servidor Publico is empty");
            return null;
        }

        if (servidorPublicoDto.getId() == null ) {
            ServidorPublicoEntity newEntity = servidorPublicoMapper.toEntity(servidorPublicoDto);
            return servidorPublicoRepository.save(newEntity);
        } else {
            return servidorPublicoRepository.findById(servidorPublicoDto.getId()).get();
        }
//        // Flujo de búsqueda o creación (Upsert)
//        return servidorPublicoRepository
//                .findById(spId)
//                .orElseGet(() -> {
//                    logger.info("Servidor Publico ID {} not found, creating new entry", servidorPublicoDto.getId());
//                    ServidorPublicoEntity newEntity = servidorPublicoMapper.toEntity(servidorPublicoDto);
//                    return servidorPublicoRepository.save(newEntity);
//        });
    }
}
