package com.sesesp.almacen.domain.service;

import com.sesesp.almacen.common.exception.ContratoValidacionException;
import com.sesesp.almacen.domain.entity.ContratoEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Valida que un contrato cumpla los requisitos para cambiar de estatus.
 * Centralizar las validaciones aquí permite reutilizarlas en otros
 * puntos del flujo sin duplicar lógica.
 */
@Service
public class ContratoValidacionService {

    /**
     * Valida que el contrato tenga todos los datos necesarios
     * para ser enviado al almacén.
     *
     * Si hay errores, lanza ContratoValidacionException con la lista completa
     * para que el usuario pueda corregir todo de una vez — no uno por uno.
     */
    public void validarParaEnviarAlmacen(ContratoEntity contrato) {
        List<String> errores = new ArrayList<>();

        // Datos básicos
        if (esBlanco(contrato.getNumeroContrato()))
            errores.add("Falta el número de contrato");

        if (esBlanco(contrato.getAdquisicion()))
            errores.add("Falta la descripción de la adquisición");

        if (contrato.getFechaTentativaLlegada() == null)
            errores.add("Falta la fecha tentativa de llegada");

        // Montos
        if (contrato.getMontoSinImpuestos() == null)
            errores.add("Falta el monto sin impuestos");

        if (contrato.getImpuestos() == null)
            errores.add("Faltan los impuestos");

        if (contrato.getMontoTotal() == null)
            errores.add("Falta el monto total");

        // Participantes
        if (contrato.getProveedor() == null)
            errores.add("Falta el proveedor");

        if (contrato.getComprador() == null)
            errores.add("Falta el comprador");

        if (contrato.getAdministradorContrato() == null)
            errores.add("Falta el administrador del contrato");

        // Relaciones hijas
        long beneficiariosActivos = contrato.getBeneficiarios().stream()
                .filter(b -> Boolean.TRUE.equals(b.getActivo()))
                .count();

        if (beneficiariosActivos == 0)
            errores.add("El contrato debe tener al menos un beneficiario");

        long clavesActivas = contrato.getClavesPresupuestales().stream()
                .filter(c -> Boolean.TRUE.equals(c.getActivo()))
                .count();

        if (clavesActivas == 0)
            errores.add("El contrato debe tener al menos una clave presupuestal");

        long bienesActivos = contrato.getBienes().stream()
                .filter(b -> Boolean.TRUE.equals(b.getActivo()))
                .count();

        if (bienesActivos == 0)
            errores.add("El contrato debe tener al menos un bien");

        if (!errores.isEmpty()) {
            throw new ContratoValidacionException(errores);
        }
    }

    private boolean esBlanco(String valor) {
        return valor == null || valor.isBlank();
    }

}
