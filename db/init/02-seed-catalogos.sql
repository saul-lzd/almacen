-- ================================================================
-- SEED — catálogos reales para un ambiente nuevo (EC2/Docker)
-- Se ejecuta una sola vez, junto con 01-schema.sql, cuando el volumen
-- de MySQL está vacío (docker-entrypoint-initdb.d).
--
-- Solo incluye el catálogo `unidad_medida` (fijo, no depende del
-- entorno). NO se seedean funcionarios: los de reset_dev.sql son
-- nombres de ejemplo para pruebas, no funcionarios reales de SESESP,
-- y hoy no existe endpoint para crearlos vía API (solo GET) — hay que
-- insertar los reales a mano vía SQL antes de operar en producción.
--
-- FOREIGN_KEY_CHECKS se desactiva porque este script corre ANTES de
-- que el backend arranque y cree el usuario SISTEMA (id_usuario=1,
-- ver DataInitializer.crearUsuarioSistema) al que usuario_creacion
-- hace referencia — mismo truco que usa ese mismo componente.
-- ================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

INSERT INTO unidad_medida (nombre, activo, fecha_creacion, usuario_creacion)
VALUES
    ('Pieza',       1, NOW(), 1),
    ('Vehículo',    1, NOW(), 1),
    ('Unidad',      1, NOW(), 1),
    ('Kit',         1, NOW(), 1),
    ('Juego',       1, NOW(), 1),
    ('Litro',       1, NOW(), 1),
    ('Kilogramo',   1, NOW(), 1),
    ('Gramo',       1, NOW(), 1),
    ('Metro',       1, NOW(), 1),
    ('Metro²',      1, NOW(), 1),
    ('Metro³',      1, NOW(), 1),
    ('Caja',        1, NOW(), 1),
    ('Paquete',     1, NOW(), 1),
    ('Rollo',       1, NOW(), 1),
    ('Par',         1, NOW(), 1),
    ('Servicio',    1, NOW(), 1);

-- Catálogo de nombres de componente para bienes "conjunto" (ej. workstation
-- = Monitor + CPU + Teclado). Crece solo: cuando el almacenista escribe un
-- nombre nuevo al capturar evidencia, se agrega aquí para autocompletado futuro.
INSERT INTO catalogo_componente (nombre, activo, fecha_creacion, usuario_creacion)
VALUES
    ('CPU',      1, NOW(), 1),
    ('Teclado',  1, NOW(), 1),
    ('Ratón',    1, NOW(), 1);

SET FOREIGN_KEY_CHECKS = 1;
