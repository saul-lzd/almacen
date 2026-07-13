
/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
DROP TABLE IF EXISTS `almacen_bien`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `almacen_bien` (
  `id_almacen_bien` int unsigned NOT NULL AUTO_INCREMENT,
  `id_contrato` int unsigned NOT NULL,
  `id_contrato_bien` int unsigned NOT NULL,
  `id_recepcion_almacen_bien` int unsigned DEFAULT NULL,
  `id_beneficiario` int unsigned DEFAULT NULL,
  `codigo_interno` varchar(100) NOT NULL,
  `numero_serie` varchar(150) DEFAULT NULL,
  `numero_motor` varchar(150) DEFAULT NULL,
  `marca` varchar(150) DEFAULT NULL,
  `modelo` varchar(150) DEFAULT NULL,
  `descripcion_complementaria` varchar(500) DEFAULT NULL,
  `estatus` enum('RECIBIDO','EN_PROCESO','PROCESADO','LISTO_PARA_ENTREGAR','ENTREGADO') NOT NULL,
  `fecha_recepcion` datetime NOT NULL,
  `fecha_procesamiento` datetime DEFAULT NULL,
  `fecha_entrega` datetime DEFAULT NULL,
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `usuario_creacion` int unsigned NOT NULL DEFAULT '1',
  `fecha_modificacion` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `usuario_modificacion` int unsigned DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id_almacen_bien`),
  KEY `fk_almacen_bien_contrato` (`id_contrato`),
  KEY `fk_almacen_bien_contrato_bien` (`id_contrato_bien`),
  KEY `fk_almacen_bien_recepcion_bien` (`id_recepcion_almacen_bien`),
  KEY `idx_almacen_bien_estatus` (`estatus`),
  KEY `idx_almacen_bien_beneficiario` (`id_beneficiario`),
  KEY `fk_almacen_bien_creacion` (`usuario_creacion`),
  KEY `fk_almacen_bien_modificacion` (`usuario_modificacion`),
  CONSTRAINT `fk_almacen_bien_beneficiario` FOREIGN KEY (`id_beneficiario`) REFERENCES `beneficiario` (`id_beneficiario`),
  CONSTRAINT `fk_almacen_bien_contrato` FOREIGN KEY (`id_contrato`) REFERENCES `contrato` (`id_contrato`),
  CONSTRAINT `fk_almacen_bien_contrato_bien` FOREIGN KEY (`id_contrato_bien`) REFERENCES `contrato_bien` (`id_contrato_bien`),
  CONSTRAINT `fk_almacen_bien_creacion` FOREIGN KEY (`usuario_creacion`) REFERENCES `usuario` (`id_usuario`),
  CONSTRAINT `fk_almacen_bien_modificacion` FOREIGN KEY (`usuario_modificacion`) REFERENCES `usuario` (`id_usuario`),
  CONSTRAINT `fk_almacen_bien_recepcion_bien` FOREIGN KEY (`id_recepcion_almacen_bien`) REFERENCES `recepcion_almacen_bien` (`id_recepcion_almacen_bien`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `beneficiario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `beneficiario` (
  `id_beneficiario` int unsigned NOT NULL AUTO_INCREMENT,
  `nombre` varchar(255) NOT NULL,
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `usuario_creacion` int unsigned NOT NULL DEFAULT '1',
  `fecha_modificacion` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `usuario_modificacion` int unsigned DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id_beneficiario`),
  KEY `fk_beneficiario_creacion` (`usuario_creacion`),
  KEY `fk_beneficiario_modificacion` (`usuario_modificacion`),
  CONSTRAINT `fk_beneficiario_creacion` FOREIGN KEY (`usuario_creacion`) REFERENCES `usuario` (`id_usuario`),
  CONSTRAINT `fk_beneficiario_modificacion` FOREIGN KEY (`usuario_modificacion`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `clave_presupuestal`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `clave_presupuestal` (
  `id_clave_presupuestal` int unsigned NOT NULL AUTO_INCREMENT,
  `clave` varchar(100) NOT NULL,
  `partida_especifica` varchar(255) NOT NULL,
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `usuario_creacion` int unsigned NOT NULL DEFAULT '1',
  `fecha_modificacion` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `usuario_modificacion` int unsigned DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id_clave_presupuestal`),
  UNIQUE KEY `uk_clave_presupuestal` (`clave`),
  KEY `fk_clave_presupuestal_creacion` (`usuario_creacion`),
  KEY `fk_clave_presupuestal_modificacion` (`usuario_modificacion`),
  CONSTRAINT `fk_clave_presupuestal_creacion` FOREIGN KEY (`usuario_creacion`) REFERENCES `usuario` (`id_usuario`),
  CONSTRAINT `fk_clave_presupuestal_modificacion` FOREIGN KEY (`usuario_modificacion`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `contrato`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `contrato` (
  `id_contrato` int unsigned NOT NULL AUTO_INCREMENT,
  `numero_contrato` varchar(100) NOT NULL,
  `adquisicion` varchar(300) NOT NULL,
  `id_proveedor` int unsigned DEFAULT NULL,
  `id_comprador` int unsigned DEFAULT NULL,
  `id_administrador_contrato` int unsigned DEFAULT NULL,
  `monto_sin_impuestos` decimal(15,2) DEFAULT NULL,
  `impuestos` decimal(15,2) DEFAULT NULL,
  `monto_total` decimal(15,2) DEFAULT NULL,
  `fecha_tentativa_llegada` datetime DEFAULT NULL,
  `estatus` enum('CAPTURA','POR_RECIBIR','RECEPCION_PARCIAL','EN_ALMACEN','LISTO_PARA_ENTREGAR','ENTREGA_PARCIAL','ENTREGADO','CERRADO') NOT NULL,
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `usuario_creacion` int unsigned NOT NULL DEFAULT '1',
  `fecha_modificacion` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `usuario_modificacion` int unsigned DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  `primera_recepcion_registrada` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'True cuando se registró al menos una recepción en almacén',
  `primera_entrega_autorizada` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'True cuando el admin autorizó la primera entrega a beneficiarios',
  `todos_bienes_recibidos` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'True cuando la suma de cantidades recibidas iguala lo contratado',
  `contrato_cerrado` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'True cuando todos los bienes físicos han sido entregados a beneficiarios',
  PRIMARY KEY (`id_contrato`),
  UNIQUE KEY `uk_contrato_numero` (`numero_contrato`),
  KEY `fk_contrato_proveedor` (`id_proveedor`),
  KEY `fk_contrato_comprador` (`id_comprador`),
  KEY `fk_contrato_administrador` (`id_administrador_contrato`),
  KEY `idx_contrato_estatus` (`estatus`),
  KEY `fk_contrato_creacion` (`usuario_creacion`),
  KEY `fk_contrato_modificacion` (`usuario_modificacion`),
  CONSTRAINT `fk_contrato_administrador` FOREIGN KEY (`id_administrador_contrato`) REFERENCES `funcionario` (`id_funcionario`),
  CONSTRAINT `fk_contrato_comprador` FOREIGN KEY (`id_comprador`) REFERENCES `funcionario` (`id_funcionario`),
  CONSTRAINT `fk_contrato_creacion` FOREIGN KEY (`usuario_creacion`) REFERENCES `usuario` (`id_usuario`),
  CONSTRAINT `fk_contrato_modificacion` FOREIGN KEY (`usuario_modificacion`) REFERENCES `usuario` (`id_usuario`),
  CONSTRAINT `fk_contrato_proveedor` FOREIGN KEY (`id_proveedor`) REFERENCES `proveedor` (`id_proveedor`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `contrato_beneficiario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `contrato_beneficiario` (
  `id_contrato_beneficiario` int unsigned NOT NULL AUTO_INCREMENT,
  `id_contrato` int unsigned NOT NULL,
  `id_beneficiario` int unsigned NOT NULL,
  `observaciones` varchar(500) DEFAULT NULL,
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `usuario_creacion` int unsigned NOT NULL DEFAULT '1',
  `fecha_modificacion` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `usuario_modificacion` int unsigned DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id_contrato_beneficiario`),
  UNIQUE KEY `uk_contrato_beneficiario` (`id_contrato`,`id_beneficiario`),
  KEY `fk_contrato_beneficiario_beneficiario` (`id_beneficiario`),
  KEY `fk_contrato_beneficiario_creacion` (`usuario_creacion`),
  KEY `fk_contrato_beneficiario_modificacion` (`usuario_modificacion`),
  CONSTRAINT `fk_contrato_beneficiario_beneficiario` FOREIGN KEY (`id_beneficiario`) REFERENCES `beneficiario` (`id_beneficiario`),
  CONSTRAINT `fk_contrato_beneficiario_contrato` FOREIGN KEY (`id_contrato`) REFERENCES `contrato` (`id_contrato`),
  CONSTRAINT `fk_contrato_beneficiario_creacion` FOREIGN KEY (`usuario_creacion`) REFERENCES `usuario` (`id_usuario`),
  CONSTRAINT `fk_contrato_beneficiario_modificacion` FOREIGN KEY (`usuario_modificacion`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `contrato_bien`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `contrato_bien` (
  `id_contrato_bien` int unsigned NOT NULL AUTO_INCREMENT,
  `id_contrato` int unsigned NOT NULL,
  `lote` smallint unsigned DEFAULT NULL,
  `partida` smallint unsigned DEFAULT NULL,
  `descripcion_tecnica` text NOT NULL,
  `id_unidad_medida` int unsigned NOT NULL,
  `cantidad` int unsigned DEFAULT NULL,
  `precio_unitario` decimal(15,2) NOT NULL,
  `subtotal` decimal(15,2) NOT NULL,
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `usuario_creacion` int unsigned NOT NULL DEFAULT '1',
  `fecha_modificacion` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `usuario_modificacion` int unsigned DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id_contrato_bien`),
  KEY `fk_contrato_bien_contrato` (`id_contrato`),
  KEY `fk_contrato_bien_unidad` (`id_unidad_medida`),
  KEY `fk_contrato_bien_creacion` (`usuario_creacion`),
  KEY `fk_contrato_bien_modificacion` (`usuario_modificacion`),
  CONSTRAINT `fk_contrato_bien_contrato` FOREIGN KEY (`id_contrato`) REFERENCES `contrato` (`id_contrato`),
  CONSTRAINT `fk_contrato_bien_creacion` FOREIGN KEY (`usuario_creacion`) REFERENCES `usuario` (`id_usuario`),
  CONSTRAINT `fk_contrato_bien_modificacion` FOREIGN KEY (`usuario_modificacion`) REFERENCES `usuario` (`id_usuario`),
  CONSTRAINT `fk_contrato_bien_unidad` FOREIGN KEY (`id_unidad_medida`) REFERENCES `unidad_medida` (`id_unidad_medida`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `contrato_clave_presupuestal`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `contrato_clave_presupuestal` (
  `id_contrato_clave_presupuestal` int unsigned NOT NULL AUTO_INCREMENT,
  `id_contrato` int unsigned NOT NULL,
  `id_clave_presupuestal` int unsigned NOT NULL,
  `monto_asignado` decimal(15,2) DEFAULT NULL,
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `usuario_creacion` int unsigned NOT NULL DEFAULT '1',
  `fecha_modificacion` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `usuario_modificacion` int unsigned DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id_contrato_clave_presupuestal`),
  UNIQUE KEY `uk_contrato_clave_presupuestal` (`id_contrato`,`id_clave_presupuestal`),
  KEY `fk_contrato_clave_presupuestal_clave` (`id_clave_presupuestal`),
  KEY `fk_contrato_clave_creacion` (`usuario_creacion`),
  KEY `fk_contrato_clave_modificacion` (`usuario_modificacion`),
  CONSTRAINT `fk_contrato_clave_creacion` FOREIGN KEY (`usuario_creacion`) REFERENCES `usuario` (`id_usuario`),
  CONSTRAINT `fk_contrato_clave_modificacion` FOREIGN KEY (`usuario_modificacion`) REFERENCES `usuario` (`id_usuario`),
  CONSTRAINT `fk_contrato_clave_presupuestal_clave` FOREIGN KEY (`id_clave_presupuestal`) REFERENCES `clave_presupuestal` (`id_clave_presupuestal`),
  CONSTRAINT `fk_contrato_clave_presupuestal_contrato` FOREIGN KEY (`id_contrato`) REFERENCES `contrato` (`id_contrato`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `evento_almacen_bien`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `evento_almacen_bien` (
  `id_evento_almacen_bien` int unsigned NOT NULL AUTO_INCREMENT,
  `id_almacen_bien` int unsigned NOT NULL,
  `fecha_evento` datetime NOT NULL,
  `tipo_evento` varchar(100) NOT NULL,
  `descripcion_evento` varchar(500) DEFAULT NULL,
  `estatus_anterior` enum('PENDIENTE_PROCESAR','PROCESADO','ASIGNADO_ENTREGA','ENTREGADO') DEFAULT NULL,
  `estatus_nuevo` enum('PENDIENTE_PROCESAR','PROCESADO','ASIGNADO_ENTREGA','ENTREGADO') NOT NULL,
  `usuario_evento` int unsigned NOT NULL,
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `usuario_creacion` int unsigned NOT NULL DEFAULT '1',
  `fecha_modificacion` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `usuario_modificacion` int unsigned DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id_evento_almacen_bien`),
  KEY `fk_evento_almacen_bien_almacen_bien` (`id_almacen_bien`),
  KEY `fk_evento_almacen_bien_creacion` (`usuario_creacion`),
  KEY `fk_evento_almacen_bien_modificacion` (`usuario_modificacion`),
  KEY `fk_evento_almacen_bien_usuario` (`usuario_evento`),
  CONSTRAINT `fk_evento_almacen_bien_almacen_bien` FOREIGN KEY (`id_almacen_bien`) REFERENCES `almacen_bien` (`id_almacen_bien`),
  CONSTRAINT `fk_evento_almacen_bien_creacion` FOREIGN KEY (`usuario_creacion`) REFERENCES `usuario` (`id_usuario`),
  CONSTRAINT `fk_evento_almacen_bien_modificacion` FOREIGN KEY (`usuario_modificacion`) REFERENCES `usuario` (`id_usuario`),
  CONSTRAINT `fk_evento_almacen_bien_usuario` FOREIGN KEY (`usuario_evento`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `evento_contrato`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `evento_contrato` (
  `id_evento_contrato` int unsigned NOT NULL AUTO_INCREMENT,
  `id_contrato` int unsigned NOT NULL,
  `fecha_evento` datetime NOT NULL,
  `tipo_evento` varchar(100) NOT NULL,
  `descripcion_evento` varchar(500) DEFAULT NULL,
  `estatus_anterior` enum('CAPTURA','POR_RECIBIR','EN_ALMACEN','LISTO_PARA_ENTREGAR','ENTREGA_PARCIAL','ENTREGADO','CERRADO') DEFAULT NULL,
  `estatus_nuevo` enum('CAPTURA','POR_RECIBIR','EN_ALMACEN','LISTO_PARA_ENTREGAR','ENTREGA_PARCIAL','ENTREGADO','CERRADO') NOT NULL,
  `usuario_evento` int unsigned NOT NULL,
  `origen_evento` varchar(100) DEFAULT NULL,
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `usuario_creacion` int unsigned NOT NULL DEFAULT '1',
  `fecha_modificacion` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `usuario_modificacion` int unsigned DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id_evento_contrato`),
  KEY `fk_evento_contrato_contrato` (`id_contrato`),
  KEY `fk_evento_contrato_creacion` (`usuario_creacion`),
  KEY `fk_evento_contrato_modificacion` (`usuario_modificacion`),
  KEY `fk_evento_contrato_usuario` (`usuario_evento`),
  CONSTRAINT `fk_evento_contrato_contrato` FOREIGN KEY (`id_contrato`) REFERENCES `contrato` (`id_contrato`),
  CONSTRAINT `fk_evento_contrato_creacion` FOREIGN KEY (`usuario_creacion`) REFERENCES `usuario` (`id_usuario`),
  CONSTRAINT `fk_evento_contrato_modificacion` FOREIGN KEY (`usuario_modificacion`) REFERENCES `usuario` (`id_usuario`),
  CONSTRAINT `fk_evento_contrato_usuario` FOREIGN KEY (`usuario_evento`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `evidencia_bien`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `evidencia_bien` (
  `id_evidencia_bien` int unsigned NOT NULL AUTO_INCREMENT,
  `id_almacen_bien` int unsigned NOT NULL,
  `url` varchar(500) DEFAULT NULL,
  `nombre_archivo` varchar(255) NOT NULL,
  `fecha_captura` datetime NOT NULL,
  `usuario_captura` int unsigned DEFAULT NULL,
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `usuario_creacion` int unsigned NOT NULL DEFAULT '1',
  `fecha_modificacion` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `usuario_modificacion` int unsigned DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id_evidencia_bien`),
  KEY `fk_evidencia_bien_almacen_bien` (`id_almacen_bien`),
  KEY `fk_evidencia_bien_creacion` (`usuario_creacion`),
  KEY `fk_evidencia_bien_modificacion` (`usuario_modificacion`),
  KEY `fk_evidencia_bien_captura` (`usuario_captura`),
  CONSTRAINT `fk_evidencia_bien_almacen_bien` FOREIGN KEY (`id_almacen_bien`) REFERENCES `almacen_bien` (`id_almacen_bien`),
  CONSTRAINT `fk_evidencia_bien_captura` FOREIGN KEY (`usuario_captura`) REFERENCES `usuario` (`id_usuario`),
  CONSTRAINT `fk_evidencia_bien_creacion` FOREIGN KEY (`usuario_creacion`) REFERENCES `usuario` (`id_usuario`),
  CONSTRAINT `fk_evidencia_bien_modificacion` FOREIGN KEY (`usuario_modificacion`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `evidencia_entrada`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `evidencia_entrada` (
  `id_evidencia_entrada` int unsigned NOT NULL AUTO_INCREMENT,
  `id_recepcion_almacen` int unsigned NOT NULL,
  `url` varchar(500) DEFAULT NULL,
  `nombre_archivo` varchar(255) NOT NULL,
  `fecha_captura` datetime NOT NULL,
  `usuario_captura` int unsigned DEFAULT NULL,
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `usuario_creacion` int unsigned NOT NULL DEFAULT '1',
  `fecha_modificacion` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `usuario_modificacion` int unsigned DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id_evidencia_entrada`),
  KEY `fk_evidencia_entrada_recepcion` (`id_recepcion_almacen`),
  KEY `fk_evidencia_entrada_creacion` (`usuario_creacion`),
  KEY `fk_evidencia_entrada_modificacion` (`usuario_modificacion`),
  KEY `fk_evidencia_entrada_captura` (`usuario_captura`),
  CONSTRAINT `fk_evidencia_entrada_captura` FOREIGN KEY (`usuario_captura`) REFERENCES `usuario` (`id_usuario`),
  CONSTRAINT `fk_evidencia_entrada_creacion` FOREIGN KEY (`usuario_creacion`) REFERENCES `usuario` (`id_usuario`),
  CONSTRAINT `fk_evidencia_entrada_modificacion` FOREIGN KEY (`usuario_modificacion`) REFERENCES `usuario` (`id_usuario`),
  CONSTRAINT `fk_evidencia_entrada_recepcion` FOREIGN KEY (`id_recepcion_almacen`) REFERENCES `recepcion_almacen` (`id_recepcion_almacen`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `evidencia_salida`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `evidencia_salida` (
  `id_evidencia_salida` int unsigned NOT NULL AUTO_INCREMENT,
  `id_salida_almacen` int unsigned NOT NULL,
  `url` varchar(500) DEFAULT NULL,
  `nombre_archivo` varchar(255) NOT NULL,
  `fecha_captura` datetime NOT NULL,
  `usuario_captura` int unsigned DEFAULT NULL,
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `usuario_creacion` int unsigned NOT NULL DEFAULT '1',
  `fecha_modificacion` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `usuario_modificacion` int unsigned DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id_evidencia_salida`),
  KEY `fk_evidencia_salida_salida` (`id_salida_almacen`),
  KEY `fk_evidencia_salida_creacion` (`usuario_creacion`),
  KEY `fk_evidencia_salida_modificacion` (`usuario_modificacion`),
  KEY `fk_evidencia_salida_captura` (`usuario_captura`),
  CONSTRAINT `fk_evidencia_salida_captura` FOREIGN KEY (`usuario_captura`) REFERENCES `usuario` (`id_usuario`),
  CONSTRAINT `fk_evidencia_salida_creacion` FOREIGN KEY (`usuario_creacion`) REFERENCES `usuario` (`id_usuario`),
  CONSTRAINT `fk_evidencia_salida_modificacion` FOREIGN KEY (`usuario_modificacion`) REFERENCES `usuario` (`id_usuario`),
  CONSTRAINT `fk_evidencia_salida_salida` FOREIGN KEY (`id_salida_almacen`) REFERENCES `salida_almacen` (`id_salida_almacen`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `funcionario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `funcionario` (
  `id_funcionario` int unsigned NOT NULL AUTO_INCREMENT,
  `nombre` varchar(255) NOT NULL,
  `dependencia` varchar(255) NOT NULL,
  `caracter` varchar(150) DEFAULT NULL,
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `usuario_creacion` int unsigned NOT NULL DEFAULT '1',
  `fecha_modificacion` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `usuario_modificacion` int unsigned DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  `es_activo_en_rol` tinyint(1) NOT NULL DEFAULT '0',
  `tipo_funcionario` varchar(20) NOT NULL DEFAULT 'TITULAR',
  PRIMARY KEY (`id_funcionario`),
  KEY `fk_funcionario_creacion` (`usuario_creacion`),
  KEY `fk_funcionario_modificacion` (`usuario_modificacion`),
  CONSTRAINT `fk_funcionario_creacion` FOREIGN KEY (`usuario_creacion`) REFERENCES `usuario` (`id_usuario`),
  CONSTRAINT `fk_funcionario_modificacion` FOREIGN KEY (`usuario_modificacion`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `proveedor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `proveedor` (
  `id_proveedor` int unsigned NOT NULL AUTO_INCREMENT,
  `razon_social` varchar(255) NOT NULL,
  `domicilio_fiscal` varchar(500) DEFAULT NULL,
  `representante` varchar(255) DEFAULT NULL,
  `caracter` varchar(150) DEFAULT NULL,
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `usuario_creacion` int unsigned NOT NULL DEFAULT '1',
  `fecha_modificacion` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `usuario_modificacion` int unsigned DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id_proveedor`),
  KEY `fk_proveedor_creacion` (`usuario_creacion`),
  KEY `fk_proveedor_modificacion` (`usuario_modificacion`),
  CONSTRAINT `fk_proveedor_creacion` FOREIGN KEY (`usuario_creacion`) REFERENCES `usuario` (`id_usuario`),
  CONSTRAINT `fk_proveedor_modificacion` FOREIGN KEY (`usuario_modificacion`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `recepcion_almacen`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `recepcion_almacen` (
  `id_recepcion_almacen` int unsigned NOT NULL AUTO_INCREMENT,
  `id_contrato` int unsigned NOT NULL,
  `folio_entrada_almacen` varchar(100) NOT NULL,
  `fecha_recepcion` datetime NOT NULL,
  `id_proveedor` int unsigned NOT NULL,
  `nombre_entrega` varchar(255) NOT NULL,
  `nombre_recibe` varchar(255) NOT NULL,
  `observaciones` varchar(500) DEFAULT NULL,
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `usuario_creacion` int unsigned NOT NULL DEFAULT '1',
  `fecha_modificacion` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `usuario_modificacion` int unsigned DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  `estatus` varchar(50) NOT NULL DEFAULT 'INICIADA' COMMENT 'Ciclo de vida: INICIADA → EN_PROCESO → PROCESADA → ENTREGADA',
  PRIMARY KEY (`id_recepcion_almacen`),
  UNIQUE KEY `uk_recepcion_almacen_folio` (`folio_entrada_almacen`),
  KEY `fk_recepcion_almacen_contrato` (`id_contrato`),
  KEY `fk_recepcion_almacen_proveedor` (`id_proveedor`),
  KEY `fk_recepcion_almacen_creacion` (`usuario_creacion`),
  KEY `fk_recepcion_almacen_modificacion` (`usuario_modificacion`),
  CONSTRAINT `fk_recepcion_almacen_contrato` FOREIGN KEY (`id_contrato`) REFERENCES `contrato` (`id_contrato`),
  CONSTRAINT `fk_recepcion_almacen_creacion` FOREIGN KEY (`usuario_creacion`) REFERENCES `usuario` (`id_usuario`),
  CONSTRAINT `fk_recepcion_almacen_modificacion` FOREIGN KEY (`usuario_modificacion`) REFERENCES `usuario` (`id_usuario`),
  CONSTRAINT `fk_recepcion_almacen_proveedor` FOREIGN KEY (`id_proveedor`) REFERENCES `proveedor` (`id_proveedor`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `recepcion_almacen_bien`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `recepcion_almacen_bien` (
  `id_recepcion_almacen_bien` int unsigned NOT NULL AUTO_INCREMENT,
  `id_recepcion_almacen` int unsigned NOT NULL,
  `id_contrato_bien` int unsigned NOT NULL,
  `cantidad_recibida` decimal(15,2) NOT NULL,
  `cantidad_rechazada` decimal(15,2) NOT NULL DEFAULT '0.00',
  `comentarios` varchar(500) DEFAULT NULL,
  `motivo_rechazo` varchar(500) DEFAULT NULL,
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `usuario_creacion` int unsigned NOT NULL DEFAULT '1',
  `fecha_modificacion` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `usuario_modificacion` int unsigned DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id_recepcion_almacen_bien`),
  KEY `fk_recepcion_almacen_bien_recepcion` (`id_recepcion_almacen`),
  KEY `fk_recepcion_almacen_bien_contrato_bien` (`id_contrato_bien`),
  KEY `fk_recepcion_almacen_bien_creacion` (`usuario_creacion`),
  KEY `fk_recepcion_almacen_bien_modificacion` (`usuario_modificacion`),
  CONSTRAINT `fk_recepcion_almacen_bien_contrato_bien` FOREIGN KEY (`id_contrato_bien`) REFERENCES `contrato_bien` (`id_contrato_bien`),
  CONSTRAINT `fk_recepcion_almacen_bien_creacion` FOREIGN KEY (`usuario_creacion`) REFERENCES `usuario` (`id_usuario`),
  CONSTRAINT `fk_recepcion_almacen_bien_modificacion` FOREIGN KEY (`usuario_modificacion`) REFERENCES `usuario` (`id_usuario`),
  CONSTRAINT `fk_recepcion_almacen_bien_recepcion` FOREIGN KEY (`id_recepcion_almacen`) REFERENCES `recepcion_almacen` (`id_recepcion_almacen`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `salida_almacen`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `salida_almacen` (
  `id_salida_almacen` int unsigned NOT NULL AUTO_INCREMENT,
  `id_contrato` int unsigned NOT NULL,
  `id_beneficiario` int unsigned NOT NULL,
  `folio_salida_almacen` varchar(100) NOT NULL,
  `fecha_salida` datetime NOT NULL,
  `nombre_entrega_almacen` varchar(255) NOT NULL,
  `nombre_recibe_beneficiario` varchar(255) NOT NULL,
  `es_entrega_total` tinyint(1) NOT NULL DEFAULT '0',
  `beneficiario_firma` tinyint(1) NOT NULL DEFAULT '1',
  `observaciones` varchar(500) DEFAULT NULL,
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `usuario_creacion` int unsigned NOT NULL DEFAULT '1',
  `fecha_modificacion` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `usuario_modificacion` int unsigned DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id_salida_almacen`),
  UNIQUE KEY `uk_salida_almacen_folio` (`folio_salida_almacen`),
  KEY `fk_salida_almacen_contrato` (`id_contrato`),
  KEY `fk_salida_almacen_beneficiario` (`id_beneficiario`),
  KEY `idx_salida_almacen_es_total` (`es_entrega_total`),
  KEY `fk_salida_almacen_creacion` (`usuario_creacion`),
  KEY `fk_salida_almacen_modificacion` (`usuario_modificacion`),
  CONSTRAINT `fk_salida_almacen_beneficiario` FOREIGN KEY (`id_beneficiario`) REFERENCES `beneficiario` (`id_beneficiario`),
  CONSTRAINT `fk_salida_almacen_contrato` FOREIGN KEY (`id_contrato`) REFERENCES `contrato` (`id_contrato`),
  CONSTRAINT `fk_salida_almacen_creacion` FOREIGN KEY (`usuario_creacion`) REFERENCES `usuario` (`id_usuario`),
  CONSTRAINT `fk_salida_almacen_modificacion` FOREIGN KEY (`usuario_modificacion`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `salida_almacen_bien`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `salida_almacen_bien` (
  `id_salida_almacen_bien` int unsigned NOT NULL AUTO_INCREMENT,
  `id_salida_almacen` int unsigned NOT NULL,
  `id_almacen_bien` int unsigned NOT NULL,
  `observaciones` varchar(500) DEFAULT NULL,
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `usuario_creacion` int unsigned NOT NULL DEFAULT '1',
  `fecha_modificacion` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `usuario_modificacion` int unsigned DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id_salida_almacen_bien`),
  KEY `fk_salida_almacen_bien_salida` (`id_salida_almacen`),
  KEY `fk_salida_almacen_bien_almacen_bien` (`id_almacen_bien`),
  KEY `fk_salida_almacen_bien_creacion` (`usuario_creacion`),
  KEY `fk_salida_almacen_bien_modificacion` (`usuario_modificacion`),
  CONSTRAINT `fk_salida_almacen_bien_almacen_bien` FOREIGN KEY (`id_almacen_bien`) REFERENCES `almacen_bien` (`id_almacen_bien`),
  CONSTRAINT `fk_salida_almacen_bien_creacion` FOREIGN KEY (`usuario_creacion`) REFERENCES `usuario` (`id_usuario`),
  CONSTRAINT `fk_salida_almacen_bien_modificacion` FOREIGN KEY (`usuario_modificacion`) REFERENCES `usuario` (`id_usuario`),
  CONSTRAINT `fk_salida_almacen_bien_salida` FOREIGN KEY (`id_salida_almacen`) REFERENCES `salida_almacen` (`id_salida_almacen`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `unidad_medida`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `unidad_medida` (
  `id_unidad_medida` int unsigned NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `usuario_creacion` int unsigned NOT NULL DEFAULT '1',
  `fecha_modificacion` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `usuario_modificacion` int unsigned DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id_unidad_medida`),
  UNIQUE KEY `uk_unidad_medida_nombre` (`nombre`),
  KEY `fk_unidad_medida_creacion` (`usuario_creacion`),
  KEY `fk_unidad_medida_modificacion` (`usuario_modificacion`),
  CONSTRAINT `fk_unidad_medida_creacion` FOREIGN KEY (`usuario_creacion`) REFERENCES `usuario` (`id_usuario`),
  CONSTRAINT `fk_unidad_medida_modificacion` FOREIGN KEY (`usuario_modificacion`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `usuario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario` (
  `id_usuario` int unsigned NOT NULL AUTO_INCREMENT,
  `username` varchar(80) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `nombre` varchar(150) NOT NULL,
  `correo` varchar(150) DEFAULT NULL,
  `usuario_sistema` tinyint(1) NOT NULL DEFAULT '0',
  `rol` enum('SISTEMA','ADMINISTRADOR','ALMACEN','CAPTURA') NOT NULL DEFAULT 'CAPTURA',
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `usuario_creacion` int unsigned NOT NULL DEFAULT '1',
  `fecha_modificacion` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `usuario_modificacion` int unsigned DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id_usuario`),
  UNIQUE KEY `unique_username` (`username`),
  UNIQUE KEY `uk_usuario_correo` (`correo`),
  KEY `fk_usuario_creacion` (`usuario_creacion`),
  KEY `fk_usuario_modificacion` (`usuario_modificacion`),
  CONSTRAINT `fk_usuario_creacion` FOREIGN KEY (`usuario_creacion`) REFERENCES `usuario` (`id_usuario`),
  CONSTRAINT `fk_usuario_modificacion` FOREIGN KEY (`usuario_modificacion`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `v_avance_recepcion`;
/*!50001 DROP VIEW IF EXISTS `v_avance_recepcion`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_avance_recepcion` AS SELECT 
 1 AS `id_contrato`,
 1 AS `id_contrato_bien`,
 1 AS `lote`,
 1 AS `partida`,
 1 AS `descripcion_tecnica`,
 1 AS `cantidad_esperada`,
 1 AS `cantidad_recibida`,
 1 AS `cantidad_rechazada`,
 1 AS `cantidad_pendiente`,
 1 AS `recepcion_completa`*/;
SET character_set_client = @saved_cs_client;
/*!50001 DROP VIEW IF EXISTS `v_avance_recepcion`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_avance_recepcion` AS select `cb`.`id_contrato` AS `id_contrato`,`cb`.`id_contrato_bien` AS `id_contrato_bien`,`cb`.`lote` AS `lote`,`cb`.`partida` AS `partida`,`cb`.`descripcion_tecnica` AS `descripcion_tecnica`,`cb`.`cantidad` AS `cantidad_esperada`,coalesce(sum(`rab`.`cantidad_recibida`),0) AS `cantidad_recibida`,coalesce(sum(`rab`.`cantidad_rechazada`),0) AS `cantidad_rechazada`,(`cb`.`cantidad` - coalesce(sum(`rab`.`cantidad_recibida`),0)) AS `cantidad_pendiente`,(case when (`cb`.`cantidad` <= coalesce(sum(`rab`.`cantidad_recibida`),0)) then 1 else 0 end) AS `recepcion_completa` from (`contrato_bien` `cb` left join `recepcion_almacen_bien` `rab` on(((`rab`.`id_contrato_bien` = `cb`.`id_contrato_bien`) and (`rab`.`activo` = 1)))) where (`cb`.`activo` = 1) group by `cb`.`id_contrato_bien` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

