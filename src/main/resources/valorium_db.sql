-- Base de datos
CREATE DATABASE IF NOT EXISTS valorium_db;
USE valorium_db;


-- Tabla de Sedes
CREATE TABLE IF NOT EXISTS sedes (
    id_sede INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    direccion VARCHAR(255) NOT NULL
);

INSERT INTO sedes (nombre, direccion)
SELECT 'Torre Arequipa', 'Av. Arequipa 265, Lima 15046'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM sedes WHERE nombre = 'Torre Arequipa'
);

INSERT INTO sedes (nombre, direccion)
SELECT 'Sede Central', 'Jr. Hernán Velarde 260, Lima 15046'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM sedes WHERE nombre = 'Sede Central'
);

INSERT INTO sedes (nombre, direccion)
SELECT 'Torre Petit Thouars', 'Av. Arequipa 265, Lima 15046'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM sedes WHERE nombre = 'Torre Petit Thouars'
);

INSERT INTO sedes (nombre, direccion)
SELECT 'Torre Pacífico', 'Av. Arequipa 660, Lima 15046'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM sedes WHERE nombre = 'Torre Pacífico'
);


-- Tabla de Activos
CREATE TABLE IF NOT EXISTS activos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_sede INT,
    codigo_patrimonial VARCHAR(50) UNIQUE,
    nombre VARCHAR(100),
    categoria VARCHAR(100),
    estado VARCHAR(50),
    fecha_adquisicion DATE,
    vida_util INT,
    costo_inicial DECIMAL(15, 2),
    depreciacion_mensual DECIMAL(15, 2),
    depreciacion_acumulada DECIMAL(15, 2),
    valor_residual DECIMAL(15, 2),
    FOREIGN KEY (id_sede) REFERENCES sedes(id_sede)
);


-- Tabla de Movimientos
CREATE TABLE IF NOT EXISTS movimientos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_activo INT NOT NULL,
    tipo_movimiento VARCHAR(50),
    fecha_hora DATETIME,
    motivo_baja VARCHAR(255),
    tipo_baja VARCHAR(50),
    precio_venta DECIMAL(15, 2),
    detalle_general TEXT,
    FOREIGN KEY (id_activo) REFERENCES activos(id) ON DELETE CASCADE
);


-- Tabla de Ventas
CREATE TABLE IF NOT EXISTS ventas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_activo INT NOT NULL,
    comprador VARCHAR(255),
    ruc_comprador VARCHAR(11),
    fecha_venta DATETIME,
    precio_venta DECIMAL(15, 2),
    forma_pago VARCHAR(50),
    detalle_venta TEXT,
    FOREIGN KEY (id_activo) REFERENCES activos(id) ON DELETE CASCADE
);


-- Tabla de Usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre_usuario VARCHAR(100) UNIQUE,
    contrasena VARCHAR(255),
    rol VARCHAR(50)
);

INSERT INTO usuarios (nombre_usuario, contrasena, rol)
SELECT 'Fernando', '1234', 'Administrador'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM usuarios WHERE nombre_usuario = 'Fernando'
);

INSERT INTO usuarios (nombre_usuario, contrasena, rol)
SELECT 'Axel', '1234', 'Administrador'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM usuarios WHERE nombre_usuario = 'Axel'
);

INSERT INTO usuarios (nombre_usuario, contrasena, rol)
SELECT 'Rosa', '1234', 'Administrador'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM usuarios WHERE nombre_usuario = 'Rosa'
);

INSERT INTO usuarios (nombre_usuario, contrasena, rol)
SELECT 'Constantino', '1234', 'Administrador'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM usuarios WHERE nombre_usuario = 'Constantino'
);

INSERT INTO usuarios (nombre_usuario, contrasena, rol)
SELECT 'Profe', '1234', 'Administrador'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM usuarios WHERE nombre_usuario = 'Profe'
);


-- Tabla de Notificaciones
CREATE TABLE IF NOT EXISTS notificaciones (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_activo INT NOT NULL,
    mensaje VARCHAR(255),
    estado VARCHAR(50) DEFAULT 'Pendiente', -- 'Pendiente', 'Recordar', 'Listo'
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_activo) REFERENCES activos(id) ON DELETE CASCADE,
    UNIQUE KEY (id_activo)
);


-- Tabla de Revaluaciones
CREATE TABLE IF NOT EXISTS revaluaciones (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_movimiento INT NOT NULL, -- Relación con la tabla movimientos
    valor_anterior DECIMAL(15, 2) NOT NULL, -- Valor antes de la revaluación
    valor_revaluado DECIMAL(15, 2) NOT NULL, -- Nuevo valor del activo
    depreciacion_anterior DECIMAL(15, 2) NOT NULL, -- Depreciación antes de la revaluación
    nueva_depreciacion DECIMAL(15, 2) NOT NULL, -- Nueva depreciación mensual
    impacto_total DECIMAL(15, 2) NOT NULL, -- Impacto acumulado de la revaluación
    FOREIGN KEY (id_movimiento) REFERENCES movimientos(id) ON DELETE CASCADE
);