package com.sesesp.almacen.config;

import com.sesesp.almacen.domain.entity.UsuarioEntity;
import com.sesesp.almacen.domain.entity.UsuarioEntity.RolUsuario;
import com.sesesp.almacen.domain.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Garantiza que los usuarios de sistema existen al arrancar.
 *
 * Orden de creación:
 *   1. SISTEMA  — usuario técnico, nunca inicia sesión, ID = 1 (referencia de auditoría por defecto)
 *   2. admin    — administrador operativo
 *   3. almacen  — operador de almacén
 *
 * El usuario SISTEMA se inserta via JDBC para resolver la FK circular
 * (usuario_creacion → id_usuario) cuando la tabla está vacía.
 * Los demás se crean via JPA porque ya existe el ID 1 como referencia válida.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final JdbcTemplate jdbc;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(JdbcTemplate jdbc,
                           UsuarioRepository usuarioRepository,
                           PasswordEncoder passwordEncoder) {
        this.jdbc = jdbc;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        crearUsuarioSistema();
        crearUsuario("admin",   "Administrador", "admin@sesesp.mx",   RolUsuario.ADMINISTRADOR);
        crearUsuario("almacen", "Almacén",       "almacen@sesesp.mx", RolUsuario.ALMACEN);
    }

    /**
     * Crea el usuario técnico SISTEMA (ID 1) si no existe.
     * Usa JDBC con FK checks desactivados para resolver la auto-referencia
     * en usuario_creacion al insertar en una tabla vacía.
     * Este usuario nunca inicia sesión — no tiene contraseña válida.
     */
    private void crearUsuarioSistema() {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM usuario WHERE username = 'sistema'", Integer.class);
        if (count != null && count > 0) return;

        jdbc.execute("SET FOREIGN_KEY_CHECKS = 0");
        try {
            jdbc.update(
                "INSERT INTO usuario " +
                "(username, password_hash, nombre, correo, rol, usuario_sistema, " +
                " activo, fecha_creacion, usuario_creacion) " +
                "VALUES ('sistema', '[SISTEMA]', 'Sistema', 'sistema@sesesp.mx', " +
                "        'SISTEMA', 1, 1, NOW(), 0)");
            // Corrección: apuntar usuario_creacion a su propio ID recién generado
            jdbc.update(
                "UPDATE usuario SET usuario_creacion = id_usuario WHERE username = 'sistema'");
            logger.info("Usuario SISTEMA creado (ID de referencia para auditoría).");
        } finally {
            jdbc.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }

    /**
     * Crea un usuario operativo via JPA. Requiere que el usuario SISTEMA (ID 1) ya exista
     * para satisfacer la FK de usuario_creacion en @PrePersist.
     * Contraseña inicial: Sesesp2026!
     */
    private void crearUsuario(String username, String nombre, String correo, RolUsuario rol) {
        if (usuarioRepository.findByUsername(username).isPresent()) return;

        UsuarioEntity user = new UsuarioEntity();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode("Sesesp2026!"));
        user.setNombre(nombre);
        user.setCorreo(correo);
        user.setRol(rol);
        user.setUsuarioSistema(false);
        usuarioRepository.save(user);

        logger.info("Usuario '{}' creado. Contraseña inicial: Sesesp2026! — cámbiala.", username);
    }
}
