import React, { useEffect, useState } from "react";
import {
  Plus,
  MapPin,
  Clock,
  DollarSign,
  Pause,
  Trash2,
  Play,
  Edit3,
  X,
  Save,
  AlignLeft,
  FileText,
} from "lucide-react";
import { motion, AnimatePresence } from "motion/react";
import {
  LOCATION_OPTIONS,
  TIME_OPTIONS,
  WEEK_DAYS,
  formatMoney,
  fromApiModality,
  servifyApi,
  type ApiAvailability,
  type ApiPublication,
} from "../api";

const categories = [
  "Oficios", "Clases particulares", "Soporte tÃ©cnico", "Limpieza",
  "DiseÃ±o", "Reparaciones", "FotografÃ­a", "Salud y bienestar", "Otro",
];
const modalities = ["Presencial", "Virtual", "Ambas"];

interface Publication {
  id: number | string;
  title: string;
  category: string;
  description: string;
  price: string;
  modality: string;
  location: string;
  schedule: string;
  active: boolean;
  raw: ApiPublication;
}

interface EditForm {
  title: string;
  description: string;
  category: string;
  modality: string;
  price: string;
  areas: string[];
  address: string;
  availabilityDayFrom: string;
  availabilityDayTo: string;
  availabilityFrom: string;
  availabilityTo: string;
}

interface MyPublicationsProps {
  userId?: string;
  onNew: () => void;
}

