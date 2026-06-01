package com.sesesp.almacen.auth.dto;

public record LoginResponseDto(String token, String rol, String nombre, Integer idUsuario) {}
