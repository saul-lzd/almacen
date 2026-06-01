-- ================================================================
-- MIGRACIÓN: Agregar autenticación a la tabla usuario
-- Ejecutar ANTES de arrancar el backend con las nuevas clases.
-- ================================================================

ALTER TABLE usuario
    ADD COLUMN username      VARCHAR(80)  NOT NULL UNIQUE AFTER id_usuario,
    ADD COLUMN password_hash VARCHAR(255) NOT NULL        AFTER username;

-- Al correr el backend por primera vez, DataInitializer crea
-- el usuario admin con:
--   username: admin
--   password: Sesesp2026!   ← cambiar después del primer acceso

-- Para crear usuarios adicionales desde MySQL con contraseña temporal:
-- (Generar el hash con BCrypt cost-10 y actualizar el campo password_hash)
-- UPDATE usuario SET password_hash = '<bcrypt-hash>' WHERE username = '<user>';
