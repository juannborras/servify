export type RoleType = "client" | "provider" | "both" | null;
export type ApiRole = "ADMIN" | "USUARIO";
export type ApiModality = "PRESENCIAL" | "VIRTUAL" | "MIXTA";

export interface SessionUser {
  id: string;
  name: string;
  email: string;
  role: RoleType;
  accessToken?: string;
  refreshToken?: string;
}

export const ACCOUNT_ROLES: { id: Exclude<RoleType, null>; label: string }[] = [
  { id: "client", label: "Cliente" },
  { id: "provider", label: "Prestador" },
  { id: "both", label: "Ambos" },
];

export const LOCATION_OPTIONS = [
  "CABA",
  "Agronomia",
  "Almagro",
  "Balvanera",
  "Barracas",
  "Belgrano",
  "Boedo",
  "Caballito",
  "Chacarita",
  "Coghlan",
  "Colegiales",
  "Constitucion",
  "Flores",
  "Floresta",
  "La Boca",
  "La Paternal",
  "Liniers",
  "Mataderos",
  "Monte Castro",
  "Monserrat",
  "Nueva Pompeya",
  "Nuñez",
  "Palermo",
  "Parque Avellaneda",
  "Parque Chacabuco",
  "Parque Chas",
  "Parque Patricios",
  "Puerto Madero",
  "Recoleta",
  "Retiro",
  "Saavedra",
  "San Cristobal",
  "San Nicolas",
  "San Telmo",
  "Velez Sarsfield",
  "Versalles",
  "Villa Crespo",
  "Villa del Parque",
  "Villa Devoto",
  "Villa General Mitre",
  "Villa Lugano",
  "Villa Luro",
  "Villa Ortuzar",
  "Villa Pueyrredon",
  "Villa Real",
  "Villa Riachuelo",
  "Villa Santa Rita",
  "Villa Soldati",
  "Villa Urquiza",
];

export const WEEK_DAYS = [
  { value: "MONDAY", label: "Lunes" },
  { value: "TUESDAY", label: "Martes" },
  { value: "WEDNESDAY", label: "Miercoles" },
  { value: "THURSDAY", label: "Jueves" },
  { value: "FRIDAY", label: "Viernes" },
  { value: "SATURDAY", label: "Sabado" },
  { value: "SUNDAY", label: "Domingo" },
];

export const TIME_OPTIONS = [
  "00:00",
  "00:30",
  "01:00",
  "01:30",
  "02:00",
  "02:30",
  "03:00",
  "03:30",
  "04:00",
  "04:30",
  "05:00",
  "05:30",
  "06:00",
  "06:30",
  "07:00",
  "07:30",
  "08:00",
  "08:30",
  "09:00",
  "09:30",
  "10:00",
  "10:30",
  "11:00",
  "11:30",
  "12:00",
  "12:30",
  "13:00",
  "13:30",
  "14:00",
  "14:30",
  "15:00",
  "15:30",
  "16:00",
  "16:30",
  "17:00",
  "17:30",
  "18:00",
  "18:30",
  "19:00",
  "19:30",
  "20:00",
  "20:30",
  "21:00",
  "21:30",
  "22:00",
  "22:30",
  "23:00",
  "23:30",
];

export interface ApiCategory {
  id: string;
  nombre: string;
  descripcion?: string;
  estado?: string;
}

export interface ApiPublication {
  id: string;
  usuarioId?: string;
  categoriaServicioId?: string;
  categoriaServicio?: ApiCategory;
  titulo: string;
  descripcion: string;
  modalidadServicio: ApiModality;
  ubicacion?: ApiLocation;
  zonasCobertura?: ApiLocation[];
  disponibilidadesHorarias?: ApiAvailability[];
  precioBase?: number;
  estado?: string;
}

export interface ApiRequest {
  id: string;
  solicitanteId?: string;
  categoriaServicioId?: string;
  modalidadServicio: ApiModality;
  ubicacion?: ApiLocation;
  disponibilidadRequerida?: ApiAvailability;
  descripcionNecesidad: string;
  precioReferencia?: number;
  estado?: string;
  createdAt?: string;
  fechaSolicitud?: string;
}

