import { useCallback, useEffect, useMemo, useRef, useState, type ReactNode } from "react";
import { AlertCircle, CalendarDays, Clock, DollarSign, Loader2, MessageSquare, RefreshCcw, Trash2, UserRound } from "lucide-react";
import { motion, AnimatePresence } from "motion/react";
import {
  WEEK_DAYS,
  formatMoney,
  fromApiModality,
  servifyApi,
  type ApiAssignmentState,
  type ApiCategory,
  type ApiReceivedRequest,
  type ApiServiceEncounter,
  type ApiServiceRecurrence,
  type ApiUserProfile,
} from "../api";
import type { ServiceRequest } from "./RequestsScreen";
import { PullToRefreshIndicator, usePullToRefresh } from "./PullToRefresh";
import { ServisHint } from "./ServisHint";

interface ProviderAgendaProps {
  userId?: string;
  onOpenRequest: (request: ServiceRequest) => void;
}

type ProviderAgendaItemType = "encounter" | "recurrence";

interface ProviderAgendaItem {
  id: string;
  type: ProviderAgendaItemType;
  request: ApiReceivedRequest;
  encounter?: ApiServiceEncounter;
  recurrence?: ApiServiceRecurrence;
  assignment: NonNullable<ApiAssignmentState["asignacion"]>;
  requesterName: string;
  category: string;
  title: string;
  location: string;
  priceLabel: string;
  dateLabel: string;
  timeLabel: string;
  statusLabel: string;
  statusColor: string;
  sortAt: number;
  closesRequest: boolean;
  requestDetail: ServiceRequest;
}

