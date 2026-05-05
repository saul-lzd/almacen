package com.sesesp.almacen.common.types;

public enum DefaultUser {

    SISTEMA (1),
    ALMACEN(3);

    public final int ID;

    DefaultUser(int ID) {
        this.ID = ID;
    }
}