export interface ApiReceivedRequest extends ApiRequest {
  distribucionSolicitudId?: string;
  solicitudId?: string;
  publicacionServicioId?: string;
  prestadorId?: string;
  estadoDistribucion?: string;
  rondaDistribucion?: number;
  fechaEnvio?: string;
  fechaExpiracion?: string;
}

export interface ApiAcceptedDistribution {
  id: string;
  solicitudId?: string;
  publicacionServicioId?: string;
  prestadorId?: string;
  estado?: string;
  rondaDistribucion?: number;
  fechaEnvio?: string;
  fechaRespuesta?: string;
  fechaExpiracion?: string;
}

export interface ApiCounterOffer {
  id: string;
  distribucionSolicitudId?: string;
  prestadorId?: string;
  precioOriginal?: number;
  precioPropuesto?: number;
  mensaje?: string;
  estado?: string;
  fechaEmision?: string;
  fechaResolucion?: string;
}

export interface ApiAssignmentState {
  solicitudId: string;
  solicitanteId?: string;
  estadoSolicitud?: string;
  asignacion?: {
    id: string;
    solicitudId?: string;
    distribucionSolicitudId?: string;
    prestadorId?: string;
    publicacionServicioId?: string;
    precioAcordado?: number;
    estado?: string;
    fechaAsignacion?: string;
    fechaFinalizacion?: string;
  };
  contraofertasPendientes?: ApiCounterOffer[];
  distribucionesAceptadas?: ApiAcceptedDistribution[];
  distribucionesActivas?: number;
  confirmadoPorSolicitante?: boolean;
  confirmadoPorPrestador?: boolean;
  finalizacionConfirmada?: boolean;
}

export interface ApiLocation {
  pais: string;
  provincia: string;
  ciudad: string;
  localidad: string;
  calle: string;
  altura: string;
  referencia: string;
  latitud: number;
  longitud: number;
}

export interface ApiAvailability {
  diaSemana: string;
  horaDesde: string;
  horaHasta: string;
}

export interface ApiUserProfile {
  id: string;
  usuarioId: string;
  nombre: string;
  apellido: string;
  edad?: number;
  fotoPerfilUrl?: string;
  ubicacion?: ApiLocation;
  descripcionPersonal?: string;
  perfilCompleto?: boolean;
}

export interface ApiAccountConfig {
  usuario: {
    id: string;
    email: string;
    telefono?: string;
    rol?: ApiRole;
  };
  perfil?: ApiUserProfile;
}

export interface ApiRatingSummary {
  usuarioId: string;
  cantidadValoraciones: number;
  promedioEstrellas: number;
}

export interface ApiServiceRating {
  id?: string;
  solicitudId: string;
  asignacionServicioId: string;
  calificadorId?: string;
  calificadoId?: string;
  rolCalificador: "SOLICITANTE" | "PRESTADOR";
  puntaje: number;
  fechaCalificacion?: string;
}

export interface ProfilePreferences {
  email?: string;
  availabilityDayFrom?: string;
  availabilityDayTo?: string;
  availabilityFrom?: string;
  availabilityTo?: string;
  role?: Exclude<RoleType, null>;
}

function getDefaultApiBaseUrl() {
  if (typeof window === "undefined") {
    return "http://localhost:8080/api/v1";
  }

  const host = window.location.hostname || "localhost";
  return `${window.location.protocol}//${host}:8080/api/v1`;
}

const API_BASE_URL =
  import.meta.env.VITE_SERVIFY_API_URL ?? (import.meta.env.DEV ? "/api/v1" : getDefaultApiBaseUrl());

