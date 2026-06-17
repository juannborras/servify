import React, { useCallback, useEffect, useMemo, useState } from "react";
import { Plus, MapPin, Users, DollarSign, Clock, CheckCircle, XCircle, MessageSquare, Edit2, Trash2, Ban, X, Save, RefreshCcw } from "lucide-react";
import { motion } from "motion/react";
import { LOCATION_OPTIONS, TIME_OPTIONS, WEEK_DAYS, formatMoney, fromApiModality, servifyApi, type ApiCategory, type ApiReceivedRequest, type ApiRequest, type ApiUserProfile } from "../api";

type RequestTab = "my-requests" | "my-proposals";
type TimeFilter = "active" | "30" | "90" | "all";
type AssignmentState = Awaited<ReturnType<typeof servifyApi.getAssignmentState>>;

export interface ServiceRequest {
  id: number | string;
  viewerRole?: "SOLICITANTE" | "PRESTADOR" | null;
  title: string;
  description: string;
  category: string;
  location: string;
  proposals: number;
  price: string;
  schedule: string;
  date: string;
  rawDate?: string;
  status: "open" | "completed" | "cancelled" | "in-progress" | "counter-offer";
  requesterName: string;
  requesterInitials: string;
  providerName?: string;
  providerInitials?: string;
  modal: "Presencial" | "Virtual" | "Ambas";
  locality?: string;
  availabilityDay?: string;
  availabilityFrom?: string;
  availabilityTo?: string;
  distributionId?: string;
  counterOfferId?: string;
  providerId?: string;
  rawStatus?: string;
}

type EditRequestForm = {
  description: string;
  modal: "Presencial" | "Virtual" | "Ambas";
  locality: string;
  price: string;
  availabilityDay: string;
  availabilityFrom: string;
  availabilityTo: string;
};

// Demo placeholders removed to avoid showing fake proposals to external users.

const statusConfig = {
  open: { label: "Abierta", bg: "#eff6ff", color: "#2563eb" },
  completed: { label: "Completada", bg: "#f0fdf4", color: "#16a34a" },
  cancelled: { label: "Cancelada", bg: "#fef2f2", color: "#ef4444" },
  "in-progress": { label: "En curso", bg: "#fffbeb", color: "#d97706" },
  "counter-offer": { label: "Contraoferta", bg: "#fff7ed", color: "#ea580c" },
};

const timeFilterOptions: { id: TimeFilter; label: string }[] = [
  { id: "active", label: "Activas" },
  { id: "30", label: "30 dias" },
  { id: "90", label: "90 dias" },
  { id: "all", label: "Todas" },
];

interface RequestsScreenProps {
  userId?: string;
  onRequestPress: (req: ServiceRequest) => void;
  onNewRequest: () => void;
  onRepeatRequest: (req: ServiceRequest) => void;
  initialRequestId?: string | null;
  onInitialRequestOpened?: () => void;
}

