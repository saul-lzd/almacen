package com.sesesp.almacen.domain.entity;

import com.sesesp.almacen.common.entity.AuditoriaEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Formula;

import java.math.BigDecimal;

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

    /** Unidades individuales que ya fueron procesadas (PROCESADO, LISTO_PARA_ENTREGAR o ENTREGADO). */
    @Formula("(SELECT COUNT(*) FROM almacen_bien ab WHERE ab.id_contrato_bien = id_contrato_bien" +
             " AND ab.estatus IN ('PROCESADO', 'LISTO_PARA_ENTREGAR', 'ENTREGADO'))")
    private Long cantidadProcesadaTotal;

    /** Unidades individuales ya entregadas al beneficiario. */
    @Formula("(SELECT COUNT(*) FROM almacen_bien ab WHERE ab.id_contrato_bien = id_contrato_bien" +
             " AND ab.estatus = 'ENTREGADO')")
    private Long cantidadEntregadaTotal;
}
