CREATE TABLE IF NOT EXISTS public.password_reset_token (
    id uuid PRIMARY KEY,
    usuario_id bigint NOT NULL,
    credencial_acceso_id uuid NOT NULL,
    email character varying(255) NOT NULL,
    token_hash character varying(255) NOT NULL UNIQUE,
    fecha_creacion timestamp without time zone NOT NULL,
    fecha_expiracion timestamp without time zone NOT NULL,
    fecha_uso timestamp without time zone,
    utilizado boolean DEFAULT false NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    CONSTRAINT fk_password_reset_usuario
        FOREIGN KEY (usuario_id) REFERENCES public.usuario(id),
    CONSTRAINT fk_password_reset_credencial
        FOREIGN KEY (credencial_acceso_id) REFERENCES public.credencial_acceso(id)
);

CREATE INDEX IF NOT EXISTS idx_password_reset_token_hash
    ON public.password_reset_token (token_hash);

CREATE INDEX IF NOT EXISTS idx_password_reset_usuario_activo
    ON public.password_reset_token (usuario_id, utilizado, fecha_expiracion);

UPDATE public.perfil_usuario p
SET perfil_completo = CASE
        WHEN u.email IS NOT NULL
          AND btrim(u.email) <> ''
          AND position('@' in u.email) > 1
          AND nombre IS NOT NULL
          AND btrim(nombre) <> ''
          AND apellido IS NOT NULL
          AND btrim(apellido) <> ''
          AND edad BETWEEN 18 AND 120
          AND (
              (latitud BETWEEN -90 AND 90 AND longitud BETWEEN -180 AND 180)
              OR (
                  localidad IS NOT NULL
                  AND btrim(localidad) <> ''
                  AND ciudad IS NOT NULL
                  AND btrim(ciudad) <> ''
                  AND provincia IS NOT NULL
                  AND btrim(provincia) <> ''
              )
          )
        THEN true
        ELSE false
    END,
    updated_at = now()
FROM public.usuario u
WHERE p.usuario_id = u.id;
