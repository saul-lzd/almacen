-- ================================================================
-- MIGRACIÓN: Rediseño de estatus de contrato y recepción
-- Compatible con MySQL 5.7+ y MySQL 8.x
-- Idempotente: se puede correr más de una vez sin errores.
--
-- Cambios:
--   1. contrato: agregar 4 checkpoints booleanos (irreversibles)
--   2. contrato: migrar estatus intermedios a solo CAPTURA / POR_RECIBIR
--   3. recepcion_almacen: agregar columna estatus
-- ================================================================

-- ────────────────────────────────────────────────────────────────
-- 1. TABLA contrato — agregar columnas de checkpoint (si no existen)
-- ────────────────────────────────────────────────────────────────

DROP PROCEDURE IF EXISTS sp_migration_contrato;

DELIMITER $$
CREATE PROCEDURE sp_migration_contrato()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name   = 'contrato'
          AND column_name  = 'primera_recepcion_registrada'
    ) THEN
        ALTER TABLE contrato
            ADD COLUMN primera_recepcion_registrada TINYINT(1) NOT NULL DEFAULT 0,
            ADD COLUMN primera_entrega_autorizada   TINYINT(1) NOT NULL DEFAULT 0,
            ADD COLUMN todos_bienes_recibidos       TINYINT(1) NOT NULL DEFAULT 0,
            ADD COLUMN contrato_cerrado             TINYINT(1) NOT NULL DEFAULT 0;
    END IF;
END$$
DELIMITER ;

CALL sp_migration_contrato();
DROP PROCEDURE IF EXISTS sp_migration_contrato;

-- Aplicar comentarios (MODIFY siempre corre — las columnas ya existen en este punto)
ALTER TABLE contrato
    MODIFY COLUMN primera_recepcion_registrada TINYINT(1) NOT NULL DEFAULT 0
        COMMENT 'True cuando se registró al menos una recepción en almacén',
    MODIFY COLUMN primera_entrega_autorizada   TINYINT(1) NOT NULL DEFAULT 0
        COMMENT 'True cuando el admin autorizó la primera entrega a beneficiarios',
    MODIFY COLUMN todos_bienes_recibidos       TINYINT(1) NOT NULL DEFAULT 0
        COMMENT 'True cuando la suma de cantidades recibidas iguala lo contratado',
    MODIFY COLUMN contrato_cerrado             TINYINT(1) NOT NULL DEFAULT 0
        COMMENT 'True cuando todos los bienes físicos han sido entregados a beneficiarios';

-- ────────────────────────────────────────────────────────────────
-- 2. TABLA contrato — migrar estatus al nuevo catálogo
--
--   CAPTURA                                    → CAPTURA      (sin cambio)
--   POR_RECIBIR                                → POR_RECIBIR  (sin cambio)
--   EN_ALMACEN / RECEPCION_PARCIAL
--   / LISTO_PARA_ENTREGAR / ENTREGA_PARCIAL    → POR_RECIBIR
--   ENTREGADO                                  → POR_RECIBIR + contrato_cerrado = 1
-- ────────────────────────────────────────────────────────────────

UPDATE contrato
SET estatus = 'POR_RECIBIR', contrato_cerrado = 1
WHERE estatus = 'ENTREGADO';

UPDATE contrato
SET estatus = 'POR_RECIBIR'
WHERE estatus IN ('EN_ALMACEN', 'RECEPCION_PARCIAL', 'LISTO_PARA_ENTREGAR', 'ENTREGA_PARCIAL');

-- Marcar checkpoints según datos existentes
UPDATE contrato c
SET c.primera_recepcion_registrada = 1
WHERE EXISTS (
    SELECT 1 FROM recepcion_almacen r
    WHERE r.id_contrato = c.id_contrato AND r.activo = 1
);

UPDATE contrato c
SET c.primera_entrega_autorizada = 1
WHERE EXISTS (
    SELECT 1 FROM salida_almacen s
    WHERE s.id_contrato = c.id_contrato AND s.activo = 1
);

UPDATE contrato c
SET c.todos_bienes_recibidos = 1
WHERE c.estatus = 'POR_RECIBIR'
  AND EXISTS (SELECT 1 FROM contrato_bien cb2 WHERE cb2.id_contrato = c.id_contrato AND cb2.activo = 1)
  AND NOT EXISTS (
      SELECT 1
      FROM contrato_bien cb
      LEFT JOIN (
          SELECT rab.id_contrato_bien, SUM(rab.cantidad_recibida) AS total_recibido
          FROM recepcion_almacen_bien rab
          JOIN recepcion_almacen ra ON ra.id_recepcion_almacen = rab.id_recepcion_almacen AND ra.activo = 1
          GROUP BY rab.id_contrato_bien
      ) recibidos ON recibidos.id_contrato_bien = cb.id_contrato_bien
      WHERE cb.id_contrato = c.id_contrato
        AND cb.activo = 1
        AND COALESCE(recibidos.total_recibido, 0) < cb.cantidad
  );

