UPDATE public.usuario
SET rol = 'admin',
    estado = 'activo',
    updated_at = CURRENT_TIMESTAMP
WHERE lower(email) = lower('${servifyAdminEmail}');
