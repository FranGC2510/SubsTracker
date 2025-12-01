-- TABLA USUARIO
CREATE TABLE IF NOT EXISTS usuario (
                                       id_usuario INTEGER PRIMARY KEY AUTOINCREMENT,
                                       email TEXT NOT NULL UNIQUE,
                                       nombre TEXT NOT NULL,
                                       apellidos TEXT NOT NULL,
                                       password TEXT NOT NULL
);

-- TABLA SUSCRIPCION
CREATE TABLE IF NOT EXISTS suscripcion (
                                           id_suscripcion INTEGER PRIMARY KEY AUTOINCREMENT,
                                           nombre TEXT NOT NULL,
                                           precio REAL NOT NULL, -- REAL es el Decimal de SQLite
                                           ciclo TEXT NOT NULL,  -- Guardamos el Enum como Texto
                                           categoria TEXT NOT NULL,
                                           activo BOOLEAN DEFAULT 1,
                                           fecha_activacion TEXT NOT NULL, -- SQLite guarda fechas como Texto ISO8601
                                           fecha_renovacion TEXT NOT NULL,
                                           id_titular INTEGER NOT NULL,
                                           FOREIGN KEY (id_titular) REFERENCES usuario(id_usuario) ON DELETE CASCADE
);

-- TABLA COBRO
CREATE TABLE IF NOT EXISTS cobro (
                                     id_cobro INTEGER PRIMARY KEY AUTOINCREMENT,
                                     id_suscripcion INTEGER NOT NULL,
                                     fecha_cobro TEXT NOT NULL,
                                     metodo_pago TEXT NOT NULL,
                                     descripcion TEXT,
                                     periodos_cubiertos INTEGER DEFAULT 1,
                                     FOREIGN KEY (id_suscripcion) REFERENCES suscripcion(id_suscripcion) ON DELETE CASCADE
);

-- TABLA PARTICIPA
CREATE TABLE IF NOT EXISTS participa (
                                         id_participa INTEGER PRIMARY KEY AUTOINCREMENT,
                                         id_suscripcion INTEGER NOT NULL,
                                         id_usuario INTEGER NULL,
                                         nombre_invitado TEXT NULL,
                                         cantidadApagar REAL NOT NULL,
                                         fecha_pagado TEXT NULL,
                                         metodo_pago TEXT NOT NULL,
                                         descripcion TEXT,
                                         periodos_cubiertos INTEGER DEFAULT 1,
                                         FOREIGN KEY (id_suscripcion) REFERENCES suscripcion(id_suscripcion) ON DELETE CASCADE,
                                         FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE SET NULL
);