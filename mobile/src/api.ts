import AsyncStorage from "@react-native-async-storage/async-storage";

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
  "Colegiales",
  "Constitucion",
  "Flores",
  "Palermo",
  "Recoleta",
  "Retiro",
  "Saavedra",
  "San Telmo",
  "Villa Crespo",
  "Villa Devoto",
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
  "08:00",
  "09:00",
  "10:00",
  "11:00",
  "12:00",
  "13:00",
  "14:00",
  "15:00",
  "16:00",
  "17:00",
  "18:00",
  "19:00",
  "20:00",
  "21:00",
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
  fechaEnvio?: string;
}

export interface ApiAssignmentState {
  solicitudId: string;
  solicitanteId?: string;
  estadoSolicitud?: string;
  asignacion?: {
    id: string;
    prestadorId?: string;
    publicacionServicioId?: string;
    precioAcordado?: number;
    estado?: string;
  };
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

export interface ProfilePreferences {
  email?: string;
  availabilityDayFrom?: string;
  availabilityDayTo?: string;
  availabilityFrom?: string;
  availabilityTo?: string;
  role?: Exclude<RoleType, null>;
}

const API_BASE_URL = process.env.EXPO_PUBLIC_SERVIFY_API_URL || "http://localhost:8080/api/v1";
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
    throw new Error(`No se pudo conectar con el backend en ${API_BASE_URL}. Verifica que este publicado o accesible desde el dispositivo.`);
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

  if (response.status === 204 || rawText.trim().length === 0) return undefined as T;
  if (!hasJsonBody) return rawText as T;
  return JSON.parse(rawText) as T;
}

export const storage = {
  get: (key: string) => AsyncStorage.getItem(key),
  set: (key: string, value: string) => AsyncStorage.setItem(key, value),
  remove: (key: string) => AsyncStorage.removeItem(key),
};

