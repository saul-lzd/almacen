package com.sesesp.almacen.domain.entity;

import com.sesesp.almacen.common.entity.AuditoriaEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "contrato")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContratoEntity extends AuditoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contrato")
    private Integer idContrato;

    @Column(name = "identificador_contrato", length = 100)
    private String identificadorContrato;

    @Column(name = "adquisicion", length = 500)
    private String adquisicion;

    @Column(name = "folio_origen", length = 100)
    private String folioOrigen;

    @Column(name = "fecha_origen")
    private LocalDateTime fechaOrigen;

    @Column(name = "fecha_tentativa_llegada")
    private LocalDateTime fechaTentativaLlegada;

    // =========================================================
    // RELACIONES
    // =========================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estatus_contrato")
    private EstatusContratoEntity estatusContrato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proveedor")
    private ProveedorEntity proveedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_comprador")
    private ServidorPublicoEntity comprador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_administrador_contrato")
    private ServidorPublicoEntity administradorContrato;

    // =========================================================
    // DETALLES DE PAGO
    // =========================================================

    @Column(name = "monto_sin_impuestos", precision = 15)
    private Double montoSinImpuestos;

    @Column(name = "impuestos", precision = 15)
    private Double impuestos;

    @Column(name = "monto_total", precision = 15)
    private Double montoTotal;

    @Column(name = "tiene_anticipo")
    private Boolean tieneAnticipo;

    @Column(name = "porcentaje_anticipo", precision = 5)
    private Double porcentajeAnticipo;

    @Column(name = "monto_anticipo", precision = 15)
    private Double montoAnticipo;

    @Column(name = "numero_exhibiciones")
    private Integer numeroExhibiciones;

    @Column(name = "tiene_finiquito")
    private Boolean tieneFiniquito;

    @Column(name = "porcentaje_finiquito", precision = 5)
    private Double porcentajeFiniquito;

    @Column(name = "monto_finiquito", precision = 15)
    private Double montoFiniquito;

    // =========================================================
    // RELACIONES HIJAS
    // =========================================================

    @OneToMany(
            mappedBy = "contrato",
            cascade = CascadeType.ALL,
            orphanRemoval = true )
    private List<ContratoClavePresupuestalEntity> clavesPresupuestales;

    @OneToMany(
            mappedBy = "contrato",
            cascade = CascadeType.ALL,
            orphanRemoval = true )
    private List<ContratoBeneficiarioEntity> beneficiarios;

    @OneToMany(
            mappedBy = "contrato",
            cascade = CascadeType.ALL,
            orphanRemoval = true )
    private List<ProductoEntity> productos;

}