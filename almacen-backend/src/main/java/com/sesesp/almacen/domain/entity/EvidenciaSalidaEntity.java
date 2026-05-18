package com.sesesp.almacen.domain.entity;

import com.sesesp.almacen.common.entity.AuditoriaEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Evidencia fotográfica del evento de entrega al beneficiario.
 * Mínimo 5, máximo 10 fotos por entrega.
 *
 * Estas fotos se usan en el documento de auditoría de entregas
 * (el usuario selecciona 5 de todas las fotos de las entregas del contrato).
 *
 * DDL v5: tabla evidencia_salida
 */
@Entity
@Table(name = "evidencia_salida")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvidenciaSalidaEntity extends AuditoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evidencia_salida")
    private Integer idEvidenciaSalida;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_salida_almacen", nullable = false)
    private SalidaAlmacenEntity salidaAlmacen;

    /** URL en S3. Nullable hasta que la foto se suba (v2+) */
    @Column(name = "url", length = 500)
    private String url;

    /** Nombre estable del archivo, independiente de la URL */
    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo;

    @Column(name = "fecha_captura", nullable = false)
    private LocalDateTime fechaCaptura;

    @Column(name = "usuario_captura")
    private Integer usuarioCaptura;
}