export function ProviderAgenda({ userId, onOpenRequest }: ProviderAgendaProps) {
  const [items, setItems] = useState<ProviderAgendaItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [actionLoading, setActionLoading] = useState("");
  const [cancelTarget, setCancelTarget] = useState<ProviderAgendaItem | null>(null);
  const cancelBackButtonRef = useRef<HTMLButtonElement>(null);

  const loadAgenda = useCallback(async () => {
    if (!userId) {
      setItems([]);
      return;
    }

    setLoading(true);
    setError("");
    try {
      const [received, categories] = await Promise.all([
        servifyApi.listReceivedRequests(userId),
        servifyApi.listCategories().catch(() => []),
      ]);
      const categoryMap = buildCategoryMap(categories);
      const uniqueRequests = uniqueByRequestId(received || []);

      const nested = await Promise.all(
        uniqueRequests.map(async (request) => buildAgendaItemsForRequest(request, userId, categoryMap))
      );

      setItems(nested.flat().sort((a, b) => a.sortAt - b.sortAt));
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo cargar la agenda");
    } finally {
      setLoading(false);
    }
  }, [userId]);

  useEffect(() => {
    void loadAgenda();
  }, [loadAgenda]);

  useEffect(() => {
    if (!cancelTarget) return;
    cancelBackButtonRef.current?.focus();
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === "Escape" && !actionLoading) setCancelTarget(null);
    };
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [actionLoading, cancelTarget]);

  const { pullDistance, refreshing, pullHandlers } = usePullToRefresh(loadAgenda, Boolean(userId));
  const nextItem = items[0];
  const confirmedCount = useMemo(() => items.filter((item) => item.type === "recurrence" || item.encounter?.estado === "CONFIRMADO").length, [items]);

  const cancelItem = async () => {
    if (!userId || !cancelTarget) return;
    setActionLoading(cancelTarget.id);
    setError("");
    try {
      if (cancelTarget.type === "recurrence") {
        await servifyApi.cancelServiceRecurrence({
          solicitudId: cancelTarget.request.id,
          usuarioId: userId,
          motivo: "Cancelado desde agenda del prestador",
        });
      } else if (cancelTarget.encounter) {
        await servifyApi.cancelServiceEncounter({ encuentroId: cancelTarget.encounter.id, usuarioId: userId });
      }
      setCancelTarget(null);
      await loadAgenda();
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo cancelar el servicio agendado");
    } finally {
      setActionLoading("");
    }
  };

  return (
    <div className="flex-1 overflow-y-auto px-5 pt-4 pb-6 flex flex-col gap-4" {...pullHandlers}>
      <PullToRefreshIndicator pullDistance={pullDistance} refreshing={refreshing} />

      <section className="servify-agenda-summary rounded-3xl px-4 py-4" style={{ background: "linear-gradient(135deg, #eff6ff 0%, #ecfeff 100%)", border: "1px solid #bfdbfe" }}>
        <div className="flex items-start justify-between gap-3">
          <div>
            <p style={{ color: "#2563eb", fontSize: 12, fontWeight: 900 }}>Agenda de trabajo</p>
            <h2 style={{ color: "#0f172a", fontSize: 18, fontWeight: 900, marginTop: 4 }}>Proximos servicios</h2>
            <p style={{ color: "#64748b", fontSize: 12, fontWeight: 700, marginTop: 5, lineHeight: 1.4 }}>
              {nextItem ? `Siguiente: ${nextItem.dateLabel} ${nextItem.timeLabel}` : "Aca vas a ver servicios programados, recurrentes y segundas visitas."}
            </p>
          </div>
          <div className="servify-agenda-summary-icon flex items-center justify-center rounded-2xl" style={{ width: 48, height: 48, background: "white", color: "#2563eb", boxShadow: "0 10px 22px rgba(37,99,235,0.12)" }}>
            <CalendarDays size={23} strokeWidth={2.1} />
          </div>
        </div>
        <div className="mt-4 grid grid-cols-2 gap-2">
          <StatPill label="Proximos" value={String(items.length)} />
          <StatPill label="Confirmados" value={String(confirmedCount)} />
        </div>
      </section>

      {error ? (
        <div className="rounded-2xl px-4 py-3 flex items-center gap-2" style={{ background: "#fef2f2", color: "#b91c1c" }}>
          <AlertCircle size={16} strokeWidth={2} />
          <p style={{ fontSize: 13, fontWeight: 800 }}>{error}</p>
        </div>
      ) : null}

      {loading ? (
        <div className="flex items-center gap-2 px-1" style={{ color: "#64748b", fontSize: 13, fontWeight: 800 }}>
          <Loader2 size={15} className="animate-spin" />
          Cargando agenda...
        </div>
      ) : null}

      {!loading && !error && items.length === 0 ? (
        <ServisHint
          pose="peek"
          tone="quiet"
          title="Sin servicios agendados"
          detail="Cuando confirmes una solicitud programada, recurrente o una segunda visita, la voy a dejar ordenada aca."
        />
      ) : null}

      <div className="flex flex-col gap-3">
        {items.map((item, index) => (
          <motion.article
            key={item.id}
            initial={{ opacity: 0, y: 8 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.25, delay: index * 0.04 }}
            className="servify-card bg-white rounded-2xl p-4"
            style={{ border: "1px solid rgba(0,0,0,0.06)", boxShadow: "0 1px 4px rgba(0,0,0,0.04)" }}
          >
            <div className="flex items-start justify-between gap-3">
              <div className="min-w-0 flex-1">
                <p style={{ color: "#0f172a", fontSize: 15, fontWeight: 900, lineHeight: 1.25 }}>{item.title}</p>
                <p style={{ color: "#0891b2", fontSize: 12, fontWeight: 900, marginTop: 4 }}>{item.category}</p>
              </div>
              <span className="servify-status-badge px-2.5 py-1 rounded-full shrink-0" style={{ background: "#f8fafc", color: item.statusColor, fontSize: 11, fontWeight: 900 }}>
                {item.statusLabel}
              </span>
            </div>

            <div className="mt-3 flex flex-col gap-2.5">
              <AgendaRow icon={<UserRound size={14} color="#2563eb" strokeWidth={1.9} />} label="Solicitante" value={item.requesterName} />
              <AgendaRow icon={<CalendarDays size={14} color="#0891b2" strokeWidth={1.9} />} label="Dia" value={item.dateLabel} />
              <AgendaRow icon={<Clock size={14} color="#d97706" strokeWidth={1.9} />} label="Horario" value={item.timeLabel} />
              <AgendaRow icon={<DollarSign size={14} color="#16a34a" strokeWidth={1.9} />} label="Precio" value={item.priceLabel} />
            </div>

            <div className="mt-4 grid grid-cols-2 gap-2">
              <button
                type="button"
                onClick={() => onOpenRequest({ ...item.requestDetail, openChat: true })}
                className="flex items-center justify-center gap-1.5 rounded-xl py-2.5 transition-all active:scale-95"
                style={{ background: "#eff6ff", color: "#2563eb", border: "1.5px solid #bfdbfe", fontSize: 12, fontWeight: 900 }}
              >
                <MessageSquare size={14} strokeWidth={2} />
                Contactar
              </button>
              <button
                type="button"
                onClick={() => setCancelTarget(item)}
                disabled={Boolean(actionLoading)}
                className="flex items-center justify-center gap-1.5 rounded-xl py-2.5 transition-all active:scale-95"
                style={{ background: "#fef2f2", color: "#dc2626", border: "1.5px solid #fecaca", fontSize: 12, fontWeight: 900 }}
              >
                <Trash2 size={14} strokeWidth={2} />
                {item.type === "recurrence"
                  ? "Cancelar serie"
                  : item.closesRequest
                  ? "Cancelar servicio"
                  : "Cancelar visita"}
              </button>
            </div>
          </motion.article>
        ))}
      </div>

      <button
        type="button"
        onClick={loadAgenda}
        disabled={loading}
        className="mt-1 flex items-center justify-center gap-2 rounded-2xl py-3 transition-all active:scale-95"
        style={{ background: "#f1f5f9", color: "#475569", fontSize: 13, fontWeight: 900 }}
      >
        <RefreshCcw size={15} strokeWidth={2} />
        Actualizar agenda
      </button>

      <AnimatePresence>
        {cancelTarget ? (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 z-50 flex items-end justify-center bg-black/35 px-4 pb-6"
          >
            <motion.div
              initial={{ y: 24, opacity: 0 }}
              animate={{ y: 0, opacity: 1 }}
              exit={{ y: 24, opacity: 0 }}
              className="servify-agenda-dialog w-full max-w-sm rounded-3xl bg-white p-5"
              style={{ boxShadow: "0 24px 60px rgba(15,23,42,0.28)" }}
              role="dialog"
              aria-modal="true"
              aria-label={cancelTarget.type === "recurrence"
                ? "Confirmar cancelación de la serie"
                : cancelTarget.closesRequest
                ? "Confirmar cancelación del servicio programado"
                : "Confirmar cancelación de la visita"}
            >
              <ServisHint
                compact
                pose="wave"
                title="Avisale al solicitante"
                detail={cancelTarget.type === "recurrence"
                  ? "Vas a cancelar este servicio recurrente. Te recomendamos escribirle antes por chat para coordinarlo bien."
                  : cancelTarget.closesRequest
                  ? "Vas a cancelar el servicio programado, sus visitas pendientes y la solicitud completa. Te recomendamos avisarle antes por chat."
                  : "Vas a cancelar este encuentro. Te recomendamos escribirle antes por chat para que no quede esperando."
                }
              />
              <button
                type="button"
                onClick={() => {
                  const target = cancelTarget;
                  setCancelTarget(null);
                  onOpenRequest({ ...target.requestDetail, openChat: true });
                }}
                disabled={Boolean(actionLoading)}
                className="mt-4 flex w-full items-center justify-center gap-2 rounded-2xl py-3 transition-all active:scale-95"
                style={{ background: "#eff6ff", color: "#2563eb", border: "1.5px solid #bfdbfe", fontSize: 13, fontWeight: 900, opacity: actionLoading ? 0.6 : 1 }}
              >
                <MessageSquare size={15} strokeWidth={2.2} />
                Contactar antes de cancelar
              </button>
              <div className="mt-2 grid grid-cols-2 gap-2">
                <button
                  ref={cancelBackButtonRef}
                  type="button"
                  onClick={() => setCancelTarget(null)}
                  disabled={Boolean(actionLoading)}
                  className="rounded-2xl py-3 transition-all active:scale-95"
                  style={{ background: "#f1f5f9", color: "#475569", fontSize: 13, fontWeight: 900, opacity: actionLoading ? 0.6 : 1 }}
                >
                  Volver
                </button>
                <button
                  type="button"
                  onClick={cancelItem}
                  disabled={actionLoading === cancelTarget.id}
                  className="rounded-2xl py-3 transition-all active:scale-95"
                  style={{ background: "#dc2626", color: "white", fontSize: 13, fontWeight: 900 }}
                >
                  {actionLoading === cancelTarget.id
                    ? "Cancelando..."
                    : cancelTarget.type === "recurrence"
                    ? "Cancelar serie"
                    : cancelTarget.closesRequest
                    ? "Cancelar servicio"
                    : "Cancelar visita"}
                </button>
              </div>
            </motion.div>
          </motion.div>
        ) : null}
      </AnimatePresence>
    </div>
  );
}

