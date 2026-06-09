package com.sesesp.almacen.domain.service;

import com.sesesp.almacen.common.exception.ContratoValidacionException;
import com.sesesp.almacen.domain.dto.EntregaRequestDto;
import com.sesesp.almacen.domain.entity.AlmacenBienEntity;
import com.sesesp.almacen.domain.entity.AlmacenBienEntity.EstatusBien;
import com.sesesp.almacen.domain.entity.BeneficiarioEntity;
import com.sesesp.almacen.domain.entity.ContratoEntity;
import com.sesesp.almacen.domain.entity.ContratoEntity.EstatusContrato;
import com.sesesp.almacen.domain.entity.RecepcionAlmacenEntity;
import com.sesesp.almacen.domain.entity.RecepcionAlmacenEntity.EstatusRecepcion;
import com.sesesp.almacen.domain.entity.SalidaAlmacenBienEntity;
import com.sesesp.almacen.domain.entity.SalidaAlmacenEntity;
import com.sesesp.almacen.domain.repository.AlmacenBienRepository;
import com.sesesp.almacen.domain.repository.BeneficiarioRepository;
import com.sesesp.almacen.domain.repository.ContratoRepository;
import com.sesesp.almacen.domain.repository.RecepcionAlmacenRepository;
import com.sesesp.almacen.domain.repository.SalidaAlmacenRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalidaAlmacenService {

    private static final Logger logger = LoggerFactory.getLogger(SalidaAlmacenService.class);

    private final ContratoRepository contratoRepository;
    private final BeneficiarioRepository beneficiarioRepository;
    private final AlmacenBienRepository almacenBienRepository;
    private final SalidaAlmacenRepository salidaAlmacenRepository;
    private final RecepcionAlmacenRepository recepcionAlmacenRepository;

    /**
     * Registra la entrega de bienes a un beneficiario.
     *
     * Validaciones:
     *   - Contrato en POR_RECIBIR y no cerrado
     *   - El beneficiario existe
     *   - Todos los bienes indicados están en LISTO_PARA_ENTREGAR y pertenecen al contrato
     *
     * Efecto:
     *   - Crea SalidaAlmacenEntity con su folio
     *   - Marca los bienes como ENTREGADO
     *   - Actualiza checkpoints del contrato (primeraEntregaAutorizada, contratoCerrado)
     *   - Marca la recepción como ENTREGADA si todos sus bienes están en ENTREGADO
     */
    @Transactional
    public void registrarEntrega(Integer idContrato, EntregaRequestDto request) {
        logger.info("Registrando entrega para contrato ID: {}", idContrato);

        // 1. Validar contrato
        ContratoEntity contrato = contratoRepository.findById(idContrato)
                .orElseThrow(() -> new EntityNotFoundException("Contrato no encontrado: " + idContrato));

        if (contrato.getEstatus() != EstatusContrato.POR_RECIBIR) {
            throw new ContratoValidacionException(List.of(
                    "El contrato no puede entregarse porque su estatus es: "
                            + contrato.getEstatus().name() + "."));
        }

        if (contrato.isContratoCerrado()) {
            throw new ContratoValidacionException(List.of(
                    "El contrato ya está cerrado — todos los bienes han sido entregados."));
        }

        // 2. Validar request
        List<String> errores = new ArrayList<>();
        if (request.getNombreEntregaAlmacen() == null || request.getNombreEntregaAlmacen().isBlank())
            errores.add("El nombre de quien entrega es obligatorio.");
        if (request.getNombreRecibeBeneficiario() == null || request.getNombreRecibeBeneficiario().isBlank())
            errores.add("El nombre de quien recibe es obligatorio.");
        if (request.getIdsAlmacenBien() == null || request.getIdsAlmacenBien().isEmpty())
            errores.add("Debe seleccionar al menos un bien para entregar.");
        if (!errores.isEmpty()) throw new ContratoValidacionException(errores);

        // 3. Cargar beneficiario
        BeneficiarioEntity beneficiario = beneficiarioRepository.findById(request.getIdBeneficiario())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Beneficiario no encontrado: " + request.getIdBeneficiario()));

        // 4. Cargar y validar bienes
        List<AlmacenBienEntity> bienes = almacenBienRepository
                .findByIdAlmacenBienInAndActivoTrue(request.getIdsAlmacenBien());

        if (bienes.size() != request.getIdsAlmacenBien().size()) {
            throw new ContratoValidacionException(List.of("Uno o más bienes no encontrados."));
        }

        List<String> erroresBienes = bienes.stream()
                .filter(b -> !b.getContrato().getIdContrato().equals(idContrato))
                .map(b -> "El bien " + b.getCodigoInterno() + " no pertenece a este contrato.")
                .collect(Collectors.toList());

        bienes.stream()
                .filter(b -> b.getEstatus() != EstatusBien.LISTO_PARA_ENTREGAR)
                .forEach(b -> erroresBienes.add(
                        "El bien " + b.getCodigoInterno() + " no está listo para entregar (estatus: " + b.getEstatus() + ")."));

        if (!erroresBienes.isEmpty()) throw new ContratoValidacionException(erroresBienes);

        // 5. Crear la salida
        LocalDateTime ahora = LocalDateTime.now();
        SalidaAlmacenEntity salida = SalidaAlmacenEntity.builder()
                .contrato(contrato)
                .beneficiario(beneficiario)
                .folioSalidaAlmacen(generarFolio())
                .fechaSalida(ahora)
                .nombreEntregaAlmacen(request.getNombreEntregaAlmacen().trim())
                .nombreRecibeBeneficiario(request.getNombreRecibeBeneficiario().trim())
                .beneficiarioFirma(Boolean.TRUE.equals(request.getBeneficiarioFirma()))
                .observaciones(request.getObservaciones())
                .esEntregaTotal(false) // se actualiza abajo
                .build();

        // 6. Vincular bienes y marcarlos como ENTREGADO
        for (AlmacenBienEntity b : bienes) {
            salida.getBienes().add(SalidaAlmacenBienEntity.builder()
                    .salidaAlmacen(salida)
                    .almacenBien(b)
                    .build());
            b.setEstatus(EstatusBien.ENTREGADO);
            b.setFechaEntrega(ahora);
        }

        salidaAlmacenRepository.save(salida);
        almacenBienRepository.saveAll(bienes);

        // 7. Marcar checkpoint de primera entrega
        if (!contrato.isPrimeraEntregaAutorizada()) {
            contrato.setPrimeraEntregaAutorizada(true);
        }

        // 8. Verificar si todos los bienes del contrato están entregados → cerrar contrato
        long totalBienes    = almacenBienRepository.countByContratoIdContratoAndActivoTrue(idContrato);
        long totalEntregados = almacenBienRepository.countByContratoIdContratoAndEstatusAndActivoTrue(
                idContrato, EstatusBien.ENTREGADO);
        boolean contratoCompleto = totalBienes > 0 && totalEntregados == totalBienes;

        salida.setEsEntregaTotal(contratoCompleto);

        if (contratoCompleto) {
            contrato.setContratoCerrado(true);
        }
        contratoRepository.save(contrato);

        // 9. Verificar recepciones afectadas → marcar ENTREGADA si todos sus bienes están entregados
        Set<Integer> recepcionesAfectadas = bienes.stream()
                .filter(b -> b.getRecepcionAlmacenBien() != null)
                .map(b -> b.getRecepcionAlmacenBien().getRecepcionAlmacen().getIdRecepcionAlmacen())
                .collect(Collectors.toSet());

        for (Integer idRecepcion : recepcionesAfectadas) {
            long totalRec    = almacenBienRepository
                    .countByRecepcionAlmacenBienRecepcionAlmacenIdRecepcionAlmacenAndActivoTrue(idRecepcion);
            long entregadosRec = almacenBienRepository
                    .countByRecepcionAlmacenBienRecepcionAlmacenIdRecepcionAlmacenAndEstatusInAndActivoTrue(
                            idRecepcion, List.of(EstatusBien.ENTREGADO));

            if (totalRec > 0 && entregadosRec == totalRec) {
                recepcionAlmacenRepository.findById(idRecepcion).ifPresent(rec -> {
                    rec.setEstatus(EstatusRecepcion.ENTREGADA);
                    recepcionAlmacenRepository.save(rec);
                    logger.info("Recepción {} → ENTREGADA", rec.getFolioEntradaAlmacen());
                });
            }
        }

        logger.info("Entrega registrada. Folio: {}. Contrato ID: {}. Cerrado: {}",
                salida.getFolioSalidaAlmacen(), idContrato, contratoCompleto);
    }

    private String generarFolio() {
        String anio = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy"));
        long secuencial = salidaAlmacenRepository.count() + 1;
        return String.format("SA-%s-%04d", anio, secuencial);
    }
}