const SESSION_KEY = "servify.session";
const PROFILE_PREFS_KEY = "servify.profile-prefs";
const PROFILE_PHOTO_PREFIX = "servify.profile-photo.";

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  let response: Response;

  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      ...options,
      headers: {
        "Content-Type": "application/json",
        ...(options.headers ?? {}),
      },
    });
  } catch {
    throw new Error(
      `No se pudo conectar con el backend en ${API_BASE_URL}. Verificá que esté levantado y accesible desde este dispositivo.`
    );
  }

  const rawText = await response.text();
  const contentType = response.headers.get("content-type") ?? "";
  const hasJsonBody = rawText.trim().length > 0 && contentType.includes("application/json");

  if (!response.ok) {
    let message = `Error ${response.status}`;

    if (hasJsonBody) {
      try {
        const body = JSON.parse(rawText) as { message?: string; error?: string };
        message = body.message ?? body.error ?? message;
      } catch {
        message = rawText || message;
      }
    } else if (rawText.trim()) {
      message = rawText;
    }

    throw new Error(message);
  }

  if (response.status === 204 || rawText.trim().length === 0) {
    return undefined as T;
  }

  if (!hasJsonBody) {
    return rawText as T;
  }

  return JSON.parse(rawText) as T;
}

export const servifyApi = {
  getStoredSession(): SessionUser | null {
    const raw = localStorage.getItem(SESSION_KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as SessionUser;
    } catch {
      localStorage.removeItem(SESSION_KEY);
      return null;
    }
  },

  storeSession(user: SessionUser) {
    localStorage.setItem(SESSION_KEY, JSON.stringify(user));
  },

  clearSession() {
    localStorage.removeItem(SESSION_KEY);
  },

  updateStoredSession(patch: Partial<SessionUser>) {
    const current = this.getStoredSession();
    if (!current) return;
    this.storeSession({ ...current, ...patch });
  },

  getStoredProfilePhoto(userId: string): string {
    return localStorage.getItem(PROFILE_PHOTO_PREFIX + userId) ?? "";
  },

  saveProfilePhoto(userId: string, dataUrl: string) {
    if (dataUrl) {
      localStorage.setItem(PROFILE_PHOTO_PREFIX + userId, dataUrl);
    } else {
      localStorage.removeItem(PROFILE_PHOTO_PREFIX + userId);
    }
  },

  getProfilePreferences(userId: string): ProfilePreferences {
    const raw = localStorage.getItem(PROFILE_PREFS_KEY);
    if (!raw) return {};
    try {
      const parsed = JSON.parse(raw) as Record<string, ProfilePreferences>;
      return parsed[userId] ?? {};
    } catch {
      localStorage.removeItem(PROFILE_PREFS_KEY);
      return {};
    }
  },

  saveProfilePreferences(userId: string, preferences: ProfilePreferences) {
    const raw = localStorage.getItem(PROFILE_PREFS_KEY);
    let all: Record<string, ProfilePreferences> = {};
    if (raw) {
      try {
        all = JSON.parse(raw) as Record<string, ProfilePreferences>;
      } catch {
        all = {};
      }
    }
    all[userId] = { ...(all[userId] ?? {}), ...preferences };
    localStorage.setItem(PROFILE_PREFS_KEY, JSON.stringify(all));
  },

  async login(email: string, password: string): Promise<SessionUser> {
    const session = await request<{
      usuarioId: string;
      emailAcceso?: string;
      accessToken?: { token: string };
      refreshToken?: { token: string };
    }>("/auth/login", {
      method: "POST",
      body: JSON.stringify({ emailAcceso: email, passwordPlano: password }),
    });

    const prefs = this.getProfilePreferences(session.usuarioId);
    const user: SessionUser = {
      id: session.usuarioId,
      name: email.split("@")[0] || "Usuario",
      email,
      role: prefs.role ?? "both",
      accessToken: session.accessToken?.token,
      refreshToken: session.refreshToken?.token,
    };
    this.storeSession(user);
    return user;
  },

  async loginWithGoogle(idToken: string): Promise<SessionUser> {
    const session = await request<{
      usuarioId: string;
      emailAcceso?: string;
      accessToken?: { token: string };
      refreshToken?: { token: string };
    }>("/auth/social/google", {
      method: "POST",
      body: JSON.stringify({
        idToken,
        rol: "USUARIO",
      }),
    });

    const email = session.emailAcceso ?? "google-user@servify.local";
    const profile = await this.getUserProfile(session.usuarioId).catch(() => null);
    const prefs = this.getProfilePreferences(session.usuarioId);
    const displayName = `${profile?.nombre ?? ""} ${profile?.apellido ?? ""}`.trim();
    const user: SessionUser = {
      id: session.usuarioId,
      name: displayName || email.split("@")[0] || "Usuario",
      email,
      role: prefs.role ?? "both",
      accessToken: session.accessToken?.token,
      refreshToken: session.refreshToken?.token,
    };
    this.storeSession(user);
    return user;
  },

  async register(input: {
    nombre: string;
    apellido: string;
    email: string;
    password: string;
    localidad: string;
    disponibilidadDiaDesde?: string;
    disponibilidadDiaHasta?: string;
    horaDesde?: string;
    horaHasta?: string;
    role: RoleType;
  }): Promise<SessionUser> {
    const usuario = await request<{ id: string }>("/usuarios", {
      method: "POST",
      body: JSON.stringify({
        email: input.email,
        telefono: "Sin telefono",
        rol: "USUARIO" satisfies ApiRole,
      }),
    });

    await request<void>("/auth/credenciales", {
      method: "POST",
      body: JSON.stringify({
        usuarioId: usuario.id,
        emailAcceso: input.email,
        passwordPlano: input.password,
      }),
    });

    await request("/usuarios/" + usuario.id + "/perfil", {
      method: "PUT",
      body: JSON.stringify({
        nombre: input.nombre || "Nuevo",
        apellido: input.apellido || "Usuario",
        edad: 25,
        fotoPerfilUrl: "",
        ubicacion: buildLocation(input.localidad),
        descripcionPersonal:
          input.role === "provider" ? "Prestador de servicios" : "Cliente Servify",
      }),
    });

    const user: SessionUser = {
      id: usuario.id,
      name: `${input.nombre || "Nuevo"} ${input.apellido || "Usuario"}`.trim(),
      email: input.email,
      role: input.role,
    };
    if (input.role) {
      this.saveProfilePreferences(usuario.id, {
        role: input.role,
        availabilityDayFrom: input.disponibilidadDiaDesde,
        availabilityDayTo: input.disponibilidadDiaHasta,
        availabilityFrom: input.horaDesde,
        availabilityTo: input.horaHasta,
      });
    }
    this.storeSession(user);
    return user;
  },

  listCategories() {
    return request<ApiCategory[]>("/categorias/activas");
  },

  async listCategoryPublications(categoryName: string) {
    const active = await this.listCategories().catch(() => []);
    const category = active.find((cat) => cat.nombre.toLowerCase() === categoryName.toLowerCase());
    if (!category) return [];
    return request<ApiPublication[]>(`/categorias/${category.id}/publicaciones`);
  },

  async ensureCategory(nombre: string): Promise<ApiCategory> {
    const active = await this.listCategories().catch(() => []);
    const existing = active.find((cat) => cat.nombre.toLowerCase() === nombre.toLowerCase());
    if (existing) return existing;

    const created = await request<ApiCategory>("/categorias", {
      method: "POST",
      body: JSON.stringify({ nombre, descripcion: `Servicios de ${nombre}` }),
    });

    return request<ApiCategory>(`/categorias/${created.id}/estado`, {
      method: "PATCH",
      body: JSON.stringify({
        estadoDestino: "ACTIVA",
        motivo: "Disponible desde frontend",
      }),
    });
  },

  async createPublication(input: {
    usuarioId: string;
    categoria: string;
    titulo: string;
    descripcion: string;
    modalidad: string;
    localidad?: string;
    localidades?: string[];
    direccion: string;
    precio: string;
    disponibilidadDia?: string;
    disponibilidadDiaDesde?: string;
    disponibilidadDiaHasta?: string;
    horaDesde?: string;
    horaHasta?: string;
  }) {
    const category = await this.ensureCategory(input.categoria);
    const localidades = uniqueValues(input.localidades?.length
      ? input.localidades
      : [input.localidad || LOCATION_OPTIONS[0]]);
    const zonasCobertura = localidades.map((localidad) => buildLocation(localidad, input.direccion));
    const disponibilidadesHorarias = buildAvailabilityRange(
      input.disponibilidadDiaDesde ?? input.disponibilidadDia,
      input.disponibilidadDiaHasta ?? input.disponibilidadDia,
      input.horaDesde,
      input.horaHasta
    );

    const publication = await request<ApiPublication>("/publicaciones", {
      method: "POST",
      body: JSON.stringify({
        usuarioId: input.usuarioId,
        categoriaServicioId: category.id,
        titulo: input.titulo,
        descripcion: input.descripcion,
        modalidadServicio: toApiModality(input.modalidad),
        ubicacion: zonasCobertura[0],
        zonasCobertura,
        disponibilidadesHorarias,
        precioBase: parseMoney(input.precio),
      }),
    });

    return request<ApiPublication>(`/publicaciones/${publication.id}/estado`, {
      method: "PATCH",
      body: JSON.stringify({
        usuarioId: input.usuarioId,
        estadoDestino: "ACTIVA",
        motivo: "Lista para recibir solicitudes",
      }),
    });
  },

  async updatePublication(publicacionId: string, input: {
    usuarioId: string;
    categoria: string;
    titulo: string;
    descripcion: string;
    modalidad: string;
    localidades?: string[];
    direccion: string;
    precio: string;
    disponibilidadDiaDesde?: string;
    disponibilidadDiaHasta?: string;
    horaDesde?: string;
    horaHasta?: string;
  }) {
    const category = await this.ensureCategory(input.categoria);
    const localidades = uniqueValues(input.localidades?.length ? input.localidades : [LOCATION_OPTIONS[0]]);
    const zonasCobertura = localidades.map((localidad) => buildLocation(localidad, input.direccion));
    return request<ApiPublication>(`/publicaciones/${publicacionId}`, {
      method: "PUT",
      body: JSON.stringify({
        usuarioId: input.usuarioId,
        categoriaServicioId: category.id,
        titulo: input.titulo,
        descripcion: input.descripcion,
        modalidadServicio: toApiModality(input.modalidad),
        ubicacion: zonasCobertura[0],
        zonasCobertura,
        disponibilidadesHorarias: buildAvailabilityRange(
          input.disponibilidadDiaDesde,
          input.disponibilidadDiaHasta,
          input.horaDesde,
          input.horaHasta
        ),
        precioBase: parseMoney(input.precio),
      }),
    });
  },

  listUserPublications(usuarioId: string) {
    return request<ApiPublication[]>(`/usuarios/${usuarioId}/publicaciones`);
  },

  async changePublicationState(publicacionId: string, usuarioId: string, active: boolean) {
    return request<ApiPublication>(`/publicaciones/${publicacionId}/estado`, {
      method: "PATCH",
      body: JSON.stringify({
        usuarioId,
        estadoDestino: active ? "ACTIVA" : "PAUSADA",
        motivo: active ? "Activada desde frontend" : "Pausada desde frontend",
      }),
    });
  },

  async deletePublication(publicacionId: string, usuarioId: string) {
    return request<ApiPublication>(`/publicaciones/${publicacionId}/estado`, {
      method: "PATCH",
      body: JSON.stringify({
        usuarioId,
        estadoDestino: "ELIMINADA",
        motivo: "Eliminada desde frontend",
      }),
    });
  },

  async createServiceRequest(input: {
    solicitanteId: string;
    categoria: string;
    descripcion: string;
    modalidad: string;
    localidad: string;
    precio: string;
    disponibilidadDia?: string;
    horaDesde?: string;
    horaHasta?: string;
  }) {
    const category = await this.ensureCategory(input.categoria);
    return request<ApiRequest>("/solicitudes", {
      method: "POST",
      body: JSON.stringify({
        solicitanteId: input.solicitanteId,
        categoriaServicioId: category.id,
        modalidadServicio: toApiModality(input.modalidad),
        ubicacion: buildLocation(input.localidad),
        disponibilidadRequerida: buildAvailability(input.disponibilidadDia, input.horaDesde, input.horaHasta),
        descripcionNecesidad: input.descripcion,
        precioReferencia: parseOptionalMoney(input.precio),
      }),
    });
  },

  async updateServiceRequest(input: {
    solicitudId: string;
    solicitanteId: string;
    descripcion: string;
    modalidad: string;
    localidad: string;
    precio?: string;
    disponibilidadDia?: string;
    horaDesde?: string;
    horaHasta?: string;
  }) {
    return request<ApiRequest>(`/solicitudes/${input.solicitudId}`, {
      method: "PUT",
      body: JSON.stringify({
        solicitanteId: input.solicitanteId,
        modalidadServicio: toApiModality(input.modalidad),
        ubicacion: buildLocation(input.localidad),
        disponibilidadRequerida: buildAvailability(input.disponibilidadDia, input.horaDesde, input.horaHasta),
        descripcionNecesidad: input.descripcion,
        precioReferencia: parseOptionalMoney(input.precio),
      }),
    });
  },

  listUserRequests(usuarioId: string) {
    return request<ApiRequest[]>(`/usuarios/${usuarioId}/solicitudes`);
  },

  cancelRequest(solicitudId: string, solicitanteId: string) {
    return request<void>(`/solicitudes/${solicitudId}`, {
      method: "DELETE",
      body: JSON.stringify({ solicitanteId }),
    });
  },

  deleteRequest(solicitudId: string, solicitanteId: string) {
    return this.cancelRequest(solicitudId, solicitanteId);
  },

  listReceivedRequests(usuarioId: string) {
    return request<ApiReceivedRequest[]>(`/prestadores/${usuarioId}/solicitudes-recibidas`).then(
      (items) => items.map(normalizeReceivedRequest)
    );
  },

  respondToDistribution(input: {
    distribucionSolicitudId: string;
    prestadorId: string;
    tipoRespuesta: "ACEPTAR" | "RECHAZAR";
  }) {
    return request<void>(`/distribuciones/${input.distribucionSolicitudId}/respuestas`, {
      method: "POST",
      body: JSON.stringify({
        prestadorId: input.prestadorId,
        tipoRespuesta: input.tipoRespuesta,
      }),
    });
  },

  createCounterOffer(input: {
    distribucionSolicitudId: string;
    prestadorId: string;
    precioPropuesto: string;
    mensaje?: string;
  }) {
    return request<void>(`/distribuciones/${input.distribucionSolicitudId}/contraofertas`, {
      method: "POST",
      body: JSON.stringify({
        prestadorId: input.prestadorId,
        precioPropuesto: parseMoney(input.precioPropuesto),
        mensaje: input.mensaje ?? "",
      }),
    });
  },

  resolveCounterOffer(input: {
    contraofertaId: string;
    solicitanteId: string;
    decision: "ACEPTAR" | "RECHAZAR";
  }) {
    return request<ApiCounterOffer>(`/contraofertas/${input.contraofertaId}/resoluciones`, {
      method: "POST",
      body: JSON.stringify({
        solicitanteId: input.solicitanteId,
        decision: input.decision,
      }),
    });
  },

  getAssignmentState(solicitudId: string) {
    return request<ApiAssignmentState>(`/solicitudes/${solicitudId}/estado-asignacion`);
  },

  confirmAssignment(input: {
    solicitudId: string;
    distribucionSolicitudId: string;
    solicitanteId: string;
  }) {
    return request<NonNullable<ApiAssignmentState["asignacion"]>>(
      `/solicitudes/${input.solicitudId}/asignaciones/confirmaciones`,
      {
        method: "POST",
        body: JSON.stringify({
          distribucionSolicitudId: input.distribucionSolicitudId,
          solicitanteId: input.solicitanteId,
        }),
      }
    );
  },

  async confirmServiceCompletion(input: {
    solicitudId: string;
    asignacionServicioId: string;
    confirmanteId: string;
    rolConfirmante: "SOLICITANTE" | "PRESTADOR";
    observacion?: string;
  }) {
    return request<void>(`/solicitudes/${input.solicitudId}/finalizaciones/confirmaciones`, {
      method: "POST",
      body: JSON.stringify({
        asignacionServicioId: input.asignacionServicioId,
        confirmanteId: input.confirmanteId,
        rolConfirmante: input.rolConfirmante,
        observacion: input.observacion ?? "",
      }),
    });
  },

  async rateService(input: {
    solicitudId: string;
    asignacionServicioId: string;
    solicitanteId: string;
    prestadorId: string;
    calificadorId: string;
    rolCalificador: "SOLICITANTE" | "PRESTADOR";
    puntaje: number;
  }) {
    return request<void>(`/solicitudes/${input.solicitudId}/calificaciones`, {
      method: "POST",
      body: JSON.stringify({
        asignacionServicioId: input.asignacionServicioId,
        solicitanteId: input.solicitanteId,
        prestadorId: input.prestadorId,
        calificadorId: input.calificadorId,
        rolCalificador: input.rolCalificador,
        puntaje: input.puntaje,
      }),
    });
  },

  async getServiceRating(input: {
    solicitudId: string;
    asignacionServicioId: string;
    rolCalificador: "SOLICITANTE" | "PRESTADOR";
  }) {
    const params = new URLSearchParams({
      asignacionServicioId: input.asignacionServicioId,
      rolCalificador: input.rolCalificador,
    });
    return request<ApiServiceRating>(`/solicitudes/${input.solicitudId}/calificaciones?${params.toString()}`);
  },

  getAccountConfig(userId: string) {
    return request<ApiAccountConfig>(`/usuarios/${userId}/cuenta`);
  },

  getUserProfile(userId: string) {
    return request<ApiUserProfile>(`/usuarios/${userId}/perfil`);
  },

  getUserRatingSummary(userId: string) {
    return request<ApiRatingSummary>(`/usuarios/${userId}/reputacion`);
  },

  updateUserProfile(
    userId: string,
    input: {
      nombre: string;
      apellido: string;
      edad?: number;
      fotoPerfilUrl?: string;
      localidad: string;
      descripcionPersonal?: string;
    }
  ) {
    return request<ApiUserProfile>(`/usuarios/${userId}/perfil`, {
      method: "PUT",
      body: JSON.stringify({
        nombre: input.nombre,
        apellido: input.apellido,
        edad: input.edad ?? 25,
        fotoPerfilUrl: input.fotoPerfilUrl ?? "",
        ubicacion: buildLocation(input.localidad),
        descripcionPersonal: input.descripcionPersonal ?? "",
      }),
    });
  },
};

