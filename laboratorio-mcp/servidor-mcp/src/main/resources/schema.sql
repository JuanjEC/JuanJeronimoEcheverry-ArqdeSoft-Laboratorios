-- MySQL 8.x. Ejecutar manualmente; spring.sql.init.mode=never.
-- No elimina ni modifica datos existentes. No es una migración de esquemas antiguos.
CREATE DATABASE IF NOT EXISTS tareas_mcp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE tareas_mcp;

CREATE TABLE IF NOT EXISTS tarea (
    id BIGINT NOT NULL AUTO_INCREMENT,
    titulo VARCHAR(255) NOT NULL,
    fecha DATE NOT NULL,
    completado BIT(1) NOT NULL DEFAULT b'0',
    version BIGINT DEFAULT 0,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS producto (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(150) NOT NULL,
    precio DECIMAL(19,2) NOT NULL,
    stock INT NOT NULL,
    version BIGINT DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_producto_nombre UNIQUE (nombre),
    CONSTRAINT ck_producto_precio CHECK (precio > 0),
    CONSTRAINT ck_producto_stock CHECK (stock >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS pedido (
    id BIGINT NOT NULL AUTO_INCREMENT,
    cliente VARCHAR(150) NOT NULL,
    direccion VARCHAR(500) NOT NULL,
    creado_en TIMESTAMP(6) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    total DECIMAL(19,2) NOT NULL,
    version BIGINT DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT ck_pedido_estado CHECK (estado IN ('CREADO', 'CANCELADO')),
    CONSTRAINT ck_pedido_total CHECK (total >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS pedido_item (
    id BIGINT NOT NULL AUTO_INCREMENT,
    pedido_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(19,2) NOT NULL,
    subtotal DECIMAL(19,2) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_pedido_producto UNIQUE (pedido_id, producto_id),
    CONSTRAINT fk_item_pedido FOREIGN KEY (pedido_id) REFERENCES pedido(id),
    -- producto_id conserva un snapshot histórico; coincide con el mapeo escalar JPA.
    CONSTRAINT ck_item_cantidad CHECK (cantidad > 0),
    CONSTRAINT ck_item_precio CHECK (precio_unitario > 0),
    CONSTRAINT ck_item_subtotal CHECK (subtotal > 0)
) ENGINE=InnoDB;