export function MyPublications({ userId, onNew }: MyPublicationsProps) {
  const [pubs, setPubs] = useState<Publication[]>([]);
  const [deletingId, setDeletingId] = useState<number | string | null>(null);
  const [editing, setEditing] = useState<Publication | null>(null);
  const [editForm, setEditForm] = useState<EditForm | null>(null);
  const [savingEdit, setSavingEdit] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!userId) return;
    let ignore = false;
    servifyApi
      .listUserPublications(userId)
      .then((items) => {
        if (!ignore) setPubs(items.filter((item) => !isDeleted(item.estado)).map(mapPublication));
      })
      .catch((err) => {
        if (!ignore) setError(err instanceof Error ? err.message : "No se pudieron cargar las publicaciones");
      });
    return () => {
      ignore = true;
    };
  }, [userId]);

  const toggleActive = async (id: number | string) => {
    const current = pubs.find((p) => p.id === id);
    if (!current) return;
    setError("");
    setPubs((prev) => prev.map((p) => (p.id === id ? { ...p, active: !p.active } : p)));
    if (userId && typeof id === "string") {
      try {
        const updated = await servifyApi.changePublicationState(id, userId, !current.active);
        setPubs((prev) => prev.map((p) => (p.id === id ? mapPublication(updated) : p)));
      } catch (err) {
        setPubs((prev) => prev.map((p) => (p.id === id ? current : p)));
        setError(err instanceof Error ? err.message : "No se pudo cambiar el estado");
      }
    }
  };

  const handleDelete = async (id: number | string) => {
    if (!userId || typeof id !== "string") {
      setError("No se pudo identificar la publicacion para eliminarla.");
      return;
    }
    setError("");
    setDeletingId(id);
    try {
      await servifyApi.deletePublication(id, userId);
      setPubs((prev) => prev.filter((p) => p.id !== id));
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo eliminar la publicacion");
    } finally {
      setDeletingId(null);
    }
  };

  const openEdit = (publication: Publication) => {
    setError("");
    setEditing(publication);
    setEditForm(formFromPublication(publication.raw));
  };

  const closeEdit = () => {
    if (savingEdit) return;
    setEditing(null);
    setEditForm(null);
  };

  const toggleEditArea = (area: string) => {
    setEditForm((current) => {
      if (!current) return current;
      if (current.areas.includes(area)) {
        return { ...current, areas: current.areas.length === 1 ? current.areas : current.areas.filter((item) => item !== area) };
      }
      return { ...current, areas: [...current.areas, area] };
    });
  };

  const saveEdit = async () => {
    if (!userId || !editing || !editForm || typeof editing.id !== "string") return;
    setSavingEdit(true);
    setError("");
    try {
      const updated = await servifyApi.updatePublication(editing.id, {
        usuarioId: userId,
        categoria: editForm.category,
        titulo: editForm.title,
        descripcion: editForm.description,
        modalidad: editForm.modality,
        localidades: editForm.areas,
        direccion: editForm.address,
        precio: editForm.price,
        disponibilidadDiaDesde: editForm.availabilityDayFrom,
        disponibilidadDiaHasta: editForm.availabilityDayTo,
        horaDesde: editForm.availabilityFrom,
        horaHasta: editForm.availabilityTo,
      });
      setPubs((prev) => prev.map((p) => (p.id === editing.id ? mapPublication(updated) : p)));
      setEditing(null);
      setEditForm(null);
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo guardar la publicacion");
    } finally {
      setSavingEdit(false);
    }
  };

  const active = pubs.filter((p) => p.active).length;
  const canSaveEdit = Boolean(editForm?.title && editForm.description && editForm.category && editForm.modality && editForm.price);

  return (
    <div className="relative flex flex-col h-full" style={{ background: "#f8fafc" }}>
      <div className="px-5 pt-12 pb-5 bg-white">
        <div className="flex items-center justify-between mb-1">
          <h1 style={{ fontSize: 24, fontWeight: 800, color: "#0f172a" }}>Mis publicaciones</h1>
          <button
            onClick={onNew}
            className="flex items-center gap-1.5 px-4 py-2 rounded-xl transition-all active:scale-95"
            style={{ background: "#0891b2", color: "white", fontWeight: 700, fontSize: 13 }}
          >
            <Plus size={16} strokeWidth={2.5} />
            Nuevo
          </button>
        </div>
        <p style={{ fontSize: 13, color: "#64748b", fontWeight: 500 }}>
          {pubs.length} servicios publicados - {active} activos
        </p>
      </div>

      <div className="flex-1 overflow-y-auto px-5 pt-4 pb-6 flex flex-col gap-4">
        {error && (
          <p className="rounded-2xl px-4 py-3" style={{ background: "#fef2f2", color: "#b91c1c", fontSize: 13, fontWeight: 700 }}>
            {error}
          </p>
        )}
        <AnimatePresence>
          {pubs.map((pub, i) => (
            <motion.div
              key={pub.id}
              initial={{ opacity: 0, y: 8 }}
              animate={{ opacity: deletingId === pub.id ? 0 : 1, y: 0, scale: deletingId === pub.id ? 0.95 : 1 }}
              exit={{ opacity: 0, scale: 0.95 }}
              transition={{ duration: 0.3, delay: deletingId === pub.id ? 0 : i * 0.06 }}
              className="bg-white rounded-2xl overflow-hidden"
              style={{ border: "1px solid rgba(0,0,0,0.06)", boxShadow: "0 1px 4px rgba(0,0,0,0.04)" }}
            >
              <div className="p-4">
                <div className="flex items-start justify-between gap-2 mb-1.5">
                  <h3 style={{ fontWeight: 700, fontSize: 15, color: "#0f172a", flex: 1, lineHeight: 1.3 }}>
                    {pub.title}
                  </h3>
                  <span
                    className="px-2.5 py-1 rounded-full shrink-0"
                    style={{
                      background: pub.active ? "#f0fdf4" : "#f1f5f9",
                      color: pub.active ? "#16a34a" : "#94a3b8",
                      fontSize: 11,
                      fontWeight: 700,
                    }}
                  >
                    {pub.active ? "Activo" : "Pausado"}
                  </span>
                </div>

                <span
                  className="inline-block px-2.5 py-1 rounded-full mb-2"
                  style={{ background: "#ecfeff", color: "#0891b2", fontSize: 11, fontWeight: 700 }}
                >
                  {pub.category}
                </span>

                <p style={{ fontSize: 13, color: "#64748b", lineHeight: 1.5, marginBottom: 12 }}>
                  {pub.description}
                </p>

                <div className="flex flex-wrap gap-3">
                  <InfoItem icon={<DollarSign size={12} color="#2563eb" strokeWidth={2} />} label={pub.price} color="#2563eb" />
                  <InfoItem icon={<MapPin size={12} color="#94a3b8" strokeWidth={1.8} />} label={pub.location} />
                  <InfoItem icon={<Clock size={12} color="#94a3b8" strokeWidth={1.8} />} label={pub.schedule} />
                </div>
              </div>

              <div className="grid grid-cols-3 gap-2 px-4 py-3" style={{ borderTop: "1px solid #f1f5f9" }}>
                <button
                  onClick={() => toggleActive(pub.id)}
                  className="flex items-center justify-center gap-1.5 py-2.5 rounded-xl transition-all active:scale-95"
                  style={{
                    background: pub.active ? "#fff7ed" : "#f0fdf4",
                    color: pub.active ? "#d97706" : "#16a34a",
                    fontWeight: 700,
                    fontSize: 12,
                    border: `1.5px solid ${pub.active ? "#fed7aa" : "#bbf7d0"}`,
                  }}
                >
                  {pub.active ? <Pause size={14} strokeWidth={2} /> : <Play size={14} strokeWidth={2} />}
                  {pub.active ? "Pausar" : "Activar"}
                </button>
                <button
                  onClick={() => openEdit(pub)}
                  className="flex items-center justify-center gap-1.5 py-2.5 rounded-xl transition-all active:scale-95"
                  style={{ background: "#eff6ff", color: "#2563eb", fontWeight: 700, fontSize: 12, border: "1.5px solid #bfdbfe" }}
                >
                  <Edit3 size={14} strokeWidth={1.8} />
                  Editar
                </button>
                <button
                  onClick={() => handleDelete(pub.id)}
                  className="flex items-center justify-center gap-1.5 py-2.5 rounded-xl transition-all active:scale-95"
                  style={{ background: "#fef2f2", color: "#ef4444", fontWeight: 700, fontSize: 12, border: "1.5px solid #fecaca" }}
                >
                  <Trash2 size={14} strokeWidth={1.8} />
                  Eliminar
                </button>
              </div>
            </motion.div>
          ))}
        </AnimatePresence>

        {pubs.length === 0 && (
          <div className="flex flex-col items-center justify-center py-16 gap-4">
            <div className="flex items-center justify-center rounded-3xl" style={{ width: 72, height: 72, background: "#f1f5f9" }}>
              <FileText size={30} color="#64748b" strokeWidth={1.6} />
            </div>
            <div className="text-center">
              <p style={{ fontWeight: 700, fontSize: 16, color: "#0f172a" }}>Sin publicaciones</p>
              <p style={{ fontSize: 13, color: "#94a3b8", marginTop: 4 }}>
                Publica tu primer servicio para empezar a recibir pedidos
              </p>
            </div>
            <button
              onClick={onNew}
              className="px-6 py-3 rounded-2xl transition-all active:scale-95"
              style={{ background: "#2563eb", color: "white", fontWeight: 700, fontSize: 14 }}
            >
              Publicar servicio
            </button>
          </div>
        )}
      </div>

      <AnimatePresence>
        {editing && editForm ? (
          <motion.div
            initial={{ opacity: 0, y: 18 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: 18 }}
            className="absolute inset-0 z-40 flex flex-col"
            style={{ background: "#f8fafc" }}
          >
            <div className="px-5 pt-12 pb-5 bg-white">
              <div className="flex items-center justify-between">
                <div>
                  <p style={{ fontSize: 12, color: "#64748b", fontWeight: 700 }}>Publicacion</p>
                  <h2 style={{ fontSize: 22, fontWeight: 800, color: "#0f172a" }}>Editar servicio</h2>
                </div>
                <button
                  type="button"
                  onClick={closeEdit}
                  className="flex items-center justify-center rounded-xl"
                  style={{ width: 38, height: 38, background: "#f1f5f9" }}
                >
                  <X size={18} color="#475569" strokeWidth={2} />
                </button>
              </div>
            </div>

            <div className="flex-1 overflow-y-auto px-5 pt-5 pb-8 flex flex-col gap-5">
              <EditField label="Titulo del servicio" icon={<FileText size={16} color="#0891b2" strokeWidth={1.8} />}>
                <input
                  value={editForm.title}
                  onChange={(e) => setEditForm({ ...editForm, title: e.target.value })}
                  className="w-full bg-transparent outline-none"
                  style={{ fontSize: 14, color: "#0f172a" }}
                />
              </EditField>

              <EditField label="Descripcion" icon={<AlignLeft size={16} color="#0891b2" strokeWidth={1.8} />}>
                <textarea
                  value={editForm.description}
                  onChange={(e) => setEditForm({ ...editForm, description: e.target.value })}
                  rows={3}
                  className="w-full bg-transparent outline-none resize-none"
                  style={{ fontSize: 14, color: "#0f172a" }}
                />
              </EditField>

              <ChipGroup
                label="Categoria"
                items={categories}
                value={editForm.category}
                color="#0891b2"
                onChange={(value) => setEditForm({ ...editForm, category: value })}
              />

              <ChipGroup
                label="Modalidad"
                items={modalities}
                value={editForm.modality}
                color="#2563eb"
                onChange={(value) => setEditForm({ ...editForm, modality: value })}
              />

              <EditField label="Precio base (ARS)" icon={<DollarSign size={16} color="#2563eb" strokeWidth={1.8} />}>
                <input
                  value={editForm.price}
                  onChange={(e) => setEditForm({ ...editForm, price: e.target.value })}
                  type="number"
                  className="w-full bg-transparent outline-none"
                  style={{ fontSize: 14, color: "#0f172a" }}
                />
              </EditField>

              <EditField label="Areas de trabajo deseadas" icon={<MapPin size={16} color="#ef4444" strokeWidth={1.8} />}>
                <div className="grid grid-cols-2 gap-2 max-h-44 overflow-y-auto pr-1">
                  {LOCATION_OPTIONS.map((option) => {
                    const selected = editForm.areas.includes(option);
                    return (
                      <button
                        key={option}
                        type="button"
                        onClick={() => toggleEditArea(option)}
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
              </EditField>

              <EditField label="Direccion exacta (opcional)" icon={<MapPin size={16} color="#94a3b8" strokeWidth={1.8} />}>
                <input
                  value={editForm.address}
                  onChange={(e) => setEditForm({ ...editForm, address: e.target.value })}
                  className="w-full bg-transparent outline-none"
                  style={{ fontSize: 14, color: "#0f172a" }}
                />
              </EditField>

              <EditField label="Disponibilidad fija" icon={<Clock size={16} color="#7c3aed" strokeWidth={1.8} />}>
                <div className="grid grid-cols-2 gap-2">
                  <select
                    value={editForm.availabilityDayFrom}
                    onChange={(e) => setEditForm({ ...editForm, availabilityDayFrom: e.target.value })}
                    className="bg-transparent outline-none min-w-0"
                    style={{ fontSize: 13, color: "#0f172a" }}
                  >
                    {WEEK_DAYS.map((day) => (
                      <option key={day.value} value={day.value}>Desde {day.label}</option>
                    ))}
                  </select>
                  <select
                    value={editForm.availabilityDayTo}
                    onChange={(e) => setEditForm({ ...editForm, availabilityDayTo: e.target.value })}
                    className="bg-transparent outline-none min-w-0"
                    style={{ fontSize: 13, color: "#0f172a" }}
                  >
                    {WEEK_DAYS.map((day) => (
                      <option key={day.value} value={day.value}>Hasta {day.label}</option>
                    ))}
                  </select>
                  <select
                    value={editForm.availabilityFrom}
                    onChange={(e) => setEditForm({ ...editForm, availabilityFrom: e.target.value })}
                    className="bg-transparent outline-none min-w-0"
                    style={{ fontSize: 13, color: "#0f172a" }}
                  >
                    {TIME_OPTIONS.map((time) => (
                      <option key={time} value={time}>{time}</option>
                    ))}
                  </select>
                  <select
                    value={editForm.availabilityTo}
                    onChange={(e) => setEditForm({ ...editForm, availabilityTo: e.target.value })}
                    className="bg-transparent outline-none min-w-0"
                    style={{ fontSize: 13, color: "#0f172a" }}
                  >
                    {TIME_OPTIONS.map((time) => (
                      <option key={time} value={time}>{time}</option>
                    ))}
                  </select>
                </div>
              </EditField>

              <button
                type="button"
                disabled={!canSaveEdit || savingEdit}
                onClick={saveEdit}
                className="w-full py-4 rounded-2xl mt-1 flex items-center justify-center gap-2 transition-all active:scale-95"
                style={{
                  background: canSaveEdit ? "#2563eb" : "#cbd5e1",
                  color: "white",
                  fontWeight: 700,
                  fontSize: 15,
                  opacity: savingEdit ? 0.82 : 1,
                }}
              >
                <Save size={18} strokeWidth={2} />
                {savingEdit ? "Guardando..." : "Guardar cambios"}
              </button>
            </div>
          </motion.div>
        ) : null}
      </AnimatePresence>
    </div>
  );
}

function mapPublication(pub: ApiPublication): Publication {
  const areas = getPublicationAreas(pub);
  return {
    id: pub.id,
    title: pub.titulo,
    category: pub.categoriaServicio?.nombre ?? "Categoria Servify",
    description: pub.descripcion,
    price: formatMoney(pub.precioBase),
    modality: fromApiModality(pub.modalidadServicio),
    location: formatAreas(areas),
    schedule: formatAvailability(pub),
    active: !["PAUSADA", "INACTIVA", "ELIMINADA"].includes((pub.estado ?? "").toUpperCase()),
    raw: pub,
  };
}

function formFromPublication(pub: ApiPublication): EditForm {
  const availability = normalizeAvailabilityRange(pub.disponibilidadesHorarias);
  return {
    title: pub.titulo,
    description: pub.descripcion,
    category: pub.categoriaServicio?.nombre ?? "Otro",
    modality: fromApiModality(pub.modalidadServicio),
    price: pub.precioBase ? String(pub.precioBase) : "",
    areas: getPublicationAreas(pub),
    address: pub.ubicacion?.referencia ?? "",
    availabilityDayFrom: availability.dayFrom,
    availabilityDayTo: availability.dayTo,
    availabilityFrom: availability.from,
    availabilityTo: availability.to,
  };
}

function getPublicationAreas(pub: ApiPublication): string[] {
  const areas = (pub.zonasCobertura?.length ? pub.zonasCobertura : pub.ubicacion ? [pub.ubicacion] : [])
    .map((zona) => zona.localidad || zona.ciudad || "CABA")
    .filter(Boolean);
  return Array.from(new Set(areas.length ? areas : ["CABA"]));
}

function formatAreas(areas: string[]): string {
  if (areas.length <= 2) return areas.join(", ");
  return `${areas.slice(0, 2).join(", ")} +${areas.length - 2}`;
}

function isDeleted(status?: string): boolean {
  return (status ?? "").toUpperCase() === "ELIMINADA";
}

function formatAvailability(pub: ApiPublication): string {
  const availability = normalizeAvailabilityRange(pub.disponibilidadesHorarias);
  const dayFrom = dayLabel(availability.dayFrom);
  const dayTo = dayLabel(availability.dayTo);
  const days = availability.dayFrom === availability.dayTo ? dayFrom : `${dayFrom}-${dayTo}`;
  return `${days} ${availability.from}-${availability.to}`;
}

function normalizeAvailabilityRange(items?: ApiAvailability[]) {
  const first = items?.[0];
  const last = items?.[items.length - 1] ?? first;
  return {
    dayFrom: first?.diaSemana ?? WEEK_DAYS[0].value,
    dayTo: last?.diaSemana ?? first?.diaSemana ?? WEEK_DAYS[4].value,
    from: first?.horaDesde?.slice(0, 5) ?? "09:00",
    to: first?.horaHasta?.slice(0, 5) ?? "18:00",
  };
}

function dayLabel(value: string): string {
  return WEEK_DAYS.find((item) => item.value === value)?.label ?? value;
}

function InfoItem({
  icon,
  label,
  color = "#64748b",
}: {
  icon: React.ReactNode;
  label: string;
  color?: string;
}) {
  return (
    <div className="flex items-center gap-1">
      {icon}
      <span style={{ fontSize: 12, color, fontWeight: 500 }}>{label}</span>
    </div>
  );
}

function EditField({
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
      <div className="flex items-start gap-3 px-4 py-3.5 rounded-2xl bg-white" style={{ border: "1.5px solid #e2e8f0" }}>
        <div className="mt-0.5">{icon}</div>
        <div className="flex-1">{children}</div>
      </div>
    </div>
  );
}

function ChipGroup({
  label,
  items,
  value,
  color,
  onChange,
}: {
  label: string;
  items: string[];
  value: string;
  color: string;
  onChange: (value: string) => void;
}) {
  return (
    <div>
      <p style={{ fontSize: 13, fontWeight: 700, color: "#475569", marginBottom: 10 }}>{label}</p>
      <div className="flex flex-wrap gap-2">
        {items.map((item) => {
          const selected = value === item;
          return (
            <button
              key={item}
              type="button"
              onClick={() => onChange(item)}
              className="px-3.5 py-2 rounded-full transition-all"
              style={{
                background: selected ? color : "#f1f5f9",
                color: selected ? "white" : "#475569",
                fontWeight: selected ? 700 : 500,
                fontSize: 13,
                border: selected ? "none" : "1.5px solid #e2e8f0",
              }}
            >
              {item}
            </button>
          );
        })}
      </div>
    </div>
  );
}
