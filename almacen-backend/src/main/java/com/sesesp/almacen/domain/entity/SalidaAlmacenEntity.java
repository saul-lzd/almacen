package com.sesesp.almacen.domain.entity;

import com.sesesp.almacen.common.entity.AuditoriaEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Evento de entrega de bienes al beneficiario.
 * Se genera uno por cada evento de entrega (parcial o total).
 *
 * es_entrega_total: true cuando esta salida completa el contrato.
 * beneficiario_firma: false cuando el beneficiario solo firma en entrega total
 *                     (algunos no firman entregas parciales).
 *
 * folio_salida_almacen: generado automáticamente por el sistema.
 * nombre_entrega_almacen: persona del almacén que entrega.
 * nombre_recibe_beneficiario: persona que recibe en el beneficiario
 *                             (puede cambiar en cada entrega).
 *
 * tabla salida_almacen
 */
@Entity
@Table(name = "salida_almacen")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalidaAlmacenEntity extends AuditoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_salida_almacen")
    private Integer idSalidaAlmacen;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_contrato", nullable = false)
    private ContratoEntity contrato;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_beneficiario", nullable = false)
    private BeneficiarioEntity beneficiario;

    /** Folio autogenerado por el sistema (ej: SA-2026-001) */
    @Column(name = "folio_salida_almacen", nullable = false, length = 100, unique = true)
    private String folioSalidaAlmacen;

    @Column(name = "fecha_salida", nullable = false)
    private LocalDateTime fechaSalida;

    /** Persona del almacén que realiza la entrega */
    @Column(name = "nombre_entrega_almacen", nullable = false, length = 255)
    private String nombreEntregaAlmacen;

    /** Persona del beneficiario que recibe en este evento */
    @Column(name = "nombre_recibe_beneficiario", nullable = false, length = 255)
    private String nombreRecibeBeneficiario;

    /**
     * true = esta salida completa el pedido del contrato.
     * Genera el documento de entrega final.
     */
    @Column(name = "es_entrega_total", nullable = false)
    //private Boolean esEntregaTotal = false;
    private Boolean esEntregaTotal;

    /**
     * true = el beneficiario firmó en esta entrega.
     * false = no firmó (algunos beneficiarios solo firman en entrega total).
     */
    @Column(name = "beneficiario_firma", nullable = false)
    //private Boolean beneficiarioFirma = true;
    private Boolean beneficiarioFirma;

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @OneToMany(mappedBy = "salidaAlmacen", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SalidaAlmacenBienEntity> bienes = new ArrayList<>();

    @OneToMany(mappedBy = "salidaAlmacen", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EvidenciaSalidaEntity> evidencias = new ArrayList<>();
}