async function buildAgendaItemsForRequest(
  request: ApiReceivedRequest,
  providerId: string,
  categoryMap: Map<string, string>
): Promise<ProviderAgendaItem[]> {
  const [state, encounters, recurrence] = await Promise.all([
    servifyApi.getAssignmentState(request.id),
    servifyApi.listServiceEncounters(request.id),
    servifyApi.getServiceRecurrence(request.id),
  ]);
  const assignment = state?.asignacion;
  if (!assignment || assignment.prestadorId !== providerId || assignment.estado === "CANCELADA") {
    return [];
  }
  const futureEncounters = encounters.filter(isFutureAgendaEncounter);
  const assignmentFinished = assignment.estado === "FINALIZADA" || Boolean(state?.finalizacionConfirmada);
  // Una segunda visita futura sigue en agenda aunque el servicio principal ya se haya cerrado.
  if (assignmentFinished && futureEncounters.length === 0) return [];
  const activeRecurrence = !assignmentFinished && recurrence?.estado === "ACTIVA" ? recurrence : null;

  const requesterId = state?.solicitanteId || request.solicitanteId || "";
  const requesterName = await resolveRequesterName(requesterId);
  const category = request.categoriaServicioId
    ? categoryMap.get(request.categoriaServicioId) ?? `Categoria ${request.categoriaServicioId.slice(0, 6)}`
    : "Sin categoria";
  const title = titleFromDescription(request.descripcionNecesidad);
  const location = request.ubicacion?.localidad || request.ubicacion?.ciudad || "CABA";
  const priceLabel = formatMoney(assignment.precioAcordado ?? request.precioReferencia);

  const requestDetail = toRequestDetail(
    request,
    assignment,
    requesterName,
    category,
    title,
    location,
    priceLabel,
    providerId,
    activeRecurrence ? "RECURRENTE" : request.tipoProgramacion
  );

  const encounterItems = futureEncounters
    .map((encounter) => toEncounterAgendaItem(request, encounter, assignment, requesterName, category, title, location, priceLabel, requestDetail, activeRecurrence));

  const recurrenceItem = activeRecurrence && !encounterItems.length
    ? toRecurrenceAgendaItem(request, activeRecurrence, assignment, requesterName, category, title, location, priceLabel, requestDetail)
    : null;

  return recurrenceItem ? [...encounterItems, recurrenceItem] : encounterItems;
}

