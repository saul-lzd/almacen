package com.sesesp.almacen.common;

import org.jsoup.Jsoup;

/**
 * Variables globales / configuración del sistema — valores que aplican
 * transversalmente a varios servicios, en vez de estar duplicados (y
 * potencialmente divergentes) dentro de cada uno.
 */
public class SESESP_UTILS {

    // ── Descripción técnica de bienes (Quill/HTML → texto plano truncado) ──
    public static final int DESCRIPCION_CORTA_BIEN_MAX_LENGTH = 100;

    // ── Evidencias fotográficas — recepción de bienes (RecepcionAlmacenService) ──
    public static final int MIN_EVIDENCIAS_RECEPCION = 5;
    public static final int MAX_EVIDENCIAS_RECEPCION = 10;

    // ── Evidencias fotográficas — entrega de bienes (SalidaAlmacenService) ──
    public static final int MIN_EVIDENCIAS_ENTREGA = 5;
    public static final int MAX_EVIDENCIAS_ENTREGA = 10;

    // ── Evidencias fotográficas — bienes procesados, catálogo por grupo (AlmacenBienService) ──
    public static final int MIN_EVIDENCIAS_BIEN_GRUPO = 5;
    public static final int MAX_EVIDENCIAS_BIEN_GRUPO = 10;

    // ── Evidencias fotográficas — bienes procesados, por unidad (AlmacenBienService) ──
    public static final int MIN_EVIDENCIAS_BIEN_UNIDAD = 5;
    public static final int MAX_EVIDENCIAS_BIEN_UNIDAD = 10;

    // ── Prefijos de folio / nomenclatura de archivos (naming convention del sistema) ──
    public static final String PREFIJO_FOLIO_RECEPCION           = "EA";
    public static final String PREFIJO_FOLIO_ENTREGA             = "SA";
    public static final String PREFIJO_EVIDENCIA_BIEN_PROCESADO  = "BP";
    public static final String PREFIJO_CODIGO_INTERNO_BIEN       = "AB";

    // ── Ancho de relleno con ceros (padding) de los consecutivos ──
    public static final int DIGITOS_CONSECUTIVO_FOLIO = 4; // EA-0001, SA-2026-0001
    public static final int DIGITOS_CONSECUTIVO_BIEN  = 5; // BP_00001, AB-2026-00001

    /**
     * Convierte HTML (ej. salida del editor Quill) a texto plano truncado.
     * Usa Jsoup para extraer el texto (elimina etiquetas y convierte entidades
     * como &amp;/&nbsp; en caracteres normales), luego recorta a maxLen.
     */
    public static String stripHtml(String html, int maxLen) {
        if (html == null || html.isBlank()) return "";
        String text = Jsoup.parse(html).text().trim();
        return text.length() > maxLen ? text.substring(0, maxLen) + "…" : text;
    }
}
