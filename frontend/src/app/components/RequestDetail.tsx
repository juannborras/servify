import React, { useCallback, useEffect, useState } from "react";
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
  MessageSquare,
  Maximize2,
  Send,
  UserRound,
  X,
} from "lucide-react";
import { motion, AnimatePresence } from "motion/react";
import type { ApiChatMessage, ApiPublicProvider, ApiServiceRating, ApiUserProfile, SessionUser } from "../api";
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
  onSubmitted?: (puntaje: number, comentario?: string) => void;
}

interface RequestDetailProps {
  request: ServiceRequest;
  onBack: () => void;
  onRate: (target: RatingTarget) => void;
  currentUser?: SessionUser | null;
  onProviderPress?: (provider: ApiPublicProvider) => void;
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

export function RequestDetail({ request, onBack, onRate, currentUser, onProviderPress }: RequestDetailProps) {
  const [assignmentState, setAssignmentState] = useState<AssignmentState | null>(null);
  const [providerProfileName, setProviderProfileName] = useState("");
  const [existingRating, setExistingRating] = useState<ApiServiceRating | null>(null);
  const [loadingRating, setLoadingRating] = useState(false);
  const [loadingState, setLoadingState] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [showComplete, setShowComplete] = useState(false);
  const [showChat, setShowChat] = useState(false);
  const [profileLoading, setProfileLoading] = useState(false);
  const [error, setError] = useState("");

  const applyLoadedAssignmentState = useCallback((state: AssignmentState) => {
    setAssignmentState(state);
    setShowComplete(isServiceClosed(state));
  }, []);

  const loadAssignmentState = useCallback(
    async (silent = false) => {
      if (typeof request.id !== "string") {
        setAssignmentState(null);
        setShowComplete(false);
        return null;
      }

      if (!silent) {
        setLoadingState(true);
      }
      setError("");

      try {
        const state = await servifyApi.getAssignmentState(request.id);
        applyLoadedAssignmentState(state);
        return state;
      } catch (err) {
        setAssignmentState(null);
        setError(err instanceof Error ? err.message : "No se pudo cargar el estado del servicio");
        return null;
      } finally {
        if (!silent) {
          setLoadingState(false);
        }
      }
    },
    [applyLoadedAssignmentState, request.id]
  );

  useEffect(() => {
    if (typeof request.id !== "string") {
      setAssignmentState(null);
      setShowComplete(false);
      return;
    }

    void loadAssignmentState();
    const intervalId = window.setInterval(() => {
      void loadAssignmentState(true);
    }, 6000);

    return () => {
      window.clearInterval(intervalId);
    };
  }, [loadAssignmentState, request.id]);

  const assignment = assignmentState?.asignacion;
  const acceptedDistribution = assignmentState?.distribucionesAceptadas?.[0];
  const pendingCounterOffer = assignmentState?.contraofertasPendientes?.[0];
  const providerId = assignment?.prestadorId ?? acceptedDistribution?.prestadorId ?? pendingCounterOffer?.prestadorId ?? "";

  useEffect(() => {
    if (!providerId) {
      setProviderProfileName("");
      return;
    }

    let ignore = false;
    setProviderProfileName("");

    servifyApi
      .getUserProfile(providerId)
      .then((profile) => {
        if (!ignore) setProviderProfileName(formatProfileName(profile));
      })
      .catch(() => {
        if (!ignore) setProviderProfileName("");
      });

    return () => {
      ignore = true;
    };
  }, [providerId]);

  const providerName = providerProfileName || request.providerName || (providerId ? "Prestador asignado" : proposalData.providerName);
  const providerInitials = providerProfileName || request.providerName
    ? initialsFromName(providerName)
    : request.providerInitials || initialsFromName(providerName);
  const proposalMessage = assignment?.estado
    ? `Estado de asignación: ${assignment.estado}`
    : proposalData.message;
  const assignmentCompleted = assignment?.estado === "FINALIZADA" || assignmentState?.estadoSolicitud === "FINALIZADA";
  const requesterConfirmed = assignmentCompleted || (assignmentState?.confirmadoPorSolicitante ?? false);
  const providerConfirmed = assignmentCompleted || (assignmentState?.confirmadoPorPrestador ?? false);
  const isCompleted = request.status === "completed" || assignmentCompleted || Boolean(assignmentState?.finalizacionConfirmada);
  const hasProposal = Boolean(assignment);
  const hasAcceptedPending = !assignment && isAcceptedDistribution(acceptedDistribution);
  const hasPendingCounterOffer = !assignment && Boolean(pendingCounterOffer?.id);
  const isRequesterParticipant = Boolean(currentUser?.id && assignmentState?.solicitanteId === currentUser.id);
  const isAssignedProvider = Boolean(currentUser?.id && assignment?.prestadorId === currentUser.id);
  const isAcceptedProvider = Boolean(currentUser?.id && acceptedDistribution?.prestadorId === currentUser.id);
  const isCounterOfferProvider = Boolean(currentUser?.id && pendingCounterOffer?.prestadorId === currentUser.id);
  const hasProviderParticipation = isAssignedProvider || isAcceptedProvider || isCounterOfferProvider;
  const displayRequesterName = isRequesterParticipant ? "Tu" : request.requesterName;
  const displayRequesterInitials = isRequesterParticipant ? "TU" : request.requesterInitials;
  const displayProviderName = hasProviderParticipation ? "Tu" : providerName;
  const displayProviderInitials = hasProviderParticipation ? "TU" : providerInitials;
  const participantRole: "SOLICITANTE" | "PRESTADOR" | null = request.viewerRole === "SOLICITANTE" && isRequesterParticipant
    ? "SOLICITANTE"
    : request.viewerRole === "PRESTADOR" && hasProviderParticipation
    ? "PRESTADOR"
    : isRequesterParticipant
    ? "SOLICITANTE"
    : hasProviderParticipation
    ? "PRESTADOR"
    : null;
  const confirmationRole: "SOLICITANTE" | "PRESTADOR" | null = assignment
    ? isRequesterParticipant
      ? "SOLICITANTE"
      : isAssignedProvider
      ? "PRESTADOR"
      : null
    : null;
  const currentUserAlreadyConfirmed = confirmationRole === "SOLICITANTE"
    ? requesterConfirmed
    : confirmationRole === "PRESTADOR"
    ? providerConfirmed
    : false;
  const acceptedPrice = assignment?.precioAcordado ? formatMoney(Number(assignment.precioAcordado)) : proposalData.offeredPrice;
  const counterOfferPrice = pendingCounterOffer?.precioPropuesto
    ? formatMoney(Number(pendingCounterOffer.precioPropuesto))
    : proposalData.offeredPrice;
  const canConfirm = Boolean(currentUser?.id && assignment?.id && confirmationRole && !currentUserAlreadyConfirmed && !isCompleted && !submitting);
  const canResolveCounterOffer = Boolean(
    currentUser?.id &&
      pendingCounterOffer?.id &&
      isRequesterParticipant &&
      !submitting
  );
  const canConfirmAssignment = Boolean(
    currentUser?.id &&
      acceptedDistribution?.id &&
      isRequesterParticipant &&
      !submitting
  );
  const chatAvailable = Boolean(
    currentUser?.id &&
      providerId &&
      participantRole &&
      (hasPendingCounterOffer || hasAcceptedPending || hasProposal)
  );
  const bothConfirmed = requesterConfirmed && providerConfirmed;
  const canRate = Boolean(
    assignment?.id &&
      assignmentState?.solicitanteId &&
      assignment?.prestadorId &&
      currentUser?.id &&
      confirmationRole &&
      isCompleted &&
      !loadingRating &&
      !existingRating
  );
  const ratingTargetName = confirmationRole === "PRESTADOR" ? request.requesterName : providerName;
  const ratingTargetLabel = confirmationRole === "PRESTADOR" ? "cliente" : "prestador";
  const chatCounterpartName = participantRole === "SOLICITANTE" ? providerName : request.requesterName;
  const chatCounterpartRole = participantRole === "SOLICITANTE" ? "prestador" : "solicitante";

  useEffect(() => {
    if (!assignment?.id || !confirmationRole || !currentUser?.id || typeof request.id !== "string" || !isCompleted) {
      setExistingRating(null);
      setLoadingRating(false);
      return;
    }

    let ignore = false;
    setLoadingRating(true);

    servifyApi
      .getServiceRating({
        solicitudId: request.id,
        asignacionServicioId: assignment.id,
        rolCalificador: confirmationRole,
      })
      .then((rating) => {
        if (ignore) return;
        setExistingRating(rating.calificadorId && rating.calificadorId !== currentUser.id ? null : rating);
      })
      .catch(() => {
        if (!ignore) setExistingRating(null);
      })
      .finally(() => {
        if (!ignore) setLoadingRating(false);
      });

    return () => {
      ignore = true;
    };
  }, [assignment?.id, confirmationRole, currentUser?.id, isCompleted, request.id]);

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
      await loadAssignmentState(true);
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo confirmar el prestador");
    } finally {
      setSubmitting(false);
    }
  };

  const handleOpenProviderProfile = async () => {
    if (!providerId || !onProviderPress) return;
    setProfileLoading(true);
    setError("");
    try {
      const provider = await servifyApi.getPublicProvider(providerId);
      onProviderPress(provider);
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo abrir el perfil del prestador");
    } finally {
      setProfileLoading(false);
    }
  };

  const handleKeepSearching = async () => {
    if (typeof request.id !== "string") return;
    setSubmitting(true);
    setError("");
    try {
      await servifyApi.retryDistribution(request.id);
      await loadAssignmentState(true);
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo buscar otro prestador");
    } finally {
      setSubmitting(false);
    }
  };

  const handleResolveCounterOffer = async (decision: "ACEPTAR" | "RECHAZAR") => {
    if (!currentUser?.id || !pendingCounterOffer?.id) return;
    setSubmitting(true);
    setError("");

    try {
      await servifyApi.resolveCounterOffer({
        contraofertaId: pendingCounterOffer.id,
        solicitanteId: currentUser.id,
        decision,
      });
      await loadAssignmentState(true);
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo resolver la contraoferta");
    } finally {
      setSubmitting(false);
    }
  };

  const handleKeepSearchingFromCounterOffer = async () => {
    if (!currentUser?.id || !pendingCounterOffer?.id || typeof request.id !== "string") return;
    setSubmitting(true);
    setError("");

    try {
      await servifyApi.resolveCounterOffer({
        contraofertaId: pendingCounterOffer.id,
        solicitanteId: currentUser.id,
        decision: "RECHAZAR",
      });
      await servifyApi.retryDistribution(request.id);
      await loadAssignmentState(true);
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo seguir buscando prestadores");
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
      await loadAssignmentState(true);
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
    <div className="servify-dark-screen flex flex-col h-full" style={{ background: "#f8fafc" }}>
      <div className="servify-page-header bg-white px-5 pt-12 pb-4">
        <button onClick={onBack} className="flex items-center gap-2 mb-4">
          <ChevronLeft size={20} color="#2563eb" strokeWidth={2} />
          <span style={{ fontSize: 14, color: "#2563eb", fontWeight: 600 }}>Volver</span>
        </button>

        <div className="flex items-start justify-between gap-3">
          <h1 style={{ fontSize: 19, fontWeight: 800, color: "#0f172a", flex: 1, lineHeight: 1.25 }}>
            {request.title}
          </h1>
          <span
            className="servify-status-badge px-3 py-1.5 rounded-full shrink-0"
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
              <span style={{ fontWeight: 800, fontSize: 14, color: "#2563eb" }}>{displayRequesterInitials}</span>
            </div>
            <div>
              <p style={{ fontWeight: 700, fontSize: 14, color: "#0f172a" }}>{displayRequesterName}</p>
              <p style={{ fontSize: 12, color: "#64748b" }}>Solicitante del servicio</p>
            </div>
          </div>
        </Card>

        {hasPendingCounterOffer && (
          <Card title={participantRole === "SOLICITANTE" ? "Contraoferta recibida" : "Contraoferta enviada"}>
            <div className="flex items-center gap-3 mb-3">
              <div className="flex items-center justify-center rounded-full" style={{ width: 44, height: 44, background: "#fff7ed", flexShrink: 0 }}>
                <MessageSquare size={20} color="#ea580c" strokeWidth={2} />
              </div>
              <div className="flex-1">
                <p style={{ fontWeight: 700, fontSize: 14, color: "#0f172a" }}>{displayProviderName}</p>
                <p style={{ fontSize: 12, color: "#64748b", marginTop: 2 }}>
                  {participantRole === "SOLICITANTE"
                    ? "El prestador propuso nuevas condiciones para este pedido."
                    : "Esperando respuesta del solicitante."}
                </p>
              </div>
              <div className="px-3 py-1.5 rounded-xl" style={{ background: "#fffbeb" }}>
                <span style={{ fontSize: 14, fontWeight: 800, color: "#d97706" }}>{counterOfferPrice}</span>
              </div>
            </div>

            <div className="servify-form-surface p-3 rounded-xl" style={{ background: "#f8fafc", border: "1px solid #e2e8f0" }}>
              <p style={{ fontSize: 13, color: "#475569", lineHeight: 1.6 }}>
                {pendingCounterOffer?.mensaje || "El prestador no agrego un mensaje."}
              </p>
            </div>

            {participantRole === "SOLICITANTE" && (
              <div className="grid grid-cols-2 gap-2 mt-3">
                <button
                  type="button"
                  onClick={handleOpenProviderProfile}
                  disabled={profileLoading || !providerId}
                  className="col-span-2 flex items-center justify-center gap-1.5 py-3 rounded-xl transition-all active:scale-95"
                  style={{ background: "#eff6ff", color: "#2563eb", border: "1.5px solid #bfdbfe", fontWeight: 800, fontSize: 13, opacity: profileLoading ? 0.65 : 1 }}
                >
                  <UserRound size={15} strokeWidth={2} />
                  {profileLoading ? "Abriendo..." : "Ver perfil publico"}
                </button>
                <button
                  type="button"
                  onClick={handleKeepSearchingFromCounterOffer}
                  disabled={!canResolveCounterOffer}
                  className="flex items-center justify-center gap-1.5 py-3 rounded-xl transition-all active:scale-95"
                  style={{ background: "#fffbeb", color: "#d97706", border: "1.5px solid #fde68a", fontWeight: 800, fontSize: 13, opacity: canResolveCounterOffer ? 1 : 0.65 }}
                >
                  <RefreshCw size={15} strokeWidth={2} />
                  {submitting ? "Buscando..." : "Seguir buscando"}
                </button>
                <button
                  type="button"
                  onClick={() => handleResolveCounterOffer("ACEPTAR")}
                  disabled={!canResolveCounterOffer}
                  className="flex items-center justify-center gap-1.5 py-3 rounded-xl transition-all active:scale-95"
                  style={{ background: "#16a34a", color: "white", fontWeight: 800, fontSize: 13, opacity: canResolveCounterOffer ? 1 : 0.65 }}
                >
                  <CheckCircle size={15} strokeWidth={2} />
                  {submitting ? "Procesando..." : "Aceptar precio"}
                </button>
              </div>
            )}
          </Card>
        )}

        {hasAcceptedPending && (
          <Card title={participantRole === "SOLICITANTE" ? "Prestador disponible" : "Aceptacion enviada"}>
            <div className="flex items-center gap-3 mb-3">
              <div className="flex items-center justify-center rounded-full" style={{ width: 44, height: 44, background: "#f0fdf4", flexShrink: 0 }}>
                <span style={{ fontWeight: 800, fontSize: 14, color: "#16a34a" }}>{displayProviderInitials}</span>
              </div>
              <div className="flex-1">
                <p style={{ fontWeight: 700, fontSize: 14, color: "#0f172a" }}>{displayProviderName}</p>
                <p style={{ fontSize: 12, color: "#64748b", marginTop: 2 }}>
                  {participantRole === "SOLICITANTE"
                    ? "Acepto tu solicitud. Confirmalo para iniciar el servicio."
                    : "Ya aceptaste esta solicitud. Falta que el solicitante confirme la asignacion."}
                </p>
              </div>
            </div>

            {participantRole === "SOLICITANTE" && (
              <div className="grid grid-cols-2 gap-2 mt-3">
                <button
                  type="button"
                  onClick={handleOpenProviderProfile}
                  disabled={profileLoading || !providerId}
                  className="flex items-center justify-center gap-1.5 py-3 rounded-xl transition-all active:scale-95"
                  style={{ background: "#eff6ff", color: "#2563eb", border: "1.5px solid #bfdbfe", fontWeight: 800, fontSize: 13, opacity: profileLoading ? 0.65 : 1 }}
                >
                  <UserRound size={15} strokeWidth={2} />
                  Perfil
                </button>
                <button
                  type="button"
                  onClick={handleKeepSearching}
                  disabled={submitting}
                  className="flex items-center justify-center gap-1.5 py-3 rounded-xl transition-all active:scale-95"
                  style={{ background: "#fffbeb", color: "#d97706", border: "1.5px solid #fde68a", fontWeight: 800, fontSize: 13, opacity: submitting ? 0.65 : 1 }}
                >
                  <RefreshCw size={15} strokeWidth={2} />
                  Seguir buscando
                </button>
                <button
                  type="button"
                  onClick={handleConfirmAssignment}
                  disabled={!canConfirmAssignment}
                  className="col-span-2 py-3 rounded-xl transition-all active:scale-95"
                  style={{ background: "#16a34a", color: "white", fontWeight: 800, fontSize: 14, opacity: canConfirmAssignment ? 1 : 0.65 }}
                >
                  {submitting ? "Confirmando..." : "Confirmar prestador"}
                </button>
              </div>
            )}
          </Card>
        )}

        {hasProposal && (
          <Card title="Propuesta aceptada">
            <div className="flex items-center gap-3 mb-3">
              <div className="flex items-center justify-center rounded-full" style={{ width: 44, height: 44, background: "#f0fdf4", flexShrink: 0 }}>
                <span style={{ fontWeight: 800, fontSize: 14, color: "#16a34a" }}>{displayProviderInitials}</span>
              </div>
              <div className="flex-1">
                <p style={{ fontWeight: 700, fontSize: 14, color: "#0f172a" }}>{displayProviderName}</p>
              </div>
              <div className="px-3 py-1.5 rounded-xl" style={{ background: "#eff6ff" }}>
                <span style={{ fontSize: 14, fontWeight: 800, color: "#2563eb" }}>{acceptedPrice}</span>
              </div>
            </div>

            <div className="servify-form-surface p-3 rounded-xl" style={{ background: "#f8fafc", border: "1px solid #e2e8f0" }}>
              <p style={{ fontSize: 13, color: "#475569", lineHeight: 1.6 }}>{proposalMessage}</p>
            </div>
            {providerId && onProviderPress ? (
              <button
                type="button"
                onClick={handleOpenProviderProfile}
                disabled={profileLoading}
                className="mt-3 flex w-full items-center justify-center gap-1.5 py-3 rounded-xl transition-all active:scale-95"
                style={{ background: "#eff6ff", color: "#2563eb", border: "1.5px solid #bfdbfe", fontWeight: 800, fontSize: 13, opacity: profileLoading ? 0.65 : 1 }}
              >
                <UserRound size={15} strokeWidth={2} />
                {profileLoading ? "Abriendo..." : "Ver perfil publico"}
              </button>
            ) : null}
          </Card>
        )}

        {chatAvailable && currentUser?.id && (
          <Card title="Chat">
            <button
              type="button"
              onClick={() => setShowChat(true)}
              className="flex w-full items-center justify-center gap-2 rounded-2xl py-3 transition-all active:scale-95"
              style={{ background: "#eff6ff", color: "#2563eb", border: "1.5px solid #bfdbfe", fontSize: 14, fontWeight: 900 }}
            >
              <MessageSquare size={17} strokeWidth={2.2} />
              Abrir chat con {chatCounterpartRole}
              <Maximize2 size={15} strokeWidth={2.1} />
            </button>
          </Card>
        )}

        {hasProposal && !isCompleted && (
          <Card title="Confirmar finalización">
            <div className="servify-form-surface rounded-2xl p-4" style={{ background: "#ffffff", border: "1.5px solid #dfe7f2" }}>
              <div className="flex items-center justify-between gap-3">
                <ConfirmationPill
                  label="Solicitante"
                  name={displayRequesterName}
                  initials={displayRequesterInitials}
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
                  name={displayProviderName}
                  initials={displayProviderInitials}
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
              className="servify-success-card rounded-2xl p-4 flex flex-col items-center gap-3"
              style={{ background: "#f0fdf4", border: "1.5px solid #bbf7d0" }}
            >
              <CheckCircle size={32} color="#16a34a" strokeWidth={1.8} />
              <p style={{ fontWeight: 700, fontSize: 15, color: "#15803d", textAlign: "center" }}>
                Servicio completado con confirmación de ambas partes
              </p>
              {loadingRating && confirmationRole && (
                <p style={{ fontSize: 13, color: "#64748b", fontWeight: 700 }}>
                  Revisando tu calificacion...
                </p>
              )}
              {existingRating && (
                <div className="w-full rounded-2xl px-4 py-3 flex flex-col items-center gap-2" style={{ background: "#fff7ed", border: "1.5px solid #fed7aa" }}>
                  <div className="flex items-center gap-1">
                    {[1, 2, 3, 4, 5].map((value) => (
                      <Star
                        key={value}
                        size={18}
                        color={value <= existingRating.puntaje ? "#f59e0b" : "#fdba74"}
                        fill={value <= existingRating.puntaje ? "#f59e0b" : "none"}
                        strokeWidth={1.8}
                      />
                    ))}
                  </div>
                  <p style={{ fontWeight: 800, fontSize: 14, color: "#c2410c", textAlign: "center" }}>
                    Ya calificaste a este {ratingTargetLabel}
                  </p>
                  <p style={{ fontSize: 12, fontWeight: 700, color: "#9a3412" }}>
                    Tu calificacion: {existingRating.puntaje}/5
                  </p>
                </div>
              )}
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
                      onSubmitted: (puntaje, comentario) => {
                        setExistingRating({
                          solicitudId: String(request.id),
                          asignacionServicioId: assignment?.id ?? "",
                          calificadorId: currentUser?.id,
                          calificadoId: confirmationRole === "PRESTADOR"
                            ? assignmentState?.solicitanteId
                            : assignment?.prestadorId,
                          rolCalificador: confirmationRole as "SOLICITANTE" | "PRESTADOR",
                          puntaje,
                          comentario,
                          fechaCalificacion: new Date().toISOString(),
                        });
                      },
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
                <ConfirmRow label={displayRequesterName} initials={displayRequesterInitials} confirmed={requesterConfirmed} />
                <ConfirmRow label={displayProviderName} initials={displayProviderInitials} confirmed={providerConfirmed} />
              </div>
            </motion.div>
          )}
        </AnimatePresence>
      </div>
      {showChat && chatAvailable && currentUser?.id ? (
        <ServiceChat
          solicitudId={String(request.id)}
          prestadorId={providerId}
          currentUserId={currentUser.id}
          counterpartName={chatCounterpartName}
          onClose={() => setShowChat(false)}
        />
      ) : null}
    </div>
  );
}

