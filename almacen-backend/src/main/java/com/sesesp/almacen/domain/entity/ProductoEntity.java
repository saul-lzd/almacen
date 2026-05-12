package com.sesesp.almacen.domain.entity;

import com.sesesp.almacen.common.entity.AuditoriaEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "contrato_producto")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductoEntity extends AuditoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contrato_producto")
    private Integer idContratoProducto;

    /**
     * Contrato al que pertenece este producto
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_contrato", nullable = false)
    private ContratoEntity contrato;

    @Column(name = "lote")
    private Integer lote;

    @Column(name = "partida")
    private Integer partida;

    @Column(name = "descripcion_tecnica")
    private String descripcionTecnica;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_unidad_medida")
    private UnidadMedidaEntity unidadMedida;

    @Column(name = "cantidad")
    private Integer cantidad;

    @Column(name = "precio_unitario")
    private Double precioUnitario;

    @Column(name = "subtotal")
    private Double subtotal;
}
