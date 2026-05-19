package com.sesesp.almacen.domain.service;

//import com.sesesp.almacen.domain.mapper.FuncionarioMapper;
import com.sesesp.almacen.domain.dto.FuncionarioDto;
import com.sesesp.almacen.domain.entity.FuncionarioEntity;
import com.sesesp.almacen.domain.mapper.FuncionarioMapper;
import com.sesesp.almacen.domain.repository.FuncionarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FuncionarioService {

    Logger logger = LoggerFactory.getLogger(FuncionarioService.class);

    private final FuncionarioRepository funcionarioRepository;
    private final FuncionarioMapper funcionarioMapper;

    /**
     * Busca un funcionario por ID.
     * Se usa al asignar comprador y administrador del contrato.
     * El ID viene del dropdown de funcionarios en la UI.
     */
    public FuncionarioEntity buscarPorId(Integer id, String rol) {
        if (id == null) return null;
        return funcionarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        rol + " no encontrado con ID: " + id));
    }

    /**
     * Lista todos los funcionarios activos.
     * Se usa para poblar los dropdowns de comprador y administrador en la UI.
     */
    public List<FuncionarioDto> findOptions() {
        return funcionarioRepository.findByActivoTrue()
                .stream()
                .map(funcionarioMapper::toResponse)
                .toList();
    }


}
