import React, { useEffect, useState } from "react";
import {
  ChevronLeft,
  MapPin,
  Calendar,
  DollarSign,
  Tag,
  Smartphone,
  CheckCircle,
  Star,
  Loader2,
  AlertCircle,
  RefreshCw,
  XCircle,
  MessageSquare,
} from "lucide-react";
import { motion, AnimatePresence } from "motion/react";
import type { SessionUser } from "../api";
import { formatMoney, servifyApi } from "../api";
import type { ServiceRequest } from "./RequestsScreen";

export interface RatingTarget {
  name: string;
  solicitudId: string;
  asignacionServicioId: string;
  solicitanteId: string;
  prestadorId: string;
  calificadorId: string;
  rolCalificador: "SOLICITANTE" | "PRESTADOR";
}

interface RequestDetailProps {
  request: ServiceRequest;
  onBack: () => void;
  onRate: (target: RatingTarget) => void;
  currentUser?: SessionUser | null;
}

const proposalData = {
  providerName: "Prestador",
  providerInitials: "PR",
  offeredPrice: "A convenir",
  message: "No hay detalles adicionales de propuesta disponibles.",
  confirmedRequester: true,
  confirmedProvider: false,
};

type AssignmentState = Awaited<ReturnType<typeof servifyApi.getAssignmentState>>;

