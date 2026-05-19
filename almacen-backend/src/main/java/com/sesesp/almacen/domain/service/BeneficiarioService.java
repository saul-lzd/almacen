package com.sesesp.almacen.domain.service;

import com.sesesp.almacen.domain.entity.BeneficiarioEntity;
import com.sesesp.almacen.domain.entity.ContratoBeneficiarioEntity;
import com.sesesp.almacen.domain.entity.ContratoEntity;
import com.sesesp.almacen.domain.repository.BeneficiarioRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BeneficiarioService {

    private static final Logger logger = LoggerFactory.getLogger(BeneficiarioService.class);

    private final BeneficiarioRepository beneficiarioRepository;


    /**
     * Crea las relaciones contrato-beneficiario a partir del string de nombres.
     * Si el beneficiario ya existe en el catálogo lo reutiliza; si no, lo crea.
     * Se usa en createContrato.
     */
    public List<ContratoBeneficiarioEntity> crear(ContratoEntity contrato, String beneficiariosStr) {
        return parsearNombres(beneficiariosStr).stream()
                .map(nombre -> {
                    BeneficiarioEntity beneficiario = buscarOCrear(nombre);
                    return ContratoBeneficiarioEntity.builder()
                            .contrato(contrato)
                            .beneficiario(beneficiario)
                            .build();
                })
                .toList();
    }

    /**
     * Sincroniza los beneficiarios del contrato.
     *
     * null  → no se toca nada (usuario no modificó la sección)
     * ""    → se desactivan todos
     * names → agrega nuevos, desactiva los que ya no están, reactiva los que vuelven
     *
     * Se usa en updateContrato.
     */
    public void sincronizar(ContratoEntity contrato, String beneficiariosStr) {
        if (beneficiariosStr == null) return;

        List<String> nombresRequest = parsearNombres(beneficiariosStr);
        List<String> nombresNormalizados = nombresRequest.stream()
                .map(this::normalizar)
                .toList();

        // Desactivar o reactivar según presencia en el request
        contrato.getBeneficiarios().forEach(relacion -> {
            String nombreActual = normalizar(relacion.getBeneficiario().getNombre());
            relacion.setActivo(nombresNormalizados.contains(nombreActual));
        });

        // Nombres que ya existen en el contrato (para no duplicar)
        List<String> nombresExistentes = contrato.getBeneficiarios().stream()
                .map(r -> normalizar(r.getBeneficiario().getNombre()))
                .toList();

        // Agregar los que son nuevos
        for (String nombre : nombresRequest) {
            if (nombresExistentes.contains(normalizar(nombre))) continue;

            BeneficiarioEntity beneficiario = buscarOCrear(nombre);
            contrato.getBeneficiarios().add(
                    ContratoBeneficiarioEntity.builder()
                            .contrato(contrato)
                            .beneficiario(beneficiario)
                            .build()
            );
        }

        logger.info("Beneficiarios sincronizados para contrato ID: {}", contrato.getIdContrato());
    }

    // ─── helpers ───────────────────────────────────────────────

    private BeneficiarioEntity buscarOCrear(String nombre) {
        return beneficiarioRepository
                .findByNombreAndActivoTrue(nombre)
                .orElseGet(() -> beneficiarioRepository.save(
                        BeneficiarioEntity.builder().nombre(nombre).build()
                ));
    }

    private List<String> parsearNombres(String str) {
        if (str == null || str.isBlank()) return List.of();
        return Arrays.stream(str.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .toList();
    }

    private String normalizar(String valor) {
        return valor == null ? "" : valor.trim().toLowerCase();
    }


}
