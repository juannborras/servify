-- Servify initial schema for Flyway.
-- Generated from init.sql without psql-only meta commands or local owner statements.

--
-- PostgreSQL database dump
--


-- Dumped from database version 18.3
-- Dumped by pg_dump version 18.3

-- Started on 2026-05-29 11:33:32

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;

SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 893 (class 1247 OID 16474)
-- Name: dia_semana; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.dia_semana AS ENUM (
    'lunes',
    'martes',
    'miercoles',
    'jueves',
    'viernes',
    'sabado',
    'domingo'
);



--
-- TOC entry 911 (class 1247 OID 16586)
-- Name: estado_asignacion; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.estado_asignacion AS ENUM (
    'en_curso',
    'finalizada',
    'pendiente_confirmacion',
    'activa',
    'cancelada'
);



--
-- TOC entry 926 (class 1247 OID 16726)
-- Name: estado_contraoferta; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.estado_contraoferta AS ENUM (
    'pendiente',
    'aceptada',
    'rechazada',
    'cancelada'
);



--
-- TOC entry 908 (class 1247 OID 16574)
-- Name: estado_distribucion; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.estado_distribucion AS ENUM (
    'enviada',
    'aceptada',
    'rechazada',
    'contraofertada',
    'expirada',
    'cerrada'
);



--
-- TOC entry 890 (class 1247 OID 16466)
-- Name: estado_publicacion; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.estado_publicacion AS ENUM (
    'activa',
    'inactiva',
    'suspendida',
    'pausada',
    'bloqueada',
    'eliminada'
);



--
-- TOC entry 905 (class 1247 OID 16565)
-- Name: estado_solicitud; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.estado_solicitud AS ENUM (
    'buscando_prestador',
    'asignada',
    'finalizada',
    'cancelada'
);



--
-- TOC entry 878 (class 1247 OID 16396)
-- Name: estado_usuario; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.estado_usuario AS ENUM (
    'activo',
    'inactivo',
    'suspendido',
    'bloqueado'
);



--
-- TOC entry 887 (class 1247 OID 16459)
-- Name: modalidad_servicio; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.modalidad_servicio AS ENUM (
    'presencial',
    'virtual',
    'ambas',
    'mixta'
);



--
-- TOC entry 929 (class 1247 OID 16736)
-- Name: rol_confirmante; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.rol_confirmante AS ENUM (
    'solicitante',
    'prestador'
);



--
-- TOC entry 875 (class 1247 OID 16390)
-- Name: rol_usuario; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.rol_usuario AS ENUM (
    'usuario',
    'administrador'
);



--
-- TOC entry 932 (class 1247 OID 16742)
-- Name: tipo_medida; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.tipo_medida AS ENUM (
    'bloqueo',
    'suspension',
    'advertencia'
);



SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 234 (class 1259 OID 16660)
-- Name: asignacion_servicio; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.asignacion_servicio (
    id bigint NOT NULL,
    solicitud_id bigint NOT NULL,
    distribucion_id bigint NOT NULL,
    estado character varying(50) DEFAULT 'en_curso' NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    fecha_asignacion timestamp without time zone,
    fecha_finalizacion timestamp without time zone,
    prestador_id bigint NOT NULL,
    publicacion_id bigint NOT NULL,
    precio_acordado numeric(10,2)
);



--
-- TOC entry 233 (class 1259 OID 16659)
-- Name: asignacion_servicio_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.asignacion_servicio_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;



--
-- TOC entry 5299 (class 0 OID 0)
-- Dependencies: 233
-- Name: asignacion_servicio_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.asignacion_servicio_id_seq OWNED BY public.asignacion_servicio.id;


--
-- TOC entry 236 (class 1259 OID 16693)
-- Name: calificacion; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.calificacion (
    id bigint NOT NULL,
    asignacion_id bigint NOT NULL,
    calificador_id bigint NOT NULL,
    calificado_id bigint NOT NULL,
    rol_calificador character varying(50) NOT NULL,
    puntaje integer NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    CONSTRAINT chk_calificacion_puntaje CHECK (((puntaje >= 1) AND (puntaje <= 5))),
    CONSTRAINT chk_calificacion_rol CHECK (((rol_calificador)::text = ANY ((ARRAY['solicitante'::character varying, 'prestador'::character varying])::text[])))
);



--
-- TOC entry 235 (class 1259 OID 16692)
-- Name: calificacion_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.calificacion_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;



--
-- TOC entry 5300 (class 0 OID 0)
-- Dependencies: 235
-- Name: calificacion_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.calificacion_id_seq OWNED BY public.calificacion.id;


--
-- TOC entry 224 (class 1259 OID 16490)
-- Name: categoria_servicio; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.categoria_servicio (
    id bigint NOT NULL,
    nombre character varying(100) NOT NULL,
    descripcion character varying(500),
    activa boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL
);



--
-- TOC entry 223 (class 1259 OID 16489)
-- Name: categoria_servicio_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.categoria_servicio_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;



--
-- TOC entry 5301 (class 0 OID 0)
-- Dependencies: 223
-- Name: categoria_servicio_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.categoria_servicio_id_seq OWNED BY public.categoria_servicio.id;


--
-- TOC entry 241 (class 1259 OID 16881)
-- Name: configuracion_general; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.configuracion_general (
    id uuid NOT NULL,
    radio_busqueda_inicial_km integer DEFAULT 10 NOT NULL,
    radio_busqueda_expansion_km integer DEFAULT 25 NOT NULL,
    tiempo_espera_expansion_min integer DEFAULT 30 NOT NULL,
    validacion_identidad_requerida boolean DEFAULT false NOT NULL,
    precio_base_minimo_referencia numeric(10,2) DEFAULT 0 NOT NULL,
    plataforma_activa boolean DEFAULT true NOT NULL,
    fecha_ultima_actualizacion timestamp without time zone DEFAULT now() NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    CONSTRAINT chk_config_precio_base CHECK ((precio_base_minimo_referencia >= (0)::numeric)),
    CONSTRAINT chk_config_radio_expansion CHECK ((radio_busqueda_expansion_km > radio_busqueda_inicial_km)),
    CONSTRAINT chk_config_radio_inicial CHECK ((radio_busqueda_inicial_km > 0)),
    CONSTRAINT chk_config_tiempo_espera CHECK ((tiempo_espera_expansion_min > 0))
);



