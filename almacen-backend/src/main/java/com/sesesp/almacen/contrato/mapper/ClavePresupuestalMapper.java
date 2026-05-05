package com.sesesp.almacen.contrato.mapper;

import com.sesesp.almacen.common.types.DefaultUser;
import com.sesesp.almacen.contrato.dto.ClavePresupuestalRequest;
import com.sesesp.almacen.contrato.dto.ClavePresupuestalResponse;
import com.sesesp.almacen.contrato.entity.ClavePresupuestalEntity;

import java.time.LocalDateTime;

public class ClavePresupuestalMapper {

//    public static ClavePresupuestalEntity toEntity(ClavePresupuestalRequest request) {
//
//        ClavePresupuestalEntity entity = new ClavePresupuestalEntity();
//
//        entity.setClavePresupuestal(request.getClavePresupuestal());
//        entity.setPartidaEspecifica(request.getPartidaEspecifica());
//        entity.setFechaCreacion(LocalDateTime.now());
//
//        // campos de auditoria
//        entity.setUsuarioCreacion(DefaultUser.SISTEMA.ID);
//        entity.setFechaModificacion(LocalDateTime.now());
//        entity.setUsuarioModificacion(DefaultUser.SISTEMA.ID);
//        entity.setActivo(Boolean.TRUE);
//        return entity;
//    }

    public static ClavePresupuestalResponse toResponse(ClavePresupuestalEntity entity) {
        ClavePresupuestalResponse response = new ClavePresupuestalResponse();
        response.setClavePresupuestal(entity.getClavePresupuestal());
        response.setPartidaEspecifica(entity.getPartidaEspecifica());
        return response;
    }
}
