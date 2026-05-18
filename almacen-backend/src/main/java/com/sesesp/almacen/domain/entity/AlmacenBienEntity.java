package com.sesesp.almacen.domain.entity;

import com.sesesp.almacen.common.entity.AuditoriaEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Unidad física individual de un bien recibido en almacén.
 * Se crea una instancia por cada unidad recibida en RecepcionAlmacenBienEntity.
 *
 * Ejemplo: si se reciben 3 laptops en una recepción, se crean 3 AlmacenBienEntity,
 * cada una con su propio número de serie y sus propias fotos de evidencia.
 *
 * id_beneficiario: asignación previa a la entrega. El administrador asigna
 * a qué beneficiario va cada bien antes de que el almacén lo entregue.
 *
 * Ciclo de estatus:
 *   PENDIENTE_PROCESAR → PROCESADO → ASIGNADO_ENTREGA → ENTREGADO
 *
 * tabla almacen_bien
 */
@Entity
@Table(name = "almacen_bien")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlmacenBienEntity extends AuditoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_almacen_bien")
    private Integer idAlmacenBien;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_contrato", nullable = false)
    private ContratoEntity contrato;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_contrato_bien", nullable = false)
    private ContratoBienEntity contratoBien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_recepcion_almacen_bien")
    private RecepcionAlmacenBienEntity recepcionAlmacenBien;

    /**
     * Beneficiario al que está destinado este bien.
     * Lo asigna el administrador antes de la entrega.
     * Nullable hasta que se haga la asignación.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_beneficiario")
    private BeneficiarioEntity beneficiario;

    /** Código interno generado por el sistema para identificar el bien en almacén */
    @Column(name = "codigo_interno", nullable = false, length = 100)
    private String codigoInterno;

    /** Número de serie del bien. Aplica para equipos electrónicos y similares. */
    @Column(name = "numero_serie", length = 150)
    private String numeroSerie;

    /** Número de motor. Aplica solo para vehículos. */
    @Column(name = "numero_motor", length = 150)
    private String numeroMotor;

    /** Marca del bien. Se captura cuando no hay número de serie. */
    @Column(name = "marca", length = 150)
    private String marca;

    /** Modelo del bien. Se captura cuando no hay número de serie. */
    @Column(name = "modelo", length = 150)
    private String modelo;

    @Column(name = "descripcion_complementaria", length = 500)
    private String descripcionComplementaria;

    /**
     * Estatus actual del bien dentro del almacén.
     * PENDIENTE_PROCESAR: llegó pero aún no se capturan sus datos y fotos.
     * PROCESADO:          datos y fotos capturados, listo para asignar.
     * ASIGNADO_ENTREGA:   asignado a un beneficiario, pendiente de entregar.
     * ENTREGADO:          entregado al beneficiario.
     */
    @Column(name = "estatus", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    //private EstatusBien estatus = EstatusBien.PENDIENTE_PROCESAR;
    private EstatusBien estatus;

    @Column(name = "fecha_recepcion", nullable = false)
    private LocalDateTime fechaRecepcion;

    @Column(name = "fecha_procesamiento")
    private LocalDateTime fechaProcesamiento;

    @Column(name = "fecha_entrega")
    private LocalDateTime fechaEntrega;

    @OneToMany(mappedBy = "almacenBien", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EvidenciaBienEntity> evidencias = new ArrayList<>();

    public enum EstatusBien {
        PENDIENTE_PROCESAR,
        PROCESADO,
        ASIGNADO_ENTREGA,
        ENTREGADO
    }
}