package com.sesesp.almacen.domain.entity;

import com.sesesp.almacen.common.entity.AuditoriaEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Evento de recepción de bienes en almacén.
 * Se genera uno por cada visita del proveedor (parcial o total).
 * El proveedor firma este documento como evidencia de entrega.
 *
 * folio_entrada_almacen: generado automáticamente por el sistema.
 * nombre_entrega: persona del proveedor que entrega físicamente.
 * nombre_recibe: persona del almacén que recibe físicamente.
 *
 * tabla recepcion_almacen
 */
@Entity
@Table(name = "recepcion_almacen")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecepcionAlmacenEntity extends AuditoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_recepcion_almacen")
    private Integer idRecepcionAlmacen;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_contrato", nullable = false)
    private ContratoEntity contrato;

    /** Folio autogenerado por el sistema (ej: EA-2026-001) */
    @Column(name = "folio_entrada_almacen", nullable = false, length = 100, unique = true)
    private String folioEntradaAlmacen;

    @Column(name = "fecha_recepcion", nullable = false)
    private LocalDateTime fechaRecepcion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_proveedor", nullable = false)
    private ProveedorEntity proveedor;

    /** Nombre de la persona del proveedor que entrega */
    @Column(name = "nombre_entrega", nullable = false, length = 255)
    private String nombreEntrega;

    /** Nombre de la persona del almacén que recibe */
    @Column(name = "nombre_recibe", nullable = false, length = 255)
    private String nombreRecibe;

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @Column(name = "estatus", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EstatusRecepcion estatus = EstatusRecepcion.INICIADA;

    @OneToMany(mappedBy = "recepcionAlmacen", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RecepcionAlmacenBienEntity> bienes = new ArrayList<>();

    @OneToMany(mappedBy = "recepcionAlmacen", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EvidenciaEntradaEntity> evidencias = new ArrayList<>();

    public enum EstatusRecepcion {
        INICIADA,    // recepción registrada, bienes en estatus RECIBIDO
        EN_PROCESO,  // al menos un bien de esta recepción está siendo procesado
        PROCESADA,   // todos los bienes de esta recepción están en PROCESADO
        ENTREGADA    // todos los bienes de esta recepción están en ENTREGADO
    }
}