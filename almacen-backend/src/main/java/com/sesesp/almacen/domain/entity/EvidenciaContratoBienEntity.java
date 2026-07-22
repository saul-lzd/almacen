package com.sesesp.almacen.domain.entity;

import com.sesesp.almacen.common.entity.AuditoriaEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Evidencia fotográfica "de catálogo" de un tipo de bien (ContratoBien) —
 * mínimo 5 fotos mostrando cómo es el bien adquirido (frente, lado, detalle,
 * etc.), capturadas UNA SOLA VEZ por grupo sin importar cuántas unidades
 * físicas se reciban (ej. 950 pares de botas comparten las mismas 5 fotos).
 *
 * No aplica para bienes con unidadMedida = "Vehículo" — ahí la evidencia es
 * por unidad individual (ver EvidenciaBienEntity), porque cada vehículo es
 * distinguible del resto.
 *
 * tabla evidencia_contrato_bien
 */
@Entity
@Table(name = "evidencia_contrato_bien")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvidenciaContratoBienEntity extends AuditoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evidencia_contrato_bien")
    private Integer idEvidenciaContratoBien;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_contrato_bien", nullable = false)
    private ContratoBienEntity contratoBien;

    @Column(name = "url", length = 500)
    private String url;

    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo;

    @Column(name = "fecha_captura", nullable = false)
    private LocalDateTime fechaCaptura;

    @Column(name = "usuario_captura")
    private Integer usuarioCaptura;
}
