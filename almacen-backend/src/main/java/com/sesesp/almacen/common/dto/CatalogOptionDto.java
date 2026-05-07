package com.sesesp.almacen.common.dto;

import lombok.Data;

import java.util.Map;

@Data
public class CatalogOptionDto {

    private String value;
    private String label;
    private Map<String, Object> metadata;

    // getters and setters auto-generated
}