function toEncounterAgendaItem(
  request: ApiReceivedRequest,
  encounter: ApiServiceEncounter,
  assignment: NonNullable<ApiAssignmentState["asignacion"]>,
  requesterName: string,
  category: string,
  title: string,
  location: string,
  priceLabel: string,
  requestDetail: ServiceRequest,
  recurrence: ApiServiceRecurrence | null
): ProviderAgendaItem {
  const sortAt = new Date(encounter.fechaInicio).getTime();
  return {
    id: encounter.id,
    type: "encounter",
    request,
    encounter,
    assignment,
    requesterName,
    category,
    title,
    location,
    priceLabel,
    dateLabel: formatEncounterDay(encounter.fechaInicio),
    timeLabel: formatEncounterTime(encounter),
    statusLabel: encounter.estado === "PROPUESTO" ? "Pendiente" : recurrence?.estado === "ACTIVA" ? "Recurrente" : "Confirmado",
    statusColor: encounter.estado === "PROPUESTO" ? "#0891b2" : recurrence?.estado === "ACTIVA" ? "#7c3aed" : "#16a34a",
    sortAt: Number.isFinite(sortAt) ? sortAt : Date.now(),
    closesRequest: isPrimaryScheduledEncounter(request, encounter),
    requestDetail,
  };
}