--
-- TOC entry 240 (class 1259 OID 16843)
-- Name: confirmacion_finalizacion; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.confirmacion_finalizacion (
    id uuid NOT NULL,
    solicitud_id bigint NOT NULL,
    asignacion_servicio_id bigint NOT NULL,
    confirmante_id bigint NOT NULL,
    rol_confirmante character varying(50) NOT NULL,
    confirmada boolean DEFAULT false NOT NULL,
    fecha_confirmacion timestamp without time zone,
    observacion character varying(500),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL
);



--
-- TOC entry 239 (class 1259 OID 16809)
-- Name: contraoferta; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.contraoferta (
    id uuid NOT NULL,
    distribucion_solicitud_id bigint NOT NULL,
    prestador_id bigint NOT NULL,
    precio_original numeric(10,2) NOT NULL,
    precio_propuesto numeric(10,2) NOT NULL,
    mensaje character varying(500),
    estado character varying(50) DEFAULT 'pendiente' NOT NULL,
    fecha_emision timestamp without time zone NOT NULL,
    fecha_resolucion timestamp without time zone,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    CONSTRAINT chk_contraoferta_precio_original CHECK ((precio_original >= (0)::numeric)),
    CONSTRAINT chk_contraoferta_precio_propuesto CHECK ((precio_propuesto >= (0)::numeric))
);



--
-- TOC entry 237 (class 1259 OID 16749)
-- Name: credencial_acceso; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.credencial_acceso (
    id uuid NOT NULL,
    usuario_id bigint NOT NULL,
    email_acceso character varying(255) NOT NULL,
    password_hash character varying(255) NOT NULL,
    habilitada boolean DEFAULT true NOT NULL,
    ultimo_acceso timestamp without time zone,
    intentos_fallidos integer DEFAULT 0 NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    CONSTRAINT chk_intentos_fallidos CHECK ((intentos_fallidos >= 0))
);



