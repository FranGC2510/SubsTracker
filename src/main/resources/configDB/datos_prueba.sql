-- =======================================================
-- 1. LIMPIEZA TOTAL (RESET)
-- =======================================================
-- Desactivar foreign keys temporalmente para borrar sin orden
PRAGMA foreign_keys = OFF;

DELETE FROM cobro;
DELETE FROM participa;
DELETE FROM suscripcion;
DELETE FROM usuario;

-- Reiniciar los contadores autoincrementales (IDs vuelven a 1)
DELETE FROM sqlite_sequence WHERE name='cobro';
DELETE FROM sqlite_sequence WHERE name='participa';
DELETE FROM sqlite_sequence WHERE name='suscripcion';
DELETE FROM sqlite_sequence WHERE name='usuario';

PRAGMA foreign_keys = ON;

-- =======================================================
-- 2. USUARIOS
-- =======================================================
-- IDs forzados: 1 (Fran), 2 (Ana), 3 (Carlos)
INSERT INTO usuario (id_usuario, email, nombre, apellidos, password) VALUES
                                                                         (1, 'fran@test.com', 'Fran', 'Developer', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWrn96pzvPNPeb.APCN8..6kPOmGud'),
                                                                         (2, 'ana@test.com', 'Ana', 'García', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWrn96pzvPNPeb.APCN8..6kPOmGud'),
                                                                         (3, 'carlos@test.com', 'Carlos', 'López', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWrn96pzvPNPeb.APCN8..6kPOmGud');


-- =======================================================
-- 3. SUSCRIPCIONES (Titular: Fran [ID 1])
-- =======================================================

-- 1. NETFLIX (Activa, Mensual)
INSERT INTO suscripcion (id_suscripcion, nombre, precio, ciclo, categoria, activo, fecha_activacion, fecha_renovacion, id_titular) VALUES
    (1, 'Netflix Premium', 17.99, 'MENSUAL', 'OCIO', 1, date('now', '-1 year'), date('now', '+15 days'), 1);

-- 2. ADOBE (Vencida ayer - ROJO)
INSERT INTO suscripcion (id_suscripcion, nombre, precio, ciclo, categoria, activo, fecha_activacion, fecha_renovacion, id_titular) VALUES
    (2, 'Adobe Creative Cloud', 65.00, 'MENSUAL', 'TRABAJO', 1, date('now', '-6 months'), date('now', '-1 day'), 1);

-- 3. SPOTIFY (Vence en 2 días - NARANJA)
INSERT INTO suscripcion (id_suscripcion, nombre, precio, ciclo, categoria, activo, fecha_activacion, fecha_renovacion, id_titular) VALUES
    (3, 'Spotify Duo', 12.99, 'MENSUAL', 'OCIO', 1, date('now', '-3 months'), date('now', '+2 days'), 1);

-- 4. GIMNASIO (Activa)
INSERT INTO suscripcion (id_suscripcion, nombre, precio, ciclo, categoria, activo, fecha_activacion, fecha_renovacion, id_titular) VALUES
    (4, 'Gimnasio Municipal', 30.00, 'MENSUAL', 'SALUD', 1, date('now', '-2 months'), date('now', '+28 days'), 1);

-- 5. DISNEY+ (Pausada - GRIS)
INSERT INTO suscripcion (id_suscripcion, nombre, precio, ciclo, categoria, activo, fecha_activacion, fecha_renovacion, id_titular) VALUES
    (5, 'Disney+', 8.99, 'MENSUAL', 'OCIO', 0, '2023-01-01', '2023-06-01', 1);

-- 6. SEGURO MOTO (Anual, Vencida - ROJO)
INSERT INTO suscripcion (id_suscripcion, nombre, precio, ciclo, categoria, activo, fecha_activacion, fecha_renovacion, id_titular) VALUES
    (6, 'Seguro Moto', 150.00, 'ANUAL', 'HOGAR', 1, date('now', '-1 year'), date('now', '-1 day'), 1);


-- =======================================================
-- 4. COLABORADORES (Participa)
-- =======================================================

-- En Netflix (ID 1): Ana (ID 2) paga (Verde)
INSERT INTO participa (id_suscripcion, id_usuario, cantidadApagar, fecha_pagado, metodo_pago, descripcion, periodos_cubiertos) VALUES
    (1, 2, 4.50, date('now'), 'BIZUM', 'Pago mensual', 1);

-- En Netflix (ID 1): Invitado "Primo Luis" (Verde)
INSERT INTO participa (id_suscripcion, nombre_invitado, cantidadApagar, fecha_pagado, metodo_pago, descripcion, periodos_cubiertos) VALUES
    (1, 'Primo Luis', 4.50, date('now'), 'EFECTIVO', 'En mano', 1);

-- En Spotify (ID 3): Invitado "Compañero Piso" DEBE dinero (Pendiente = Rojo)
INSERT INTO participa (id_suscripcion, nombre_invitado, cantidadApagar, fecha_pagado, metodo_pago, descripcion, periodos_cubiertos) VALUES
    (3, 'Compañero Piso', 6.50, NULL, 'BIZUM', 'Aún no me ha hecho el bizum', 1);

-- En Gimnasio (ID 4): Invitado "Empresa" pagó adelantado (Verde aunque fecha antigua)
INSERT INTO participa (id_suscripcion, nombre_invitado, cantidadApagar, fecha_pagado, metodo_pago, descripcion, periodos_cubiertos) VALUES
    (4, 'Subvención Empresa', 15.00, date('now', '-60 days'), 'TRANSFERENCIA', 'Semestre adelantado', 6);


-- =======================================================
-- 5. HISTORIAL DE PAGOS (Cobros)
-- =======================================================

-- Netflix (3 meses pagados)
INSERT INTO cobro (id_suscripcion, fecha_cobro, metodo_pago, descripcion) VALUES
                                                                              (1, date('now', '-1 month'), 'TARJETA', 'Mes pasado'),
                                                                              (1, date('now', '-2 months'), 'TARJETA', 'Hace 2 meses'),
                                                                              (1, date('now', '-3 months'), 'TARJETA', 'Hace 3 meses');

-- Adobe
INSERT INTO cobro (id_suscripcion, fecha_cobro, metodo_pago, descripcion) VALUES
    (2, date('now', '-1 month'), 'EFECTIVO', 'Licencia mensual');

-- Seguro Moto
INSERT INTO cobro (id_suscripcion, fecha_cobro, metodo_pago, descripcion, periodos_cubiertos) VALUES
    (6, date('now', '-1 year'), 'TRANSFERENCIA', 'Anualidad 2023', 1);