function toRecurrenceAgendaItem(
  request: ApiReceivedRequest,
  recurrence: ApiServiceRecurrence,
  assignment: NonNullable<ApiAssignmentState["asignacion"]>,
  requesterName: string,
  category: string,
  title: string,
  location: string,
  priceLabel: string,
  requestDetail: ServiceRequest
): ProviderAgendaItem | null {
  const nextStart = getNextRecurrenceStart(recurrence);
  if (!nextStart) return null;
  return {
    id: `recurrence-${recurrence.id}`,
    type: "recurrence",
    request,
    recurrence,
    assignment,
    requesterName,
    category,
    title,
    location,
    priceLabel,
    dateLabel: formatRecurrenceDay(recurrence, nextStart),
    timeLabel: `${normalizeTime(recurrence.horaDesde)} - ${normalizeTime(recurrence.horaHasta)}`,
    statusLabel: "Recurrente",
    statusColor: "#7c3aed",
    sortAt: nextStart.getTime(),
    closesRequest: true,
    requestDetail,
  };
}

function buildCategoryMap(categories: ApiCategory[]): Map<string, string> {
  return new Map((categories || []).map((category) => [category.id, category.nombre]));
}

function uniqueByRequestId(requests: ApiReceivedRequest[]): ApiReceivedRequest[] {
  const seen = new Set<string>();
  return requests.filter((request) => {
    if (!request.id || seen.has(request.id)) return false;
    seen.add(request.id);
    return true;
  });
}

async function resolveRequesterName(requesterId: string): Promise<string> {
  if (!requesterId) return "Solicitante";
  const profile = await servifyApi.getUserProfile(requesterId).catch(() => null);
  const name = formatProfileName(profile);
  return name || `Usuario ${requesterId.slice(0, 6)}`;
}

function formatProfileName(profile: ApiUserProfile | null): string {
  if (!profile) return "";
  return [profile.nombre, profile.apellido].filter(Boolean).join(" ").trim();
}

function isFutureAgendaEncounter(encounter: ApiServiceEncounter): boolean {
  const start = new Date(encounter.fechaInicio).getTime();
  if (!Number.isFinite(start)) return false;
  return start >= Date.now() - 5 * 60 * 1000 && (encounter.estado === "CONFIRMADO" || encounter.estado === "PROPUESTO");
}

function isPrimaryScheduledEncounter(request: ApiReceivedRequest, encounter: ApiServiceEncounter): boolean {
  if (request.tipoProgramacion !== "PROGRAMADA" || encounter.recurrenciaServicioId) return false;
  return sameDateTime(request.fechaProgramadaInicio, encounter.fechaInicio)
    && sameDateTime(request.fechaProgramadaFin, encounter.fechaFin);
}

function sameDateTime(left?: string, right?: string): boolean {
  if (!left || !right) return false;
  const leftTime = new Date(left).getTime();
  const rightTime = new Date(right).getTime();
  return Number.isFinite(leftTime) && Number.isFinite(rightTime) && leftTime === rightTime;
}

