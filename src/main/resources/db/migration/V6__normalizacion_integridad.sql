ALTER TABLE public.usuario
    DROP COLUMN IF EXISTS password_hash;

ALTER TABLE public.distribucion_solicitud
    ALTER COLUMN prestador_id SET NOT NULL;

ALTER TABLE public.asignacion_servicio
    ALTER COLUMN prestador_id SET NOT NULL,
    ALTER COLUMN publicacion_id SET NOT NULL;

ALTER TABLE public.refresh_token
    DROP CONSTRAINT IF EXISTS chk_refresh_token_origen;

ALTER TABLE ONLY public.refresh_token
    ADD CONSTRAINT chk_refresh_token_origen
    CHECK (((credencial_acceso_id IS NOT NULL) <> (identidad_externa_id IS NOT NULL)));

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uq_credencial_id_usuario'
    ) THEN
        ALTER TABLE ONLY public.credencial_acceso
            ADD CONSTRAINT uq_credencial_id_usuario UNIQUE (id, usuario_id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uq_identidad_externa_id_usuario'
    ) THEN
        ALTER TABLE ONLY public.identidad_externa
            ADD CONSTRAINT uq_identidad_externa_id_usuario UNIQUE (id, usuario_id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uq_publicacion_id_usuario'
    ) THEN
        ALTER TABLE ONLY public.publicacion_servicio
            ADD CONSTRAINT uq_publicacion_id_usuario UNIQUE (id, usuario_id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uq_distribucion_integridad'
    ) THEN
        ALTER TABLE ONLY public.distribucion_solicitud
            ADD CONSTRAINT uq_distribucion_integridad UNIQUE (id, solicitud_id, publicacion_id, prestador_id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uq_distribucion_id_prestador'
    ) THEN
        ALTER TABLE ONLY public.distribucion_solicitud
            ADD CONSTRAINT uq_distribucion_id_prestador UNIQUE (id, prestador_id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uq_asignacion_integridad'
    ) THEN
        ALTER TABLE ONLY public.asignacion_servicio
            ADD CONSTRAINT uq_asignacion_integridad UNIQUE (id, solicitud_id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_refresh_token_credencial_usuario'
    ) THEN
        ALTER TABLE ONLY public.refresh_token
            ADD CONSTRAINT fk_refresh_token_credencial_usuario
            FOREIGN KEY (credencial_acceso_id, usuario_id)
            REFERENCES public.credencial_acceso(id, usuario_id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_refresh_token_identidad_externa_usuario'
    ) THEN
        ALTER TABLE ONLY public.refresh_token
            ADD CONSTRAINT fk_refresh_token_identidad_externa_usuario
            FOREIGN KEY (identidad_externa_id, usuario_id)
            REFERENCES public.identidad_externa(id, usuario_id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_distribucion_prestador'
    ) THEN
        ALTER TABLE ONLY public.distribucion_solicitud
            ADD CONSTRAINT fk_distribucion_prestador
            FOREIGN KEY (prestador_id) REFERENCES public.usuario(id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_distribucion_publicacion_prestador'
    ) THEN
        ALTER TABLE ONLY public.distribucion_solicitud
            ADD CONSTRAINT fk_distribucion_publicacion_prestador
            FOREIGN KEY (publicacion_id, prestador_id)
            REFERENCES public.publicacion_servicio(id, usuario_id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_asignacion_distribucion_integridad'
    ) THEN
        ALTER TABLE ONLY public.asignacion_servicio
            ADD CONSTRAINT fk_asignacion_distribucion_integridad
            FOREIGN KEY (distribucion_id, solicitud_id, publicacion_id, prestador_id)
            REFERENCES public.distribucion_solicitud(id, solicitud_id, publicacion_id, prestador_id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_asignacion_prestador'
    ) THEN
        ALTER TABLE ONLY public.asignacion_servicio
            ADD CONSTRAINT fk_asignacion_prestador
            FOREIGN KEY (prestador_id) REFERENCES public.usuario(id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_asignacion_publicacion'
    ) THEN
        ALTER TABLE ONLY public.asignacion_servicio
            ADD CONSTRAINT fk_asignacion_publicacion
            FOREIGN KEY (publicacion_id) REFERENCES public.publicacion_servicio(id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_confirmacion_asignacion_solicitud'
    ) THEN
        ALTER TABLE ONLY public.confirmacion_finalizacion
            ADD CONSTRAINT fk_confirmacion_asignacion_solicitud
            FOREIGN KEY (asignacion_servicio_id, solicitud_id)
            REFERENCES public.asignacion_servicio(id, solicitud_id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_contraoferta_distribucion_prestador'
    ) THEN
        ALTER TABLE ONLY public.contraoferta
            ADD CONSTRAINT fk_contraoferta_distribucion_prestador
            FOREIGN KEY (distribucion_solicitud_id, prestador_id)
            REFERENCES public.distribucion_solicitud(id, prestador_id);
    END IF;
END $$;
