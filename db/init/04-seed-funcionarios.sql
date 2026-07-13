-- ================================================================
-- SEED — funcionarios reales de SESESP (comprador / administrador de contrato)
-- Se ejecuta una sola vez, junto con los demás scripts de db/init,
-- cuando el volumen de MySQL está vacío (docker-entrypoint-initdb.d).
--
-- A diferencia de los funcionarios de ejemplo que traía reset_dev.sql
-- (nombres ficticios para pruebas), estos son los funcionarios reales
-- proporcionados por el usuario — resuelve el pendiente anotado al
-- dockerizar el proyecto (ver memoria del proyecto).
-- ================================================================

SET FOREIGN_KEY_CHECKS = 0;

INSERT INTO funcionario (nombre, dependencia, caracter, tipo_funcionario, es_activo_en_rol, activo, fecha_creacion, usuario_creacion)
VALUES
    ('Lic. Natalia Karina Barón Ortíz',
     'SESESP',
     'Secretaria del Secretariado Ejecutivo del Sistema Estatal de Seguridad Pública',
     'TITULAR', 1, 1, NOW(), 1),

    ('Dra. Argelia Delgado Caballero',
     'SESESP',
     'Jefa de la Unidad Administrativa',
     'ADMINISTRADOR', 1, 1, NOW(), 1);

SET FOREIGN_KEY_CHECKS = 1;