export function buildLocation(localidad = "CABA", direccion = ""): ApiLocation {
  const [calle, altura] = splitAddress(direccion);
  const coordinates = LOCATION_COORDS[normalizeLocationKey(localidad)] ?? LOCATION_COORDS.caba;
  return {
    pais: "Argentina",
    provincia: "Buenos Aires",
    ciudad: "CABA",
    localidad: localidad || "CABA",
    calle,
    altura,
    referencia: direccion,
    latitud: coordinates.latitud,
    longitud: coordinates.longitud,
  };
}

export function defaultAvailability(): ApiAvailability {
  return buildAvailability();
}

export function buildAvailability(day = "MONDAY", from = "09:00", to = "18:00"): ApiAvailability {
  return {
    diaSemana: day,
    horaDesde: normalizeTime(from),
    horaHasta: normalizeTime(to),
  };
}

export function buildAvailabilityRange(
  dayFrom = "MONDAY",
  dayTo = dayFrom,
  from = "09:00",
  to = "18:00"
): ApiAvailability[] {
  const start = WEEK_DAYS.findIndex((day) => day.value === dayFrom);
  const end = WEEK_DAYS.findIndex((day) => day.value === dayTo);
  if (start < 0 || end < 0) {
    return [buildAvailability(dayFrom, from, to)];
  }

  const selectedDays: string[] = [];
  for (let offset = 0; offset < WEEK_DAYS.length; offset += 1) {
    const index = (start + offset) % WEEK_DAYS.length;
    selectedDays.push(WEEK_DAYS[index].value);
    if (index === end) break;
  }
  return selectedDays.map((day) => buildAvailability(day, from, to));
}