export const servifyApi = {
  async getStoredSession(): Promise<SessionUser | null> {
    const raw = await AsyncStorage.getItem(SESSION_KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as SessionUser;
    } catch {
      await AsyncStorage.removeItem(SESSION_KEY);
      return null;
    }
  },

  storeSession(user: SessionUser) {
    return AsyncStorage.setItem(SESSION_KEY, JSON.stringify(user));
  },

  clearSession() {
    return AsyncStorage.removeItem(SESSION_KEY);
  },

  async getStoredProfilePhoto(userId: string): Promise<string> {
    return (await AsyncStorage.getItem(PROFILE_PHOTO_PREFIX + userId)) ?? "";
  },

  async saveProfilePhoto(userId: string, dataUrl: string) {
    if (dataUrl) await AsyncStorage.setItem(PROFILE_PHOTO_PREFIX + userId, dataUrl);
    else await AsyncStorage.removeItem(PROFILE_PHOTO_PREFIX + userId);
  },

  async getProfilePreferences(userId: string): Promise<ProfilePreferences> {
    const raw = await AsyncStorage.getItem(PROFILE_PREFS_KEY);
    if (!raw) return {};
    try {
      const parsed = JSON.parse(raw) as Record<string, ProfilePreferences>;
      return parsed[userId] ?? {};
    } catch {
      await AsyncStorage.removeItem(PROFILE_PREFS_KEY);
      return {};
    }
  },

  async saveProfilePreferences(userId: string, preferences: ProfilePreferences) {
    const raw = await AsyncStorage.getItem(PROFILE_PREFS_KEY);
    let all: Record<string, ProfilePreferences> = {};
    if (raw) {
      try {
        all = JSON.parse(raw) as Record<string, ProfilePreferences>;
      } catch {
        all = {};
      }
    }
    all[userId] = { ...(all[userId] ?? {}), ...preferences };
    await AsyncStorage.setItem(PROFILE_PREFS_KEY, JSON.stringify(all));
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
    const prefs = await this.getProfilePreferences(session.usuarioId);
    const user: SessionUser = {
      id: session.usuarioId,
      name: email.split("@")[0] || "Usuario",
      email,
      role: prefs.role ?? "both",
      accessToken: session.accessToken?.token,
      refreshToken: session.refreshToken?.token,
    };
    await this.storeSession(user);
    return user;
  },

  async register(input: {
    nombre: string;
    apellido: string;
    email: string;
    password: string;
    localidad: string;
    role: RoleType;
    disponibilidadDiaDesde?: string;
    disponibilidadDiaHasta?: string;
    horaDesde?: string;
    horaHasta?: string;
  }): Promise<SessionUser> {
    const usuario = await request<{ id: string }>("/usuarios", {
      method: "POST",
      body: JSON.stringify({ email: input.email, telefono: "Sin telefono", rol: "USUARIO" satisfies ApiRole }),
    });

    await request<void>("/auth/credenciales", {
      method: "POST",
      body: JSON.stringify({ usuarioId: usuario.id, emailAcceso: input.email, passwordPlano: input.password }),
    });

    await request(`/usuarios/${usuario.id}/perfil`, {
      method: "PUT",
      body: JSON.stringify({
        nombre: input.nombre || "Nuevo",
        apellido: input.apellido || "Usuario",
        edad: 25,
        fotoPerfilUrl: "",
        ubicacion: buildLocation(input.localidad),
        descripcionPersonal: input.role === "provider" ? "Prestador de servicios" : "Cliente Servify",
      }),
    });

    const user: SessionUser = {
      id: usuario.id,
      name: `${input.nombre || "Nuevo"} ${input.apellido || "Usuario"}`.trim(),
      email: input.email,
      role: input.role,
    };
    if (input.role) {
      await this.saveProfilePreferences(usuario.id, {
        role: input.role,
        availabilityDayFrom: input.disponibilidadDiaDesde,
        availabilityDayTo: input.disponibilidadDiaHasta,
        availabilityFrom: input.horaDesde,
        availabilityTo: input.horaHasta,
      });
    }
    await this.storeSession(user);
    return user;
  },

  listCategories: () => request<ApiCategory[]>("/categorias/activas"),

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
      body: JSON.stringify({ estadoDestino: "ACTIVA", motivo: "Disponible desde app movil" }),
    });
  },

  async createPublication(input: {
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
    const localidades = input.localidades?.length ? input.localidades : [LOCATION_OPTIONS[0]];
    const disponibilidadesHorarias = buildAvailabilityRange(input.disponibilidadDiaDesde, input.disponibilidadDiaHasta, input.horaDesde, input.horaHasta);
    const created: ApiPublication[] = [];
    for (const localidad of localidades) {
      const publication = await request<ApiPublication>("/publicaciones", {
        method: "POST",
        body: JSON.stringify({
          usuarioId: input.usuarioId,
          categoriaServicioId: category.id,
          titulo: input.titulo,
          descripcion: input.descripcion,
          modalidadServicio: toApiModality(input.modalidad),
          ubicacion: buildLocation(localidad, input.direccion),
          disponibilidadesHorarias,
          precioBase: parseMoney(input.precio),
        }),
      });
      created.push(await request<ApiPublication>(`/publicaciones/${publication.id}/estado`, {
        method: "PATCH",
        body: JSON.stringify({ usuarioId: input.usuarioId, estadoDestino: "ACTIVA", motivo: "Lista para recibir solicitudes" }),
      }));
    }
    return created[0];
  },

  listUserPublications: (usuarioId: string) => request<ApiPublication[]>(`/usuarios/${usuarioId}/publicaciones`),

  changePublicationState(publicacionId: string, usuarioId: string, active: boolean) {
    return request<ApiPublication>(`/publicaciones/${publicacionId}/estado`, {
      method: "PATCH",
      body: JSON.stringify({ usuarioId, estadoDestino: active ? "ACTIVA" : "PAUSADA", motivo: active ? "Activada desde app" : "Pausada desde app" }),
    });
  },

  deletePublication(publicacionId: string, usuarioId: string) {
    return request<ApiPublication>(`/publicaciones/${publicacionId}/estado`, {
      method: "PATCH",
      body: JSON.stringify({ usuarioId, estadoDestino: "ELIMINADA", motivo: "Eliminada desde app" }),
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

  listUserRequests: (usuarioId: string) => request<ApiRequest[]>(`/usuarios/${usuarioId}/solicitudes`),

  listReceivedRequests(usuarioId: string) {
    return request<ApiReceivedRequest[]>(`/prestadores/${usuarioId}/solicitudes-recibidas`).then((items) => items.map(normalizeReceivedRequest));
  },

  getAssignmentState: (solicitudId: string) => request<ApiAssignmentState>(`/solicitudes/${solicitudId}/estado-asignacion`),

  getAccountConfig: (userId: string) => request<ApiAccountConfig>(`/usuarios/${userId}/cuenta`),
  getUserProfile: (userId: string) => request<ApiUserProfile>(`/usuarios/${userId}/perfil`),
  getUserRatingSummary: (userId: string) => request<ApiRatingSummary>(`/usuarios/${userId}/reputacion`),

  updateUserProfile(userId: string, input: { nombre: string; apellido: string; fotoPerfilUrl?: string; localidad: string; descripcionPersonal?: string }) {
    return request<ApiUserProfile>(`/usuarios/${userId}/perfil`, {
      method: "PUT",
      body: JSON.stringify({
        nombre: input.nombre,
        apellido: input.apellido,
        edad: 25,
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

export function buildAvailability(day = "MONDAY", from = "09:00", to = "18:00"): ApiAvailability {
  return { diaSemana: day, horaDesde: normalizeTime(from), horaHasta: normalizeTime(to) };
}

export function buildAvailabilityRange(dayFrom = "MONDAY", dayTo = dayFrom, from = "09:00", to = "18:00"): ApiAvailability[] {
  const start = WEEK_DAYS.findIndex((day) => day.value === dayFrom);
  const end = WEEK_DAYS.findIndex((day) => day.value === dayTo);
  if (start < 0 || end < 0) return [buildAvailability(dayFrom, from, to)];
  const selectedDays: string[] = [];
  for (let offset = 0; offset < WEEK_DAYS.length; offset += 1) {
    const index = (start + offset) % WEEK_DAYS.length;
    selectedDays.push(WEEK_DAYS[index].value);
    if (index === end) break;
  }
  return selectedDays.map((day) => buildAvailability(day, from, to));
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
  return new Intl.NumberFormat("es-AR", { style: "currency", currency: "ARS", maximumFractionDigits: 0 }).format(value);
}

function normalizeTime(value: string): string {
  return value.length === 5 ? `${value}:00` : value;
}

function splitAddress(address: string): [string, string] {
  const match = address.match(/^(.*?)(\d+.*)?$/);
  return [match?.[1]?.trim() || "Sin calle", match?.[2]?.trim() || "S/N"];
}

const LOCATION_COORDS: Record<string, { latitud: number; longitud: number }> = {
  caba: { latitud: -34.6037, longitud: -58.3816 },
  almagro: { latitud: -34.609, longitud: -58.4215 },
  belgrano: { latitud: -34.5621, longitud: -58.4567 },
  caballito: { latitud: -34.6167, longitud: -58.4433 },
  palermo: { latitud: -34.5889, longitud: -58.4306 },
  recoleta: { latitud: -34.5883, longitud: -58.397 },
  retiro: { latitud: -34.5921, longitud: -58.375 },
  villacrespo: { latitud: -34.5987, longitud: -58.4428 },
  villadevoto: { latitud: -34.6007, longitud: -58.5134 },
  villaurquiza: { latitud: -34.5737, longitud: -58.4915 },
};

function normalizeLocationKey(value: string): string {
  return value.normalize("NFD").replace(/[\u0300-\u036f]/g, "").replace(/[^a-zA-Z0-9]/g, "").toLowerCase();
}

function normalizeReceivedRequest(request: ApiReceivedRequest): ApiReceivedRequest {
  return {
    ...request,
    id: request.id ?? request.solicitudId ?? request.distribucionSolicitudId ?? "",
    estado: request.estado ?? request.estadoDistribucion,
    fechaSolicitud: request.fechaSolicitud ?? request.fechaEnvio,
  };
}
