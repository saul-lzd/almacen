package com.sesesp.almacen.domain.entity;

import com.sesesp.almacen.common.entity.AuditoriaEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Número de serie + foto de etiqueta de un componente individual dentro de
 * una unidad "conjunto" (ej. la unidad física es 1 workstation, y tiene un
 * ComponenteBienEntity por cada Monitor/CPU/Teclado, cada uno con su propio
 * número de serie y su propia foto — relación 1:1 componente-foto, por eso
 * la evidencia vive embebida aquí en vez de en una tabla de evidencias aparte).
 *
 * Los nombres de componente esperados para el grupo viven en
 * ContratoBienComponenteEntity — este registro es el valor ya capturado
 * para una unidad específica.
 *
 * tabla componente_bien
 */
@Entity
@Table(name = "componente_bien")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComponenteBienEntity extends AuditoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_componente_bien")
    private Integer idComponenteBien;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_almacen_bien", nullable = false)
    private AlmacenBienEntity almacenBien;

    @Column(name = "nombre_componente", nullable = false, length = 150)
    private String nombreComponente;

    @Column(name = "numero_serie", nullable = false, length = 150)
    private String numeroSerie;

    /** URL en S3 de la foto de la etiqueta de este componente. */
    @Column(name = "url", length = 500)
    private String url;

    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo;

    @Column(name = "fecha_captura", nullable = false)
    private LocalDateTime fechaCaptura;

    @Column(name = "usuario_captura")
    private Integer usuarioCaptura;
}
