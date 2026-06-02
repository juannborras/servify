import { useEffect, useState } from "react";
import type React from "react";
import { ArrowLeft, Clock, DollarSign, MapPin, SearchX } from "lucide-react";
import { WEEK_DAYS, formatMoney, fromApiModality, servifyApi, type ApiPublication } from "../api";

interface CategoryPublicationsScreenProps {
  categoryName: string;
  onBack: () => void;
}

export function CategoryPublicationsScreen({ categoryName, onBack }: CategoryPublicationsScreenProps) {
  const [publications, setPublications] = useState<ApiPublication[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

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

  return (
    <div className="flex flex-col h-full" style={{ background: "#f8fafc" }}>
      <div className="px-5 pt-12 pb-5 bg-white">
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
          <div className="flex flex-col items-center justify-center py-20 gap-4 text-center">
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

        {publications.map((publication) => (
          <article
            key={publication.id}
            className="bg-white rounded-2xl overflow-hidden"
            style={{ border: "1px solid rgba(0,0,0,0.06)", boxShadow: "0 1px 4px rgba(0,0,0,0.04)" }}
          >
            <div className="p-4">
              <div className="flex items-start justify-between gap-3">
                <div>
                  <h2 style={{ fontSize: 16, fontWeight: 800, color: "#0f172a", lineHeight: 1.25 }}>{publication.titulo}</h2>
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
          </article>
        ))}
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

function formatAreas(publication: ApiPublication) {
  const areas = (publication.zonasCobertura?.length ? publication.zonasCobertura : publication.ubicacion ? [publication.ubicacion] : [])
    .map((zona) => zona.localidad || zona.ciudad || "CABA")
    .filter(Boolean);
  const unique = Array.from(new Set(areas.length ? areas : ["CABA"]));
  if (unique.length <= 2) return unique.join(", ");
  return `${unique.slice(0, 2).join(", ")} +${unique.length - 2}`;
}

function formatAvailability(publication: ApiPublication) {
  const availability = publication.disponibilidadesHorarias?.[0];
  if (!availability) return "Horario a coordinar";
  const day = WEEK_DAYS.find((item) => item.value === availability.diaSemana)?.label ?? availability.diaSemana;
  return `${day} ${availability.horaDesde.slice(0, 5)}-${availability.horaHasta.slice(0, 5)}`;
}
