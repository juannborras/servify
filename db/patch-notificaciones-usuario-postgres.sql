CREATE TABLE IF NOT EXISTS public.notificacion_usuario (
    id uuid NOT NULL,
    usuario_id bigint NOT NULL,
    tipo character varying(60) NOT NULL,
    titulo character varying(140) NOT NULL,
    mensaje character varying(800) NOT NULL,
    referencia_tipo character varying(60),
    referencia_id uuid,
    leida boolean DEFAULT false NOT NULL,
    fecha_creacion timestamp without time zone DEFAULT now() NOT NULL,
    fecha_lectura timestamp without time zone,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    CONSTRAINT notificacion_usuario_pkey PRIMARY KEY (id),
    CONSTRAINT fk_notificacion_usuario FOREIGN KEY (usuario_id) REFERENCES public.usuario(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_notificacion_usuario_id ON public.notificacion_usuario USING btree (usuario_id);
CREATE INDEX IF NOT EXISTS idx_notificacion_leida ON public.notificacion_usuario USING btree (leida);
CREATE INDEX IF NOT EXISTS idx_notificacion_fecha_creacion ON public.notificacion_usuario USING btree (fecha_creacion DESC);
