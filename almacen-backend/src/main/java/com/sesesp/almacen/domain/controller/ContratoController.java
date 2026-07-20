package com.sesesp.almacen.domain.controller;

import com.sesesp.almacen.domain.dto.ActualizarFechaTentativaRequestDto;
import com.sesesp.almacen.domain.dto.AlmacenBienGrupoDto;
import com.sesesp.almacen.domain.dto.ContratoCreateRequestDto;
import com.sesesp.almacen.domain.dto.ContratoDto;
import com.sesesp.almacen.domain.dto.EntregaRequestDto;
import com.sesesp.almacen.domain.dto.RecepcionAlmacenRequestDto;
import com.sesesp.almacen.domain.dto.RecepcionAlmacenResponseDto;
import com.sesesp.almacen.domain.dto.RecepcionDetalleDto;
import com.sesesp.almacen.domain.service.AlmacenBienService;
import com.sesesp.almacen.domain.service.ContratoService;
import com.sesesp.almacen.domain.service.RecepcionAlmacenService;
import com.sesesp.almacen.domain.service.SalidaAlmacenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/contratos")
@RequiredArgsConstructor
public class ContratoController {

    private final ContratoService contratoService;
    private final RecepcionAlmacenService recepcionAlmacenService;
    private final AlmacenBienService almacenBienService;
    private final SalidaAlmacenService salidaAlmacenService;

    @GetMapping
    public ResponseEntity<List<ContratoDto>> findAll() {
        return ResponseEntity
                .ok(contratoService.findAllContratos());
    }

    @GetMapping("/{idContrato}")
    public ResponseEntity<ContratoDto> findById(
            @PathVariable Integer idContrato) {
        return ResponseEntity.
                ok(contratoService.findContratoById(idContrato));
    }

    @PostMapping
    public ResponseEntity<ContratoDto> create(
            @RequestBody ContratoCreateRequestDto request) {
        return ResponseEntity.
                status(HttpStatus.CREATED)
                .body(contratoService.createContrato(request));
    }

    @PutMapping("/{idContrato}")
    public ResponseEntity<ContratoDto> update(
            @PathVariable Integer idContrato,
            @RequestBody ContratoCreateRequestDto request) {
        return ResponseEntity
                .ok(contratoService.updateContrato(idContrato, request));
    }

    /**
     * Envía el contrato al almacén.
     * Cambia el estatus de CAPTURA a POR_RECIBIR y bloquea la edición.
     * No recibe body — la acción se ejecuta sobre el contrato existente.
     * Regresa 400 con la lista de errores si el contrato está incompleto.
     */
    @PatchMapping("/{idContrato}/enviar-almacen")
    public ResponseEntity<?> enviarAlmacen(@PathVariable Integer idContrato) {
        contratoService.enviarAlmacen(idContrato);
        return ResponseEntity
                .ok().build();
    }

    /**
     * Actualiza la fecha tentativa de llegada del proveedor.
     * Solo disponible cuando el contrato está en POR_RECIBIR.
     * Una vez que llega el primer lote (RECEPCION_PARCIAL en adelante) queda bloqueado.
     */
    @PatchMapping("/{idContrato}/fecha-tentativa")
    public ResponseEntity<?> actualizarFechaTentativa(
            @PathVariable Integer idContrato,
            @RequestBody ActualizarFechaTentativaRequestDto request) {
        contratoService.actualizarFechaTentativa(idContrato, request);
        return ResponseEntity.ok().build();
    }

    /**
     * Autoriza la entrega de los bienes PROCESADO del contrato.
     * Acepta contratos en EN_ALMACEN o RECEPCION_PARCIAL.
     * Los bienes pasan a LISTO_PARA_ENTREGAR; el contrato solo avanza de estatus si venía de EN_ALMACEN.
     */
    @PatchMapping("/{idContrato}/autorizar-entrega")
    public ResponseEntity<?> autorizarEntrega(@PathVariable Integer idContrato) {
        contratoService.autorizarEntrega(idContrato);
        return ResponseEntity.ok().build();
    }