function titleFromDescription(description?: string): string {
  const clean = (description || "Solicitud de servicio").trim();
  return clean.split(".")[0] || "Solicitud de servicio";
}

function formatEncounterDay(value: string): string {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "Fecha a coordinar";
  return date.toLocaleDateString("es-AR", { weekday: "short", day: "2-digit", month: "2-digit" });
}

function formatEncounterTime(encounter: ApiServiceEncounter): string {
  const start = new Date(encounter.fechaInicio);
  const end = new Date(encounter.fechaFin);
  if (Number.isNaN(start.getTime())) return "Horario a coordinar";
  const from = start.toLocaleTimeString("es-AR", { hour: "2-digit", minute: "2-digit" });
  const to = Number.isNaN(end.getTime()) ? "" : end.toLocaleTimeString("es-AR", { hour: "2-digit", minute: "2-digit" });
  return `${from}${to ? ` - ${to}` : ""}`;
}

function getNextRecurrenceStart(recurrence: ApiServiceRecurrence): Date | null {
  const dayIndex = jsDayIndex(recurrence.diaSemana);
  if (dayIndex < 0) return null;

  const [hour, minute] = normalizeTime(recurrence.horaDesde).split(":").map(Number);
  const now = new Date();
  const candidate = new Date(`${recurrence.fechaInicio}T00:00:00`);
  if (Number.isNaN(candidate.getTime())) return null;
  candidate.setHours(hour || 0, minute || 0, 0, 0);
  const daysAhead = (dayIndex - candidate.getDay() + 7) % 7;
  candidate.setDate(candidate.getDate() + daysAhead);
  while (candidate < now) {
    advanceRecurrence(candidate, recurrence.frecuencia);
  }

  if (recurrence.fechaFin) {
    const endsAt = new Date(`${recurrence.fechaFin}T23:59:59`);
    if (!Number.isNaN(endsAt.getTime()) && candidate > endsAt) return null;
  }
  return candidate;
}

function advanceRecurrence(date: Date, frequency: ApiServiceRecurrence["frecuencia"]) {
  if (frequency === "MENSUAL") {
    const originalDay = date.getDate();
    date.setDate(1);
    date.setMonth(date.getMonth() + 1);
    const lastDayOfTargetMonth = new Date(date.getFullYear(), date.getMonth() + 1, 0).getDate();
    date.setDate(Math.min(originalDay, lastDayOfTargetMonth));
    return;
  }
  date.setDate(date.getDate() + (frequency === "QUINCENAL" ? 14 : 7));
}

function jsDayIndex(day: string): number {
  const normalized = day?.toUpperCase();
  const map: Record<string, number> = {
    SUNDAY: 0,
    MONDAY: 1,
    TUESDAY: 2,
    WEDNESDAY: 3,
    THURSDAY: 4,
    FRIDAY: 5,
    SATURDAY: 6,
  };
  return map[normalized] ?? -1;
}

function formatRecurrenceDay(recurrence: ApiServiceRecurrence, nextStart: Date): string {
  const day = WEEK_DAYS.find((item) => item.value === recurrence.diaSemana)?.label ?? recurrence.diaSemana;
  return `${formatFrequency(recurrence.frecuencia)} ${day} desde ${nextStart.toLocaleDateString("es-AR", { day: "2-digit", month: "2-digit" })}`;
}

function formatFrequency(frequency: ApiServiceRecurrence["frecuencia"]): string {
  if (frequency === "QUINCENAL") return "Cada 2 semanas";
  if (frequency === "MENSUAL") return "Cada mes";
  return "Cada semana";
}

function normalizeTime(value?: string): string {
  return (value || "00:00").slice(0, 5);
}

