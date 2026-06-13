import { useEffect, useMemo, useState, type ReactNode } from "react";
import { ArrowLeft, Briefcase, MapPin, Menu, Star, UserRound } from "lucide-react";
import {
  formatMoney,
  fromApiModality,
  servifyApi,
  type ApiPublicProvider,
  type ApiPublicProviderPublication,
  type ApiRatingSummary,
  type SessionUser,
} from "../api";

interface PublicProfileScreenProps {
  user?: SessionUser | null;
  provider?: ApiPublicProvider | null;
  ownProfile?: boolean;
  onBack?: () => void;
  onOpenSettings?: () => void;
}

const emptyRating = (usuarioId: string): ApiRatingSummary => ({
  usuarioId,
  cantidadValoraciones: 0,
  promedioEstrellas: 0,
});

export function PublicProfileScreen({ user, provider, ownProfile = false, onBack, onOpenSettings }: PublicProfileScreenProps) {
  const [ownProvider, setOwnProvider] = useState<ApiPublicProvider | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!ownProfile || !user) return;

    let cancelled = false;
    setLoading(true);

    Promise.all([
      servifyApi.getAccountConfig(user.id).catch(() => null),
      servifyApi.getUserProfile(user.id).catch(() => null),
      servifyApi.listUserPublications(user.id).catch(() => []),
      servifyApi.getUserRatingSummary(user.id).catch(() => emptyRating(user.id)),
    ])
      .then(([account, profile, publications, rating]) => {
        if (cancelled) return;
        const activePublications = publications.filter((publication) => (publication.estado ?? "").toUpperCase() === "ACTIVA");
        const publicProvider: ApiPublicProvider = {
          usuarioId: user.id,
          nombreUsuario: account?.usuario.nombreUsuario ?? user.username ?? "",
          nombre: profile?.nombre ?? user.name.split(" ")[0],
          apellido: profile?.apellido ?? user.name.split(" ").slice(1).join(" "),
          fotoPerfilUrl: servifyApi.getStoredProfilePhoto(user.id) || profile?.fotoPerfilUrl,
          descripcionPersonal: profile?.descripcionPersonal,
          localidad: profile?.ubicacion?.localidad,
          cantidadPublicacionesActivas: activePublications.length,
          categorias: uniqueValues(activePublications.map((publication) => publication.categoriaServicio?.nombre ?? "")),
          servicios: activePublications.map((publication) => publication.titulo).filter(Boolean),
          zonasCobertura: uniqueValues(activePublications.flatMap((publication) =>
            (publication.zonasCobertura?.length ? publication.zonasCobertura : publication.ubicacion ? [publication.ubicacion] : [])
              .map((zona) => zona.localidad || zona.ciudad || "")
          )),
          precioDesde: activePublications
            .map((publication) => publication.precioBase)
            .filter((price): price is number => Boolean(price && price > 0))
            .sort((a, b) => a - b)[0],
          publicacionesActivas: activePublications.map((publication) => ({
            id: publication.id,
            titulo: publication.titulo,
            descripcion: publication.descripcion,
            categoria: publication.categoriaServicio?.nombre,
            modalidadServicio: publication.modalidadServicio,
            zonasCobertura: uniqueValues((publication.zonasCobertura?.length ? publication.zonasCobertura : publication.ubicacion ? [publication.ubicacion] : [])
              .map((zona) => zona.localidad || zona.ciudad || "")),
            precioBase: publication.precioBase,
          })),
          cantidadValoraciones: rating.cantidadValoraciones,
          promedioEstrellas: rating.promedioEstrellas,
        };
        setOwnProvider(publicProvider);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [ownProfile, user]);

  const profile = ownProfile ? ownProvider : provider;
  const displayName = formatProviderName(profile) || (ownProfile ? user?.name : "") || (profile?.nombreUsuario ? `@${profile.nombreUsuario}` : "Perfil publico");
  const publications = useMemo(() => profile?.publicacionesActivas ?? [], [profile]);

  if (!profile && loading) {
    return (
      <div className="flex h-full items-center justify-center" style={{ background: "#f8fafc" }}>
        <p style={{ color: "#64748b", fontSize: 14, fontWeight: 700 }}>Cargando perfil...</p>
      </div>
    );
  }

  if (!profile) {
    return (
      <div className="flex flex-col h-full items-center justify-center px-8 text-center gap-4" style={{ background: "#f8fafc" }}>
        <UserRound size={40} color="#94a3b8" strokeWidth={1.6} />
        <p style={{ color: "#0f172a", fontSize: 18, fontWeight: 800 }}>Perfil no disponible</p>
        {onBack ? (
          <button onClick={onBack} className="px-5 py-2.5 rounded-xl" style={{ background: "#2563eb", color: "white", fontWeight: 800 }}>
            Volver
          </button>
        ) : null}
      </div>
    );
  }

  return (
    <div className="flex flex-col h-full" style={{ background: "#f8fafc" }}>
      <div className="px-5 pt-12 pb-6" style={{ background: "linear-gradient(160deg, #0f766e 0%, #0891b2 52%, #2563eb 100%)" }}>
        <div className="flex items-center justify-between mb-5">
          {onBack ? (
            <button onClick={onBack} className="flex items-center justify-center rounded-xl" style={{ width: 38, height: 38, background: "rgba(255,255,255,0.2)" }}>
              <ArrowLeft size={18} color="white" strokeWidth={2} />
            </button>
          ) : (
            <div />
          )}
          {ownProfile ? (
            <button onClick={onOpenSettings} className="flex items-center justify-center rounded-xl" style={{ width: 38, height: 38, background: "rgba(255,255,255,0.2)" }}>
              <Menu size={18} color="white" strokeWidth={2} />
            </button>
          ) : null}
        </div>

        <div className="flex items-start gap-4">
          <div className="flex items-center justify-center rounded-3xl overflow-hidden" style={{ width: 76, height: 76, background: "rgba(255,255,255,0.18)", border: "3px solid rgba(255,255,255,0.45)", flexShrink: 0 }}>
            {profile.fotoPerfilUrl ? (
              <img src={profile.fotoPerfilUrl} alt="" className="w-full h-full object-cover" />
            ) : (
              <UserRound size={34} color="white" strokeWidth={1.7} />
            )}
          </div>
          <div className="min-w-0 flex-1">
            <p style={{ color: "white", fontSize: 22, fontWeight: 900, lineHeight: 1.15 }}>{displayName}</p>
            {profile.nombreUsuario ? (
              <p style={{ color: "rgba(255,255,255,0.82)", fontSize: 13, fontWeight: 800, marginTop: 4 }}>@{profile.nombreUsuario}</p>
            ) : null}
            <div className="flex flex-wrap items-center gap-3 mt-3">
              <HeaderMetric icon={<Star size={14} color="white" fill="white" strokeWidth={1.8} />} label={ratingText(profile)} />
              <HeaderMetric icon={<Briefcase size={14} color="white" strokeWidth={1.8} />} label={`${profile.cantidadPublicacionesActivas} activos`} />
            </div>
          </div>
        </div>
      </div>

      <div className="flex-1 overflow-y-auto px-5 pt-5 pb-8 flex flex-col gap-4">
        <InfoBlock title="Informacion publica">
          <InfoLine label="Zona" value={formatAreas(profile)} />
          <InfoLine label="Categorias" value={profile.categorias?.length ? profile.categorias.join(", ") : "Sin categorias activas"} />
          <InfoLine label="Precio desde" value={profile.precioDesde ? formatMoney(profile.precioDesde) : "A convenir"} />
          <InfoLine label="Valoraciones" value={ratingText(profile)} />
        </InfoBlock>

        {profile.descripcionPersonal ? (
          <InfoBlock title="Presentacion">
            <p style={{ color: "#475569", fontSize: 13, lineHeight: 1.5 }}>{profile.descripcionPersonal}</p>
          </InfoBlock>
        ) : null}

        <div>
          <div className="flex items-center justify-between mb-3">
            <h2 style={{ color: "#0f172a", fontSize: 16, fontWeight: 900 }}>Publicaciones activas</h2>
            <span style={{ color: "#94a3b8", fontSize: 12, fontWeight: 800 }}>{publications.length}</span>
          </div>
          <div className="flex flex-col gap-3">
            {publications.length === 0 ? (
              <p className="rounded-2xl px-4 py-3" style={{ background: "white", color: "#64748b", fontSize: 13, fontWeight: 700 }}>
                Todavia no hay publicaciones activas.
              </p>
            ) : (
              publications.map((publication) => <PublicationCard key={publication.id} publication={publication} />)
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

function HeaderMetric({ icon, label }: { icon: ReactNode; label: string }) {
  return (
    <span className="flex items-center gap-1.5 rounded-full px-2.5 py-1" style={{ background: "rgba(255,255,255,0.16)", color: "white", fontSize: 12, fontWeight: 800 }}>
      {icon}
      {label}
    </span>
  );
}

function InfoBlock({ title, children }: { title: string; children: ReactNode }) {
  return (
    <section className="bg-white rounded-2xl p-4" style={{ border: "1px solid rgba(0,0,0,0.06)", boxShadow: "0 1px 4px rgba(0,0,0,0.04)" }}>
      <p style={{ color: "#94a3b8", fontSize: 11, fontWeight: 900, letterSpacing: "0.08em", textTransform: "uppercase", marginBottom: 12 }}>{title}</p>
      {children}
    </section>
  );
}

function InfoLine({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex items-center justify-between gap-3 py-1.5">
      <span style={{ color: "#64748b", fontSize: 13, fontWeight: 700 }}>{label}</span>
      <span style={{ color: "#0f172a", fontSize: 13, fontWeight: 800, textAlign: "right" }}>{value}</span>
    </div>
  );
}

function PublicationCard({ publication }: { publication: ApiPublicProviderPublication }) {
  return (
    <article className="bg-white rounded-2xl p-4" style={{ border: "1px solid rgba(0,0,0,0.06)", boxShadow: "0 1px 4px rgba(0,0,0,0.04)" }}>
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0">
          <p style={{ color: "#0f172a", fontSize: 15, fontWeight: 900, lineHeight: 1.25 }}>{publication.titulo}</p>
          <p style={{ color: "#0891b2", fontSize: 12, fontWeight: 800, marginTop: 4 }}>
            {[publication.categoria, publication.modalidadServicio ? fromApiModality(publication.modalidadServicio) : null].filter(Boolean).join(" - ")}
          </p>
        </div>
        <span style={{ color: "#2563eb", fontSize: 13, fontWeight: 900, whiteSpace: "nowrap" }}>
          {publication.precioBase ? formatMoney(publication.precioBase) : "A convenir"}
        </span>
      </div>
      {publication.descripcion ? (
        <p style={{ color: "#64748b", fontSize: 13, lineHeight: 1.45, marginTop: 9 }}>{publication.descripcion}</p>
      ) : null}
      <div className="flex items-center gap-1.5 mt-3">
        <MapPin size={13} color="#94a3b8" strokeWidth={1.8} />
        <span style={{ color: "#64748b", fontSize: 12, fontWeight: 700 }}>{publication.zonasCobertura?.length ? publication.zonasCobertura.join(", ") : "Zona a coordinar"}</span>
      </div>
    </article>
  );
}

function formatProviderName(provider?: ApiPublicProvider | null): string {
  if (!provider) return "";
  return [provider.nombre, provider.apellido].filter(Boolean).join(" ").trim();
}

function formatAreas(provider: ApiPublicProvider): string {
  const areas = provider.zonasCobertura?.length ? provider.zonasCobertura : provider.localidad ? [provider.localidad] : [];
  if (areas.length === 0) return "Zona no informada";
  if (areas.length <= 2) return areas.join(", ");
  return `${areas.slice(0, 2).join(", ")} +${areas.length - 2}`;
}

function ratingText(provider: ApiPublicProvider): string {
  if (!provider.cantidadValoraciones) return "Nuevo";
  return `${provider.promedioEstrellas.toFixed(1)} (${provider.cantidadValoraciones})`;
}

function uniqueValues(values: string[]): string[] {
  return Array.from(new Set(values.map((value) => value.trim()).filter(Boolean)));
}
