import React, { useEffect, useState } from "react";
import { X, FileText, AlignLeft, MapPin, DollarSign, Clock, CheckCircle, CalendarDays } from "lucide-react";
import { motion } from "motion/react";
import { LOCATION_OPTIONS, TIME_OPTIONS, WEEK_DAYS, servifyApi, type ApiRecurrenceFrequency, type ApiScheduleType } from "../api";

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
  const [scheduleType, setScheduleType] = useState<ApiScheduleType>("INMEDIATA");
  const [scheduledDate, setScheduledDate] = useState("");
  const [recurrenceFrequency, setRecurrenceFrequency] = useState<ApiRecurrenceFrequency>("SEMANAL");
  const [recurrenceEndDate, setRecurrenceEndDate] = useState("");
  const [done, setDone] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const recurrenceEndInvalid = Boolean(scheduleType === "RECURRENTE" && recurrenceEndDate && scheduledDate && recurrenceEndDate < scheduledDate);
  const canCreate = Boolean(userId && title && description && category && scheduleIsComplete(scheduleType, scheduledDate, recurrenceEndDate));
  const schedulePreview = buildSchedulePreview(scheduleType, scheduledDate, recurrenceFrequency, recurrenceEndDate, availabilityFrom, availabilityTo);

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
    setScheduleType("INMEDIATA");
    setScheduledDate("");
    setRecurrenceFrequency("SEMANAL");
    setRecurrenceEndDate("");
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
        disponibilidadDia: scheduleType === "INMEDIATA" ? availabilityDay : dayFromDate(scheduledDate),
        horaDesde: availabilityFrom,
        horaHasta: availabilityTo,
        tipoProgramacion: scheduleType,
        fechaProgramadaInicio: scheduleType === "PROGRAMADA" ? toLocalDateTime(scheduledDate, availabilityFrom) : undefined,
        fechaProgramadaFin: scheduleType === "PROGRAMADA" ? toLocalDateTime(scheduledDate, availabilityTo) : undefined,
        frecuenciaRecurrencia: scheduleType === "RECURRENTE" ? recurrenceFrequency : undefined,
        fechaInicioRecurrencia: scheduleType === "RECURRENTE" ? scheduledDate : undefined,
        fechaFinRecurrencia: scheduleType === "RECURRENTE" && recurrenceEndDate ? recurrenceEndDate : undefined,
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
        <div className="flex flex-col items-center pt-3 pb-2 shrink-0">
          <div className="rounded-full" style={{ width: 40, height: 4, background: "#e2e8f0" }} />
        </div>

        <div className="flex items-center justify-between px-6 pb-4 shrink-0">
          <div>
            <p style={{ fontSize: 19, fontWeight: 800, color: "#0f172a" }}>
              {initialValues ? "Repetir solicitud" : "Nueva solicitud"}
            </p>
            <p style={{ fontSize: 13, color: "#64748b" }}>
              {initialValues ? "Revisa los datos y publicala de nuevo" : "Describi lo que necesitas"}
            </p>
          </div>
          <button type="button" onClick={onClose}>
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
              <Field label="Que necesitas?" icon={<FileText size={15} color="#0891b2" strokeWidth={1.8} />}>
                <input
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  placeholder="Ej: Plomero para arreglar canilla"
                  className="w-full bg-transparent outline-none"
                  style={{ fontSize: 14, color: "#0f172a" }}
                />
              </Field>

              <Field label="Descripcion" icon={<AlignLeft size={15} color="#0891b2" strokeWidth={1.8} />}>
                <textarea
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  placeholder="Describi el problema o lo que necesitas con mas detalle..."
                  rows={3}
                  className="w-full bg-transparent outline-none resize-none"
                  style={{ fontSize: 14, color: "#0f172a" }}
                />
              </Field>

              <div>
                <p style={{ fontSize: 13, fontWeight: 700, color: "#475569", marginBottom: 8 }}>Categoria</p>
                <div className="flex flex-wrap gap-2">
                  {categories.map((cat) => {
                    const sel = category === cat;
                    return (
                      <button
                        key={cat}
                        type="button"
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
                        type="button"
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

              <Field label="Tipo de solicitud" icon={<CalendarDays size={15} color="#0891b2" strokeWidth={1.8} />}>
                <div className="flex flex-col gap-3">
                  <div className="grid grid-cols-3 gap-2">
                    {[
                      { id: "INMEDIATA", label: "Ahora" },
                      { id: "PROGRAMADA", label: "Programada" },
                      { id: "RECURRENTE", label: "Recurrente" },
                    ].map((option) => {
                      const selected = scheduleType === option.id;
                      return (
                        <button
                          key={option.id}
                          type="button"
                          onClick={() => setScheduleType(option.id as ApiScheduleType)}
                          className="rounded-xl px-2 py-2 transition-all active:scale-95"
                          style={{
                            background: selected ? "#dbeafe" : "#f8fafc",
                            color: selected ? "#2563eb" : "#64748b",
                            border: selected ? "1.5px solid #93c5fd" : "1.5px solid #e2e8f0",
                            fontSize: 12,
                            fontWeight: 800,
                          }}
                        >
                          {option.label}
                        </button>
                      );
                    })}
                  </div>

                  {scheduleType !== "INMEDIATA" && (
                    <div className="flex flex-col gap-2">
                      <p style={{ fontSize: 12, fontWeight: 800, color: "#64748b" }}>
                        {scheduleType === "RECURRENTE" ? "Fecha del primer encuentro" : "Fecha del servicio"}
                      </p>
                      <input
                        type="date"
                        value={scheduledDate}
                        onChange={(e) => setScheduledDate(e.target.value)}
                        min={todayInputValue()}
                        className="w-full rounded-xl px-3 py-2 outline-none"
                        style={{ border: "1.5px solid #e2e8f0", color: "#0f172a", fontSize: 13, fontWeight: 700 }}
                      />
                      {scheduleType === "RECURRENTE" && (
                        <>
                          <div className="grid grid-cols-2 gap-2">
                            <div>
                              <p style={{ fontSize: 12, fontWeight: 800, color: "#64748b", marginBottom: 6 }}>Frecuencia</p>
                              <select
                                value={recurrenceFrequency}
                                onChange={(e) => setRecurrenceFrequency(e.target.value as ApiRecurrenceFrequency)}
                                className="w-full rounded-xl px-3 py-2 outline-none"
                                style={{ border: "1.5px solid #e2e8f0", color: "#0f172a", fontSize: 13, fontWeight: 700 }}
                              >
                                <option value="SEMANAL">Semanal</option>
                                <option value="QUINCENAL">Quincenal</option>
                                <option value="MENSUAL">Mensual</option>
                              </select>
                            </div>
                            <div>
                              <p style={{ fontSize: 12, fontWeight: 800, color: "#64748b", marginBottom: 6 }}>Hasta (opcional)</p>
                              <input
                                type="date"
                                value={recurrenceEndDate}
                                onChange={(e) => setRecurrenceEndDate(e.target.value)}
                                min={scheduledDate || todayInputValue()}
                                className="w-full rounded-xl px-3 py-2 outline-none"
                                style={{ border: "1.5px solid #e2e8f0", color: "#0f172a", fontSize: 13, fontWeight: 700 }}
                              />
                            </div>
                          </div>
                          <div className="rounded-2xl px-3 py-2" style={{ background: "#eff6ff", border: "1px solid #bfdbfe" }}>
                            <p style={{ fontSize: 12, fontWeight: 900, color: "#1d4ed8" }}>{schedulePreview}</p>
                            <p style={{ fontSize: 11, fontWeight: 700, color: "#64748b", marginTop: 4 }}>
                              La fecha final puede quedar vacia. Luego vas a poder cancelar la recurrencia desde el detalle de la solicitud.
                            </p>
                          </div>
                          {recurrenceEndInvalid ? (
                            <p style={{ fontSize: 12, fontWeight: 800, color: "#b91c1c" }}>
                              La fecha final no puede ser anterior al primer encuentro.
                            </p>
                          ) : null}
                        </>
                      )}
                    </div>
                  )}
                </div>
              </Field>

              <Field
                label={scheduleType === "INMEDIATA" ? "Disponibilidad fija" : "Horario del encuentro"}
                icon={<Clock size={15} color="#7c3aed" strokeWidth={1.8} />}
              >
                <div className={`grid gap-2 ${scheduleType === "INMEDIATA" ? "grid-cols-3" : "grid-cols-2"}`}>
                  {scheduleType === "INMEDIATA" ? (
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
                  ) : null}
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
                {scheduleType !== "INMEDIATA" ? (
                  <p style={{ fontSize: 11, color: "#64748b", fontWeight: 700, marginTop: 8 }}>
                    El dia se toma automaticamente de la fecha elegida.
                  </p>
                ) : null}
              </Field>

              <Field
                label={scheduleType === "RECURRENTE" ? "Precio sugerido por encuentro (opcional)" : "Precio sugerido (opcional)"}
                icon={<DollarSign size={15} color="#2563eb" strokeWidth={1.8} />}
              >
                <input
                  value={price}
                  onChange={(e) => setPrice(e.target.value)}
                  placeholder="Ej: $8.000 - $12.000"
                  className="w-full bg-transparent outline-none"
                  style={{ fontSize: 14, color: "#0f172a" }}
                />
                {scheduleType === "RECURRENTE" ? (
                  <p className="mt-2 text-[11px] font-semibold leading-relaxed" style={{ color: "#64748b" }}>
                    Este monto se acuerda y se paga en cada sesión del programa.
                  </p>
                ) : null}
              </Field>

              <button
                type="button"
                onClick={handleCreate}
                disabled={!canCreate || loading}
                className="w-full py-3.5 rounded-2xl transition-all active:scale-95"
                style={{
                  background: canCreate && !loading ? "#2563eb" : "#cbd5e1",
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
                <p style={{ fontSize: 18, fontWeight: 800, color: "#0f172a" }}>Solicitud publicada</p>
                <p style={{ fontSize: 13, color: "#64748b", marginTop: 4 }}>
                  Los prestadores podran ver y enviar propuestas
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

function scheduleIsComplete(type: ApiScheduleType, date: string, endDate = ""): boolean {
  if (type === "INMEDIATA") return true;
  if (!date) return false;
  return !(type === "RECURRENTE" && endDate && endDate < date);
}

function toLocalDateTime(date: string, time: string): string | undefined {
  if (!date) return undefined;
  return `${date}T${time.length === 5 ? `${time}:00` : time}`;
}

function dayFromDate(date: string): string {
  if (!date) return WEEK_DAYS[0].value;
  const parsed = new Date(`${date}T00:00:00`);
  if (Number.isNaN(parsed.getTime())) return WEEK_DAYS[0].value;
  const values = ["SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"];
  return values[parsed.getDay()] ?? WEEK_DAYS[0].value;
}

function todayInputValue(): string {
  const today = new Date();
  const month = String(today.getMonth() + 1).padStart(2, "0");
  const day = String(today.getDate()).padStart(2, "0");
  return `${today.getFullYear()}-${month}-${day}`;
}

function buildSchedulePreview(
  type: ApiScheduleType,
  startDate: string,
  frequency: ApiRecurrenceFrequency,
  endDate: string,
  from: string,
  to: string
): string {
  if (type === "PROGRAMADA") {
    return startDate
      ? `Encuentro programado para ${formatInputDate(startDate)} de ${from} a ${to}.`
      : "Elegis una fecha futura y Servify busca prestadores para ese momento.";
  }
  if (type !== "RECURRENTE") return "Solicitud inmediata para coordinar con prestadores disponibles.";
  const frequencyLabel = recurrenceFrequencyLabel(frequency).toLowerCase();
  const startLabel = startDate ? `desde ${formatInputDate(startDate)}` : "desde la fecha que elijas";
  const endLabel = endDate ? `hasta ${formatInputDate(endDate)}` : "sin fecha final";
  return `Encuentros ${frequencyLabel} ${startLabel} ${endLabel}, de ${from} a ${to}.`;
}

function recurrenceFrequencyLabel(frequency: ApiRecurrenceFrequency): string {
  if (frequency === "QUINCENAL") return "quincenales";
  if (frequency === "MENSUAL") return "mensuales";
  return "semanales";
}

function formatInputDate(value: string): string {
  const parsed = new Date(`${value}T00:00:00`);
  if (Number.isNaN(parsed.getTime())) return value;
  return parsed.toLocaleDateString("es-AR", { day: "2-digit", month: "2-digit", year: "numeric" });
}