function toRequestDetail(
  request: ApiReceivedRequest,
  assignment: NonNullable<ApiAssignmentState["asignacion"]>,
  requesterName: string,
  category: string,
  title: string,
  location: string,
  priceLabel: string,
  providerId: string,
  scheduleType: ServiceRequest["scheduleType"]
): ServiceRequest {
  const requestDate = request.fechaSolicitud ?? request.createdAt;
  return {
    id: request.id,
    viewerRole: "PRESTADOR",
    title,
    description: request.descripcionNecesidad,
    category,
    location,
    proposals: 0,
    price: priceLabel,
    schedule: formatAvailability(request, scheduleType),
    date: requestDate ? new Date(requestDate).toLocaleDateString("es-AR") : "Sin fecha",
    rawDate: requestDate,
    status: assignment.estado === "PENDIENTE_CONFIRMACION" ? "pending-acceptance" : "in-progress",
    requesterName,
    requesterInitials: initialsFromName(requesterName),
    providerId,
    providerName: "Tu",
    providerInitials: "TU",
    modal: fromApiModality(request.modalidadServicio),
    locality: location,
    availabilityDay: request.disponibilidadRequerida?.diaSemana,
    availabilityFrom: request.disponibilidadRequerida?.horaDesde?.slice(0, 5),
    availabilityTo: request.disponibilidadRequerida?.horaHasta?.slice(0, 5),
    scheduleType,
    scheduledStart: request.fechaProgramadaInicio,
    scheduledEnd: request.fechaProgramadaFin,
    distributionId: request.distribucionSolicitudId,
    rawStatus: assignment.estado,
  };
}

function formatAvailability(request: ApiReceivedRequest, scheduleType?: ServiceRequest["scheduleType"]): string {
  if (scheduleType === "PROGRAMADA" && request.fechaProgramadaInicio) {
    const start = new Date(request.fechaProgramadaInicio);
    const end = request.fechaProgramadaFin ? new Date(request.fechaProgramadaFin) : null;
    if (!Number.isNaN(start.getTime())) {
      const date = start.toLocaleDateString("es-AR", { day: "2-digit", month: "2-digit" });
      const from = start.toLocaleTimeString("es-AR", { hour: "2-digit", minute: "2-digit" });
      const to = end && !Number.isNaN(end.getTime()) ? end.toLocaleTimeString("es-AR", { hour: "2-digit", minute: "2-digit" }) : "";
      return `Programada ${date} ${from}${to ? `-${to}` : ""}`;
    }
  }
  const availability = request.disponibilidadRequerida;
  if (!availability) return "Horario a coordinar";
  const day = WEEK_DAYS.find((item) => item.value === availability.diaSemana)?.label ?? availability.diaSemana;
  const prefix = scheduleType === "RECURRENTE" ? "Recurrente " : "";
  return `${prefix}${day} ${availability.horaDesde.slice(0, 5)}-${availability.horaHasta.slice(0, 5)}`;
}

function initialsFromName(name: string): string {
  return name
    .split(" ")
    .map((part) => part[0])
    .join("")
    .slice(0, 2)
    .toUpperCase();
}

function StatPill({ label, value }: { label: string; value: string }) {
  return (
    <div className="servify-agenda-stat rounded-2xl px-3 py-2" style={{ background: "rgba(255,255,255,0.78)", border: "1px solid rgba(191,219,254,0.72)" }}>
      <p style={{ color: "#64748b", fontSize: 11, fontWeight: 900 }}>{label}</p>
      <p style={{ color: "#0f172a", fontSize: 18, fontWeight: 900, marginTop: 2 }}>{value}</p>
    </div>
  );
}

function AgendaRow({ icon, label, value }: { icon: ReactNode; label: string; value: string }) {
  return (
    <div className="flex items-center gap-2">
      <span className="flex items-center justify-center rounded-xl" style={{ width: 28, height: 28, background: "#f8fafc", flexShrink: 0 }}>{icon}</span>
      <div className="min-w-0 flex-1">
        <p style={{ color: "#94a3b8", fontSize: 10, fontWeight: 900, lineHeight: 1 }}>{label}</p>
        <p style={{ color: "#0f172a", fontSize: 12, fontWeight: 800, marginTop: 2, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{value}</p>
      </div>
    </div>
  );
}