--
-- Name: identidad_externa; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.identidad_externa (
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



--
-- TOC entry 228 (class 1259 OID 16545)
-- Name: disponibilidad_horaria; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.disponibilidad_horaria (
    id bigint NOT NULL,
    publicacion_id bigint NOT NULL,
    dia_semana character varying(50) NOT NULL,
    hora_inicio time without time zone NOT NULL,
    hora_fin time without time zone NOT NULL,
    CONSTRAINT chk_disponibilidad_horas CHECK ((hora_fin > hora_inicio))
);



--
-- TOC entry 227 (class 1259 OID 16544)
-- Name: disponibilidad_horaria_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.disponibilidad_horaria_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;



--
-- TOC entry 5302 (class 0 OID 0)
-- Dependencies: 227
-- Name: disponibilidad_horaria_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.disponibilidad_horaria_id_seq OWNED BY public.disponibilidad_horaria.id;


--
-- TOC entry 232 (class 1259 OID 16630)
-- Name: distribucion_solicitud; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.distribucion_solicitud (
    id bigint NOT NULL,
    solicitud_id bigint NOT NULL,
    publicacion_id bigint NOT NULL,
    estado character varying(50) DEFAULT 'enviada' NOT NULL,
    fecha_respuesta timestamp without time zone,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    fecha_envio timestamp without time zone,
    fecha_expiracion timestamp without time zone,
    ronda_distribucion integer,
    prestador_id bigint NOT NULL
);



--
-- TOC entry 231 (class 1259 OID 16629)
-- Name: distribucion_solicitud_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.distribucion_solicitud_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;



--
-- TOC entry 5303 (class 0 OID 0)
-- Dependencies: 231
-- Name: distribucion_solicitud_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.distribucion_solicitud_id_seq OWNED BY public.distribucion_solicitud.id;


--
-- TOC entry 242 (class 1259 OID 16907)
-- Name: medida_administrativa_usuario; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.medida_administrativa_usuario (
    id uuid NOT NULL,
    usuario_id bigint NOT NULL,
    administrador_id bigint NOT NULL,
    tipo_medida character varying(50) NOT NULL,
    motivo character varying(500) NOT NULL,
    fecha_aplicacion timestamp without time zone NOT NULL,
    fecha_fin_vigencia timestamp without time zone,
    activa boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    CONSTRAINT chk_medida_vigencia CHECK (((fecha_fin_vigencia IS NULL) OR (fecha_fin_vigencia > fecha_aplicacion)))
);



--
-- Name: notificacion_usuario; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.notificacion_usuario (
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
    updated_at timestamp without time zone DEFAULT now() NOT NULL
);



--
-- TOC entry 222 (class 1259 OID 16430)
-- Name: perfil_usuario; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.perfil_usuario (
    id bigint NOT NULL,
    usuario_id bigint NOT NULL,
    localidad character varying(255),
    ciudad character varying(255),
    edad integer,
    perfil_completo boolean DEFAULT false NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    nombre character varying(255),
    apellido character varying(255),
    pais character varying(255),
    provincia character varying(255),
    calle character varying(255),
    altura character varying(50),
    referencia character varying(255),
    latitud double precision,
    longitud double precision,
    descripcion_personal character varying(1000),
    foto_perfil_url character varying(500),
    CONSTRAINT chk_perfil_edad CHECK (((edad IS NULL) OR ((edad >= 18) AND (edad <= 120))))
);



--
-- TOC entry 221 (class 1259 OID 16429)
-- Name: perfil_usuario_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.perfil_usuario_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;



--
-- TOC entry 5304 (class 0 OID 0)
-- Dependencies: 221
-- Name: perfil_usuario_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.perfil_usuario_id_seq OWNED BY public.perfil_usuario.id;


--
-- TOC entry 226 (class 1259 OID 16508)
-- Name: publicacion_servicio; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.publicacion_servicio (
    id bigint NOT NULL,
    usuario_id bigint NOT NULL,
    categoria_id bigint NOT NULL,
    descripcion character varying(1000) NOT NULL,
    modalidad character varying(50) NOT NULL,
    ciudad character varying(255) NOT NULL,
    localidad character varying(255),
    precio_base numeric(10,2) NOT NULL,
    estado character varying(50) DEFAULT 'activa' NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    titulo character varying(255),
    pais character varying(255),
    provincia character varying(255),
    calle character varying(255),
    altura character varying(50),
    referencia character varying(255),
    latitud double precision,
    longitud double precision,
    CONSTRAINT chk_publicacion_precio CHECK ((precio_base >= (0)::numeric))
);



--
-- Name: publicacion_zona_cobertura; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.publicacion_zona_cobertura (
    id bigint GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    publicacion_id bigint NOT NULL,
    pais character varying(255),
    provincia character varying(255),
    ciudad character varying(255),
    localidad character varying(255),
    calle character varying(255),
    altura character varying(50),
    referencia character varying(255),
    latitud double precision,
    longitud double precision
);



--
-- TOC entry 225 (class 1259 OID 16507)
-- Name: publicacion_servicio_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.publicacion_servicio_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;



--
-- TOC entry 5305 (class 0 OID 0)
-- Dependencies: 225
-- Name: publicacion_servicio_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.publicacion_servicio_id_seq OWNED BY public.publicacion_servicio.id;


--
-- TOC entry 238 (class 1259 OID 16780)
-- Name: refresh_token; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.refresh_token (
    id uuid NOT NULL,
    usuario_id bigint NOT NULL,
    credencial_acceso_id uuid,
    identidad_externa_id uuid,
    token_hash character varying(255) NOT NULL,
    fecha_emision timestamp without time zone NOT NULL,
    fecha_expiracion timestamp without time zone NOT NULL,
    fecha_revocacion timestamp without time zone,
    activo boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL
);



--
-- TOC entry 230 (class 1259 OID 16592)
-- Name: solicitud_servicio; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.solicitud_servicio (
    id bigint NOT NULL,
    solicitante_id bigint NOT NULL,
    categoria_id bigint NOT NULL,
    descripcion character varying(1000) NOT NULL,
    modalidad character varying(50) NOT NULL,
    ciudad character varying(255) NOT NULL,
    localidad character varying(255),
    precio_referencia numeric(10,2),
    estado character varying(50) DEFAULT 'buscando_prestador' NOT NULL,
    radio_busqueda_km integer DEFAULT 5 NOT NULL,
    fecha_ultima_ampliacion timestamp without time zone,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    disponibilidad_dia character varying(20),
    disponibilidad_hora_desde time without time zone,
    disponibilidad_hora_hasta time without time zone,
    fecha_solicitud timestamp without time zone,
    pais character varying(255),
    provincia character varying(255),
    calle character varying(255),
    altura character varying(50),
    referencia character varying(255),
    latitud double precision,
    longitud double precision,
    CONSTRAINT chk_solicitud_precio CHECK (((precio_referencia IS NULL) OR (precio_referencia >= (0)::numeric))),
    CONSTRAINT chk_solicitud_radio CHECK ((radio_busqueda_km > 0))
);



--
-- TOC entry 229 (class 1259 OID 16591)
-- Name: solicitud_servicio_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.solicitud_servicio_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;



--
-- TOC entry 5306 (class 0 OID 0)
-- Dependencies: 229
-- Name: solicitud_servicio_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.solicitud_servicio_id_seq OWNED BY public.solicitud_servicio.id;


--
-- TOC entry 220 (class 1259 OID 16406)
-- Name: usuario; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.usuario (
    id bigint NOT NULL,
    email character varying(255) NOT NULL,
    nombre_usuario character varying(50) NOT NULL,
    rol character varying(50) DEFAULT 'usuario' NOT NULL,
    estado character varying(50) DEFAULT 'activo' NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    telefono character varying(50),
    estado_validacion_identidad character varying(50),
    fecha_registro timestamp without time zone
);



--
-- TOC entry 219 (class 1259 OID 16405)
-- Name: usuario_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.usuario_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;



--
-- TOC entry 5307 (class 0 OID 0)
-- Dependencies: 219
-- Name: usuario_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.usuario_id_seq OWNED BY public.usuario.id;


--
-- TOC entry 4978 (class 2604 OID 16663)
-- Name: asignacion_servicio id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.asignacion_servicio ALTER COLUMN id SET DEFAULT nextval('public.asignacion_servicio_id_seq'::regclass);


--
-- TOC entry 4982 (class 2604 OID 16696)
-- Name: calificacion id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.calificacion ALTER COLUMN id SET DEFAULT nextval('public.calificacion_id_seq'::regclass);


--
-- TOC entry 4962 (class 2604 OID 16493)
-- Name: categoria_servicio id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.categoria_servicio ALTER COLUMN id SET DEFAULT nextval('public.categoria_servicio_id_seq'::regclass);


--
-- TOC entry 4969 (class 2604 OID 16548)
-- Name: disponibilidad_horaria id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.disponibilidad_horaria ALTER COLUMN id SET DEFAULT nextval('public.disponibilidad_horaria_id_seq'::regclass);


--
-- TOC entry 4975 (class 2604 OID 16633)
-- Name: distribucion_solicitud id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.distribucion_solicitud ALTER COLUMN id SET DEFAULT nextval('public.distribucion_solicitud_id_seq'::regclass);


--
-- TOC entry 4958 (class 2604 OID 16433)
-- Name: perfil_usuario id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.perfil_usuario ALTER COLUMN id SET DEFAULT nextval('public.perfil_usuario_id_seq'::regclass);


--
-- TOC entry 4965 (class 2604 OID 16511)
-- Name: publicacion_servicio id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.publicacion_servicio ALTER COLUMN id SET DEFAULT nextval('public.publicacion_servicio_id_seq'::regclass);


--
-- TOC entry 4970 (class 2604 OID 16595)
-- Name: solicitud_servicio id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.solicitud_servicio ALTER COLUMN id SET DEFAULT nextval('public.solicitud_servicio_id_seq'::regclass);


--
-- TOC entry 4953 (class 2604 OID 16409)
-- Name: usuario id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuario ALTER COLUMN id SET DEFAULT nextval('public.usuario_id_seq'::regclass);


--
-- TOC entry 5285 (class 0 OID 16660)
-- Dependencies: 234
-- Data for Name: asignacion_servicio; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.asignacion_servicio (id, solicitud_id, distribucion_id, estado, created_at, updated_at, fecha_asignacion, fecha_finalizacion, prestador_id, publicacion_id, precio_acordado) FROM stdin;
\.


--
-- TOC entry 5287 (class 0 OID 16693)
-- Dependencies: 236
-- Data for Name: calificacion; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.calificacion (id, asignacion_id, calificador_id, calificado_id, rol_calificador, puntaje, created_at) FROM stdin;
\.


--
-- TOC entry 5275 (class 0 OID 16490)
-- Dependencies: 224
-- Data for Name: categoria_servicio; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.categoria_servicio (id, nombre, descripcion, activa, created_at) FROM stdin;
\.


--
-- TOC entry 5292 (class 0 OID 16881)
-- Dependencies: 241
-- Data for Name: configuracion_general; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.configuracion_general (id, radio_busqueda_inicial_km, radio_busqueda_expansion_km, tiempo_espera_expansion_min, validacion_identidad_requerida, precio_base_minimo_referencia, plataforma_activa, fecha_ultima_actualizacion, created_at) FROM stdin;
a0000000-0000-0000-0000-000000000001	10	25	30	f	0.00	t	2026-05-28 10:31:31.057746	2026-05-28 10:31:31.057746
\.


--
-- TOC entry 5291 (class 0 OID 16843)
-- Dependencies: 240
-- Data for Name: confirmacion_finalizacion; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.confirmacion_finalizacion (id, solicitud_id, asignacion_servicio_id, confirmante_id, rol_confirmante, confirmada, fecha_confirmacion, observacion, created_at, updated_at) FROM stdin;
\.


--
-- TOC entry 5290 (class 0 OID 16809)
-- Dependencies: 239
-- Data for Name: contraoferta; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.contraoferta (id, distribucion_solicitud_id, prestador_id, precio_original, precio_propuesto, mensaje, estado, fecha_emision, fecha_resolucion, created_at, updated_at) FROM stdin;
\.


--
-- TOC entry 5288 (class 0 OID 16749)
-- Dependencies: 237
-- Data for Name: credencial_acceso; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.credencial_acceso (id, usuario_id, email_acceso, password_hash, habilitada, ultimo_acceso, intentos_fallidos, created_at, updated_at) FROM stdin;
\.


--
-- Data for Name: identidad_externa; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.identidad_externa (id, usuario_id, proveedor, subject, email, email_verificado, nombre_mostrado, fecha_vinculacion, ultimo_acceso, habilitada, created_at, updated_at) FROM stdin;
\.


--
-- TOC entry 5279 (class 0 OID 16545)
-- Dependencies: 228
-- Data for Name: disponibilidad_horaria; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.disponibilidad_horaria (id, publicacion_id, dia_semana, hora_inicio, hora_fin) FROM stdin;
\.


--
-- TOC entry 5283 (class 0 OID 16630)
-- Dependencies: 232
-- Data for Name: distribucion_solicitud; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.distribucion_solicitud (id, solicitud_id, publicacion_id, estado, fecha_respuesta, created_at, fecha_envio, fecha_expiracion, ronda_distribucion, prestador_id) FROM stdin;
\.


--
-- TOC entry 5293 (class 0 OID 16907)
-- Dependencies: 242
-- Data for Name: medida_administrativa_usuario; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.medida_administrativa_usuario (id, usuario_id, administrador_id, tipo_medida, motivo, fecha_aplicacion, fecha_fin_vigencia, activa, created_at, updated_at) FROM stdin;
\.


--
-- Data for Name: notificacion_usuario; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.notificacion_usuario (id, usuario_id, tipo, titulo, mensaje, referencia_tipo, referencia_id, leida, fecha_creacion, fecha_lectura, created_at, updated_at) FROM stdin;
\.


--
-- TOC entry 5273 (class 0 OID 16430)
-- Dependencies: 222
-- Data for Name: perfil_usuario; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.perfil_usuario (id, usuario_id, localidad, ciudad, edad, perfil_completo, created_at, updated_at, nombre, apellido, pais, provincia, calle, altura, referencia, latitud, longitud, descripcion_personal, foto_perfil_url) FROM stdin;
\.


--
-- TOC entry 5277 (class 0 OID 16508)
-- Dependencies: 226
-- Data for Name: publicacion_servicio; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.publicacion_servicio (id, usuario_id, categoria_id, descripcion, modalidad, ciudad, localidad, precio_base, estado, created_at, updated_at, titulo, pais, provincia, calle, altura, referencia, latitud, longitud) FROM stdin;
\.


--
-- TOC entry 5289 (class 0 OID 16780)
-- Dependencies: 238
-- Data for Name: refresh_token; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.refresh_token (id, usuario_id, credencial_acceso_id, identidad_externa_id, token_hash, fecha_emision, fecha_expiracion, fecha_revocacion, activo, created_at) FROM stdin;
\.


--
-- TOC entry 5281 (class 0 OID 16592)
-- Dependencies: 230
-- Data for Name: solicitud_servicio; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.solicitud_servicio (id, solicitante_id, categoria_id, descripcion, modalidad, ciudad, localidad, precio_referencia, estado, radio_busqueda_km, fecha_ultima_ampliacion, created_at, updated_at, disponibilidad_dia, disponibilidad_hora_desde, disponibilidad_hora_hasta, fecha_solicitud, pais, provincia, calle, altura, referencia, latitud, longitud) FROM stdin;
\.


--
-- TOC entry 5271 (class 0 OID 16406)
-- Dependencies: 220
-- Data for Name: usuario; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.usuario (id, email, nombre_usuario, rol, estado, created_at, updated_at, telefono, estado_validacion_identidad, fecha_registro) FROM stdin;
\.


--
-- TOC entry 5308 (class 0 OID 0)
-- Dependencies: 233
-- Name: asignacion_servicio_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.asignacion_servicio_id_seq', 1, false);


--
-- TOC entry 5309 (class 0 OID 0)
-- Dependencies: 235
-- Name: calificacion_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.calificacion_id_seq', 1, false);


--
-- TOC entry 5310 (class 0 OID 0)
-- Dependencies: 223
-- Name: categoria_servicio_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.categoria_servicio_id_seq', 1, false);


--
-- TOC entry 5311 (class 0 OID 0)
-- Dependencies: 227
-- Name: disponibilidad_horaria_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.disponibilidad_horaria_id_seq', 1, false);


--
-- TOC entry 5312 (class 0 OID 0)
-- Dependencies: 231
-- Name: distribucion_solicitud_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.distribucion_solicitud_id_seq', 1, false);


--
-- TOC entry 5313 (class 0 OID 0)
-- Dependencies: 221
-- Name: perfil_usuario_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.perfil_usuario_id_seq', 1, false);


--
-- TOC entry 5314 (class 0 OID 0)
-- Dependencies: 225
-- Name: publicacion_servicio_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.publicacion_servicio_id_seq', 1, false);


--
-- TOC entry 5315 (class 0 OID 0)
-- Dependencies: 229
-- Name: solicitud_servicio_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.solicitud_servicio_id_seq', 1, false);


--
-- TOC entry 5316 (class 0 OID 0)
-- Dependencies: 219
-- Name: usuario_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.usuario_id_seq', 1, false);


--
-- TOC entry 5061 (class 2606 OID 16678)
-- Name: asignacion_servicio asignacion_servicio_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.asignacion_servicio
    ADD CONSTRAINT asignacion_servicio_pkey PRIMARY KEY (id);


--
-- TOC entry 5066 (class 2606 OID 16706)
-- Name: calificacion calificacion_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.calificacion
    ADD CONSTRAINT calificacion_pkey PRIMARY KEY (id);


--
-- TOC entry 5034 (class 2606 OID 16503)
-- Name: categoria_servicio categoria_servicio_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.categoria_servicio
    ADD CONSTRAINT categoria_servicio_pkey PRIMARY KEY (id);


--
-- TOC entry 5096 (class 2606 OID 16906)
-- Name: configuracion_general configuracion_general_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.configuracion_general
    ADD CONSTRAINT configuracion_general_pkey PRIMARY KEY (id);


--
-- TOC entry 5089 (class 2606 OID 16860)
-- Name: confirmacion_finalizacion confirmacion_finalizacion_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.confirmacion_finalizacion
    ADD CONSTRAINT confirmacion_finalizacion_pkey PRIMARY KEY (id);


--
-- TOC entry 5084 (class 2606 OID 16829)
-- Name: contraoferta contraoferta_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.contraoferta
    ADD CONSTRAINT contraoferta_pkey PRIMARY KEY (id);


--
-- TOC entry 5070 (class 2606 OID 16768)
-- Name: credencial_acceso credencial_acceso_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.credencial_acceso
    ADD CONSTRAINT credencial_acceso_pkey PRIMARY KEY (id);


--
-- Name: identidad_externa identidad_externa_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.identidad_externa
    ADD CONSTRAINT identidad_externa_pkey PRIMARY KEY (id);


--
-- TOC entry 5045 (class 2606 OID 16556)
-- Name: disponibilidad_horaria disponibilidad_horaria_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.disponibilidad_horaria
    ADD CONSTRAINT disponibilidad_horaria_pkey PRIMARY KEY (id);


--
-- TOC entry 5054 (class 2606 OID 16643)
-- Name: distribucion_solicitud distribucion_solicitud_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.distribucion_solicitud
    ADD CONSTRAINT distribucion_solicitud_pkey PRIMARY KEY (id);


--
-- TOC entry 5101 (class 2606 OID 16926)
-- Name: medida_administrativa_usuario medida_administrativa_usuario_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.medida_administrativa_usuario
    ADD CONSTRAINT medida_administrativa_usuario_pkey PRIMARY KEY (id);


--
-- Name: notificacion_usuario notificacion_usuario_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notificacion_usuario
    ADD CONSTRAINT notificacion_usuario_pkey PRIMARY KEY (id);


--
-- TOC entry 5030 (class 2606 OID 16448)
-- Name: perfil_usuario perfil_usuario_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.perfil_usuario
    ADD CONSTRAINT perfil_usuario_pkey PRIMARY KEY (id);


--
-- TOC entry 5043 (class 2606 OID 16529)
-- Name: publicacion_servicio publicacion_servicio_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.publicacion_servicio
    ADD CONSTRAINT publicacion_servicio_pkey PRIMARY KEY (id);


--
-- Name: publicacion_zona_cobertura publicacion_zona_cobertura_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.publicacion_zona_cobertura
    ADD CONSTRAINT publicacion_zona_cobertura_pkey PRIMARY KEY (id);


--
-- TOC entry 5080 (class 2606 OID 16794)
-- Name: refresh_token refresh_token_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.refresh_token
    ADD CONSTRAINT refresh_token_pkey PRIMARY KEY (id);


--
-- TOC entry 5052 (class 2606 OID 16615)
-- Name: solicitud_servicio solicitud_servicio_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.solicitud_servicio
    ADD CONSTRAINT solicitud_servicio_pkey PRIMARY KEY (id);


--
-- TOC entry 5064 (class 2606 OID 16680)
-- Name: asignacion_servicio uq_asignacion_solicitud; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.asignacion_servicio
    ADD CONSTRAINT uq_asignacion_solicitud UNIQUE (solicitud_id);


--
-- TOC entry 5068 (class 2606 OID 16708)
-- Name: calificacion uq_calificacion_asignacion_rol; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.calificacion
    ADD CONSTRAINT uq_calificacion_asignacion_rol UNIQUE (asignacion_id, rol_calificador);


--
-- TOC entry 5037 (class 2606 OID 16505)
-- Name: categoria_servicio uq_categoria_nombre; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.categoria_servicio
    ADD CONSTRAINT uq_categoria_nombre UNIQUE (nombre);


--
-- TOC entry 5094 (class 2606 OID 16862)
-- Name: confirmacion_finalizacion uq_confirmacion_asignacion_rol; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.confirmacion_finalizacion
    ADD CONSTRAINT uq_confirmacion_asignacion_rol UNIQUE (asignacion_servicio_id, rol_confirmante);


--
-- TOC entry 5074 (class 2606 OID 16770)
-- Name: credencial_acceso uq_credencial_email; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.credencial_acceso
    ADD CONSTRAINT uq_credencial_email UNIQUE (email_acceso);


--
-- TOC entry 5076 (class 2606 OID 16772)
-- Name: credencial_acceso uq_credencial_usuario; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.credencial_acceso
    ADD CONSTRAINT uq_credencial_usuario UNIQUE (usuario_id);


--
-- Name: credencial_acceso uq_credencial_id_usuario; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.credencial_acceso
    ADD CONSTRAINT uq_credencial_id_usuario UNIQUE (id, usuario_id);


--
-- Name: identidad_externa uq_identidad_externa_proveedor_subject; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.identidad_externa
    ADD CONSTRAINT uq_identidad_externa_proveedor_subject UNIQUE (proveedor, subject);


--
-- Name: identidad_externa uq_identidad_externa_usuario_proveedor; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.identidad_externa
    ADD CONSTRAINT uq_identidad_externa_usuario_proveedor UNIQUE (usuario_id, proveedor);


--
-- Name: identidad_externa uq_identidad_externa_id_usuario; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.identidad_externa
    ADD CONSTRAINT uq_identidad_externa_id_usuario UNIQUE (id, usuario_id);


--
-- TOC entry 5059 (class 2606 OID 16645)
-- Name: distribucion_solicitud uq_distribucion_solicitud_publicacion; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.distribucion_solicitud
    ADD CONSTRAINT uq_distribucion_solicitud_publicacion UNIQUE (solicitud_id, publicacion_id);


--
-- Name: distribucion_solicitud uq_distribucion_integridad; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.distribucion_solicitud
    ADD CONSTRAINT uq_distribucion_integridad UNIQUE (id, solicitud_id, publicacion_id, prestador_id);


--
-- Name: distribucion_solicitud uq_distribucion_id_prestador; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.distribucion_solicitud
    ADD CONSTRAINT uq_distribucion_id_prestador UNIQUE (id, prestador_id);


--
-- TOC entry 5032 (class 2606 OID 16450)
-- Name: perfil_usuario uq_perfil_usuario; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.perfil_usuario
    ADD CONSTRAINT uq_perfil_usuario UNIQUE (usuario_id);


--
-- TOC entry 5082 (class 2606 OID 16796)
-- Name: refresh_token uq_refresh_token_hash; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.refresh_token
    ADD CONSTRAINT uq_refresh_token_hash UNIQUE (token_hash);


--
-- Name: publicacion_servicio uq_publicacion_id_usuario; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.publicacion_servicio
    ADD CONSTRAINT uq_publicacion_id_usuario UNIQUE (id, usuario_id);


--
-- Name: asignacion_servicio uq_asignacion_integridad; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.asignacion_servicio
    ADD CONSTRAINT uq_asignacion_integridad UNIQUE (id, solicitud_id);


--
-- Name: refresh_token chk_refresh_token_origen; Type: CHECK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.refresh_token
    ADD CONSTRAINT chk_refresh_token_origen CHECK (((credencial_acceso_id IS NOT NULL) <> (identidad_externa_id IS NOT NULL)));


--
-- TOC entry 5024 (class 2606 OID 16426)
-- Name: usuario uq_usuario_email; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT uq_usuario_email UNIQUE (email);


--
-- Name: usuario uq_usuario_nombre_usuario; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT uq_usuario_nombre_usuario UNIQUE (nombre_usuario);


--
-- TOC entry 5026 (class 2606 OID 16424)
-- Name: usuario usuario_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_pkey PRIMARY KEY (id);


--
-- TOC entry 5062 (class 1259 OID 16691)
-- Name: idx_asignacion_solicitud; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_asignacion_solicitud ON public.asignacion_servicio USING btree (solicitud_id);


--
-- Name: idx_calificacion_calificado; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_calificacion_calificado ON public.calificacion USING btree (calificado_id);


--
-- Name: idx_calificacion_calificador; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_calificacion_calificador ON public.calificacion USING btree (calificador_id);


--
-- TOC entry 5035 (class 1259 OID 16506)
-- Name: idx_categoria_activa; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_categoria_activa ON public.categoria_servicio USING btree (activa);


--
-- TOC entry 5090 (class 1259 OID 16879)
-- Name: idx_confirmacion_asignacion; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_confirmacion_asignacion ON public.confirmacion_finalizacion USING btree (asignacion_servicio_id);


--
-- TOC entry 5091 (class 1259 OID 16880)
-- Name: idx_confirmacion_confirmante; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_confirmacion_confirmante ON public.confirmacion_finalizacion USING btree (confirmante_id);


--
-- TOC entry 5092 (class 1259 OID 16878)
-- Name: idx_confirmacion_solicitud; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_confirmacion_solicitud ON public.confirmacion_finalizacion USING btree (solicitud_id);


--
-- TOC entry 5085 (class 1259 OID 16840)
-- Name: idx_contraoferta_distribucion; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_contraoferta_distribucion ON public.contraoferta USING btree (distribucion_solicitud_id);


--
-- TOC entry 5086 (class 1259 OID 16842)
-- Name: idx_contraoferta_estado; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_contraoferta_estado ON public.contraoferta USING btree (estado);


--
-- TOC entry 5087 (class 1259 OID 16841)
-- Name: idx_contraoferta_prestador; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_contraoferta_prestador ON public.contraoferta USING btree (prestador_id);


--
-- TOC entry 5071 (class 1259 OID 16779)
-- Name: idx_credencial_email; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_credencial_email ON public.credencial_acceso USING btree (email_acceso);


--
-- TOC entry 5072 (class 1259 OID 16778)
-- Name: idx_credencial_usuario_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_credencial_usuario_id ON public.credencial_acceso USING btree (usuario_id);


--
-- TOC entry 5046 (class 1259 OID 16563)
-- Name: idx_disponibilidad_dia; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_disponibilidad_dia ON public.disponibilidad_horaria USING btree (dia_semana);


--
-- TOC entry 5047 (class 1259 OID 16562)
-- Name: idx_disponibilidad_publicacion; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_disponibilidad_publicacion ON public.disponibilidad_horaria USING btree (publicacion_id);


--
-- TOC entry 5055 (class 1259 OID 16658)
-- Name: idx_distribucion_estado; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_distribucion_estado ON public.distribucion_solicitud USING btree (estado);


--
-- TOC entry 5056 (class 1259 OID 16657)
-- Name: idx_distribucion_publicacion; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_distribucion_publicacion ON public.distribucion_solicitud USING btree (publicacion_id);


--
-- TOC entry 5057 (class 1259 OID 16656)
-- Name: idx_distribucion_solicitud; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_distribucion_solicitud ON public.distribucion_solicitud USING btree (solicitud_id);


--
-- TOC entry 5097 (class 1259 OID 16939)
-- Name: idx_medida_activa; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_medida_activa ON public.medida_administrativa_usuario USING btree (activa);


--
-- TOC entry 5098 (class 1259 OID 16938)
-- Name: idx_medida_administrador_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_medida_administrador_id ON public.medida_administrativa_usuario USING btree (administrador_id);


--
-- TOC entry 5099 (class 1259 OID 16937)
-- Name: idx_medida_usuario_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_medida_usuario_id ON public.medida_administrativa_usuario USING btree (usuario_id);


--
-- Name: idx_notificacion_fecha_creacion; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_notificacion_fecha_creacion ON public.notificacion_usuario USING btree (fecha_creacion DESC);


--
-- Name: idx_notificacion_leida; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_notificacion_leida ON public.notificacion_usuario USING btree (leida);


--
-- Name: idx_notificacion_usuario_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_notificacion_usuario_id ON public.notificacion_usuario USING btree (usuario_id);


--
-- TOC entry 5027 (class 1259 OID 16457)
-- Name: idx_perfil_completo; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_perfil_completo ON public.perfil_usuario USING btree (perfil_completo);


--
-- TOC entry 5028 (class 1259 OID 16456)
-- Name: idx_perfil_usuario_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_perfil_usuario_id ON public.perfil_usuario USING btree (usuario_id);


--
-- TOC entry 5038 (class 1259 OID 16541)
-- Name: idx_publicacion_categoria; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_publicacion_categoria ON public.publicacion_servicio USING btree (categoria_id);


--
-- TOC entry 5039 (class 1259 OID 16543)
-- Name: idx_publicacion_ciudad; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_publicacion_ciudad ON public.publicacion_servicio USING btree (ciudad, localidad);


--
-- TOC entry 5040 (class 1259 OID 16542)
-- Name: idx_publicacion_estado; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_publicacion_estado ON public.publicacion_servicio USING btree (estado);


--
-- Name: idx_publicacion_zona_localidad; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_publicacion_zona_localidad ON public.publicacion_zona_cobertura USING btree (ciudad, localidad);


--
-- Name: idx_publicacion_zona_publicacion; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_publicacion_zona_publicacion ON public.publicacion_zona_cobertura USING btree (publicacion_id);


--
-- TOC entry 5041 (class 1259 OID 16540)
-- Name: idx_publicacion_usuario; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_publicacion_usuario ON public.publicacion_servicio USING btree (usuario_id);


--
-- TOC entry 5077 (class 1259 OID 16808)
-- Name: idx_refresh_token_activo; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_refresh_token_activo ON public.refresh_token USING btree (activo);


--
-- TOC entry 5078 (class 1259 OID 16807)
-- Name: idx_refresh_token_usuario_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_refresh_token_usuario_id ON public.refresh_token USING btree (usuario_id);


--
-- Name: idx_refresh_token_identidad_externa_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_refresh_token_identidad_externa_id ON public.refresh_token USING btree (identidad_externa_id);


--
-- Name: idx_identidad_externa_usuario_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_identidad_externa_usuario_id ON public.identidad_externa USING btree (usuario_id);


--
-- Name: idx_identidad_externa_proveedor_subject; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_identidad_externa_proveedor_subject ON public.identidad_externa USING btree (proveedor, subject);


--
-- TOC entry 5048 (class 1259 OID 16628)
-- Name: idx_solicitud_categoria_ciudad; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_solicitud_categoria_ciudad ON public.solicitud_servicio USING btree (categoria_id, ciudad);


--
-- TOC entry 5049 (class 1259 OID 16627)
-- Name: idx_solicitud_estado; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_solicitud_estado ON public.solicitud_servicio USING btree (estado);


--
-- TOC entry 5050 (class 1259 OID 16626)
-- Name: idx_solicitud_solicitante; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_solicitud_solicitante ON public.solicitud_servicio USING btree (solicitante_id);


--
-- TOC entry 5021 (class 1259 OID 16427)
-- Name: idx_usuario_email; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_usuario_email ON public.usuario USING btree (email);


--
-- Name: idx_usuario_nombre_usuario; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_usuario_nombre_usuario ON public.usuario USING btree (nombre_usuario);


--
-- TOC entry 5022 (class 1259 OID 16428)
-- Name: idx_usuario_estado; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_usuario_estado ON public.usuario USING btree (estado);


--
-- TOC entry 5110 (class 2606 OID 16686)
-- Name: asignacion_servicio fk_asignacion_distribucion; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.asignacion_servicio
    ADD CONSTRAINT fk_asignacion_distribucion FOREIGN KEY (distribucion_id) REFERENCES public.distribucion_solicitud(id);


ALTER TABLE ONLY public.asignacion_servicio
    ADD CONSTRAINT fk_asignacion_distribucion_integridad FOREIGN KEY (distribucion_id, solicitud_id, publicacion_id, prestador_id) REFERENCES public.distribucion_solicitud(id, solicitud_id, publicacion_id, prestador_id);


ALTER TABLE ONLY public.asignacion_servicio
    ADD CONSTRAINT fk_asignacion_prestador FOREIGN KEY (prestador_id) REFERENCES public.usuario(id);


ALTER TABLE ONLY public.asignacion_servicio
    ADD CONSTRAINT fk_asignacion_publicacion FOREIGN KEY (publicacion_id) REFERENCES public.publicacion_servicio(id);


--
-- TOC entry 5111 (class 2606 OID 16681)
-- Name: asignacion_servicio fk_asignacion_solicitud; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.asignacion_servicio
    ADD CONSTRAINT fk_asignacion_solicitud FOREIGN KEY (solicitud_id) REFERENCES public.solicitud_servicio(id);


--
-- TOC entry 5112 (class 2606 OID 16709)
-- Name: calificacion fk_calificacion_asignacion; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.calificacion
    ADD CONSTRAINT fk_calificacion_asignacion FOREIGN KEY (asignacion_id) REFERENCES public.asignacion_servicio(id);


--
-- Name: calificacion fk_calificacion_calificado; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.calificacion
    ADD CONSTRAINT fk_calificacion_calificado FOREIGN KEY (calificado_id) REFERENCES public.usuario(id);


--
-- Name: calificacion fk_calificacion_calificador; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.calificacion
    ADD CONSTRAINT fk_calificacion_calificador FOREIGN KEY (calificador_id) REFERENCES public.usuario(id);


--
-- TOC entry 5118 (class 2606 OID 16868)
-- Name: confirmacion_finalizacion fk_confirmacion_asignacion; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.confirmacion_finalizacion
    ADD CONSTRAINT fk_confirmacion_asignacion FOREIGN KEY (asignacion_servicio_id) REFERENCES public.asignacion_servicio(id);


ALTER TABLE ONLY public.confirmacion_finalizacion
    ADD CONSTRAINT fk_confirmacion_asignacion_solicitud FOREIGN KEY (asignacion_servicio_id, solicitud_id) REFERENCES public.asignacion_servicio(id, solicitud_id);


--
-- TOC entry 5119 (class 2606 OID 16873)
-- Name: confirmacion_finalizacion fk_confirmacion_confirmante; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.confirmacion_finalizacion
    ADD CONSTRAINT fk_confirmacion_confirmante FOREIGN KEY (confirmante_id) REFERENCES public.usuario(id);


--
-- TOC entry 5120 (class 2606 OID 16863)
-- Name: confirmacion_finalizacion fk_confirmacion_solicitud; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.confirmacion_finalizacion
    ADD CONSTRAINT fk_confirmacion_solicitud FOREIGN KEY (solicitud_id) REFERENCES public.solicitud_servicio(id);


--
-- TOC entry 5116 (class 2606 OID 16830)
-- Name: contraoferta fk_contraoferta_distribucion; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.contraoferta
    ADD CONSTRAINT fk_contraoferta_distribucion FOREIGN KEY (distribucion_solicitud_id) REFERENCES public.distribucion_solicitud(id);


ALTER TABLE ONLY public.contraoferta
    ADD CONSTRAINT fk_contraoferta_distribucion_prestador FOREIGN KEY (distribucion_solicitud_id, prestador_id) REFERENCES public.distribucion_solicitud(id, prestador_id);


--
-- TOC entry 5117 (class 2606 OID 16835)
-- Name: contraoferta fk_contraoferta_prestador; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.contraoferta
    ADD CONSTRAINT fk_contraoferta_prestador FOREIGN KEY (prestador_id) REFERENCES public.usuario(id);


--
-- TOC entry 5113 (class 2606 OID 16773)
-- Name: credencial_acceso fk_credencial_usuario; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.credencial_acceso
    ADD CONSTRAINT fk_credencial_usuario FOREIGN KEY (usuario_id) REFERENCES public.usuario(id) ON DELETE CASCADE;


--
-- TOC entry 5105 (class 2606 OID 16557)
-- Name: disponibilidad_horaria fk_disponibilidad_publicacion; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.disponibilidad_horaria
    ADD CONSTRAINT fk_disponibilidad_publicacion FOREIGN KEY (publicacion_id) REFERENCES public.publicacion_servicio(id) ON DELETE CASCADE;


--
-- TOC entry 5108 (class 2606 OID 16651)
-- Name: distribucion_solicitud fk_distribucion_publicacion; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.distribucion_solicitud
    ADD CONSTRAINT fk_distribucion_publicacion FOREIGN KEY (publicacion_id) REFERENCES public.publicacion_servicio(id);


ALTER TABLE ONLY public.distribucion_solicitud
    ADD CONSTRAINT fk_distribucion_prestador FOREIGN KEY (prestador_id) REFERENCES public.usuario(id);


ALTER TABLE ONLY public.distribucion_solicitud
    ADD CONSTRAINT fk_distribucion_publicacion_prestador FOREIGN KEY (publicacion_id, prestador_id) REFERENCES public.publicacion_servicio(id, usuario_id);


--
-- TOC entry 5109 (class 2606 OID 16646)
-- Name: distribucion_solicitud fk_distribucion_solicitud; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.distribucion_solicitud
    ADD CONSTRAINT fk_distribucion_solicitud FOREIGN KEY (solicitud_id) REFERENCES public.solicitud_servicio(id);


--
-- TOC entry 5121 (class 2606 OID 16932)
-- Name: medida_administrativa_usuario fk_medida_administrador; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.medida_administrativa_usuario
    ADD CONSTRAINT fk_medida_administrador FOREIGN KEY (administrador_id) REFERENCES public.usuario(id);


--
-- TOC entry 5122 (class 2606 OID 16927)
-- Name: medida_administrativa_usuario fk_medida_usuario; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.medida_administrativa_usuario
    ADD CONSTRAINT fk_medida_usuario FOREIGN KEY (usuario_id) REFERENCES public.usuario(id);


--
-- Name: notificacion_usuario fk_notificacion_usuario; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notificacion_usuario
    ADD CONSTRAINT fk_notificacion_usuario FOREIGN KEY (usuario_id) REFERENCES public.usuario(id) ON DELETE CASCADE;


--
-- TOC entry 5102 (class 2606 OID 16451)
-- Name: perfil_usuario fk_perfil_usuario; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.perfil_usuario
    ADD CONSTRAINT fk_perfil_usuario FOREIGN KEY (usuario_id) REFERENCES public.usuario(id) ON DELETE CASCADE;


--
-- TOC entry 5103 (class 2606 OID 16535)
-- Name: publicacion_servicio fk_publicacion_categoria; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.publicacion_servicio
    ADD CONSTRAINT fk_publicacion_categoria FOREIGN KEY (categoria_id) REFERENCES public.categoria_servicio(id);


--
-- TOC entry 5104 (class 2606 OID 16530)
-- Name: publicacion_servicio fk_publicacion_usuario; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.publicacion_servicio
    ADD CONSTRAINT fk_publicacion_usuario FOREIGN KEY (usuario_id) REFERENCES public.usuario(id) ON DELETE CASCADE;


--
-- Name: publicacion_zona_cobertura fk_publicacion_zona_publicacion; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.publicacion_zona_cobertura
    ADD CONSTRAINT fk_publicacion_zona_publicacion FOREIGN KEY (publicacion_id) REFERENCES public.publicacion_servicio(id) ON DELETE CASCADE;


--
-- Name: identidad_externa fk_identidad_externa_usuario; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.identidad_externa
    ADD CONSTRAINT fk_identidad_externa_usuario FOREIGN KEY (usuario_id) REFERENCES public.usuario(id) ON DELETE CASCADE;


--
-- TOC entry 5114 (class 2606 OID 16802)
-- Name: refresh_token fk_refresh_token_credencial; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.refresh_token
    ADD CONSTRAINT fk_refresh_token_credencial FOREIGN KEY (credencial_acceso_id) REFERENCES public.credencial_acceso(id) ON DELETE CASCADE;


ALTER TABLE ONLY public.refresh_token
    ADD CONSTRAINT fk_refresh_token_credencial_usuario FOREIGN KEY (credencial_acceso_id, usuario_id) REFERENCES public.credencial_acceso(id, usuario_id) ON DELETE CASCADE;


--
-- Name: refresh_token fk_refresh_token_identidad_externa; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.refresh_token
    ADD CONSTRAINT fk_refresh_token_identidad_externa FOREIGN KEY (identidad_externa_id) REFERENCES public.identidad_externa(id) ON DELETE CASCADE;


ALTER TABLE ONLY public.refresh_token
    ADD CONSTRAINT fk_refresh_token_identidad_externa_usuario FOREIGN KEY (identidad_externa_id, usuario_id) REFERENCES public.identidad_externa(id, usuario_id) ON DELETE CASCADE;


--
-- TOC entry 5115 (class 2606 OID 16797)
-- Name: refresh_token fk_refresh_token_usuario; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.refresh_token
    ADD CONSTRAINT fk_refresh_token_usuario FOREIGN KEY (usuario_id) REFERENCES public.usuario(id) ON DELETE CASCADE;


--
-- TOC entry 5106 (class 2606 OID 16621)
-- Name: solicitud_servicio fk_solicitud_categoria; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.solicitud_servicio
    ADD CONSTRAINT fk_solicitud_categoria FOREIGN KEY (categoria_id) REFERENCES public.categoria_servicio(id);


--
-- TOC entry 5107 (class 2606 OID 16616)
-- Name: solicitud_servicio fk_solicitud_solicitante; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.solicitud_servicio
    ADD CONSTRAINT fk_solicitud_solicitante FOREIGN KEY (solicitante_id) REFERENCES public.usuario(id);


-- Completed on 2026-05-29 11:33:32

--
-- PostgreSQL database dump complete
--