function ServiceChat({
  solicitudId,
  prestadorId,
  currentUserId,
  counterpartName,
  onClose,
}: {
  solicitudId: string;
  prestadorId: string;
  currentUserId: string;
  counterpartName: string;
  onClose: () => void;
}) {
  const [messages, setMessages] = useState<ApiChatMessage[]>([]);
  const [draft, setDraft] = useState("");
  const [loading, setLoading] = useState(false);
  const [sending, setSending] = useState(false);
  const [error, setError] = useState("");

  const loadMessages = useCallback(
    async (silent = false) => {
      if (!solicitudId || !prestadorId) return;
      if (!silent) setLoading(true);
      setError("");
      try {
        const next = await servifyApi.listChatMessages(solicitudId, prestadorId);
        setMessages(next);
      } catch (err) {
        setError(err instanceof Error ? err.message : "No se pudo cargar el chat");
      } finally {
        if (!silent) setLoading(false);
      }
    },
    [prestadorId, solicitudId]
  );

  useEffect(() => {
    void loadMessages();
    const intervalId = window.setInterval(() => {
      void loadMessages(true);
    }, 2500);
    return () => window.clearInterval(intervalId);
  }, [loadMessages]);

  const sendMessage = async () => {
    const content = draft.trim();
    if (!content || sending) return;
    setSending(true);
    setError("");
    try {
      const sent = await servifyApi.sendChatMessage({ solicitudId, prestadorId, contenido: content });
      setMessages((current) => [...current, sent]);
      setDraft("");
      void loadMessages(true);
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo enviar el mensaje");
    } finally {
      setSending(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex flex-col bg-white">
      <div className="servify-page-header flex items-center justify-between bg-white px-5 pb-4 pt-12" style={{ borderBottom: "1px solid #e2e8f0" }}>
        <div className="min-w-0">
          <p style={{ color: "#64748b", fontSize: 12, fontWeight: 800 }}>Chat</p>
          <h2 style={{ color: "#0f172a", fontSize: 19, fontWeight: 900, lineHeight: 1.15 }}>
            {counterpartName}
          </h2>
        </div>
        <button
          type="button"
          onClick={onClose}
          className="flex items-center justify-center rounded-xl"
          style={{ width: 40, height: 40, background: "#f1f5f9", color: "#64748b" }}
        >
          <X size={19} strokeWidth={2.2} />
        </button>
      </div>

      <div className="servify-form-surface flex min-h-0 flex-1 flex-col p-4" style={{ background: "#f8fafc" }}>
        <div className="flex min-h-0 flex-1 flex-col gap-2 overflow-y-auto pr-1">
          {loading ? (
            <p style={{ color: "#64748b", fontSize: 13, fontWeight: 700 }}>Cargando mensajes...</p>
          ) : null}
          {!loading && messages.length === 0 ? (
            <p style={{ color: "#64748b", fontSize: 13, fontWeight: 700 }}>Todavia no hay mensajes.</p>
          ) : null}
          {messages.map((message) => {
            const own = message.remitenteId === currentUserId;
            return (
              <div
                key={message.id}
                className={`flex ${own ? "justify-end" : "justify-start"}`}
              >
                <div
                  className="rounded-2xl px-3 py-2"
                  style={{
                    maxWidth: "82%",
                    background: own ? "#2563eb" : "#ffffff",
                    color: own ? "white" : "#0f172a",
                    border: own ? "1px solid #2563eb" : "1px solid #e2e8f0",
                  }}
                >
                  <p style={{ fontSize: 13, fontWeight: 700, lineHeight: 1.4 }}>{message.contenido}</p>
                  <p style={{ fontSize: 10, fontWeight: 800, opacity: 0.7, marginTop: 4 }}>
                    {formatChatDate(message.fechaEnvio)}
                  </p>
                </div>
              </div>
            );
          })}
        </div>

        {error ? (
          <p className="mt-2 rounded-xl px-3 py-2" style={{ background: "#fef2f2", color: "#b91c1c", fontSize: 12, fontWeight: 800 }}>
            {error}
          </p>
        ) : null}

        <div className="mt-3 flex items-end gap-2">
          <textarea
            value={draft}
            onChange={(event) => setDraft(event.target.value)}
            rows={2}
            maxLength={1200}
            placeholder="Escribi un mensaje..."
            className="min-w-0 flex-1 resize-none rounded-2xl px-3 py-2 outline-none"
            style={{ border: "1px solid #dbeafe", color: "#0f172a", fontSize: 13 }}
          />
          <button
            type="button"
            onClick={sendMessage}
            disabled={!draft.trim() || sending}
            className="flex items-center justify-center rounded-2xl transition-all active:scale-95"
            style={{ width: 44, height: 44, background: draft.trim() && !sending ? "#2563eb" : "#cbd5e1", color: "white" }}
          >
            <Send size={17} strokeWidth={2.2} />
          </button>
        </div>
      </div>
    </div>
  );
}

function Card({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <div className="servify-card servify-request-card bg-white rounded-2xl p-4" style={{ border: "1px solid rgba(0,0,0,0.06)", boxShadow: "0 1px 4px rgba(0,0,0,0.04)" }}>
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

function formatChatDate(value?: string): string {
  if (!value) return "";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "";
  return date.toLocaleTimeString("es-AR", { hour: "2-digit", minute: "2-digit" });
}

function isServiceClosed(state: AssignmentState): boolean {
  return Boolean(state.finalizacionConfirmada) ||
    state.estadoSolicitud === "FINALIZADA" ||
    state.asignacion?.estado === "FINALIZADA";
}

function isAcceptedDistribution(distribution: NonNullable<AssignmentState["distribucionesAceptadas"]>[number] | undefined): boolean {
  return Boolean(distribution?.id && (!distribution.estado || distribution.estado === "ACEPTADA"));
}

function formatProfileName(profile: ApiUserProfile | null): string {
  if (!profile) return "";
  return [profile.nombre, profile.apellido].filter(Boolean).join(" ").trim();
}

function initialsFromName(name: string): string {
  return name
    .split(" ")
    .map((part) => part[0])
    .join("")
    .slice(0, 2)
    .toUpperCase();
}
