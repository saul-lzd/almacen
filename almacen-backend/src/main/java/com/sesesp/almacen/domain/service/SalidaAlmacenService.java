package com.sesesp.almacen.domain.service;

import com.sesesp.almacen.common.exception.ContratoValidacionException;
import com.sesesp.almacen.domain.dto.EntregaRequestDto;
import com.sesesp.almacen.domain.entity.AlmacenBienEntity;
import com.sesesp.almacen.domain.entity.AlmacenBienEntity.EstatusBien;
import com.sesesp.almacen.domain.entity.BeneficiarioEntity;
import com.sesesp.almacen.domain.entity.ContratoEntity;
import com.sesesp.almacen.domain.entity.ContratoEntity.EstatusContrato;
import com.sesesp.almacen.domain.entity.SalidaAlmacenBienEntity;
import com.sesesp.almacen.domain.entity.SalidaAlmacenEntity;
import com.sesesp.almacen.domain.repository.AlmacenBienRepository;
import com.sesesp.almacen.domain.repository.BeneficiarioRepository;
import com.sesesp.almacen.domain.repository.ContratoRepository;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalidaAlmacenService {

    private static final Logger logger = LoggerFactory.getLogger(SalidaAlmacenService.class);

    private final ContratoRepository contratoRepository;
    private final BeneficiarioRepository beneficiarioRepository;
    private final AlmacenBienRepository almacenBienRepository;
    private final SalidaAlmacenRepository salidaAlmacenRepository;

    /**
     * Registra la entrega de bienes a un beneficiario.
     *
     * Validaciones:
     *   - Contrato en LISTO_PARA_ENTREGAR o ENTREGA_PARCIAL
     *   - El beneficiario existe
     *   - Todos los bienes indicados están en LISTO_PARA_ENTREGAR y pertenecen al contrato
     *
     * Efecto:
     *   - Crea SalidaAlmacenEntity con su folio
     *   - Marca los bienes como ENTREGADO
     *   - Si quedan bienes LISTO_PARA_ENTREGAR → ENTREGA_PARCIAL; si no → ENTREGADO
     */
    @Transactional
    public void registrarEntrega(Integer idContrato, EntregaRequestDto request) {
        logger.info("Registrando entrega para contrato ID: {}", idContrato);

        // 1. Validar contrato
        ContratoEntity contrato = contratoRepository.findById(idContrato)
                .orElseThrow(() -> new EntityNotFoundException("Contrato no encontrado: " + idContrato));

        if (contrato.getEstatus() != EstatusContrato.LISTO_PARA_ENTREGAR
                && contrato.getEstatus() != EstatusContrato.ENTREGA_PARCIAL
                && contrato.getEstatus() != EstatusContrato.RECEPCION_PARCIAL) {
            throw new ContratoValidacionException(List.of(
                    "El contrato no puede entregarse porque su estatus es: "
                            + contrato.getEstatus().name()
                            + ". Solo contratos LISTO_PARA_ENTREGAR, ENTREGA_PARCIAL o RECEPCION_PARCIAL pueden registrar entregas."));
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

        // 7. Actualizar estatus del contrato
        long pendientesEntrega = almacenBienRepository.countByContratoIdContratoAndEstatusAndActivoTrue(
                idContrato, EstatusBien.LISTO_PARA_ENTREGAR);

        EstatusContrato estatusOrigen = contrato.getEstatus();
        EstatusContrato nuevoEstatus;

        if (estatusOrigen == EstatusContrato.RECEPCION_PARCIAL) {
            // Aún hay recepciones pendientes — el contrato permanece en RECEPCION_PARCIAL
            // independientemente de cuántos bienes se hayan entregado ya.
            nuevoEstatus = EstatusContrato.RECEPCION_PARCIAL;
        } else {
            // Todas las recepciones están completas: avanzar según entregados restantes.
            nuevoEstatus = pendientesEntrega == 0
                    ? EstatusContrato.ENTREGADO
                    : EstatusContrato.ENTREGA_PARCIAL;
        }

        salida.setEsEntregaTotal(pendientesEntrega == 0 && estatusOrigen != EstatusContrato.RECEPCION_PARCIAL);
        contrato.setEstatus(nuevoEstatus);
        contratoRepository.save(contrato);

        logger.info("Entrega registrada. Folio: {}. Contrato ID: {} → {}",
                salida.getFolioSalidaAlmacen(), idContrato, nuevoEstatus);
    }

    private String generarFolio() {
        String anio = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy"));
        long secuencial = salidaAlmacenRepository.count() + 1;
        return String.format("SA-%s-%04d", anio, secuencial);
    }
}
