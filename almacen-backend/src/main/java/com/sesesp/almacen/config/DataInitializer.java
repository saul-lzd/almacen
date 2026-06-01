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
        if (!usuarioRepository.findByUsername("admin").isPresent()) {
            UsuarioEntity admin = buildUsuarioAdministrador();
            usuarioRepository.save(admin);
            System.out.println(">>> Usuario admin creado. Contraseña inicial: Sesesp2026! — cámbiala.");
        }

        if (!usuarioRepository.findByUsername("almacen").isPresent()) {
            UsuarioEntity almacen = buildUsuarioAlmacen();
            usuarioRepository.save(almacen);
            System.out.println(">>> Usuario almacen creado. Contraseña inicial: Sesesp2026! — cámbiala.");
        }
    }

    private UsuarioEntity buildUsuarioAdministrador() {
        UsuarioEntity user = new UsuarioEntity();
        user.setUsername("admin");
        user.setPasswordHash(passwordEncoder.encode("Sesesp2026!"));
        user.setNombre("Administrador");
        user.setCorreo("admin@sesesp.mx");
        user.setRol(UsuarioEntity.RolUsuario.ADMINISTRADOR);
        user.setUsuarioSistema(false);
        // Campos de auditoría manuales (bootstrap — no hay sesión activa)
        user.setUsuarioCreacion(1);
        user.setFechaCreacion(LocalDateTime.now());
        user.setActivo(true);

        return user;
    }

    private UsuarioEntity buildUsuarioAlmacen() {
        UsuarioEntity user = new UsuarioEntity();
        user.setUsername("almacen");
        user.setPasswordHash(passwordEncoder.encode("Sesesp2026!"));
        user.setNombre("Almacen");
        user.setCorreo("almacen@sesesp.mx");
        user.setRol(UsuarioEntity.RolUsuario.ALMACEN);
        user.setUsuarioSistema(false);
        // Campos de auditoría manuales (bootstrap — no hay sesión activa)
        user.setUsuarioCreacion(1);
        user.setFechaCreacion(LocalDateTime.now());
        user.setActivo(true);

        return user;
    }
}
