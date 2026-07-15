package com.sesesp.almacen.domain.service;

import com.sesesp.almacen.common.exception.ContratoValidacionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.List;

/**
 * Sube evidencias fotográficas a S3.
 * El bucket es público de solo lectura para las evidencias (URLs directas, sin firmar).
 */
@Service
public class S3StorageService {

    private static final Logger logger = LoggerFactory.getLogger(S3StorageService.class);

    private final S3Client s3Client;
    private final String bucket;
    private final String region;
    private final String prefixEvidencias;

    public S3StorageService(S3Client s3Client,
                             @Value("${aws.s3.bucket-evidencias}") String bucket,
                             @Value("${aws.region}") String region,
                             @Value("${aws.s3.prefix-evidencias}") String prefixEvidencias) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.region = region;
        this.prefixEvidencias = prefixEvidencias;
    }

    /**
     * Prefijo raíz de las evidencias en S3 (ej. "evidencias" en producción,
     * "evidencias-capacitacion" en el ambiente de demo) — mismo bucket, distinta
     * carpeta lógica según el ambiente, vía AWS_S3_PREFIX_EVIDENCIAS.
     */
    public String getPrefixEvidencias() {
        return prefixEvidencias;
    }

    /**
     * Sube una imagen bajo la key exacta indicada por el caller y devuelve su URL pública en S3.
     * La política de nombres (carpeta, tag de fecha/folio, etc.) es responsabilidad del caller —
     * este servicio solo sabe subir bytes a una key y devolver la URL.
     */
    public String uploadEvidencia(MultipartFile file, String key) {
        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new ContratoValidacionException(List.of(
                    "El archivo " + file.getOriginalFilename() + " no es una imagen válida."));
        }

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException e) {
            throw new RuntimeException("No se pudo leer el archivo " + file.getOriginalFilename(), e);
        }

        String url = "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
        logger.info("Evidencia subida a S3: {}", url);
        return url;
    }
}