-- ────────────────────────────────────────────────────────────────
-- 3. TABLA recepcion_almacen — agregar columna estatus (si no existe)
-- ────────────────────────────────────────────────────────────────

DROP PROCEDURE IF EXISTS sp_migration_recepcion;

DELIMITER $$
CREATE PROCEDURE sp_migration_recepcion()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name   = 'recepcion_almacen'
          AND column_name  = 'estatus'
    ) THEN
        ALTER TABLE recepcion_almacen
            ADD COLUMN estatus VARCHAR(50) NOT NULL DEFAULT 'INICIADA';
    END IF;
END$$
DELIMITER ;

CALL sp_migration_recepcion();
DROP PROCEDURE IF EXISTS sp_migration_recepcion;

-- Aplicar comentario
ALTER TABLE recepcion_almacen
    MODIFY COLUMN estatus VARCHAR(50) NOT NULL DEFAULT 'INICIADA'
        COMMENT 'Ciclo de vida: INICIADA → EN_PROCESO → PROCESADA → ENTREGADA';

-- Inferir estatus de recepciones existentes según estado de sus bienes

-- ENTREGADA: todos los bienes de la recepción están en ENTREGADO
UPDATE recepcion_almacen ra
SET ra.estatus = 'ENTREGADA'
WHERE ra.activo = 1
  AND EXISTS (
      SELECT 1 FROM almacen_bien ab
      JOIN recepcion_almacen_bien rab ON rab.id_recepcion_almacen_bien = ab.id_recepcion_almacen_bien
      WHERE rab.id_recepcion_almacen = ra.id_recepcion_almacen AND ab.activo = 1
  )
  AND NOT EXISTS (
      SELECT 1 FROM almacen_bien ab
      JOIN recepcion_almacen_bien rab ON rab.id_recepcion_almacen_bien = ab.id_recepcion_almacen_bien
      WHERE rab.id_recepcion_almacen = ra.id_recepcion_almacen AND ab.activo = 1 AND ab.estatus <> 'ENTREGADO'
  );

-- PROCESADA: todos los bienes están en PROCESADO / LISTO_PARA_ENTREGAR / ENTREGADO
UPDATE recepcion_almacen ra
SET ra.estatus = 'PROCESADA'
WHERE ra.activo = 1 AND ra.estatus = 'INICIADA'
  AND EXISTS (
      SELECT 1 FROM almacen_bien ab
      JOIN recepcion_almacen_bien rab ON rab.id_recepcion_almacen_bien = ab.id_recepcion_almacen_bien
      WHERE rab.id_recepcion_almacen = ra.id_recepcion_almacen AND ab.activo = 1
  )
  AND NOT EXISTS (
      SELECT 1 FROM almacen_bien ab
      JOIN recepcion_almacen_bien rab ON rab.id_recepcion_almacen_bien = ab.id_recepcion_almacen_bien
      WHERE rab.id_recepcion_almacen = ra.id_recepcion_almacen AND ab.activo = 1
        AND ab.estatus IN ('RECIBIDO', 'EN_PROCESO')
  );

-- EN_PROCESO: al menos un bien en EN_PROCESO, PROCESADO o LISTO_PARA_ENTREGAR
UPDATE recepcion_almacen ra
SET ra.estatus = 'EN_PROCESO'
WHERE ra.activo = 1 AND ra.estatus = 'INICIADA'
  AND EXISTS (
      SELECT 1 FROM almacen_bien ab
      JOIN recepcion_almacen_bien rab ON rab.id_recepcion_almacen_bien = ab.id_recepcion_almacen_bien
      WHERE rab.id_recepcion_almacen = ra.id_recepcion_almacen AND ab.activo = 1
        AND ab.estatus IN ('EN_PROCESO', 'PROCESADO', 'LISTO_PARA_ENTREGAR')
  );

-- ────────────────────────────────────────────────────────────────
-- Verificación
-- ────────────────────────────────────────────────────────────────
SELECT 'contrato' AS tabla, estatus AS valor, COUNT(*) AS total
FROM contrato GROUP BY estatus
UNION ALL
SELECT 'recepcion_almacen', estatus, COUNT(*)
FROM recepcion_almacen GROUP BY estatus;
