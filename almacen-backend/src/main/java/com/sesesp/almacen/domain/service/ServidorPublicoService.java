package com.sesesp.almacen.domain.service;

//import com.sesesp.almacen.domain.mapper.FuncionarioMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ServidorPublicoService {

    Logger logger = LoggerFactory.getLogger(ServidorPublicoService.class);

//    private final FuncionarioRepository funcionarioRepository;
//    private final FuncionarioMapper servidorPublicoMapper;
//
//
//    public FuncionarioEntity createCompradorFromContrato(ContratoCreateRequestDto requestContrato) {
//        logger.info("Processing Comprador for Contrato {}", requestContrato.getNumeroContrato());
//        return createServidorPublicoFromContrato(requestContrato.getComprador());
//    }
//
//    public FuncionarioEntity createAdministradorDelContratoFromContrato(ContratoCreateRequestDto requestContrato) {
//        logger.info("Creating Administrador del Contrato for Contrato {}", requestContrato.getNumeroContrato());
//        return createServidorPublicoFromContrato(requestContrato.getAdministradorContrato());
//    }
//
//    private FuncionarioEntity createServidorPublicoFromContrato(FuncionarioDto servidorPublicoDto) {
//        // If validations are required place them here.
//        if (servidorPublicoDto == null ) {
//            logger.info("Servidor Publico is empty");
//            return null;
//        }
//
//        if (servidorPublicoDto.getId() == null ) {
//            FuncionarioEntity newEntity = servidorPublicoMapper.toEntity(servidorPublicoDto);
//            return funcionarioRepository.save(newEntity);
//        } else {
//            return funcionarioRepository.findById(servidorPublicoDto.getId()).get();
//        }
//    }
}
