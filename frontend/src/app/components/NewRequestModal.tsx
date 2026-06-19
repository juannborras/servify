import React, { useEffect, useState } from "react";
import { X, FileText, AlignLeft, MapPin, DollarSign, Clock, CheckCircle } from "lucide-react";
import { motion } from "motion/react";
import { LOCATION_OPTIONS, TIME_OPTIONS, WEEK_DAYS, servifyApi } from "../api";

const categories = [
  "Oficios", "Clases particulares", "Soporte tecnico", "Limpieza",
  "Diseno", "Reparaciones", "Fotografia", "Salud y bienestar", "Otro",
];

interface NewRequestModalProps {
  userId?: string;
  initialValues?: NewRequestInitialValues;
  onClose: () => void;
  onCreated: () => void;
}

export interface NewRequestInitialValues {
  title?: string;
  description?: string;
  category?: string;
  modality?: string;
  location?: string;
  availabilityDay?: string;
  availabilityFrom?: string;
  availabilityTo?: string;
  price?: string;
}

export function NewRequestModal({ userId, initialValues, onClose, onCreated }: NewRequestModalProps) {
  const [title, setTitle] = useState(initialValues?.title ?? "");
  const [description, setDescription] = useState(initialValues?.description ?? "");
  const [category, setCategory] = useState<string | null>(initialValues?.category ?? null);
  const [modality, setModality] = useState<string | null>(initialValues?.modality ?? null);
  const [location, setLocation] = useState(initialValues?.location ?? LOCATION_OPTIONS[0]);
  const [availabilityDay, setAvailabilityDay] = useState(initialValues?.availabilityDay ?? WEEK_DAYS[0].value);
  const [availabilityFrom, setAvailabilityFrom] = useState(initialValues?.availabilityFrom ?? "09:00");
  const [availabilityTo, setAvailabilityTo] = useState(initialValues?.availabilityTo ?? "18:00");
  const [price, setPrice] = useState(initialValues?.price ?? "");
  const [done, setDone] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const canCreate = Boolean(userId && title && description && category);

  useEffect(() => {
    setTitle(initialValues?.title ?? "");
    setDescription(initialValues?.description ?? "");
    setCategory(initialValues?.category ?? null);
    setModality(initialValues?.modality ?? null);
    setLocation(initialValues?.location ?? LOCATION_OPTIONS[0]);
    setAvailabilityDay(initialValues?.availabilityDay ?? WEEK_DAYS[0].value);
    setAvailabilityFrom(initialValues?.availabilityFrom ?? "09:00");
    setAvailabilityTo(initialValues?.availabilityTo ?? "18:00");
    setPrice(initialValues?.price ?? "");
  }, [initialValues]);

  const handleCreate = async () => {
    if (!canCreate) return;
    setLoading(true);
    setError("");
    try {
      await servifyApi.createServiceRequest({
        solicitanteId: userId!,
        categoria: category!,
        descripcion: title ? `${title}. ${description}` : description,
        modalidad: modality ?? "Presencial",
        localidad: location,
        precio: price,
        disponibilidadDia: availabilityDay,
        horaDesde: availabilityFrom,
        horaHasta: availabilityTo,
      });
      setDone(true);
      setTimeout(() => {
        onCreated();
        onClose();
      }, 1200);
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo publicar la solicitud");
    } finally {
      setLoading(false);
    }
  };

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      className="absolute inset-0 flex items-end z-50"
      style={{ background: "rgba(0,0,0,0.5)" }}
      onClick={onClose}
    >
      <motion.div
        initial={{ y: "100%" }}
        animate={{ y: 0 }}
        exit={{ y: "100%" }}
        transition={{ type: "spring", damping: 28, stiffness: 280 }}
        className="w-full bg-white rounded-t-3xl max-h-[92%] flex flex-col"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Handle */}
        <div className="flex flex-col items-center pt-3 pb-2 shrink-0">
          <div className="rounded-full" style={{ width: 40, height: 4, background: "#e2e8f0" }} />
        </div>

        <div className="flex items-center justify-between px-6 pb-4 shrink-0">
          <div>
            <p style={{ fontSize: 19, fontWeight: 800, color: "#0f172a" }}>
              {initialValues ? "Repetir solicitud" : "Nueva solicitud"}
            </p>
            <p style={{ fontSize: 13, color: "#64748b" }}>
              {initialValues ? "Revisá los datos y publicala de nuevo" : "Describí lo que necesitás"}
            </p>
          </div>
          <button onClick={onClose}>
            <X size={22} color="#94a3b8" strokeWidth={1.8} />
          </button>
        </div>

        <div className="overflow-y-auto px-6 pb-8 flex flex-col gap-4">
          {!done ? (
            <>
              {!userId && (
                <p className="rounded-2xl px-4 py-3" style={{ background: "#fef2f2", color: "#b91c1c", fontSize: 13, fontWeight: 700 }}>
                  Inicia sesion para crear solicitudes.
                </p>
              )}
              {error && (
                <p className="rounded-2xl px-4 py-3" style={{ background: "#fef2f2", color: "#b91c1c", fontSize: 13, fontWeight: 700 }}>
                  {error}
                </p>
              )}
              <Field label="¿Qué necesitás?" icon={<FileText size={15} color="#0891b2" strokeWidth={1.8} />}>
                <input
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  placeholder="Ej: Plomero para arreglar canilla"
                  className="w-full bg-transparent outline-none"
                  style={{ fontSize: 14, color: "#0f172a" }}
                />
              </Field>

              <Field label="Descripción" icon={<AlignLeft size={15} color="#0891b2" strokeWidth={1.8} />}>
                <textarea
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  placeholder="Describí el problema o lo que necesitás con más detalle..."
                  rows={3}
                  className="w-full bg-transparent outline-none resize-none"
                  style={{ fontSize: 14, color: "#0f172a" }}
                />
              </Field>

              <div>
                <p style={{ fontSize: 13, fontWeight: 700, color: "#475569", marginBottom: 8 }}>Categoría</p>
                <div className="flex flex-wrap gap-2">
                  {categories.map((cat) => {
                    const sel = category === cat;
                    return (
                      <button
                        key={cat}
                        onClick={() => setCategory(sel ? null : cat)}
                        className="px-3 py-1.5 rounded-full transition-all"
                        style={{
                          background: sel ? "#0891b2" : "#f1f5f9",
                          color: sel ? "white" : "#475569",
                          fontWeight: sel ? 700 : 500,
                          fontSize: 12,
                        }}
                      >
                        {cat}
                      </button>
                    );
                  })}
                </div>
              </div>

              <div>
                <p style={{ fontSize: 13, fontWeight: 700, color: "#475569", marginBottom: 8 }}>Modalidad</p>
                <div className="flex gap-2">
                  {["Presencial", "Virtual", "Ambas"].map((m) => {
                    const sel = modality === m;
                    return (
                      <button
                        key={m}
                        onClick={() => setModality(sel ? null : m)}
                        className="px-4 py-2 rounded-full transition-all"
                        style={{
                          background: sel ? "#2563eb" : "#f1f5f9",
                          color: sel ? "white" : "#475569",
                          fontWeight: sel ? 700 : 500,
                          fontSize: 13,
                        }}
                      >
                        {m}
                      </button>
                    );
                  })}
                </div>
              </div>

              <Field label="Localidad del servicio" icon={<MapPin size={15} color="#ef4444" strokeWidth={1.8} />}>
                <select
                  value={location}
                  onChange={(e) => setLocation(e.target.value)}
                  className="w-full bg-transparent outline-none"
                  style={{ fontSize: 14, color: "#0f172a" }}
                >
                  {LOCATION_OPTIONS.map((option) => (
                    <option key={option} value={option}>{option}</option>
                  ))}
                </select>
              </Field>

              <Field label="Disponibilidad fija" icon={<Clock size={15} color="#7c3aed" strokeWidth={1.8} />}>
                <div className="grid grid-cols-3 gap-2">
                  <select
                    value={availabilityDay}
                    onChange={(e) => setAvailabilityDay(e.target.value)}
                    className="bg-transparent outline-none min-w-0"
                    style={{ fontSize: 13, color: "#0f172a" }}
                  >
                    {WEEK_DAYS.map((day) => (
                      <option key={day.value} value={day.value}>{day.label}</option>
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
              </Field>

              <Field label="Precio sugerido (opcional)" icon={<DollarSign size={15} color="#2563eb" strokeWidth={1.8} />}>
                <input
                  value={price}
                  onChange={(e) => setPrice(e.target.value)}
                  placeholder="Ej: $8.000 – $12.000"
                  className="w-full bg-transparent outline-none"
                  style={{ fontSize: 14, color: "#0f172a" }}
                />
              </Field>

              <button
                onClick={handleCreate}
                className="w-full py-3.5 rounded-2xl transition-all active:scale-95"
                style={{
                  background: canCreate ? "#2563eb" : "#cbd5e1",
                  color: "white",
                  fontWeight: 700,
                  fontSize: 15,
                  marginTop: 4,
                }}
              >
                {loading ? "Publicando..." : "Publicar solicitud"}
              </button>
            </>
          ) : (
            <div className="flex flex-col items-center gap-4 py-8">
              <motion.div
                initial={{ scale: 0 }}
                animate={{ scale: 1 }}
                transition={{ type: "spring", damping: 15 }}
                className="flex items-center justify-center rounded-full"
                style={{ width: 72, height: 72, background: "#f0fdf4" }}
              >
                <CheckCircle size={36} color="#16a34a" strokeWidth={1.8} />
              </motion.div>
              <div className="text-center">
                <p style={{ fontSize: 18, fontWeight: 800, color: "#0f172a" }}>¡Solicitud publicada!</p>
                <p style={{ fontSize: 13, color: "#64748b", marginTop: 4 }}>
                  Los prestadores podrán ver y enviar propuestas
                </p>
              </div>
            </div>
          )}
        </div>
      </motion.div>
    </motion.div>
  );
}

function Field({ label, icon, children }: { label: string; icon: React.ReactNode; children: React.ReactNode }) {
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
