package com.sesesp.almacen.common.types;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum UserType {

    /**
     * Representa una acción automatizada o cuando el contexto del usuario es nulo.
     */
    SISTEMA(1, "SISTEMA_AUTOMATICO"),

    /**
     * Representa acciones genéricas realizadas desde el módulo de almacén.
     */
    ALMACEN(3, "MODULO_ALMACEN");

    private final int id;
    private final String descripcion;

    UserType(int id, String descripcion) {
        this.id = id;
        this.descripcion = descripcion;
    }

    /**
     * Método de utilidad para auditoría.
     * Retorna el ID de SISTEMA si el ID proporcionado no coincide con ninguno conocido.
     */
    public static int getSafeId(Integer id) {
        return Arrays.stream(values())
                .filter(type -> id != null && type.id == id)
                .findFirst()
                .map(UserType::getId)
                .orElse(SISTEMA.id);
    }
}
