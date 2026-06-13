-- Promueve la cuenta oficial existente a ADMIN para operar el panel administrativo.
-- La cuenta debe existir previamente; el registro publico sigue creando usuarios comunes.

BEGIN;

UPDATE usuario
SET rol = 'admin',
    estado = 'activo',
    updated_at = CURRENT_TIMESTAMP
WHERE lower(email) = lower('servifycommunity@gmail.com');

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM usuario
        WHERE rol = 'admin'
          AND lower(email) = lower('servifycommunity@gmail.com')
    ) THEN
        RAISE EXCEPTION 'No se encontro la cuenta servifycommunity@gmail.com para promover a ADMIN.';
    END IF;
END $$;

COMMIT;