function normalizeTime(value: string): string {
  return value.length === 5 ? `${value}:00` : value;
}

export function toApiModality(modality?: string | null): ApiModality {
  if (modality === "Virtual") return "VIRTUAL";
  if (modality === "Ambas") return "MIXTA";
  return "PRESENCIAL";
}

export function fromApiModality(modality?: string): "Presencial" | "Virtual" | "Ambas" {
  if (modality === "VIRTUAL") return "Virtual";
  if (modality === "MIXTA") return "Ambas";
  return "Presencial";
}

export function parseMoney(value: string): number {
  const normalized = value.replace(/[^\d,.-]/g, "").replace(/\./g, "").replace(",", ".");
  const parsed = Number(normalized);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : 0;
}

export function parseOptionalMoney(value?: string): number | undefined {
  if (!value?.trim()) return undefined;
  const parsed = parseMoney(value);
  return parsed > 0 ? parsed : undefined;
}

export function formatMoney(value?: number): string {
  if (!value) return "A convenir";
  return new Intl.NumberFormat("es-AR", {
    style: "currency",
    currency: "ARS",
    maximumFractionDigits: 0,
  }).format(value);
}

function splitAddress(address: string): [string, string] {
  const match = address.match(/^(.*?)(\d+.*)?$/);
  return [match?.[1]?.trim() || "Sin calle", match?.[2]?.trim() || "S/N"];
}