export function RequestDetail({ request, onBack, onRate, currentUser }: RequestDetailProps) {
  const [assignmentState, setAssignmentState] = useState<AssignmentState | null>(null);
  const [loadingState, setLoadingState] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [showComplete, setShowComplete] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    if (typeof request.id !== "string") {
      setAssignmentState(null);
      setShowComplete(false);
      return;
    }

    let ignore = false;
    setLoadingState(true);
    setError("");

    servifyApi
      .getAssignmentState(request.id)
      .then((state) => {
        if (ignore) return;
        setAssignmentState(state);
        setShowComplete(
          Boolean(state.finalizacionConfirmada) ||
            state.estadoSolicitud === "FINALIZADA" ||
            state.asignacion?.estado === "FINALIZADA"
        );
      })
      .catch((err) => {
        if (ignore) return;
        setAssignmentState(null);
        setError(err instanceof Error ? err.message : "No se pudo cargar el estado del servicio");
      })
      .finally(() => {
        if (!ignore) setLoadingState(false);
      });

    return () => {
      ignore = true;
    };
  }, [request.id]);

  const assignment = assignmentState?.asignacion;
  const acceptedDistribution = assignmentState?.distribucionesAceptadas?.[0];
  const pendingCounterOffer = assignmentState?.contraofertasPendientes?.[0];
  const providerId = assignment?.prestadorId ?? acceptedDistribution?.prestadorId ?? pendingCounterOffer?.prestadorId ?? "";
  const providerName = providerId ? `Prestador ${providerId.slice(0, 6)}` : proposalData.providerName;
  const providerInitials = providerName
    .split(" ")
    .map((part) => part[0])
    .join("")
    .slice(0, 2)
    .toUpperCase();
  const proposalMessage = assignment?.estado
    ? `Estado de asignación: ${assignment.estado}`
    : proposalData.message;
  const assignmentCompleted = assignment?.estado === "FINALIZADA" || assignmentState?.estadoSolicitud === "FINALIZADA";
  const requesterConfirmed = assignmentCompleted || (assignmentState?.confirmadoPorSolicitante ?? false);
  const providerConfirmed = assignmentCompleted || (assignmentState?.confirmadoPorPrestador ?? false);
  const isCompleted = request.status === "completed" || assignmentCompleted || Boolean(assignmentState?.finalizacionConfirmada);
  const hasProposal = Boolean(assignment);
  const hasAcceptedPending = !assignment && Boolean(acceptedDistribution?.id);
  const hasPendingCounterOffer = !assignment && Boolean(pendingCounterOffer?.id);
  const confirmationRole = request.viewerRole;
  const acceptedPrice = assignment?.precioAcordado ? formatMoney(Number(assignment.precioAcordado)) : proposalData.offeredPrice;
  const counterOfferPrice = pendingCounterOffer?.precioPropuesto
    ? formatMoney(Number(pendingCounterOffer.precioPropuesto))
    : proposalData.offeredPrice;
  const canConfirm = Boolean(currentUser?.id && assignment?.id && confirmationRole && !isCompleted && !submitting);
  const canResolveCounterOffer = Boolean(
    currentUser?.id &&
      pendingCounterOffer?.id &&
      request.viewerRole === "SOLICITANTE" &&
      assignmentState?.solicitanteId === currentUser.id &&
      !submitting
  );
  const canConfirmAssignment = Boolean(
    currentUser?.id &&
      acceptedDistribution?.id &&
      request.viewerRole === "SOLICITANTE" &&
      assignmentState?.solicitanteId === currentUser.id &&
      !submitting
  );
  const bothConfirmed = requesterConfirmed && providerConfirmed;
  const canRate = Boolean(
    assignment?.id &&
      assignmentState?.solicitanteId &&
      assignment?.prestadorId &&
      currentUser?.id &&
      confirmationRole &&
      isCompleted
  );
  const ratingTargetName = confirmationRole === "PRESTADOR" ? request.requesterName : providerName;

  const handleConfirmAssignment = async () => {
    if (!currentUser?.id || !acceptedDistribution?.id) return;
    setSubmitting(true);
    setError("");

    try {
      await servifyApi.confirmAssignment({
        solicitudId: String(request.id),
        distribucionSolicitudId: acceptedDistribution.id,
        solicitanteId: currentUser.id,
      });
      const updated = await servifyApi.getAssignmentState(String(request.id));
      setAssignmentState(updated);
      setShowComplete(
        Boolean(updated.finalizacionConfirmada) ||
          updated.estadoSolicitud === "FINALIZADA" ||
          updated.asignacion?.estado === "FINALIZADA"
      );
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo confirmar el prestador");
    } finally {
      setSubmitting(false);
    }
  };

  const handleResolveCounterOffer = async (decision: "ACEPTAR" | "RECHAZAR") => {
    if (!currentUser?.id || !pendingCounterOffer?.id) return;
    setSubmitting(true);
    setError("");

    try {
      const resolved = await servifyApi.resolveCounterOffer({
        contraofertaId: pendingCounterOffer.id,
        solicitanteId: currentUser.id,
        decision,
      });
      const distribucionSolicitudId = resolved.distribucionSolicitudId ?? pendingCounterOffer.distribucionSolicitudId;
      if (decision === "ACEPTAR" && distribucionSolicitudId) {
        await servifyApi.confirmAssignment({
          solicitudId: String(request.id),
          distribucionSolicitudId,
          solicitanteId: currentUser.id,
        });
      }
      const updated = await servifyApi.getAssignmentState(String(request.id));
      setAssignmentState(updated);
      setShowComplete(
        Boolean(updated.finalizacionConfirmada) ||
          updated.estadoSolicitud === "FINALIZADA" ||
          updated.asignacion?.estado === "FINALIZADA"
      );
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo resolver la contraoferta");
    } finally {
      setSubmitting(false);
    }
  };

  const handleConfirm = async () => {
    if (!currentUser?.id || !assignment?.id || !confirmationRole) return;
    setSubmitting(true);
    setError("");

    try {
      await servifyApi.confirmServiceCompletion({
        solicitudId: String(request.id),
        asignacionServicioId: assignment.id,
        confirmanteId: currentUser.id,
        rolConfirmante: confirmationRole,
        observacion: "",
      });
      const updated = await servifyApi.getAssignmentState(String(request.id));
      setAssignmentState(updated);
      setShowComplete(
        Boolean(updated.finalizacionConfirmada) ||
          updated.estadoSolicitud === "FINALIZADA" ||
          updated.asignacion?.estado === "FINALIZADA"
      );
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo confirmar la finalización");
    } finally {
      setSubmitting(false);
    }
  };

  const statusConfig: Record<string, { label: string; bg: string; color: string }> = {
    open: { label: "Abierta", bg: "#eff6ff", color: "#2563eb" },
    completed: { label: "Completada", bg: "#f0fdf4", color: "#16a34a" },
    cancelled: { label: "Cancelada", bg: "#fef2f2", color: "#ef4444" },
    "in-progress": { label: "En curso", bg: "#fffbeb", color: "#d97706" },
    "counter-offer": { label: "Contraoferta", bg: "#fff7ed", color: "#ea580c" },
  };

  const displayStatus = isCompleted
    ? "completed"
    : hasPendingCounterOffer
    ? "counter-offer"
    : hasProposal || hasAcceptedPending
    ? "in-progress"
    : request.status;
  const st = statusConfig[displayStatus] ?? statusConfig.open;

  return (
    <div className="flex flex-col h-full" style={{ background: "#f8fafc" }}>
      <div className="bg-white px-5 pt-12 pb-4">
        <button onClick={onBack} className="flex items-center gap-2 mb-4">
          <ChevronLeft size={20} color="#2563eb" strokeWidth={2} />
          <span style={{ fontSize: 14, color: "#2563eb", fontWeight: 600 }}>Volver</span>
        </button>

        <div className="flex items-start justify-between gap-3">
          <h1 style={{ fontSize: 19, fontWeight: 800, color: "#0f172a", flex: 1, lineHeight: 1.25 }}>
            {request.title}
          </h1>
          <span
            className="px-3 py-1.5 rounded-full shrink-0"
            style={{ background: st.bg, color: st.color, fontSize: 12, fontWeight: 700 }}
          >
            {st.label}
          </span>
        </div>
      </div>

      <div className="flex-1 overflow-y-auto px-5 pt-4 pb-6 flex flex-col gap-4">
        {error && (
          <div className="rounded-2xl px-4 py-3 flex items-center gap-2" style={{ background: "#fef2f2", color: "#b91c1c" }}>
            <AlertCircle size={16} strokeWidth={2} />
            <p style={{ fontSize: 13, fontWeight: 700 }}>{error}</p>
          </div>
        )}

        <Card title="Descripción">
          <p style={{ fontSize: 14, color: "#475569", lineHeight: 1.6 }}>{request.description}</p>
        </Card>

        <Card title="Detalles">
          <div className="flex flex-col gap-3">
            <DetailRow icon={<Tag size={15} color="#0891b2" strokeWidth={1.8} />} label="Categoría" value={request.category} />
            <DetailRow icon={<Smartphone size={15} color="#7c3aed" strokeWidth={1.8} />} label="Modalidad" value={request.modal} />
            <DetailRow icon={<MapPin size={15} color="#ef4444" strokeWidth={1.8} />} label="Ubicación" value={request.location} />
            <DetailRow icon={<Calendar size={15} color="#f59e0b" strokeWidth={1.8} />} label="Fecha publicación" value={request.date} />
            <DetailRow icon={<DollarSign size={15} color="#2563eb" strokeWidth={1.8} />} label="Precio sugerido" value={request.price} />
          </div>
        </Card>

        <Card title="Solicitante">
          <div className="flex items-center gap-3">
            <div className="flex items-center justify-center rounded-full" style={{ width: 44, height: 44, background: "#eff6ff", flexShrink: 0 }}>
              <span style={{ fontWeight: 800, fontSize: 14, color: "#2563eb" }}>{request.requesterInitials}</span>
            </div>
            <div>
              <p style={{ fontWeight: 700, fontSize: 14, color: "#0f172a" }}>{request.requesterName}</p>
              <p style={{ fontSize: 12, color: "#64748b" }}>Solicitante del servicio</p>
            </div>
          </div>
        </Card>

        {hasPendingCounterOffer && (
          <Card title={request.viewerRole === "SOLICITANTE" ? "Contraoferta recibida" : "Contraoferta enviada"}>
            <div className="flex items-center gap-3 mb-3">
              <div className="flex items-center justify-center rounded-full" style={{ width: 44, height: 44, background: "#fff7ed", flexShrink: 0 }}>
                <MessageSquare size={20} color="#ea580c" strokeWidth={2} />
              </div>
              <div className="flex-1">
                <p style={{ fontWeight: 700, fontSize: 14, color: "#0f172a" }}>{providerName}</p>
                <p style={{ fontSize: 12, color: "#64748b", marginTop: 2 }}>
                  {request.viewerRole === "SOLICITANTE"
                    ? "El prestador propuso nuevas condiciones para este pedido."
                    : "Esperando respuesta del solicitante."}
                </p>
              </div>
              <div className="px-3 py-1.5 rounded-xl" style={{ background: "#fffbeb" }}>
                <span style={{ fontSize: 14, fontWeight: 800, color: "#d97706" }}>{counterOfferPrice}</span>
              </div>
            </div>

            <div className="p-3 rounded-xl" style={{ background: "#f8fafc", border: "1px solid #e2e8f0" }}>
              <p style={{ fontSize: 13, color: "#475569", lineHeight: 1.6 }}>
                {pendingCounterOffer?.mensaje || "El prestador no agrego un mensaje."}
              </p>
            </div>

            {request.viewerRole === "SOLICITANTE" && (
              <div className="grid grid-cols-2 gap-2 mt-3">
                <button
                  type="button"
                  onClick={() => handleResolveCounterOffer("ACEPTAR")}
                  disabled={!canResolveCounterOffer}
                  className="flex items-center justify-center gap-1.5 py-3 rounded-xl transition-all active:scale-95"
                  style={{ background: "#16a34a", color: "white", fontWeight: 800, fontSize: 13, opacity: canResolveCounterOffer ? 1 : 0.65 }}
                >
                  <CheckCircle size={15} strokeWidth={2} />
                  {submitting ? "Procesando..." : "Aceptar"}
                </button>
                <button
                  type="button"
                  onClick={() => handleResolveCounterOffer("RECHAZAR")}
                  disabled={!canResolveCounterOffer}
                  className="flex items-center justify-center gap-1.5 py-3 rounded-xl transition-all active:scale-95"
                  style={{ background: "#fef2f2", color: "#dc2626", border: "1.5px solid #fecaca", fontWeight: 800, fontSize: 13, opacity: canResolveCounterOffer ? 1 : 0.65 }}
                >
                  <XCircle size={15} strokeWidth={2} />
                  Rechazar
                </button>
              </div>
            )}
          </Card>
        )}

        {hasAcceptedPending && (
          <Card title={request.viewerRole === "SOLICITANTE" ? "Prestador disponible" : "Aceptacion enviada"}>
            <div className="flex items-center gap-3 mb-3">
              <div className="flex items-center justify-center rounded-full" style={{ width: 44, height: 44, background: "#f0fdf4", flexShrink: 0 }}>
                <span style={{ fontWeight: 800, fontSize: 14, color: "#16a34a" }}>{providerInitials}</span>
              </div>
              <div className="flex-1">
                <p style={{ fontWeight: 700, fontSize: 14, color: "#0f172a" }}>{providerName}</p>
                <p style={{ fontSize: 12, color: "#64748b", marginTop: 2 }}>
                  {request.viewerRole === "SOLICITANTE"
                    ? "Acepto tu solicitud. Confirmalo para iniciar el servicio."
                    : "Ya aceptaste esta solicitud. Falta que el solicitante confirme la asignacion."}
                </p>
              </div>
            </div>

            {request.viewerRole === "SOLICITANTE" && (
              <button
                type="button"
                onClick={handleConfirmAssignment}
                disabled={!canConfirmAssignment}
                className="w-full py-3 rounded-xl mt-2 transition-all active:scale-95"
                style={{ background: "#16a34a", color: "white", fontWeight: 800, fontSize: 14, opacity: canConfirmAssignment ? 1 : 0.65 }}
              >
                {submitting ? "Confirmando..." : "Confirmar prestador"}
              </button>
            )}
          </Card>
        )}

        {hasProposal && (
          <Card title="Propuesta aceptada">
            <div className="flex items-center gap-3 mb-3">
              <div className="flex items-center justify-center rounded-full" style={{ width: 44, height: 44, background: "#f0fdf4", flexShrink: 0 }}>
                <span style={{ fontWeight: 800, fontSize: 14, color: "#16a34a" }}>{providerInitials}</span>
              </div>
              <div className="flex-1">
                <p style={{ fontWeight: 700, fontSize: 14, color: "#0f172a" }}>{providerName}</p>
              </div>
              <div className="px-3 py-1.5 rounded-xl" style={{ background: "#eff6ff" }}>
                <span style={{ fontSize: 14, fontWeight: 800, color: "#2563eb" }}>{acceptedPrice}</span>
              </div>
            </div>

            <div className="p-3 rounded-xl" style={{ background: "#f8fafc", border: "1px solid #e2e8f0" }}>
              <p style={{ fontSize: 13, color: "#475569", lineHeight: 1.6 }}>{proposalMessage}</p>
            </div>
          </Card>
        )}

        {hasProposal && !isCompleted && (
          <Card title="Confirmar finalización">
            <div className="rounded-2xl p-4" style={{ background: "#ffffff", border: "1.5px solid #dfe7f2" }}>
              <div className="flex items-center justify-between gap-3">
                <ConfirmationPill
                  label="Solicitante"
                  name={request.requesterName}
                  initials={request.requesterInitials}
                  confirmed={requesterConfirmed}
                />
                <div
                  className="flex items-center justify-center rounded-full"
                  style={{ width: 34, height: 34, background: "#ffffff", border: "1px solid #dbeafe", color: "#94a3b8" }}
                >
                  <RefreshCw size={16} strokeWidth={1.8} />
                </div>
                <ConfirmationPill
                  label="Prestador"
                  name={providerName}
                  initials={providerInitials}
                  confirmed={providerConfirmed}
                />
              </div>

              <div
                className="mt-4 rounded-2xl px-4 py-3 flex items-center gap-2"
                style={{ background: bothConfirmed ? "#dcfce7" : "#f8fafc", border: bothConfirmed ? "1px solid #bbf7d0" : "1px solid #e2e8f0" }}
              >
                <CheckCircle size={18} color={bothConfirmed ? "#16a34a" : "#94a3b8"} strokeWidth={2} />
                <p style={{ fontWeight: 700, fontSize: 13, color: "#15803d", lineHeight: 1.4 }}>
                  {bothConfirmed
                    ? "¡Servicio completado exitosamente!"
                    : "Confirmá ambos lados para cerrar el servicio y habilitar la calificación."}
                </p>
              </div>

              {loadingState && (
                <div className="mt-3 flex items-center gap-2 text-sm" style={{ color: "#64748b" }}>
                  <Loader2 size={14} className="animate-spin" />
                  Cargando estado real del servicio...
                </div>
              )}

              {!loadingState && confirmationRole && canConfirm && (
                <button
                  onClick={handleConfirm}
                  className="w-full py-3 rounded-xl mt-4 transition-all active:scale-95"
                  style={{ background: "#2563eb", color: "white", fontWeight: 700, fontSize: 14, opacity: submitting ? 0.85 : 1 }}
                >
                  {submitting
                    ? "Confirmando..."
                    : confirmationRole === "SOLICITANTE"
                    ? "Confirmar como solicitante"
                    : "Confirmar como prestador"}
                </button>
              )}

              {!loadingState && !confirmationRole && (
                <p style={{ fontSize: 12, color: "#64748b", lineHeight: 1.5, marginTop: 12 }}>
                  Esta vista no tiene un rol de confirmación asignado, por eso solo muestra el estado.
                </p>
              )}
            </div>
          </Card>
        )}

        <AnimatePresence>
          {(isCompleted || showComplete) && (
            <motion.div
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              className="rounded-2xl p-4 flex flex-col items-center gap-3"
              style={{ background: "#f0fdf4", border: "1.5px solid #bbf7d0" }}
            >
              <CheckCircle size={32} color="#16a34a" strokeWidth={1.8} />
              <p style={{ fontWeight: 700, fontSize: 15, color: "#15803d", textAlign: "center" }}>
                Servicio completado con confirmación de ambas partes
              </p>
              {canRate && (
                <button
                  onClick={() =>
                    onRate({
                      name: ratingTargetName,
                      solicitudId: String(request.id),
                      asignacionServicioId: assignment?.id ?? "",
                      solicitanteId: assignmentState?.solicitanteId ?? "",
                      prestadorId: assignment?.prestadorId ?? "",
                      calificadorId: currentUser?.id ?? "",
                      rolCalificador: confirmationRole as "SOLICITANTE" | "PRESTADOR",
                    })
                  }
                  className="flex items-center gap-2 px-5 py-2.5 rounded-xl transition-all active:scale-95"
                  style={{ background: "#fef3c7", border: "1.5px solid #fde68a" }}
                >
                  <Star size={16} color="#f59e0b" fill="#f59e0b" />
                  <span style={{ fontWeight: 700, fontSize: 14, color: "#d97706" }}>
                    Calificar a {ratingTargetName}
                  </span>
                </button>
              )}
              <div className="w-full flex flex-col gap-2 pt-1">
                <ConfirmRow label={request.requesterName} initials={request.requesterInitials} confirmed={requesterConfirmed} />
                <ConfirmRow label={providerName} initials={providerInitials} confirmed={providerConfirmed} />
              </div>
            </motion.div>
          )}
        </AnimatePresence>
      </div>
    </div>
  );
}

