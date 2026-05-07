package com.sesesp.almacen.domain.service;

import com.sesesp.almacen.domain.dto.ServidorPublicoDto;
import com.sesesp.almacen.domain.entity.ServidorPublicoEntity;
import com.sesesp.almacen.domain.mapper.ServidorPublicoMapper;
import com.sesesp.almacen.domain.repository.ServidorPublicoRepository;
import org.springframework.stereotype.Service;

@Service
public class ServidorPublicoService {

    private final ServidorPublicoRepository servidorPublicoRepository;
    private final ServidorPublicoMapper servidorPublicoMapper;

    public ServidorPublicoService(ServidorPublicoRepository servidorPublicoRepository, ServidorPublicoMapper servidorPublicoMapper) {
        this.servidorPublicoRepository = servidorPublicoRepository;
        this.servidorPublicoMapper = servidorPublicoMapper;
    }

    public ServidorPublicoEntity resolveServidorPublico(ServidorPublicoDto servidorPublico) {
        if (servidorPublico == null) {
            return null;
        }

        if (servidorPublico.getId() != null) {
            return servidorPublicoRepository.findById(servidorPublico.getId())
                    .orElseThrow( () -> new RuntimeException("Servidor Publico no Encontrado: " + servidorPublico.getNombre()));
        }

        ServidorPublicoEntity servidorPublicoEntity = servidorPublicoMapper.toEntity(servidorPublico);
        return servidorPublicoRepository.save(servidorPublicoEntity);
    }
}