const LOCATION_COORDS: Record<string, { latitud: number; longitud: number }> = {
  agronomia: { latitud: -34.5925, longitud: -58.4887 },
  almagro: { latitud: -34.6090, longitud: -58.4215 },
  balvanera: { latitud: -34.6096, longitud: -58.3990 },
  barracas: { latitud: -34.6456, longitud: -58.3816 },
  belgrano: { latitud: -34.5621, longitud: -58.4567 },
  boedo: { latitud: -34.6303, longitud: -58.4187 },
  caballito: { latitud: -34.6167, longitud: -58.4433 },
  caba: { latitud: -34.6037, longitud: -58.3816 },
  chacarita: { latitud: -34.5870, longitud: -58.4542 },
  colegiales: { latitud: -34.5747, longitud: -58.4498 },
  constitucion: { latitud: -34.6277, longitud: -58.3815 },
  flores: { latitud: -34.6281, longitud: -58.4631 },
  floresta: { latitud: -34.6287, longitud: -58.4837 },
  liniers: { latitud: -34.6422, longitud: -58.5195 },
  monserrat: { latitud: -34.6125, longitud: -58.3799 },
  nunez: { latitud: -34.5486, longitud: -58.4634 },
  palermo: { latitud: -34.5889, longitud: -58.4306 },
  recoleta: { latitud: -34.5883, longitud: -58.3970 },
  retiro: { latitud: -34.5921, longitud: -58.3750 },
  saavedra: { latitud: -34.5542, longitud: -58.4866 },
  sancristobal: { latitud: -34.6236, longitud: -58.4019 },
  sannicolas: { latitud: -34.6033, longitud: -58.3817 },
  santelmo: { latitud: -34.6212, longitud: -58.3731 },
  villacrespo: { latitud: -34.5987, longitud: -58.4428 },
  villadevoto: { latitud: -34.6007, longitud: -58.5134 },
  villaortuzar: { latitud: -34.5791, longitud: -58.4692 },
  villaurquiza: { latitud: -34.5737, longitud: -58.4915 },
};

function normalizeLocationKey(value: string): string {
  return value
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/[^a-zA-Z0-9]/g, "")
    .toLowerCase();
}

function uniqueValues(values: string[]): string[] {
  const unique = Array.from(new Set(values.map((value) => value.trim()).filter(Boolean)));
  return unique.length > 0 ? unique : [LOCATION_OPTIONS[0]];
}

function normalizeReceivedRequest(request: ApiReceivedRequest): ApiReceivedRequest {
  return {
    ...request,
    id: request.id ?? request.solicitudId ?? request.distribucionSolicitudId ?? "",
    estado: request.estado ?? request.estadoDistribucion,
    fechaSolicitud: request.fechaSolicitud ?? request.fechaEnvio,
  };
}