function Card({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <div className="bg-white rounded-2xl p-4" style={{ border: "1px solid rgba(0,0,0,0.06)", boxShadow: "0 1px 4px rgba(0,0,0,0.04)" }}>
      <p style={{ fontSize: 12, fontWeight: 700, color: "#94a3b8", letterSpacing: "0.08em", textTransform: "uppercase", marginBottom: 12 }}>
        {title}
      </p>
      {children}
    </div>
  );
}

function DetailRow({
  icon,
  label,
  value,
}: {
  icon: React.ReactNode;
  label: string;
  value: string;
}) {
  return (
    <div className="flex items-center justify-between">
      <div className="flex items-center gap-2">
        {icon}
        <span style={{ fontSize: 13, color: "#64748b", fontWeight: 500 }}>{label}</span>
      </div>
      <span style={{ fontSize: 13, color: "#0f172a", fontWeight: 700 }}>{value}</span>
    </div>
  );
}

function ConfirmRow({
  label,
  initials,
  confirmed,
}: {
  label: string;
  initials: string;
  confirmed: boolean;
}) {
  return (
    <div className="flex items-center justify-between">
      <div className="flex items-center gap-3">
        <div
          className="flex items-center justify-center rounded-full"
          style={{ width: 34, height: 34, background: confirmed ? "#f0fdf4" : "#f1f5f9" }}
        >
          <span style={{ fontWeight: 700, fontSize: 12, color: confirmed ? "#16a34a" : "#94a3b8" }}>
            {initials}
          </span>
        </div>
        <span style={{ fontSize: 13, color: "#0f172a", fontWeight: 600 }}>{label}</span>
      </div>
      <div
        className="flex items-center gap-1.5 px-3 py-1 rounded-full"
        style={{
          background: confirmed ? "#f0fdf4" : "#f1f5f9",
          border: confirmed ? "1px solid #bbf7d0" : "1px solid #e2e8f0",
        }}
      >
        {confirmed ? (
          <CheckCircle size={13} color="#16a34a" strokeWidth={2} />
        ) : (
          <div style={{ width: 13, height: 13, borderRadius: "50%", border: "2px solid #cbd5e1" }} />
        )}
        <span style={{ fontSize: 11, fontWeight: 700, color: confirmed ? "#16a34a" : "#94a3b8" }}>
          {confirmed ? "Confirmó" : "Pendiente"}
        </span>
      </div>
    </div>
  );
}

function ConfirmationPill({
  label,
  name,
  initials,
  confirmed,
}: {
  label: string;
  name: string;
  initials: string;
  confirmed: boolean;
}) {
  return (
    <div className="flex flex-col items-center gap-2 flex-1 text-center">
      <div
        className="flex items-center justify-center rounded-full"
        style={{ width: 52, height: 52, background: confirmed ? "#22c55e" : "#e2e8f0" }}
      >
        {confirmed ? <CheckCircle size={26} color="white" fill="white" strokeWidth={1.8} /> : <RefreshCw size={18} color="#94a3b8" strokeWidth={1.7} />}
      </div>
      <div>
        <p style={{ fontWeight: 700, fontSize: 13, color: confirmed ? "#16a34a" : "#64748b" }}>{label}</p>
        <p style={{ fontWeight: 600, fontSize: 12, color: confirmed ? "#16a34a" : "#94a3b8" }}>
          {confirmed ? "Confirmó" : "Pendiente"}
        </p>
        <p style={{ fontWeight: 600, fontSize: 12, color: "#0f172a", marginTop: 2 }}>{name}</p>
      </div>
    </div>
  );
}
