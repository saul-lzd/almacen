package com.sesesp.almacen.domain.entity;

import com.sesesp.almacen.common.entity.AuditoriaEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Formula;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Bien (producto) que se va a adquirir en el contrato.
 * Cada registro representa un tipo de bien con su cantidad y precio.
 * Las unidades físicas individuales recibidas viven en AlmacenBienEntity.
 *
 * Validación financiera:
 *   SUM(subtotal) de todos los bienes del contrato = contrato.monto_sin_impuestos
 *
 * tabla contrato_bien
 *   lote SMALLINT, partida SMALLINT, descripcion_tecnica TEXT
 */
@Entity
@Table(name = "contrato_bien")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContratoBienEntity extends AuditoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contrato_bien")
    private Integer idContratoBien;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_contrato", nullable = false)
    private ContratoEntity contrato;

    /** Número de lote (agrupación de bienes dentro del contrato) */
    @Column(name = "lote")
    private Short lote;

    /** Número de partida dentro del lote */
    @Column(name = "partida")
    private Short partida;

    /**
     * Descripción técnica con características mínimas del bien.
     * TEXT para soportar contenido con formato del editor inline.
     */
    @Column(name = "descripcion_tecnica", nullable = false, columnDefinition = "TEXT")
    private String descripcionTecnica;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_unidad_medida", nullable = false)
    private UnidadMedidaEntity unidadMedida;

    @Column(name = "cantidad", nullable = false, precision = 15, scale = 2)
    private Integer cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 15, scale = 2)
    private BigDecimal precioUnitario;

    /** cantidad × precio_unitario. Se calcula en el servicio antes de guardar. */
    @Column(name = "subtotal", nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal;

    /** Unidades individuales ya recibidas en almacén (cualquier estatus activo). */
    @Formula("(SELECT COUNT(*) FROM almacen_bien ab WHERE ab.id_contrato_bien = id_contrato_bien AND ab.activo = true)")
    private Long cantidadRecibidaTotal;

    /** Unidades individuales que ya fueron procesadas (PROCESADO, LISTO_PARA_ENTREGAR o ENTREGADO). */
    @Formula("(SELECT COUNT(*) FROM almacen_bien ab WHERE ab.id_contrato_bien = id_contrato_bien" +
             " AND ab.estatus IN ('PROCESADO', 'LISTO_PARA_ENTREGAR', 'ENTREGADO'))")
    private Long cantidadProcesadaTotal;

    /** Unidades individuales ya entregadas al beneficiario. */
    @Formula("(SELECT COUNT(*) FROM almacen_bien ab WHERE ab.id_contrato_bien = id_contrato_bien" +
             " AND ab.estatus = 'ENTREGADO')")
    private Long cantidadEntregadaTotal;

    /**
     * Determina qué se captura por unidad al procesar los bienes de este grupo:
     *   NINGUNO:  no se captura número de serie/motor (ej. botas, gorras).
     *   SIMPLE:   un único número de serie/motor por unidad (ej. laptops).
     *   CONJUNTO: la unidad es un paquete de varias piezas, cada una con su
     *             propio número de serie (ej. workstation = Monitor+CPU+Teclado)
     *             — los nombres de los componentes esperados viven en
     *             ContratoBienComponenteEntity.
     * No aplica a bienes con unidadMedida = "Vehículo": ahí el número de motor
     * siempre se captura vía AlmacenBienEntity.numeroMotor, independientemente
     * de este campo.
     */
    @Column(name = "tipo_captura_serie", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TipoCapturaSerie tipoCapturaSerie = TipoCapturaSerie.NINGUNO;

    /** Plantilla de componentes esperados cuando tipoCapturaSerie = CONJUNTO. */
    @OneToMany(mappedBy = "contratoBien", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ContratoBienComponenteEntity> componentesEsperados = new ArrayList<>();

    /** Fotos "de catálogo" del tipo de bien (mínimo 5, una sola vez por grupo). */
    @OneToMany(mappedBy = "contratoBien", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EvidenciaContratoBienEntity> evidencias = new ArrayList<>();

    public enum TipoCapturaSerie {
        NINGUNO,
        SIMPLE,
        CONJUNTO
    }
}
