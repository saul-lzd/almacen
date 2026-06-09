package com.sesesp.almacen.domain.entity;

import com.sesesp.almacen.common.entity.AuditoriaEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Contrato de adquisición. Punto de entrada del sistema.
 *
 * Reglas de negocio importantes:
 *   - numero_contrato y adquisicion son los únicos campos obligatorios al crear.
 *   - El resto de campos se completan en captura progresiva.
 *   - Una vez que el estatus pasa a POR_RECIBIR el contrato se bloquea para edición,
 *     excepto fecha_tentativa_llegada (prórrogas del proveedor).
 *   - SUM(contrato_bien.subtotal) debe ser igual a monto_sin_impuestos.
 *   - SUM(contrato_clave_presupuestal.monto_asignado) debe ser igual a monto_total.
 *
 * tabla contrato
 */
@Entity
@Table(name = "contrato")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContratoEntity extends AuditoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contrato")
    private Integer idContrato;

    /** Número oficial del contrato. Obligatorio desde la creación. */
    @Column(name = "numero_contrato", nullable = false, length = 100)
    private String numeroContrato;

    /** Descripción breve de lo que se está comprando. Obligatorio desde la creación. */
    @Column(name = "adquisicion", nullable = false, length = 300)
    private String adquisicion;

    /**
     * Fecha estimada en que el proveedor hará la primera entrega.
     * Es el único campo editable después de enviar el contrato al almacén
     * (para manejar prórrogas).
     */
    @Column(name = "fecha_tentativa_llegada")
    private LocalDateTime fechaTentativaLlegada;

    // =========================================================
    // MONTOS — parte de la cadena de validación financiera:
    //   monto_sin_impuestos + impuestos = monto_total
    //   SUM(clave_presupuestal.monto_asignado) = monto_total
    //   SUM(contrato_bien.subtotal) = monto_sin_impuestos
    // =========================================================

    @Column(name = "monto_sin_impuestos", precision = 15, scale = 2)
    private BigDecimal montoSinImpuestos;

    @Column(name = "impuestos", precision = 15, scale = 2)
    private BigDecimal impuestos;

    @Column(name = "monto_total", precision = 15, scale = 2)
    private BigDecimal montoTotal;

    // =========================================================
    // ESTATUS — fase administrativa del contrato
    // Solo dos valores: CAPTURA (editable) y POR_RECIBIR (bloqueado, activo en almacén)
    // =========================================================

    @Column(name = "estatus", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private EstatusContrato estatus;

    // =========================================================
    // CHECKPOINTS — hitos irreversibles del ciclo de vida
    // Se marcan automáticamente conforme avanza el proceso.
    // Nunca retroceden.
    // =========================================================

    /** Se marca cuando llega la primera entrega del proveedor. */
    @Column(name = "primera_recepcion_registrada", nullable = false)
    @Builder.Default
    private boolean primeraRecepcionRegistrada = false;

    /** Se marca cuando el almacén registra la primera salida (entrega a beneficiario). */
    @Column(name = "primera_entrega_autorizada", nullable = false)
    @Builder.Default
    private boolean primeraEntregaAutorizada = false;

    /** Se marca cuando totalRecibidos === totalContratados. */
    @Column(name = "todos_bienes_recibidos", nullable = false)
    @Builder.Default
    private boolean todosLosBienesRecibidos = false;

    /** Se marca cuando el 100% de los bienes están en estatus ENTREGADO. */
    @Column(name = "contrato_cerrado", nullable = false)
    @Builder.Default
    private boolean contratoCerrado = false;

    // =========================================================
    // RELACIONES CON PARTICIPANTES DEL CONTRATO
    // =========================================================

    /** Proveedor que entrega los bienes */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proveedor")
    private ProveedorEntity proveedor;

    /** Titular de la dependencia — firma como comprador */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_comprador")
    private FuncionarioEntity comprador;

    /** Director operativo — administra el contrato */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_administrador_contrato")
    private FuncionarioEntity administradorContrato;

    // =========================================================
    // RELACIONES HIJAS
    // Cascade ALL + orphanRemoval: cuando se elimina el contrato
    // o se reemplaza la lista, JPA limpia los registros huérfanos.
    // =========================================================

    @OneToMany(mappedBy = "contrato", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ContratoClavePresupuestalEntity> clavesPresupuestales = new ArrayList<>();

    @OneToMany(mappedBy = "contrato", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ContratoBeneficiarioEntity> beneficiarios = new ArrayList<>();

    @OneToMany(mappedBy = "contrato", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ContratoBienEntity> bienes = new ArrayList<>();

    // =========================================================
    // Enum del ciclo de vida — vive aquí porque es propio del contrato
    // =========================================================

    public enum EstatusContrato {
        CAPTURA,      // admin capturando datos — contrato editable
        POR_RECIBIR   // enviado al almacén — bloqueado para edición, activo en almacén
    }
}
