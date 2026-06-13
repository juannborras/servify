ALTER TABLE public.usuario
    ALTER COLUMN rol TYPE character varying(50) USING rol::text,
    ALTER COLUMN estado TYPE character varying(50) USING estado::text;

ALTER TABLE public.publicacion_servicio
    ALTER COLUMN modalidad TYPE character varying(50) USING modalidad::text,
    ALTER COLUMN estado TYPE character varying(50) USING estado::text;

ALTER TABLE public.solicitud_servicio
    ALTER COLUMN modalidad TYPE character varying(50) USING modalidad::text,
    ALTER COLUMN estado TYPE character varying(50) USING estado::text;

ALTER TABLE public.distribucion_solicitud
    ALTER COLUMN estado TYPE character varying(50) USING estado::text;

ALTER TABLE public.asignacion_servicio
    ALTER COLUMN estado TYPE character varying(50) USING estado::text;

ALTER TABLE public.contraoferta
    ALTER COLUMN estado TYPE character varying(50) USING estado::text;

ALTER TABLE public.confirmacion_finalizacion
    ALTER COLUMN rol_confirmante TYPE character varying(50) USING rol_confirmante::text;

ALTER TABLE public.disponibilidad_horaria
    ALTER COLUMN dia_semana TYPE character varying(50) USING dia_semana::text;

ALTER TABLE public.medida_administrativa_usuario
    ALTER COLUMN tipo_medida TYPE character varying(50) USING tipo_medida::text;
