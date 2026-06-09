-- ================================================================
-- RESET DE DATOS DE DESARROLLO
-- Borra todos los datos transaccionales y recarga solo los catálogos
-- esenciales para poder correr pruebas desde cero.
--
-- Catálogos que se recargan:
--   - unidad_medida   → dropdown en captura de contrato
--   - funcionario     → comprador / administrador de contrato
--   - usuario         → vacío; el backend crea admin+almacen al arrancar
--
-- USO:
--   mysql -u root -p almacen_v5 < reset_dev.sql
--   Reiniciar el backend (DataInitializer recrea admin y almacen).
-- ================================================================

SET FOREIGN_KEY_CHECKS = 0;

-- ────────────────────────────────────────────────────────────────
-- PASO 1: Vaciar datos transaccionales (orden no importa con FK off)
-- ────────────────────────────────────────────────────────────────

TRUNCATE TABLE evidencia_bien;
TRUNCATE TABLE evidencia_entrada;
TRUNCATE TABLE evidencia_salida;
TRUNCATE TABLE salida_almacen_bien;
TRUNCATE TABLE salida_almacen;
TRUNCATE TABLE almacen_bien;
TRUNCATE TABLE recepcion_almacen_bien;
TRUNCATE TABLE recepcion_almacen;
TRUNCATE TABLE contrato_bien;
TRUNCATE TABLE contrato_beneficiario;
TRUNCATE TABLE contrato_clave_presupuestal;
TRUNCATE TABLE clave_presupuestal;
TRUNCATE TABLE beneficiario;
TRUNCATE TABLE contrato;
TRUNCATE TABLE proveedor;
TRUNCATE TABLE funcionario;
TRUNCATE TABLE usuario;

-- Tablas legacy (pueden o no existir; se ignoran si no existen)
-- TRUNCATE TABLE estatus_contrato;
-- TRUNCATE TABLE contrato_producto;

-- Catálogos también se limpian antes de reinsertar
TRUNCATE TABLE unidad_medida;
TRUNCATE TABLE funcionario;

-- (usuario_creacion = 1 se resuelve cuando el backend crea el admin al arrancar)

-- ────────────────────────────────────────────────────────────────
-- PASO 2: Catálogo — unidad_medida
-- Las IDs se resetean con TRUNCATE; se reinserta el catálogo completo.
-- ────────────────────────────────────────────────────────────────

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

-- ────────────────────────────────────────────────────────────────
-- PASO 3: Catálogo — funcionario
-- Dos titulares y dos administradores de ejemplo para pruebas.
-- Ajustar nombres y dependencias según el entorno real.
-- ────────────────────────────────────────────────────────────────

INSERT INTO funcionario (nombre, dependencia, caracter, tipo_funcionario, es_activo_en_rol,
                         activo, fecha_creacion, usuario_creacion)
VALUES
    ('Juan Carlos López Hernández',
     'SESESP — Secretaría de Seguridad Pública',
     'Titular de la Dependencia',
     'TITULAR', 1, 1, NOW(), 1),

    ('María Elena Ramírez Soto',
     'SESESP — Dirección de Administración',
     'Titular de la Dependencia',
     'TITULAR', 1, 1, NOW(), 1),

    ('Roberto Sánchez Ávila',
     'SESESP — Dirección Operativa',
     'Director Operativo',
     'ADMINISTRADOR', 1, 1, NOW(), 1),

    ('Ana Patricia Vega Torres',
     'SESESP — Dirección de Recursos Materiales',
     'Coordinador de Adquisiciones',
     'ADMINISTRADOR', 1, 1, NOW(), 1);

-- ────────────────────────────────────────────────────────────────
-- PASO 4: usuario — vacío
-- El DataInitializer del backend crea admin y almacen al arrancar
-- si la tabla está vacía (contraseña inicial: Sesesp2026!)
-- ────────────────────────────────────────────────────────────────

-- (tabla ya truncada en paso 1 — nada más que hacer aquí)

SET FOREIGN_KEY_CHECKS = 1;

-- ────────────────────────────────────────────────────────────────
-- Verificación rápida
-- ────────────────────────────────────────────────────────────────
SELECT 'unidad_medida' AS tabla, COUNT(*) AS registros FROM unidad_medida
UNION ALL
SELECT 'funcionario',  COUNT(*) FROM funcionario
UNION ALL
SELECT 'usuario',      COUNT(*) FROM usuario
UNION ALL
SELECT 'contrato',     COUNT(*) FROM contrato
UNION ALL
SELECT 'proveedor',    COUNT(*) FROM proveedor;