export function RequestsScreen({
  userId,
  onRequestPress,
  onNewRequest,
  onRepeatRequest,
  initialRequestId,
  onInitialRequestOpened,
}: RequestsScreenProps) {
  const [tab, setTab] = useState<RequestTab>("my-requests");
  const [apiRequests, setApiRequests] = useState<ServiceRequest[]>([]);
  const [apiReceived, setApiReceived] = useState<ServiceRequest[]>([]);
  const [timeFilter, setTimeFilter] = useState<TimeFilter>("active");
  const [actionLoading, setActionLoading] = useState("");
  const [counterOfferFor, setCounterOfferFor] = useState<string | null>(null);
  const [counterPrice, setCounterPrice] = useState("");
  const [counterMessage, setCounterMessage] = useState("");
  const [editingRequest, setEditingRequest] = useState<ServiceRequest | null>(null);
  const [editForm, setEditForm] = useState<EditRequestForm>(() => emptyEditForm());
  const [error, setError] = useState("");

  const loadRequests = useCallback(() => {
    if (!userId) return;
    setError("");

    return Promise.all([
      servifyApi.listUserRequests(userId).catch(() => []),
      servifyApi.listReceivedRequests(userId).catch(() => []),
      servifyApi.listCategories().catch(() => []),
    ])
      .then(async ([own, received, categories]) => {
        const categoryMap = buildCategoryMap(categories);
        const requesterMap = await buildRequesterMap(own, received);
        const ownStates = await Promise.all(
          own.map(async (req) => ({
            requestId: req.id,
            state: await servifyApi.getAssignmentState(req.id).catch(() => null),
          }))
        );
        const receivedStates = await Promise.all(
          received.map(async (req) => ({
            requestId: req.id,
            state: await servifyApi.getAssignmentState(req.id).catch(() => null),
          }))
        );
        const providerMap = await buildProfileNameMap(collectProviderIds(received, ownStates, receivedStates));
        const ownStateMap = new Map(ownStates.map((entry) => [entry.requestId, entry.state]));
        const receivedStateMap = new Map(receivedStates.map((entry) => [entry.requestId, entry.state]));
        const ownMapped = own.map((req) => {
          const mapped = mapRequest(req, "SOLICITANTE", categoryMap, requesterMap);
          return applyAssignmentState(mapped, ownStateMap.get(req.id) ?? null, providerMap);
        });
        const receivedMapped = received.map((req) => {
          const mapped = mapRequest(req, "PRESTADOR", categoryMap, requesterMap);
          return applyAssignmentState(mapped, receivedStateMap.get(req.id) ?? null, providerMap);
        });
        setApiRequests(sortRequestsForDisplay(ownMapped));
        setApiReceived(sortRequestsForDisplay(receivedMapped));
      })
      .catch((err) => {
        setError(err instanceof Error ? err.message : "No se pudieron cargar las solicitudes");
      });
  }, [userId]);

  useEffect(() => {
    void loadRequests();
    if (!userId) return;

    const intervalId = window.setInterval(() => {
      void loadRequests();
    }, 8000);

    return () => {
      window.clearInterval(intervalId);
    };
  }, [loadRequests, userId]);

  const tabs: { id: RequestTab; label: string }[] = [
    { id: "my-requests", label: "Mis pedidos" },
    { id: "my-proposals", label: "Mis propuestas" },
  ];

  const data = useMemo(
    () => filterRequestsByTime(sortRequestsForDisplay(tab === "my-requests" ? apiRequests : apiReceived), timeFilter),
    [apiRequests, apiReceived, tab, timeFilter]
  );

  useEffect(() => {
    if (!initialRequestId) return;
    const matchingRequest = [...apiRequests, ...apiReceived].find((req) => String(req.id) === initialRequestId);
    if (!matchingRequest) return;

    onInitialRequestOpened?.();
    onRequestPress(matchingRequest);
  }, [apiReceived, apiRequests, initialRequestId, onInitialRequestOpened, onRequestPress]);

  const handleDistributionResponse = async (req: ServiceRequest, tipoRespuesta: "ACEPTAR" | "RECHAZAR") => {
    if (!userId || !req.distributionId) {
      setError("No se pudo identificar la distribucion recibida.");
      return;
    }
    setError("");
    setActionLoading(`${req.distributionId}-${tipoRespuesta}`);
    try {
      await servifyApi.respondToDistribution({
        distribucionSolicitudId: req.distributionId,
        prestadorId: userId,
        tipoRespuesta,
      });
      await loadRequests();
      if (tipoRespuesta === "ACEPTAR") {
        onRequestPress({ ...req, rawStatus: "ACEPTADA", status: "in-progress", providerId: userId });
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo responder la solicitud");
    } finally {
      setActionLoading("");
    }
  };

  const handleCounterOffer = async (req: ServiceRequest) => {
    if (!userId || !req.distributionId) {
      setError("No se pudo identificar la distribucion recibida.");
      return;
    }
    if (!counterPrice.trim()) {
      setError("Indica un importe para contraofertar.");
      return;
    }
    setError("");
    setActionLoading(`${req.distributionId}-CONTRAOFERTA`);
    try {
      await servifyApi.createCounterOffer({
        distribucionSolicitudId: req.distributionId,
        prestadorId: userId,
        precioPropuesto: counterPrice,
        mensaje: counterMessage,
      });
      setCounterOfferFor(null);
      setCounterPrice("");
      setCounterMessage("");
      await loadRequests();
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo emitir la contraoferta");
    } finally {
      setActionLoading("");
    }
  };

  const handleKeepSearchingCounterOffer = async (req: ServiceRequest) => {
    if (!userId || !req.counterOfferId) {
      setError("Abri el detalle para resolver esta contraoferta.");
      return;
    }
    setError("");
    setActionLoading(`${req.id}-KEEP_SEARCHING`);
    try {
      await servifyApi.resolveCounterOffer({
        contraofertaId: req.counterOfferId,
        solicitanteId: userId,
        decision: "RECHAZAR",
      });
      await servifyApi.retryDistribution(String(req.id));
      await loadRequests();
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo seguir buscando prestadores");
    } finally {
      setActionLoading("");
    }
  };

  const openEditRequest = (req: ServiceRequest) => {
    setEditingRequest(req);
    setEditForm({
      description: req.description,
      modal: req.modal,
      locality: req.locality ?? req.location ?? LOCATION_OPTIONS[0],
      price: req.price === "A convenir" ? "" : req.price,
      availabilityDay: req.availabilityDay ?? WEEK_DAYS[0].value,
      availabilityFrom: req.availabilityFrom ?? "09:00",
      availabilityTo: req.availabilityTo ?? "18:00",
    });
    setError("");
  };

  const handleUpdateRequest = async () => {
    if (!userId || !editingRequest) return;
    if (!editForm.description.trim()) {
      setError("La descripcion no puede estar vacia.");
      return;
    }

    setError("");
    setActionLoading(`${editingRequest.id}-EDIT`);
    try {
      await servifyApi.updateServiceRequest({
        solicitudId: String(editingRequest.id),
        solicitanteId: userId,
        descripcion: editForm.description,
        modalidad: editForm.modal,
        localidad: editForm.locality,
        precio: editForm.price,
        disponibilidadDia: editForm.availabilityDay,
        horaDesde: editForm.availabilityFrom,
        horaHasta: editForm.availabilityTo,
      });
      setEditingRequest(null);
      await loadRequests();
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo editar la solicitud");
    } finally {
      setActionLoading("");
    }
  };

  const handleCancelRequest = async (req: ServiceRequest, removeFromList: boolean) => {
    if (!userId) {
      setError("No se pudo identificar el usuario actual.");
      return;
    }
    const message = removeFromList
      ? "Eliminar esta solicitud la cancela en el backend y la quita de esta lista."
      : "Cancelar esta solicitud la marca como cancelada en el backend.";
    if (!window.confirm(message)) return;

    setError("");
    setActionLoading(`${req.id}-${removeFromList ? "DELETE" : "CANCEL"}`);
    try {
      if (removeFromList) {
        await servifyApi.deleteRequest(String(req.id), userId);
        setApiRequests((items) => items.filter((item) => item.id !== req.id));
      } else {
        await servifyApi.cancelRequest(String(req.id), userId);
        await loadRequests();
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo actualizar la solicitud");
    } finally {
      setActionLoading("");
    }
  };

  return (
    <div className="servify-dark-screen flex flex-col h-full" style={{ background: "#f8fafc" }}>
      {/* Header */}
      <div className="servify-page-header px-5 pt-12 pb-0 bg-white">
        <div className="flex items-center justify-between mb-4">
          <h1 style={{ fontSize: 24, fontWeight: 800, color: "#0f172a" }}>Solicitudes</h1>
          <button
            onClick={onNewRequest}
            className="flex items-center gap-1.5 px-4 py-2 rounded-xl transition-all active:scale-95"
            style={{ background: "#2563eb", color: "white", fontWeight: 700, fontSize: 13 }}
          >
            <Plus size={16} strokeWidth={2.5} />
            Nueva
          </button>
        </div>

        {/* Tabs */}
        <div className="flex gap-1">
          {tabs.map(({ id, label }) => (
            <button
              key={id}
              onClick={() => setTab(id)}
              className={`servify-tab-button px-3.5 py-2.5 rounded-t-xl transition-all ${tab === id ? "servify-tab-active" : ""}`}
              style={{
                background: tab === id ? "#f8fafc" : "transparent",
                color: tab === id ? "#2563eb" : "#94a3b8",
                fontWeight: tab === id ? 700 : 500,
                fontSize: 13,
                borderBottom: tab === id ? "2px solid #2563eb" : "2px solid transparent",
              }}
            >
              {label}
            </button>
          ))}
        </div>

        <div className="mt-3 flex gap-2 overflow-x-auto pb-3">
          {timeFilterOptions.map((option) => (
            <button
              key={option.id}
              type="button"
              onClick={() => setTimeFilter(option.id)}
              className="servify-chip shrink-0 rounded-full px-3 py-1.5 transition-all active:scale-95"
              style={{
                background: timeFilter === option.id ? "#dbeafe" : "#f1f5f9",
                color: timeFilter === option.id ? "#2563eb" : "#64748b",
                fontSize: 11,
                fontWeight: 900,
              }}
            >
              {option.label}
            </button>
          ))}
        </div>
      </div>

      {/* List */}
      <div className="flex-1 overflow-y-auto px-5 pt-4 pb-6 flex flex-col gap-3">
        {error && (
          <p className="rounded-2xl px-4 py-3" style={{ background: "#fef2f2", color: "#b91c1c", fontSize: 13, fontWeight: 700 }}>
            {error}
          </p>
        )}
        {data.length === 0 && !error && (
          <p className="servify-empty-state rounded-2xl px-4 py-3" style={{ background: "#f8fafc", color: "#64748b", fontSize: 13, fontWeight: 700 }}>
            No hay resultados.
          </p>
        )}

        {data.map((req, i) => {
          const st = statusConfig[req.status];
          const showRepeat = canRepeatOwnRequest(req);
          const showManage = canManageOwnRequest(req);
          const showKeepSearching = canKeepSearchingCounterOffer(req);
          return (
            <motion.div
              key={req.id}
              initial={{ opacity: 0, y: 8 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.3, delay: i * 0.06 }}
              onClick={() => onRequestPress(req)}
              role="button"
              tabIndex={0}
              className="servify-card servify-request-card bg-white rounded-2xl p-4 text-left w-full transition-all active:scale-[0.98]"
              style={{ border: "1px solid rgba(0,0,0,0.06)", boxShadow: "0 1px 4px rgba(0,0,0,0.04)" }}
            >
              <div className="flex items-start justify-between gap-2 mb-2">
                <p style={{ fontWeight: 700, fontSize: 14, color: "#0f172a", flex: 1, lineHeight: 1.3 }}>
                  {req.title}
                </p>
                <span
                  className="servify-status-badge px-2.5 py-1 rounded-full shrink-0"
                  style={{ background: st.bg, color: st.color, fontSize: 11, fontWeight: 700 }}
                >
                  {st.label}
                </span>
              </div>

              <p style={{ fontSize: 12, color: "#64748b", marginBottom: 12, lineHeight: 1.5 }}>
                {req.description}
              </p>

              <div className="flex flex-wrap gap-2 mb-3">
                <Chip label={req.category} color="#0891b2" bg="#ecfeff" />
                <Chip label={req.modal} color="#7c3aed" bg="#f5f3ff" />
              </div>

              {req.viewerRole === "SOLICITANTE" && req.providerName ? (
                <div className="servify-form-surface mb-3 flex items-center gap-2 rounded-xl px-3 py-2" style={{ background: "#f0fdf4", border: "1px solid #bbf7d0" }}>
                  <div className="flex items-center justify-center rounded-full" style={{ width: 24, height: 24, background: "#dcfce7", flexShrink: 0 }}>
                    <span style={{ fontSize: 10, fontWeight: 800, color: "#16a34a" }}>{req.providerInitials}</span>
                  </div>
                  <span style={{ fontSize: 12, color: "#166534", fontWeight: 800 }}>
                    Prestador: {req.providerName}
                  </span>
                </div>
              ) : null}

              <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <div className="flex items-center gap-1">
                    <MapPin size={12} color="#94a3b8" strokeWidth={1.8} />
                    <span style={{ fontSize: 11, color: "#94a3b8", fontWeight: 500 }}>{req.location}</span>
                  </div>
                  <div className="flex items-center gap-1">
                    <Users size={12} color="#94a3b8" strokeWidth={1.8} />
                    <span style={{ fontSize: 11, color: "#94a3b8", fontWeight: 500 }}>
                      {req.proposals} prop.
                    </span>
                  </div>
                  <div className="flex items-center gap-1">
                    <Clock size={12} color="#94a3b8" strokeWidth={1.8} />
                    <span style={{ fontSize: 11, color: "#94a3b8", fontWeight: 500 }}>{req.schedule}</span>
                  </div>
                </div>
                <div className="flex items-center gap-1">
                  <DollarSign size={12} color="#2563eb" strokeWidth={2} />
                  <span style={{ fontSize: 12, color: "#2563eb", fontWeight: 700 }}>{req.price}</span>
                </div>
              </div>

              {req.viewerRole === "SOLICITANTE" && (showRepeat || showManage || showKeepSearching) ? (
                <div
                  className="servify-card-footer mt-4 pt-3 grid grid-cols-2 gap-2"
                  style={{ borderTop: "1px solid #f1f5f9" }}
                  onClick={(event) => event.stopPropagation()}
                >
                  {showRepeat ? (
                    <button
                      type="button"
                      disabled={Boolean(actionLoading)}
                      onClick={() => onRepeatRequest(req)}
                      className="servify-action-button servify-action-teal flex items-center justify-center gap-1.5 py-2.5 rounded-xl transition-all active:scale-95"
                      style={{ fontSize: 12, fontWeight: 800 }}
                    >
                      <RefreshCcw size={14} strokeWidth={2} />
                      Repetir
                    </button>
                  ) : null}
                  {showKeepSearching ? (
                    <button
                      type="button"
                      disabled={Boolean(actionLoading)}
                      onClick={() => handleKeepSearchingCounterOffer(req)}
                      className="servify-action-button flex items-center justify-center gap-1.5 py-2.5 rounded-xl transition-all active:scale-95"
                      style={{ background: "#fffbeb", color: "#d97706", border: "1.5px solid #fde68a", fontSize: 12, fontWeight: 800 }}
                    >
                      <RefreshCcw size={14} strokeWidth={2} />
                      Seguir buscando
                    </button>
                  ) : null}
                  {showManage ? (
                    <>
                  <button
                    type="button"
                    disabled={Boolean(actionLoading) || !canEditOwnRequest(req)}
                    onClick={() => openEditRequest(req)}
                    className="servify-action-button flex items-center justify-center gap-1.5 py-2.5 rounded-xl transition-all active:scale-95"
                    style={{ background: "#eff6ff", color: "#2563eb", border: "1.5px solid #bfdbfe", fontSize: 12, fontWeight: 800, opacity: canEditOwnRequest(req) ? 1 : 0.55 }}
                  >
                    <Edit2 size={14} strokeWidth={2} />
                    Editar
                  </button>
                  <button
                    type="button"
                    disabled={Boolean(actionLoading)}
                    onClick={() => handleCancelRequest(req, false)}
                    className="servify-action-button flex items-center justify-center gap-1.5 py-2.5 rounded-xl transition-all active:scale-95"
                    style={{ background: "#fffbeb", color: "#d97706", border: "1.5px solid #fde68a", fontSize: 12, fontWeight: 800 }}
                  >
                    <Ban size={14} strokeWidth={2} />
                    Cancelar
                  </button>
                  <button
                    type="button"
                    disabled={Boolean(actionLoading)}
                    onClick={() => handleCancelRequest(req, true)}
                    className="servify-action-button flex items-center justify-center gap-1.5 py-2.5 rounded-xl transition-all active:scale-95"
                    style={{ background: "#fef2f2", color: "#dc2626", border: "1.5px solid #fecaca", fontSize: 12, fontWeight: 800 }}
                  >
                    <Trash2 size={14} strokeWidth={2} />
                    Eliminar
                  </button>
                    </>
                  ) : null}
                </div>
              ) : null}

              {canProviderRespond(req) ? (
                <div className="servify-card-footer mt-4 pt-3" style={{ borderTop: "1px solid #f1f5f9" }} onClick={(event) => event.stopPropagation()}>
                  <div className="grid grid-cols-3 gap-2">
                    <button
                      type="button"
                      disabled={Boolean(actionLoading)}
                      onClick={() => handleDistributionResponse(req, "ACEPTAR")}
                      className="servify-action-button flex items-center justify-center gap-1.5 py-2.5 rounded-xl transition-all active:scale-95"
                      style={{ background: "#f0fdf4", color: "#16a34a", border: "1.5px solid #bbf7d0", fontSize: 12, fontWeight: 800 }}
                    >
                      <CheckCircle size={14} strokeWidth={2} />
                      Aceptar
                    </button>
                    <button
                      type="button"
                      disabled={Boolean(actionLoading)}
                      onClick={() => handleDistributionResponse(req, "RECHAZAR")}
                      className="servify-action-button flex items-center justify-center gap-1.5 py-2.5 rounded-xl transition-all active:scale-95"
                      style={{ background: "#fef2f2", color: "#dc2626", border: "1.5px solid #fecaca", fontSize: 12, fontWeight: 800 }}
                    >
                      <XCircle size={14} strokeWidth={2} />
                      Rechazar
                    </button>
                    <button
                      type="button"
                      disabled={Boolean(actionLoading)}
                      onClick={() => {
                        setCounterOfferFor(counterOfferFor === req.distributionId ? null : req.distributionId ?? null);
                        setCounterPrice("");
                        setCounterMessage("");
                      }}
                      className="servify-action-button flex items-center justify-center gap-1.5 py-2.5 rounded-xl transition-all active:scale-95"
                      style={{ background: "#fffbeb", color: "#d97706", border: "1.5px solid #fde68a", fontSize: 12, fontWeight: 800 }}
                    >
                      <MessageSquare size={14} strokeWidth={2} />
                      Ofertar
                    </button>
                  </div>

                  {counterOfferFor === req.distributionId ? (
                    <div className="servify-form-surface mt-3 flex flex-col gap-2 rounded-2xl p-3" style={{ background: "#f8fafc", border: "1px solid #e2e8f0" }}>
                      <input
                        value={counterPrice}
                        onChange={(event) => setCounterPrice(event.target.value)}
                        type="number"
                        placeholder="Importe propuesto"
                        className="w-full rounded-xl px-3 py-2 outline-none"
                        style={{ border: "1px solid #dbeafe", fontSize: 13, color: "#0f172a" }}
                      />
                      <textarea
                        value={counterMessage}
                        onChange={(event) => setCounterMessage(event.target.value)}
                        rows={2}
                        placeholder="Mensaje opcional"
                        className="w-full rounded-xl px-3 py-2 outline-none resize-none"
                        style={{ border: "1px solid #dbeafe", fontSize: 13, color: "#0f172a" }}
                      />
                      <button
                        type="button"
                        disabled={Boolean(actionLoading)}
                        onClick={() => handleCounterOffer(req)}
                        className="w-full py-2.5 rounded-xl transition-all active:scale-95"
                        style={{ background: "#2563eb", color: "white", fontSize: 13, fontWeight: 800 }}
                      >
                        {actionLoading.endsWith("CONTRAOFERTA") ? "Enviando..." : "Enviar contraoferta"}
                      </button>
                    </div>
                  ) : null}
                </div>
              ) : null}
            </motion.div>
          );
        })}
      </div>

      {editingRequest ? (
        <EditRequestSheet
          form={editForm}
          loading={actionLoading.endsWith("EDIT")}
          onChange={setEditForm}
          onClose={() => setEditingRequest(null)}
          onSave={handleUpdateRequest}
        />
      ) : null}
    </div>
  );
}

function mapRequest(
  req: ApiRequest | ApiReceivedRequest,
  viewerRole: "SOLICITANTE" | "PRESTADOR" | null,
  categoryMap: Map<string, string>,
  requesterMap: Map<string, string>
): ServiceRequest {
  const description = req.descripcionNecesidad ?? "Solicitud de servicio";
  const title = description.split(".")[0] || "Solicitud de servicio";
  const locality = req.ubicacion?.localidad || req.ubicacion?.ciudad || "CABA";
  const requesterId = req.solicitanteId || "";
  const requesterName = requesterId ? requesterMap.get(requesterId) ?? `Usuario ${requesterId.slice(0, 6)}` : "Solicitante";
  const date = req.fechaSolicitud ?? req.createdAt;
  const received = req as ApiReceivedRequest;
  const rawStatus = received.estadoDistribucion ?? req.estado;
  return {
    id: req.id,
    viewerRole,
    title,
    description,
    category: req.categoriaServicioId
      ? categoryMap.get(req.categoriaServicioId) ?? `Categoría ${req.categoriaServicioId.slice(0, 6)}`
      : "Sin categoría",
    location: locality,
    proposals: 0,
    price: formatMoney(req.precioReferencia),
    schedule: formatAvailability(req),
    date: date ? new Date(date).toLocaleDateString("es-AR") : "Sin fecha",
    rawDate: date,
    status: toUiStatus(rawStatus),
    requesterName,
    requesterInitials: initialsFromName(requesterName),
    modal: fromApiModality(req.modalidadServicio),
    locality,
    availabilityDay: req.disponibilidadRequerida?.diaSemana,
    availabilityFrom: req.disponibilidadRequerida?.horaDesde?.slice(0, 5),
    availabilityTo: req.disponibilidadRequerida?.horaHasta?.slice(0, 5),
    distributionId: received.distribucionSolicitudId,
    providerId: received.prestadorId,
    rawStatus,
  };
}

function formatAvailability(req: ApiRequest): string {
  const availability = req.disponibilidadRequerida;
  if (!availability) return "Horario a coordinar";
  const day = WEEK_DAYS.find((item) => item.value === availability.diaSemana)?.label ?? availability.diaSemana;
  return `${day} ${availability.horaDesde.slice(0, 5)}-${availability.horaHasta.slice(0, 5)}`;
}

function toUiStatus(status?: string): ServiceRequest["status"] {
  if (status === "CANCELADA" || status === "RECHAZADA" || status === "EXPIRADA") return "cancelled";
  if (status === "FINALIZADA") return "completed";
  if (status === "CONTRAOFERTADA") return "counter-offer";
  if (
    status === "ASIGNADA" ||
    status === "EN_CURSO" ||
    status === "ACEPTADA" ||
    status === "CERRADA"
  ) return "in-progress";
  return "open";
}

function applyAssignmentState(
  req: ServiceRequest,
  state: AssignmentState | null,
  providerMap: Map<string, string> = new Map()
): ServiceRequest {
  if (!state) return req;

  const acceptedCount = state.distribucionesAceptadas?.length ?? 0;
  const counterOfferCount = state.contraofertasPendientes?.length ?? 0;
  const activeCount = state.distribucionesActivas ?? 0;
  const proposals = Math.max(req.proposals, acceptedCount, counterOfferCount, activeCount);
  const providerId =
    state.asignacion?.prestadorId ??
    state.distribucionesAceptadas?.[0]?.prestadorId ??
    state.contraofertasPendientes?.[0]?.prestadorId ??
    req.providerId;
  const counterOfferId = state.contraofertasPendientes?.[0]?.id ?? req.counterOfferId;
  const providerName = providerId ? providerMap.get(providerId) ?? "Prestador asignado" : req.providerName;
  const providerInitials = providerName ? initialsFromName(providerName) : req.providerInitials;
  const withProvider = {
    ...req,
    proposals,
    providerId,
    counterOfferId,
    providerName,
    providerInitials,
  };
  const nextRawStatus =
    state.asignacion?.estado ??
    state.distribucionesAceptadas?.[0]?.estado ??
    (counterOfferCount > 0 ? "CONTRAOFERTADA" : undefined) ??
    state.estadoSolicitud ??
    req.rawStatus;

  if (state.finalizacionConfirmada || state.estadoSolicitud === "FINALIZADA" || state.asignacion?.estado === "FINALIZADA") {
    return { ...withProvider, rawStatus: nextRawStatus, status: "completed" };
  }

  if (state.asignacion || acceptedCount > 0 || state.estadoSolicitud === "ASIGNADA") {
    return { ...withProvider, rawStatus: nextRawStatus, status: "in-progress" };
  }

  if (counterOfferCount > 0) {
    return { ...withProvider, rawStatus: nextRawStatus, status: "counter-offer" };
  }

  return { ...withProvider, rawStatus: nextRawStatus };
}

function canProviderRespond(req: ServiceRequest): boolean {
  return req.viewerRole === "PRESTADOR"
    && Boolean(req.distributionId)
    && (req.rawStatus ?? "").toUpperCase() === "ENVIADA";
}

function canRepeatOwnRequest(req: ServiceRequest): boolean {
  return req.viewerRole === "SOLICITANTE"
    && !["in-progress", "counter-offer", "completed"].includes(req.status);
}

function canManageOwnRequest(req: ServiceRequest): boolean {
  return req.viewerRole === "SOLICITANTE"
    && !["completed", "cancelled", "counter-offer", "in-progress"].includes(req.status);
}

function canEditOwnRequest(req: ServiceRequest): boolean {
  return req.viewerRole === "SOLICITANTE" && req.status === "open";
}

function canKeepSearchingCounterOffer(req: ServiceRequest): boolean {
  return req.viewerRole === "SOLICITANTE" && req.status === "counter-offer";
}

function emptyEditForm(): EditRequestForm {
  return {
    description: "",
    modal: "Presencial",
    locality: LOCATION_OPTIONS[0],
    price: "",
    availabilityDay: WEEK_DAYS[0].value,
    availabilityFrom: "09:00",
    availabilityTo: "18:00",
  };
}

function EditRequestSheet({
  form,
  loading,
  onChange,
  onClose,
  onSave,
}: {
  form: EditRequestForm;
  loading: boolean;
  onChange: (next: EditRequestForm) => void;
  onClose: () => void;
  onSave: () => void;
}) {
  const update = (patch: Partial<EditRequestForm>) => onChange({ ...form, ...patch });

  return (
    <div className="absolute inset-0 z-50 flex items-end" style={{ background: "rgba(15,23,42,0.35)" }} onClick={onClose}>
      <motion.div
        initial={{ y: "100%" }}
        animate={{ y: 0 }}
        exit={{ y: "100%" }}
        transition={{ type: "spring", damping: 28, stiffness: 280 }}
        className="servify-sheet w-full bg-white rounded-t-3xl max-h-[88%] flex flex-col"
        onClick={(event) => event.stopPropagation()}
      >
        <div className="flex flex-col items-center pt-3 pb-2 shrink-0">
          <div className="rounded-full" style={{ width: 40, height: 4, background: "#e2e8f0" }} />
        </div>
        <div className="flex items-center justify-between px-6 pb-4 shrink-0">
          <div>
            <p style={{ fontSize: 19, fontWeight: 800, color: "#0f172a" }}>Editar solicitud</p>
            <p style={{ fontSize: 13, color: "#64748b" }}>Ajusta los datos activos del pedido</p>
          </div>
          <button type="button" onClick={onClose}>
            <X size={22} color="#94a3b8" strokeWidth={1.8} />
          </button>
        </div>

        <div className="overflow-y-auto px-6 pb-8 flex flex-col gap-4">
          <EditField label="Descripcion">
            <textarea
              value={form.description}
              onChange={(event) => update({ description: event.target.value })}
              rows={4}
              className="w-full bg-transparent outline-none resize-none"
              style={{ fontSize: 14, color: "#0f172a" }}
            />
          </EditField>

          <EditField label="Modalidad">
            <select
              value={form.modal}
              onChange={(event) => update({ modal: event.target.value as EditRequestForm["modal"] })}
              className="w-full bg-transparent outline-none"
              style={{ fontSize: 14, color: "#0f172a" }}
            >
              {["Presencial", "Virtual", "Ambas"].map((option) => (
                <option key={option} value={option}>{option}</option>
              ))}
            </select>
          </EditField>

          <EditField label="Localidad">
            <select
              value={form.locality}
              onChange={(event) => update({ locality: event.target.value })}
              className="w-full bg-transparent outline-none"
              style={{ fontSize: 14, color: "#0f172a" }}
            >
              {LOCATION_OPTIONS.map((option) => (
                <option key={option} value={option}>{option}</option>
              ))}
            </select>
          </EditField>

          <EditField label="Disponibilidad">
            <div className="grid grid-cols-3 gap-2">
              <select
                value={form.availabilityDay}
                onChange={(event) => update({ availabilityDay: event.target.value })}
                className="bg-transparent outline-none min-w-0"
                style={{ fontSize: 13, color: "#0f172a" }}
              >
                {WEEK_DAYS.map((day) => (
                  <option key={day.value} value={day.value}>{day.label}</option>
                ))}
              </select>
              <select
                value={form.availabilityFrom}
                onChange={(event) => update({ availabilityFrom: event.target.value })}
                className="bg-transparent outline-none min-w-0"
                style={{ fontSize: 13, color: "#0f172a" }}
              >
                {TIME_OPTIONS.map((time) => (
                  <option key={time} value={time}>{time}</option>
                ))}
              </select>
              <select
                value={form.availabilityTo}
                onChange={(event) => update({ availabilityTo: event.target.value })}
                className="bg-transparent outline-none min-w-0"
                style={{ fontSize: 13, color: "#0f172a" }}
              >
                {TIME_OPTIONS.map((time) => (
                  <option key={time} value={time}>{time}</option>
                ))}
              </select>
            </div>
          </EditField>

          <EditField label="Precio sugerido">
            <input
              value={form.price}
              onChange={(event) => update({ price: event.target.value })}
              placeholder="Opcional"
              className="w-full bg-transparent outline-none"
              style={{ fontSize: 14, color: "#0f172a" }}
            />
          </EditField>

          <button
            type="button"
            onClick={onSave}
            disabled={loading}
            className="w-full py-3.5 rounded-2xl transition-all active:scale-95 flex items-center justify-center gap-2"
            style={{ background: "#2563eb", color: "white", fontWeight: 800, fontSize: 15, opacity: loading ? 0.8 : 1 }}
          >
            <Save size={16} strokeWidth={2.2} />
            {loading ? "Guardando..." : "Guardar cambios"}
          </button>
        </div>
      </motion.div>
    </div>
  );
}

function EditField({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div>
      <p style={{ fontSize: 13, fontWeight: 700, color: "#475569", marginBottom: 8 }}>{label}</p>
      <div className="servify-form-surface px-4 py-3.5 rounded-2xl bg-white" style={{ border: "1.5px solid #e2e8f0" }}>
        {children}
      </div>
    </div>
  );
}

function Chip({ label, color, bg }: { label: string; color: string; bg: string }) {
  return (
    <span
      className="servify-chip px-2.5 py-1 rounded-full"
      style={{ background: bg, color, fontSize: 11, fontWeight: 700 }}
    >
      {label}
    </span>
  );
}

async function buildRequesterMap(
  own: ApiRequest[],
  received: ApiReceivedRequest[]
): Promise<Map<string, string>> {
  const ids = Array.from(
    new Set(
      [...own, ...received]
        .map((req) => req.solicitanteId)
        .filter((id): id is string => Boolean(id))
    )
  );
  const profiles = await Promise.all(
    ids.map(async (id) => ({
      id,
      profile: await servifyApi.getUserProfile(id).catch(() => null),
    }))
  );
  const map = new Map<string, string>();
  profiles.forEach(({ id, profile }) => {
    const name = formatProfileName(profile);
    if (name) map.set(id, name);
  });
  return map;
}

async function buildProfileNameMap(ids: string[]): Promise<Map<string, string>> {
  const uniqueIds = Array.from(new Set(ids.filter(Boolean)));
  const profiles = await Promise.all(
    uniqueIds.map(async (id) => ({
      id,
      profile: await servifyApi.getUserProfile(id).catch(() => null),
    }))
  );
  const map = new Map<string, string>();
  profiles.forEach(({ id, profile }) => {
    const name = formatProfileName(profile);
    if (name) map.set(id, name);
  });
  return map;
}

function collectProviderIds(
  received: ApiReceivedRequest[],
  ownStates: { state: AssignmentState | null }[],
  receivedStates: { state: AssignmentState | null }[]
): string[] {
  const stateProviderIds = [...ownStates, ...receivedStates].flatMap(({ state }) => [
    state?.asignacion?.prestadorId,
    state?.distribucionesAceptadas?.[0]?.prestadorId,
    state?.contraofertasPendientes?.[0]?.prestadorId,
  ]);
  return [
    ...received.map((req) => req.prestadorId),
    ...stateProviderIds,
  ].filter((id): id is string => Boolean(id));
}

function sortRequestsForDisplay(items: ServiceRequest[]): ServiceRequest[] {
  return [...items].sort((a, b) => requestStatusPriority(a) - requestStatusPriority(b));
}

function filterRequestsByTime(items: ServiceRequest[], filter: TimeFilter): ServiceRequest[] {
  if (filter === "all") return items;
  if (filter === "active") {
    return items.filter((req) => ["open", "in-progress", "counter-offer"].includes(req.status));
  }

  const days = Number(filter);
  const since = Date.now() - days * 24 * 60 * 60 * 1000;
  return items.filter((req) => {
    if (["open", "in-progress", "counter-offer"].includes(req.status)) return true;
    const date = req.rawDate ? new Date(req.rawDate) : null;
    return Boolean(date && !Number.isNaN(date.getTime()) && date.getTime() >= since);
  });
}

function requestStatusPriority(req: ServiceRequest): number {
  if (req.status === "in-progress") return 0;
  if (req.status === "counter-offer") return 1;
  if (req.status === "open") return 2;
  if (req.status === "completed") return 3;
  return 4;
}

function buildCategoryMap(categories: ApiCategory[]): Map<string, string> {
  return new Map(categories.map((category) => [category.id, category.nombre]));
}

function formatProfileName(profile: ApiUserProfile | null): string {
  if (!profile) return "";
  const parts = [profile.nombre, profile.apellido].filter(Boolean);
  return parts.join(" ").trim();
}

function initialsFromName(name: string): string {
  return name
    .split(" ")
    .map((chunk) => chunk[0])
    .join("")
    .slice(0, 2)
    .toUpperCase();
}
