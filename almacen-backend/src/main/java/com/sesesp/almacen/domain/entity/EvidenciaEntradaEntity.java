package com.sesesp.almacen.domain.entity;

import com.sesesp.almacen.common.entity.AuditoriaEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Evidencia fotográfica del evento de recepción.
 * Mínimo 5, máximo 10 fotos por recepción.
 * La URL se llena cuando la foto se sube a S3 (v2+).
 *
 * tabla evidencia_entrada
 */
@Entity
@Table(name = "evidencia_entrada")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvidenciaEntradaEntity extends AuditoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evidencia_entrada")
    private Integer idEvidenciaEntrada;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_recepcion_almacen", nullable = false)
    private RecepcionAlmacenEntity recepcionAlmacen;

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
