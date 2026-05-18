package com.sesesp.almacen.domain.entity;

import com.sesesp.almacen.common.entity.AuditoriaEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Evidencia fotográfica de un bien individual en almacén.
 * Mínimo 5, máximo 10 fotos por bien.
 *
 * Las fotos deben mostrar:
 *   - El bien o empaque completo
 *   - La etiqueta con número de serie, marca o modelo (según aplique)
 *   - Para vehículos: también el número de motor
 *
 * Estas fotos son el respaldo visual de los datos capturados en AlmacenBienEntity.
 * Se usan en el documento de auditoría (5 fotos por bien).
 *
 * tabla evidencia_bien
 */
@Entity
@Table(name = "evidencia_bien")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvidenciaBienEntity extends AuditoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evidencia_bien")
    private Integer idEvidenciaBien;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_almacen_bien", nullable = false)
    private AlmacenBienEntity almacenBien;

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