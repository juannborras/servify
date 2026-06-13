CREATE TABLE IF NOT EXISTS public.identidad_externa (
    id uuid NOT NULL,
    usuario_id bigint NOT NULL,
    proveedor character varying(30) NOT NULL,
    subject character varying(255) NOT NULL,
    email character varying(255) NOT NULL,
    email_verificado boolean DEFAULT false NOT NULL,
    nombre_mostrado character varying(255),
    fecha_vinculacion timestamp without time zone NOT NULL,
    ultimo_acceso timestamp without time zone,
    habilitada boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL
);

ALTER TABLE public.refresh_token
    ALTER COLUMN credencial_acceso_id DROP NOT NULL;

ALTER TABLE public.refresh_token
    ADD COLUMN IF NOT EXISTS identidad_externa_id uuid;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'identidad_externa_pkey'
    ) THEN
        ALTER TABLE ONLY public.identidad_externa
            ADD CONSTRAINT identidad_externa_pkey PRIMARY KEY (id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uq_identidad_externa_proveedor_subject'
    ) THEN
        ALTER TABLE ONLY public.identidad_externa
            ADD CONSTRAINT uq_identidad_externa_proveedor_subject UNIQUE (proveedor, subject);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uq_identidad_externa_usuario_proveedor'
    ) THEN
        ALTER TABLE ONLY public.identidad_externa
            ADD CONSTRAINT uq_identidad_externa_usuario_proveedor UNIQUE (usuario_id, proveedor);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_identidad_externa_usuario'
    ) THEN
        ALTER TABLE ONLY public.identidad_externa
            ADD CONSTRAINT fk_identidad_externa_usuario
            FOREIGN KEY (usuario_id) REFERENCES public.usuario(id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_refresh_token_identidad_externa'
    ) THEN
        ALTER TABLE ONLY public.refresh_token
            ADD CONSTRAINT fk_refresh_token_identidad_externa
            FOREIGN KEY (identidad_externa_id) REFERENCES public.identidad_externa(id) ON DELETE CASCADE;
    END IF;
END $$;

ALTER TABLE public.refresh_token
    DROP CONSTRAINT IF EXISTS chk_refresh_token_origen;

ALTER TABLE ONLY public.refresh_token
    ADD CONSTRAINT chk_refresh_token_origen
    CHECK (((credencial_acceso_id IS NOT NULL) <> (identidad_externa_id IS NOT NULL)));

CREATE INDEX IF NOT EXISTS idx_identidad_externa_usuario_id
    ON public.identidad_externa USING btree (usuario_id);

CREATE INDEX IF NOT EXISTS idx_identidad_externa_proveedor_subject
    ON public.identidad_externa USING btree (proveedor, subject);

CREATE INDEX IF NOT EXISTS idx_refresh_token_identidad_externa_id
    ON public.refresh_token USING btree (identidad_externa_id);
