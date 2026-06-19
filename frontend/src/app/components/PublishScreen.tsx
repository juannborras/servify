import React, { useEffect, useState } from "react";
import { motion } from "motion/react";
import { FileText, AlignLeft, MapPin, DollarSign, Clock, CheckCircle } from "lucide-react";
import { LOCATION_OPTIONS, TIME_OPTIONS, WEEK_DAYS, servifyApi } from "../api";

const categories = [
  "Oficios", "Clases particulares", "Soporte tecnico", "Limpieza",
  "Diseno", "Reparaciones", "Fotografia", "Salud y bienestar", "Otro",
];
const modalities = ["Presencial", "Virtual", "Ambas"];
const PUBLISH_DRAFT_PREFIX = "servify.publish-draft.";

interface PublishScreenProps {
  userId?: string;
  onPublished: () => void;
}

export function PublishScreen({ userId, onPublished }: PublishScreenProps) {
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [selectedCategory, setSelectedCategory] = useState<string | null>(null);
  const [selectedModality, setSelectedModality] = useState<string | null>(null);
  const [price, setPrice] = useState("");
  const [selectedAreas, setSelectedAreas] = useState<string[]>([LOCATION_OPTIONS[0]]);
  const [address, setAddress] = useState("");
  const [availabilityDayFrom, setAvailabilityDayFrom] = useState(WEEK_DAYS[0].value);
  const [availabilityDayTo, setAvailabilityDayTo] = useState(WEEK_DAYS[4].value);
  const [availabilityFrom, setAvailabilityFrom] = useState("09:00");
  const [availabilityTo, setAvailabilityTo] = useState("18:00");
  const [published, setPublished] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [draftLoaded, setDraftLoaded] = useState(false);

  const draftKey = userId ? `${PUBLISH_DRAFT_PREFIX}${userId}` : "";
  const canPublish = Boolean(
    userId && title && description && selectedCategory && selectedModality && price && selectedAreas.length > 0
  );

  useEffect(() => {
    setDraftLoaded(false);
    if (!userId) {
      setDraftLoaded(true);
      return;
    }

    const raw = localStorage.getItem(`${PUBLISH_DRAFT_PREFIX}${userId}`);
    if (raw) {
      try {
        const draft = JSON.parse(raw) as {
          title?: string;
          description?: string;
          selectedCategory?: string | null;
          selectedModality?: string | null;
          price?: string;
          selectedAreas?: string[];
          address?: string;
          availabilityDayFrom?: string;
          availabilityDayTo?: string;
          availabilityFrom?: string;
          availabilityTo?: string;
        };
        setTitle(draft.title ?? "");
        setDescription(draft.description ?? "");
        setSelectedCategory(draft.selectedCategory ?? null);
        setSelectedModality(draft.selectedModality ?? null);
        setPrice(draft.price ?? "");
        setSelectedAreas(draft.selectedAreas?.length ? draft.selectedAreas : [LOCATION_OPTIONS[0]]);
        setAddress(draft.address ?? "");
        setAvailabilityDayFrom(draft.availabilityDayFrom ?? WEEK_DAYS[0].value);
        setAvailabilityDayTo(draft.availabilityDayTo ?? WEEK_DAYS[4].value);
        setAvailabilityFrom(draft.availabilityFrom ?? "09:00");
        setAvailabilityTo(draft.availabilityTo ?? "18:00");
      } catch {
        localStorage.removeItem(`${PUBLISH_DRAFT_PREFIX}${userId}`);
      }
    }
    setDraftLoaded(true);
  }, [userId]);

  useEffect(() => {
    if (!draftKey || !draftLoaded || published) return;
    localStorage.setItem(
      draftKey,
      JSON.stringify({
        title,
        description,
        selectedCategory,
        selectedModality,
        price,
        selectedAreas,
        address,
        availabilityDayFrom,
        availabilityDayTo,
        availabilityFrom,
        availabilityTo,
      })
    );
  }, [
    address,
    availabilityDayFrom,
    availabilityDayTo,
    availabilityFrom,
    availabilityTo,
    description,
    draftKey,
    draftLoaded,
    price,
    published,
    selectedAreas,
    selectedCategory,
    selectedModality,
    title,
  ]);

  const toggleArea = (area: string) => {
    setSelectedAreas((current) => {
      if (current.includes(area)) {
        return current.length === 1 ? current : current.filter((item) => item !== area);
      }
      return [...current, area];
    });
  };

  const handlePublish = async () => {
    if (!canPublish) return;
    setLoading(true);
    setError("");
    try {
      await servifyApi.createPublication({
        usuarioId: userId!,
        categoria: selectedCategory!,
        titulo: title,
        descripcion: description,
        modalidad: selectedModality!,
        localidades: selectedAreas,
        direccion: address,
        precio: price,
        disponibilidadDiaDesde: availabilityDayFrom,
        disponibilidadDiaHasta: availabilityDayTo,
        horaDesde: availabilityFrom,
        horaHasta: availabilityTo,
      });
      if (draftKey) {
        localStorage.removeItem(draftKey);
      }
      setPublished(true);
      setTimeout(() => {
        setPublished(false);
        onPublished();
      }, 1200);
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo publicar el servicio");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex flex-col h-full" style={{ background: "#f8fafc" }}>
      {/* Header */}
      <div className="px-5 pt-12 pb-5 bg-white">
        <h1 style={{ fontSize: 24, fontWeight: 800, color: "#0f172a" }}>Publicar servicio</h1>
        <p style={{ fontSize: 14, color: "#64748b", marginTop: 3, fontWeight: 500 }}>
          Ofrecé tus habilidades a la comunidad
        </p>
      </div>

      {/* Form */}
      <div className="flex-1 overflow-y-auto px-5 pt-5 pb-8 flex flex-col gap-5">
        {!userId && (
          <p className="rounded-2xl px-4 py-3" style={{ background: "#fef2f2", color: "#b91c1c", fontSize: 13, fontWeight: 700 }}>
            Inicia sesion para publicar servicios.
          </p>
        )}
        {error && (
          <p className="rounded-2xl px-4 py-3" style={{ background: "#fef2f2", color: "#b91c1c", fontSize: 13, fontWeight: 700 }}>
            {error}
          </p>
        )}
        <FormField label="Título del servicio" icon={<FileText size={16} color="#0891b2" strokeWidth={1.8} />}>
          <input
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="Ej: Plomería y reparaciones generales"
            className="w-full bg-transparent outline-none"
            style={{ fontSize: 14, color: "#0f172a" }}
          />
        </FormField>

        <FormField label="Descripción" icon={<AlignLeft size={16} color="#0891b2" strokeWidth={1.8} />}>
          <textarea
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="Describí tu servicio, experiencia y lo que incluye..."
            rows={3}
            className="w-full bg-transparent outline-none resize-none"
            style={{ fontSize: 14, color: "#0f172a" }}
          />
        </FormField>

        {/* Category chips */}
        <div>
          <p style={{ fontSize: 13, fontWeight: 700, color: "#475569", marginBottom: 10 }}>
            Categoría
          </p>
          <div className="flex flex-wrap gap-2">
            {categories.map((cat) => {
              const sel = selectedCategory === cat;
              return (
                <button
                  key={cat}
                  onClick={() => setSelectedCategory(sel ? null : cat)}
                  className="px-3.5 py-2 rounded-full transition-all"
                  style={{
                    background: sel ? "#0891b2" : "#f1f5f9",
                    color: sel ? "white" : "#475569",
                    fontWeight: sel ? 700 : 500,
                    fontSize: 13,
                    border: sel ? "none" : "1.5px solid #e2e8f0",
                  }}
                >
                  {cat}
                </button>
              );
            })}
          </div>
        </div>

        {/* Modality chips */}
        <div>
          <p style={{ fontSize: 13, fontWeight: 700, color: "#475569", marginBottom: 10 }}>
            Modalidad
          </p>
          <div className="flex gap-2">
            {modalities.map((mod) => {
              const sel = selectedModality === mod;
              return (
                <button
                  key={mod}
                  onClick={() => setSelectedModality(sel ? null : mod)}
                  className="px-4 py-2 rounded-full transition-all"
                  style={{
                    background: sel ? "#2563eb" : "#f1f5f9",
                    color: sel ? "white" : "#475569",
                    fontWeight: sel ? 700 : 500,
                    fontSize: 13,
                    border: sel ? "none" : "1.5px solid #e2e8f0",
                  }}
                >
                  {mod}
                </button>
              );
            })}
          </div>
        </div>

        <FormField label="Precio base (ARS)" icon={<DollarSign size={16} color="#2563eb" strokeWidth={1.8} />}>
          <input
            value={price}
            onChange={(e) => setPrice(e.target.value)}
            placeholder="Ej: 8.000"
            type="number"
            className="w-full bg-transparent outline-none"
            style={{ fontSize: 14, color: "#0f172a" }}
          />
        </FormField>

        <FormField label="Areas de trabajo deseadas" icon={<MapPin size={16} color="#ef4444" strokeWidth={1.8} />}>
          <div className="grid grid-cols-2 gap-2 max-h-44 overflow-y-auto pr-1">
            {LOCATION_OPTIONS.map((option) => {
              const selected = selectedAreas.includes(option);
              return (
                <button
                  key={option}
                  type="button"
                  onClick={() => toggleArea(option)}
                  className="px-3 py-2 rounded-xl text-left transition-all"
                  style={{
                    background: selected ? "#eff6ff" : "#f8fafc",
                    border: selected ? "1.5px solid #2563eb" : "1px solid #e2e8f0",
                    color: selected ? "#1d4ed8" : "#475569",
                    fontSize: 12,
                    fontWeight: 700,
                  }}
                >
                  {option}
                </button>
              );
            })}
          </div>
        </FormField>

        <FormField label="Dirección exacta (opcional)" icon={<MapPin size={16} color="#94a3b8" strokeWidth={1.8} />}>
          <input
            value={address}
            onChange={(e) => setAddress(e.target.value)}
            placeholder="Ej: Av. Santa Fe 1234, Piso 3"
            className="w-full bg-transparent outline-none"
            style={{ fontSize: 14, color: "#0f172a" }}
          />
        </FormField>

        <FormField label="Disponibilidad fija" icon={<Clock size={16} color="#7c3aed" strokeWidth={1.8} />}>
          <input
            value={`${availabilityDayFrom}-${availabilityDayTo} ${availabilityFrom}-${availabilityTo}`}
            readOnly
            placeholder="Ej: Lun–Vie 9:00–18:00"
            className="hidden"
            style={{ fontSize: 14, color: "#0f172a" }}
          />
          <div className="grid grid-cols-2 gap-2">
            <select
              value={availabilityDayFrom}
              onChange={(e) => setAvailabilityDayFrom(e.target.value)}
              className="bg-transparent outline-none min-w-0"
              style={{ fontSize: 13, color: "#0f172a" }}
            >
              {WEEK_DAYS.map((day) => (
                <option key={day.value} value={day.value}>Desde {day.label}</option>
              ))}
            </select>
            <select
              value={availabilityDayTo}
              onChange={(e) => setAvailabilityDayTo(e.target.value)}
              className="bg-transparent outline-none min-w-0"
              style={{ fontSize: 13, color: "#0f172a" }}
            >
              {WEEK_DAYS.map((day) => (
                <option key={day.value} value={day.value}>Hasta {day.label}</option>
              ))}
            </select>
            <select
              value={availabilityFrom}
              onChange={(e) => setAvailabilityFrom(e.target.value)}
              className="bg-transparent outline-none min-w-0"
              style={{ fontSize: 13, color: "#0f172a" }}
            >
              {TIME_OPTIONS.map((time) => (
                <option key={time} value={time}>{time}</option>
              ))}
            </select>
            <select
              value={availabilityTo}
              onChange={(e) => setAvailabilityTo(e.target.value)}
              className="bg-transparent outline-none min-w-0"
              style={{ fontSize: 13, color: "#0f172a" }}
            >
              {TIME_OPTIONS.map((time) => (
                <option key={time} value={time}>{time}</option>
              ))}
            </select>
          </div>
        </FormField>

        {/* Publish button */}
        <motion.button
          whileTap={canPublish ? { scale: 0.97 } : {}}
          onClick={handlePublish}
          className="w-full py-4 rounded-2xl mt-2 flex items-center justify-center gap-2 transition-all"
          style={{
            background: canPublish ? "#2563eb" : "#cbd5e1",
            color: "white",
            fontWeight: 700,
            fontSize: 16,
          }}
        >
          {published ? (
            <>
              <CheckCircle size={20} strokeWidth={2} />
              ¡Servicio publicado!
            </>
          ) : (
            loading ? "Publicando..." : "Publicar servicio"
          )}
        </motion.button>
      </div>
    </div>
  );
}

function FormField({
  label,
  icon,
  children,
}: {
  label: string;
  icon: React.ReactNode;
  children: React.ReactNode;
}) {
  return (
    <div>
      <p style={{ fontSize: 13, fontWeight: 700, color: "#475569", marginBottom: 8 }}>{label}</p>
      <div
        className="flex items-start gap-3 px-4 py-3.5 rounded-2xl bg-white"
        style={{ border: "1.5px solid #e2e8f0" }}
      >
        <div className="mt-0.5">{icon}</div>
        <div className="flex-1">{children}</div>
      </div>
    </div>
  );
}
