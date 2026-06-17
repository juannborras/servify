ALTER TABLE public.calificacion
    ADD COLUMN IF NOT EXISTS comentario varchar(500);

CREATE TABLE IF NOT EXISTS public.chat_mensaje (
    id uuid NOT NULL,
    solicitud_id bigint NOT NULL,
    solicitante_id bigint NOT NULL,
    prestador_id bigint NOT NULL,
    remitente_id bigint NOT NULL,
    contenido varchar(1200) NOT NULL,
    fecha_envio timestamp without time zone NOT NULL,
    created_at timestamp without time zone NOT NULL DEFAULT now(),
    updated_at timestamp without time zone NOT NULL DEFAULT now()
);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chat_mensaje_pkey'
    ) THEN
        ALTER TABLE ONLY public.chat_mensaje
            ADD CONSTRAINT chat_mensaje_pkey PRIMARY KEY (id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_chat_mensaje_solicitud'
    ) THEN
        ALTER TABLE ONLY public.chat_mensaje
            ADD CONSTRAINT fk_chat_mensaje_solicitud
            FOREIGN KEY (solicitud_id) REFERENCES public.solicitud_servicio(id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_chat_mensaje_solicitante'
    ) THEN
        ALTER TABLE ONLY public.chat_mensaje
            ADD CONSTRAINT fk_chat_mensaje_solicitante
            FOREIGN KEY (solicitante_id) REFERENCES public.usuario(id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_chat_mensaje_prestador'
    ) THEN
        ALTER TABLE ONLY public.chat_mensaje
            ADD CONSTRAINT fk_chat_mensaje_prestador
            FOREIGN KEY (prestador_id) REFERENCES public.usuario(id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_chat_mensaje_remitente'
    ) THEN
        ALTER TABLE ONLY public.chat_mensaje
            ADD CONSTRAINT fk_chat_mensaje_remitente
            FOREIGN KEY (remitente_id) REFERENCES public.usuario(id) ON DELETE CASCADE;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_chat_mensaje_solicitud_prestador
    ON public.chat_mensaje USING btree (solicitud_id, prestador_id, fecha_envio);

CREATE INDEX IF NOT EXISTS idx_chat_mensaje_destinatarios
    ON public.chat_mensaje USING btree (solicitante_id, prestador_id);
