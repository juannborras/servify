import { useEffect, useState } from "react";
import type React from "react";
import { ArrowLeft, Clock, DollarSign, MapPin, SearchX, Send, UserRound } from "lucide-react";
import {
  WEEK_DAYS,
  formatMoney,
  fromApiModality,
  servifyApi,
  type ApiPublicProvider,
  type ApiPublication,
  type ApiRatingSummary,
} from "../api";
import type { NewRequestInitialValues } from "./NewRequestModal";

interface CategoryPublicationsScreenProps {
  categoryName: string;
  currentUserId?: string;
  onBack: () => void;
  onRequestPublication: (initialValues: NewRequestInitialValues) => void;
  onProviderPress: (provider: ApiPublicProvider) => void;
}

const emptyRating = (usuarioId: string): ApiRatingSummary => ({
  usuarioId,
  cantidadValoraciones: 0,
  promedioEstrellas: 0,
});

export function CategoryPublicationsScreen({
  categoryName,
  currentUserId,
  onBack,
  onRequestPublication,
  onProviderPress,
}: CategoryPublicationsScreenProps) {
  const [publications, setPublications] = useState<ApiPublication[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [openingProviderId, setOpeningProviderId] = useState("");

  useEffect(() => {
    let ignore = false;
    setLoading(true);
    setError("");

    servifyApi
      .listCategoryPublications(categoryName)
      .then((items) => {
        if (!ignore) setPublications(items);
      })
      .catch((err) => {
        if (!ignore) setError(err instanceof Error ? err.message : "No se pudieron cargar las publicaciones");
      })
      .finally(() => {
        if (!ignore) setLoading(false);
      });

    return () => {
      ignore = true;
    };
  }, [categoryName]);

  const openProviderProfile = async (publication: ApiPublication) => {
    if (!publication.usuarioId) {
      setError("No se pudo identificar el prestador de esta publicacion.");
      return;
    }

    setOpeningProviderId(publication.id);
    setError("");
    try {
      const provider = await buildPublicProvider(publication.usuarioId);
      onProviderPress(provider);
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo abrir el perfil del prestador");
    } finally {
      setOpeningProviderId("");
    }
  };

  return (
    <div className="servify-dark-screen flex flex-col h-full" style={{ background: "#f8fafc" }}>
      <div className="servify-page-header px-5 pt-12 pb-5 bg-white">
        <div className="flex items-center gap-3">
          <button
            onClick={onBack}
            className="flex items-center justify-center rounded-xl"
            style={{ width: 38, height: 38, background: "#f1f5f9" }}
          >
            <ArrowLeft size={18} color="#475569" strokeWidth={2} />
          </button>
          <div>
            <p style={{ fontSize: 12, color: "#64748b", fontWeight: 700 }}>Categoria</p>
            <h1 style={{ fontSize: 22, fontWeight: 800, color: "#0f172a", lineHeight: 1.2 }}>{categoryName}</h1>
          </div>
        </div>
      </div>

      <div className="flex-1 overflow-y-auto px-5 pt-4 pb-6 flex flex-col gap-4">
        {error ? (
          <p className="rounded-2xl px-4 py-3" style={{ background: "#fef2f2", color: "#b91c1c", fontSize: 13, fontWeight: 700 }}>
            {error}
          </p>
        ) : null}

        {loading ? (
          <p style={{ color: "#64748b", fontSize: 14, fontWeight: 600 }}>Cargando publicaciones...</p>
        ) : null}

        {!loading && publications.length === 0 ? (
          <div className="servify-empty-state flex flex-col items-center justify-center py-20 gap-4 text-center rounded-3xl px-5">
            <div className="flex items-center justify-center rounded-3xl" style={{ width: 76, height: 76, background: "#eef2ff" }}>
              <SearchX size={32} color="#64748b" strokeWidth={1.8} />
            </div>
            <div>
              <p style={{ fontWeight: 800, fontSize: 17, color: "#0f172a" }}>Todavia no hay publicaciones</p>
              <p style={{ fontSize: 13, color: "#64748b", marginTop: 5, lineHeight: 1.45 }}>
                Cuando haya servicios activos en esta categoria van a aparecer aca.
              </p>
            </div>
          </div>
        ) : null}

        {publications.map((publication) => {
          const isOwnPublication = Boolean(currentUserId && publication.usuarioId === currentUserId);

          return (
            <article
              key={publication.id}
              className="servify-card servify-publication-card bg-white rounded-2xl overflow-hidden"
              style={{ border: "1px solid rgba(0,0,0,0.06)", boxShadow: "0 1px 4px rgba(0,0,0,0.04)" }}
            >
            <div className="p-4">
              <div className="flex items-start justify-between gap-3">
                <div>
                  <div className="flex flex-wrap items-center gap-2">
                    <h2 style={{ fontSize: 16, fontWeight: 800, color: "#0f172a", lineHeight: 1.25 }}>{publication.titulo}</h2>
                    {isOwnPublication ? (
                      <span
                        className="servify-status-badge px-2 py-1 rounded-full"
                        style={{ background: "#e0f2fe", color: "#0369a1", fontSize: 10, fontWeight: 800 }}
                      >
                        Tu publicacion
                      </span>
                    ) : null}
                  </div>
                  <p style={{ fontSize: 12, color: "#0891b2", fontWeight: 800, marginTop: 5 }}>
                    {fromApiModality(publication.modalidadServicio)}
                  </p>
                </div>
                <span style={{ fontSize: 13, fontWeight: 800, color: "#2563eb" }}>
                  {formatMoney(publication.precioBase)}
                </span>
              </div>

              <p style={{ fontSize: 13, color: "#64748b", lineHeight: 1.5, marginTop: 10 }}>{publication.descripcion}</p>

              <div className="flex flex-wrap gap-3 mt-3">
                <Info icon={<MapPin size={13} color="#94a3b8" strokeWidth={1.8} />} label={formatAreas(publication)} />
                <Info icon={<Clock size={13} color="#94a3b8" strokeWidth={1.8} />} label={formatAvailability(publication)} />
                <Info icon={<DollarSign size={13} color="#94a3b8" strokeWidth={1.8} />} label={formatMoney(publication.precioBase)} />
              </div>
            </div>

            <div className="servify-card-footer grid grid-cols-2 gap-2 px-4 py-3" style={{ borderTop: "1px solid #f1f5f9" }}>
              <button
                type="button"
                disabled={isOwnPublication}
                onClick={() => {
                  if (!isOwnPublication) onRequestPublication(toRequestInitialValues(publication, categoryName));
                }}
                className={`servify-action-button flex items-center justify-center gap-1.5 py-2.5 rounded-xl transition-all active:scale-95 ${
                  isOwnPublication ? "servify-action-muted" : "servify-action-primary"
                }`}
                style={{ fontSize: 12, fontWeight: 800 }}
              >
                <Send size={14} strokeWidth={2} />
                {isOwnPublication ? "Tu servicio" : "Solicitar"}
              </button>
              <button
                type="button"
                onClick={() => openProviderProfile(publication)}
                disabled={openingProviderId === publication.id}
                className="servify-action-button servify-action-teal flex items-center justify-center gap-1.5 py-2.5 rounded-xl transition-all active:scale-95"
                style={{ fontSize: 12, fontWeight: 800, opacity: openingProviderId === publication.id ? 0.72 : 1 }}
              >
                <UserRound size={14} strokeWidth={2} />
                {openingProviderId === publication.id ? "Abriendo..." : "Ver perfil"}
              </button>
            </div>
          </article>
          );
        })}
      </div>
    </div>
  );
}

function Info({ icon, label }: { icon: React.ReactNode; label: string }) {
  return (
    <div className="flex items-center gap-1.5">
      {icon}
      <span style={{ fontSize: 12, color: "#64748b", fontWeight: 600 }}>{label}</span>
    </div>
  );
}

async function buildPublicProvider(userId: string): Promise<ApiPublicProvider> {
  const [account, profile, publications, rating] = await Promise.all([
    servifyApi.getAccountConfig(userId).catch(() => null),
    servifyApi.getUserProfile(userId).catch(() => null),
    servifyApi.listUserPublications(userId).catch(() => []),
    servifyApi.getUserRatingSummary(userId).catch(() => emptyRating(userId)),
  ]);
  const activePublications = publications.filter((publication) => (publication.estado ?? "").toUpperCase() === "ACTIVA");
  const zonasCobertura = uniqueValues(activePublications.flatMap(publicationAreas));
  const prices = activePublications
    .map((publication) => publication.precioBase)
    .filter((price): price is number => Boolean(price && price > 0))
    .sort((a, b) => a - b);

  return {
    usuarioId: userId,
    nombreUsuario: account?.usuario.nombreUsuario ?? `prestador-${userId.slice(0, 6)}`,
    nombre: profile?.nombre,
    apellido: profile?.apellido,
    fotoPerfilUrl: servifyApi.getStoredProfilePhoto(userId) || profile?.fotoPerfilUrl,
    descripcionPersonal: profile?.descripcionPersonal,
    localidad: profile?.ubicacion?.localidad ?? profile?.ubicacion?.ciudad,
    cantidadPublicacionesActivas: activePublications.length,
    categorias: uniqueValues(activePublications.map((publication) => publication.categoriaServicio?.nombre ?? "")),
    servicios: activePublications.map((publication) => publication.titulo).filter(Boolean),
    zonasCobertura,
    precioDesde: prices[0],
    publicacionesActivas: activePublications.map((publication) => ({
      id: publication.id,
      titulo: publication.titulo,
      descripcion: publication.descripcion,
      categoria: publication.categoriaServicio?.nombre,
      modalidadServicio: publication.modalidadServicio,
      zonasCobertura: publicationAreas(publication),
      precioBase: publication.precioBase,
    })),
    cantidadValoraciones: rating.cantidadValoraciones,
    promedioEstrellas: rating.promedioEstrellas,
  };
}

function toRequestInitialValues(publication: ApiPublication, categoryName: string): NewRequestInitialValues {
  const availability = publication.disponibilidadesHorarias?.[0];
  const title = publication.titulo || "Servicio";

  return {
    title: `Solicitud para ${title}`,
    description: publication.descripcion
      ? `Me interesa solicitar este servicio publicado: ${publication.descripcion}`
      : `Me interesa solicitar el servicio ${title}.`,
    category: normalizeCategoryName(publication.categoriaServicio?.nombre ?? categoryName) ?? categoryName,
    modality: fromApiModality(publication.modalidadServicio),
    location: firstPublicationArea(publication),
    price: publication.precioBase ? String(publication.precioBase) : "",
    availabilityDay: availability?.diaSemana,
    availabilityFrom: availability?.horaDesde?.slice(0, 5),
    availabilityTo: availability?.horaHasta?.slice(0, 5),
  };
}

function normalizeCategoryName(category?: string): string | undefined {
  const raw = category?.trim();
  if (!raw || raw.toLowerCase().startsWith("sin categor")) return undefined;

  const key = raw
    .replace(/tÃ©cnico/gi, "tecnico")
    .replace(/diseÃ±o/gi, "diseno")
    .replace(/fotografÃ­a/gi, "fotografia")
    .replace(/Ã¡/g, "a")
    .replace(/Ã©/g, "e")
    .replace(/Ã­/g, "i")
    .replace(/Ã³/g, "o")
    .replace(/Ãº/g, "u")
    .replace(/Ã±/g, "n")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/[^a-zA-Z0-9]/g, "")
    .toLowerCase();

  if (key === "soportetecnico") return "Soporte tecnico";
  if (key === "diseno") return "Diseno";
  if (key === "fotografia") return "Fotografia";
  return raw;
}

function formatAreas(publication: ApiPublication) {
  const unique = publicationAreas(publication);
  if (unique.length <= 2) return unique.join(", ");
  return `${unique.slice(0, 2).join(", ")} +${unique.length - 2}`;
}

function publicationAreas(publication: ApiPublication): string[] {
  const areas = (publication.zonasCobertura?.length ? publication.zonasCobertura : publication.ubicacion ? [publication.ubicacion] : [])
    .map((zona) => zona.localidad || zona.ciudad || "CABA")
    .filter(Boolean);
  return uniqueValues(areas.length ? areas : ["CABA"]);
}

function firstPublicationArea(publication: ApiPublication): string {
  return publicationAreas(publication)[0] ?? "CABA";
}

function formatAvailability(publication: ApiPublication) {
  const availability = publication.disponibilidadesHorarias?.[0];
  if (!availability) return "Horario a coordinar";
  const day = WEEK_DAYS.find((item) => item.value === availability.diaSemana)?.label ?? availability.diaSemana;
  return `${day} ${availability.horaDesde.slice(0, 5)}-${availability.horaHasta.slice(0, 5)}`;
}

function uniqueValues(values: string[]): string[] {
  return Array.from(new Set(values.map((value) => value.trim()).filter(Boolean)));
}
