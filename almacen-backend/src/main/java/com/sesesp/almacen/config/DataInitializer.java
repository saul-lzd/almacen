package com.sesesp.almacen.config;

import com.sesesp.almacen.domain.entity.UsuarioEntity;
import com.sesesp.almacen.domain.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Crea el usuario admin inicial si la tabla usuario está vacía.
 * Cambiar la contraseña desde la app o directamente en BD después del primer arranque.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.findByUsername("admin").isPresent()) {
            return;
        }

        UsuarioEntity admin = new UsuarioEntity();
        admin.setUsername("admin");
        admin.setPasswordHash(passwordEncoder.encode("Sesesp2026!"));
        admin.setNombre("Administrador");
        admin.setCorreo("admin@sesesp.mx");
        admin.setRol(UsuarioEntity.RolUsuario.ADMINISTRADOR);
        admin.setUsuarioSistema(false);
        // Campos de auditoría manuales (bootstrap — no hay sesión activa)
        admin.setUsuarioCreacion(1);
        admin.setFechaCreacion(LocalDateTime.now());
        admin.setActivo(true);

        usuarioRepository.save(admin);
        System.out.println(">>> Usuario admin creado. Contraseña inicial: Sesesp2026! — cámbiala.");
    }
}