    /**
     * Devuelve la lista completa de recepciones de un contrato, ordenadas de más reciente a más antigua.
     * Cada recepción incluye su folio, fechas, transportista, almacenista, observaciones, estatus y bienes recibidos.
     */
    @GetMapping("/{idContrato}/recepciones")
    public ResponseEntity<List<RecepcionDetalleDto>> listarRecepciones(
            @PathVariable Integer idContrato) {
        return ResponseEntity.ok(recepcionAlmacenService.listarRecepciones(idContrato));
    }

    /**
     * Devuelve los datos de la recepción guardada para un contrato.
     * Usado en la vista de solo lectura del almacén.
     */
    @GetMapping("/{idContrato}/recepcion")
    public ResponseEntity<RecepcionAlmacenResponseDto> findRecepcion(
            @PathVariable Integer idContrato) {
        return ResponseEntity.ok(recepcionAlmacenService.findByContrato(idContrato));
    }

    /**
     * Registra la recepción de bienes cuando llega el proveedor al almacén.
     * Cambia el estatus de POR_RECIBIR a EN_ALMACEN (recepción completa)
     * o RECEPCION_PARCIAL (si algún bien llegó con menos unidades de las esperadas).
     * Recibe multipart/form-data: la parte "request" con el JSON del payload
     * y la parte "evidencias" con las fotos (mínimo 5, máximo 10).
     */
    @PostMapping(value = "/{idContrato}/recepcion", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registrarRecepcion(
            @PathVariable Integer idContrato,
            @RequestPart("request") RecepcionAlmacenRequestDto request,
            @RequestPart("evidencias") List<MultipartFile> evidencias) {
        recepcionAlmacenService.recibirBienes(idContrato, request, evidencias);
        return ResponseEntity.ok().build();
    }

    /**
     * Devuelve los bienes recibidos de un contrato agrupados por tipo (contrato_bien),
     * con sus unidades físicas individuales y su estatus de procesamiento.
     */
    @GetMapping("/{idContrato}/almacen-bienes")
    public ResponseEntity<List<AlmacenBienGrupoDto>> getAlmacenBienes(
            @PathVariable Integer idContrato,
            @RequestParam(required = false) Integer recepcionId) {
        if (recepcionId != null) {
            return ResponseEntity.ok(almacenBienService.getBienesAgrupadosPorRecepcion(idContrato, recepcionId));
        }
        return ResponseEntity.ok(almacenBienService.getBienesAgrupados(idContrato));
    }

    /**
     * Devuelve todos los bienes de una recepción sin filtro de estatus (para vista de detalle).
     */
    @GetMapping("/{idContrato}/recepciones/{idRecepcion}/bienes")
    public ResponseEntity<List<AlmacenBienGrupoDto>> getBienesDetalleRecepcion(
            @PathVariable Integer idContrato,
            @PathVariable Integer idRecepcion) {
        return ResponseEntity.ok(almacenBienService.getBienesDetalleRecepcion(idContrato, idRecepcion));
    }

    /**
     * Devuelve los bienes listos para entregar (LISTO_PARA_ENTREGAR) agrupados por tipo.
     */
    @GetMapping("/{idContrato}/bienes-entrega")
    public ResponseEntity<List<AlmacenBienGrupoDto>> getBienesEntrega(
            @PathVariable Integer idContrato) {
        return ResponseEntity.ok(almacenBienService.getBienesListosParaEntregar(idContrato));
    }

    /**
     * Registra la entrega de bienes a un beneficiario.
     * Marca los bienes como ENTREGADO y actualiza el estatus del contrato.
     *
     * multipart/form-data: la parte "request" con el JSON de EntregaRequestDto
     * y la parte "evidencias" con las fotos (mínimo 5, máximo 10).
     */
    @PostMapping(value = "/{idContrato}/entrega", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registrarEntrega(
            @PathVariable Integer idContrato,
            @RequestPart("request") EntregaRequestDto request,
            @RequestPart("evidencias") List<MultipartFile> evidencias) {
        salidaAlmacenService.registrarEntrega(idContrato, request, evidencias);
        return ResponseEntity.ok().build();
    }
}