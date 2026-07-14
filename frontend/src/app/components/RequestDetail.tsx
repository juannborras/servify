import React, { useCallback, useEffect, useRef, useState } from "react";
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
  CalendarPlus,
  Clock,
  ChevronDown,
  XCircle,
} from "lucide-react";
import { motion, AnimatePresence } from "motion/react";
import type { ApiChatMessage, ApiPublicProvider, ApiServiceEncounter, ApiServiceRating, ApiServiceRecurrence, ApiUserProfile, SessionUser } from "../api";
import { TIME_OPTIONS, formatMoney, parseMoney, servifyApi } from "../api";
import type { ServiceRequest } from "./RequestsScreen";
import { ServiceConnectionCelebration } from "./ServiceConnectionCelebration";
import { ServicePaymentCard } from "./ServicePaymentCard";
import { ServisHint } from "./ServisHint";

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
  const [showChat, setShowChat] = useState(Boolean(request.openChat));
  const [profileLoading, setProfileLoading] = useState(false);
  const [error, setError] = useState("");
  const [encounters, setEncounters] = useState<ApiServiceEncounter[]>([]);
  const [recurrence, setRecurrence] = useState<ApiServiceRecurrence | null>(null);
  const [loadingAgenda, setLoadingAgenda] = useState(false);
  const [agendaDate, setAgendaDate] = useState("");
  const [agendaFrom, setAgendaFrom] = useState("09:00");
  const [agendaTo, setAgendaTo] = useState("10:00");
  const [agendaMessage, setAgendaMessage] = useState("");
  const [showAgendaComposer, setShowAgendaComposer] = useState(false);
  const [encounterToCancel, setEncounterToCancel] = useState<ApiServiceEncounter | null>(null);
  const [showCancelRecurrenceConfirm, setShowCancelRecurrenceConfirm] = useState(false);
  const [distributionStatus, setDistributionStatus] = useState(request.rawStatus ?? "");
  const [actionNotice, setActionNotice] = useState("");
  const [showConnectionCelebration, setShowConnectionCelebration] = useState(false);
  const [showPriceAgreement, setShowPriceAgreement] = useState(false);
  const [priceAgreement, setPriceAgreement] = useState("");
  const agendaDateInputRef = useRef<HTMLInputElement>(null);
  const priceAgreementInputRef = useRef<HTMLInputElement>(null);
  const agendaTriggerRef = useRef<HTMLButtonElement>(null);
  const cancelEncounterBackButtonRef = useRef<HTMLButtonElement>(null);
  const cancelRecurrenceBackButtonRef = useRef<HTMLButtonElement>(null);

  useEffect(() => {
    setShowChat(Boolean(request.openChat));
  }, [request.id, request.openChat]);

  useEffect(() => {
    setDistributionStatus(request.rawStatus ?? "");
    setActionNotice("");
    setShowConnectionCelebration(false);
    setShowPriceAgreement(false);
    setPriceAgreement("");
  }, [request.id, request.rawStatus]);

  const dismissConnectionCelebration = useCallback(() => {
    setShowConnectionCelebration(false);
  }, []);

  useEffect(() => {
    if (showPriceAgreement) priceAgreementInputRef.current?.focus();
  }, [showPriceAgreement]);

  useEffect(() => {
    if (!showAgendaComposer) return;
    agendaDateInputRef.current?.focus();
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === "Escape" && !submitting) {
        setShowAgendaComposer(false);
        window.requestAnimationFrame(() => agendaTriggerRef.current?.focus());
      }
    };
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [showAgendaComposer, submitting]);

  useEffect(() => {
    if (!encounterToCancel) return;
    cancelEncounterBackButtonRef.current?.focus();
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === "Escape" && !submitting) {
        setEncounterToCancel(null);
      }
    };
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [encounterToCancel, submitting]);

  useEffect(() => {
    if (!showCancelRecurrenceConfirm) return;
    cancelRecurrenceBackButtonRef.current?.focus();
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === "Escape" && !submitting) {
        setShowCancelRecurrenceConfirm(false);
      }
    };
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [showCancelRecurrenceConfirm, submitting]);

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

  const loadAgenda = useCallback(
    async (silent = false) => {
      if (typeof request.id !== "string") {
        setEncounters([]);
        setRecurrence(null);
        return;
      }
      if (!silent) setLoadingAgenda(true);
      try {
        const [nextEncounters, nextRecurrence] = await Promise.all([
          servifyApi.listServiceEncounters(request.id).catch(() => []),
          servifyApi.getServiceRecurrence(request.id).catch(() => null),
        ]);
        setEncounters(Array.isArray(nextEncounters) ? nextEncounters : []);
        setRecurrence(nextRecurrence ?? null);
      } finally {
        if (!silent) setLoadingAgenda(false);
      }
    },
    [request.id]
  );

  useEffect(() => {
    void loadAgenda();
    const intervalId = window.setInterval(() => {
      void loadAgenda(true);
    }, 8000);
    return () => window.clearInterval(intervalId);
  }, [loadAgenda]);

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
    ? `Estado de asignacion: ${assignment.estado}`
    : proposalData.message;
  const assignmentCompleted = assignment?.estado === "FINALIZADA" || assignmentState?.estadoSolicitud === "FINALIZADA";
  const requesterConfirmed = assignmentCompleted || (assignmentState?.confirmadoPorSolicitante ?? false);
  const providerConfirmed = assignmentCompleted || (assignmentState?.confirmadoPorPrestador ?? false);
  const isCompleted = request.status === "completed" || assignmentCompleted || Boolean(assignmentState?.finalizacionConfirmada);
  const isCancelled = request.status === "cancelled"
    || assignment?.estado === "CANCELADA"
    || assignmentState?.estadoSolicitud === "CANCELADA"
    || recurrence?.estado === "CANCELADA";
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
  const canConfirm = Boolean(currentUser?.id && assignment?.id && confirmationRole && !currentUserAlreadyConfirmed && !isCompleted && !isCancelled && !submitting);
  const canResolveCounterOffer = Boolean(
    currentUser?.id &&
      pendingCounterOffer?.id &&
      isRequesterParticipant &&
      !isCancelled &&
      !submitting
  );
  const canConfirmAssignment = Boolean(
    currentUser?.id &&
      acceptedDistribution?.id &&
      isRequesterParticipant &&
      !isCancelled &&
      !submitting
  );
  const normalizedDistributionStatus = distributionStatus.toUpperCase();
  const providerInvitationPending = Boolean(
    currentUser?.id &&
      request.viewerRole === "PRESTADOR" &&
      request.distributionId &&
      !hasProposal &&
      !hasAcceptedPending &&
      !hasPendingCounterOffer &&
      !isCompleted &&
      !isCancelled &&
      ["", "ABIERTA", "ENVIADA", "PENDIENTE"].includes(normalizedDistributionStatus)
  );
  const providerInvitationAccepted = request.viewerRole === "PRESTADOR"
    && normalizedDistributionStatus === "ACEPTADA";
  const providerInvitationDeclined = request.viewerRole === "PRESTADOR"
    && ["RECHAZADA", "EXPIRADA"].includes(normalizedDistributionStatus);
  const hasChatContext = Boolean(
    currentUser?.id &&
      providerId &&
      participantRole &&
      (hasPendingCounterOffer || hasAcceptedPending || hasProposal)
  );
  const chatClosed = Boolean(hasChatContext && (isCompleted || isCancelled));
  const chatAvailable = Boolean(hasChatContext && !isCompleted && !isCancelled);
  const isRecurringRequest = request.scheduleType === "RECURRENTE" || Boolean(recurrence);
  const canManageAgenda = Boolean(currentUser?.id && assignment?.id && participantRole && !isCompleted && !isCancelled && !isRecurringRequest);
  const agendaTimeRangeValid = isTimeRangeValid(agendaFrom, agendaTo);
  const agendaUnavailableReason = !currentUser?.id
    ? "Inicia sesion para coordinar otra visita."
    : !assignment?.id
    ? "La segunda visita se habilita cuando el solicitante confirma al prestador y la solicitud queda en curso."
    : !participantRole
    ? "Solo el solicitante o el prestador asignado pueden coordinar otra visita."
    : isCancelled
    ? "La solicitud fue cancelada. El historial de encuentros queda disponible solo para consulta."
    : isCompleted
    ? "La solicitud ya finalizo. El historial de encuentros queda disponible solo para consulta."
    : "";
  const canProposeAgenda = Boolean(canManageAgenda && agendaDate && agendaTimeRangeValid && !submitting);
  const upcomingEncounters = encounters.filter((encounter) => !isClosedEncounter(encounter.estado));
  const encounterHistory = encounters.filter((encounter) => isClosedEncounter(encounter.estado));
  const activeRecurringEncounter = isRecurringRequest
    ? encounters.find((encounter) => encounter.id === assignmentState?.encuentroActivoId)
      ?? upcomingEncounters.find((encounter) => encounter.estado === "CONFIRMADO")
    : undefined;
  const paymentEncounterId = isRecurringRequest
    ? assignmentState?.encuentroActivoId ?? activeRecurringEncounter?.id
    : undefined;
  const recurringEncounterReadyForPayment = !isRecurringRequest
    || Boolean(activeRecurringEncounter && hasEncounterEnded(activeRecurringEncounter));
  const cancelsScheduledRequest = Boolean(
    encounterToCancel && isPrimaryScheduledEncounter(request, encounterToCancel)
  );
  const paymentAmount = Number(assignment?.precioAcordado ?? parseMoney(request.price));
  const needsAgreedPrice = Boolean(assignment?.id && (!Number.isFinite(paymentAmount) || paymentAmount <= 0));
  const canAgreePrice = Boolean(
    needsAgreedPrice &&
      confirmationRole === "SOLICITANTE" &&
      currentUser?.id &&
      !submitting &&
      !isCancelled &&
      !isCompleted
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
    if ((isCompleted || isCancelled) && showChat) {
      setShowChat(false);
    }
  }, [isCancelled, isCompleted, showChat]);

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
      await Promise.all([loadAssignmentState(true), loadAgenda(true)]);
      setShowConnectionCelebration(true);
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
        encuentroId: isRecurringRequest ? paymentEncounterId : undefined,
        confirmanteId: currentUser.id,
        rolConfirmante: confirmationRole,
        observacion: "",
      });
      await Promise.all([loadAssignmentState(true), loadAgenda(true)]);
    } catch (err) {
      setError(err instanceof Error
        ? err.message
        : isRecurringRequest
        ? "No se pudo confirmar este encuentro"
        : "No se pudo confirmar la finalización");
    } finally {
      setSubmitting(false);
    }
  };

  const handleAgreePrice = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!currentUser?.id || !assignment?.id || !canAgreePrice) return;
    const parsedPrice = parseMoney(priceAgreement);
    if (parsedPrice <= 0) {
      setError("Ingresá un precio mayor a cero.");
      priceAgreementInputRef.current?.focus();
      return;
    }

    setSubmitting(true);
    setError("");
    setActionNotice("");
    try {
      await servifyApi.agreeAssignmentPrice({
        solicitudId: String(request.id),
        asignacionServicioId: assignment.id,
        solicitanteId: currentUser.id,
        precioAcordado: priceAgreement,
      });
      setShowPriceAgreement(false);
      setPriceAgreement("");
      setActionNotice(`Precio acordado guardado: ${formatMoney(parsedPrice)}. Ya podés continuar con el pago.`);
      await loadAssignmentState(true);
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo guardar el precio acordado");
    } finally {
      setSubmitting(false);
    }
  };

  const handlePaymentStateRefresh = useCallback(async () => {
    await Promise.all([loadAssignmentState(true), loadAgenda(true)]);
  }, [loadAgenda, loadAssignmentState]);

  const handleDistributionResponse = async (tipoRespuesta: "ACEPTAR" | "RECHAZAR") => {
    if (!currentUser?.id || !request.distributionId) return;
    setSubmitting(true);
    setError("");
    setActionNotice("");
    try {
      await servifyApi.respondToDistribution({
        distribucionSolicitudId: request.distributionId,
        prestadorId: currentUser.id,
        tipoRespuesta,
      });
      const nextStatus = tipoRespuesta === "ACEPTAR" ? "ACEPTADA" : "RECHAZADA";
      setDistributionStatus(nextStatus);
      setActionNotice(
        tipoRespuesta === "ACEPTAR"
          ? "Aceptaste la solicitud. Ahora falta la confirmación del solicitante."
          : "Rechazaste la solicitud. Ya no requiere ninguna acción de tu parte."
      );
      await loadAssignmentState(true);
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo responder la solicitud");
    } finally {
      setSubmitting(false);
    }
  };

  const closeAgendaComposer = () => {
    setShowAgendaComposer(false);
    window.requestAnimationFrame(() => agendaTriggerRef.current?.focus());
  };

  const handleContactFromConfirmation = () => {
    setEncounterToCancel(null);
    setShowCancelRecurrenceConfirm(false);
    setShowChat(true);
  };

  const statusConfig: Record<string, { label: string; bg: string; color: string }> = {
    open: { label: "Abierta", bg: "#eff6ff", color: "#2563eb" },
    completed: { label: "Completada", bg: "#f0fdf4", color: "#16a34a" },
    cancelled: { label: "Cancelada", bg: "#fef2f2", color: "#ef4444" },
    "in-progress": { label: "En curso", bg: "#fffbeb", color: "#d97706" },
    "pending-acceptance": { label: "Pendiente de aceptacion", bg: "#ecfeff", color: "#0891b2" },
    "counter-offer": { label: "Contraoferta", bg: "#fff7ed", color: "#ea580c" },
    declined: { label: "Rechazada", bg: "#fef2f2", color: "#ef4444" },
  };

  const handleProposeEncounter = async () => {
    if (!currentUser?.id || !assignment?.id || typeof request.id !== "string") return;
    const fechaInicio = toLocalDateTime(agendaDate, agendaFrom);
    const fechaFin = toLocalDateTime(agendaDate, agendaTo);
    if (!fechaInicio || !fechaFin) return;
    setSubmitting(true);
    setError("");
    try {
      await servifyApi.proposeServiceEncounter({
        solicitudId: request.id,
        asignacionServicioId: assignment.id,
        propuestoPorId: currentUser.id,
        fechaInicio,
        fechaFin,
        mensaje: agendaMessage,
      });
      setAgendaDate("");
      setAgendaMessage("");
      closeAgendaComposer();
      await loadAgenda(true);
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo proponer el encuentro");
    } finally {
      setSubmitting(false);
    }
  };

  const handleResolveEncounter = async (encounter: ApiServiceEncounter, decision: "ACEPTAR" | "RECHAZAR") => {
    if (!currentUser?.id) return;
    setSubmitting(true);
    setError("");
    try {
      await servifyApi.resolveServiceEncounter({
        encuentroId: encounter.id,
        usuarioId: currentUser.id,
        decision,
      });
      await loadAgenda(true);
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo resolver el encuentro");
    } finally {
      setSubmitting(false);
    }
  };

  const handleCancelEncounter = async () => {
    if (!currentUser?.id || !encounterToCancel) return;
    setSubmitting(true);
    setError("");
    try {
      await servifyApi.cancelServiceEncounter({
        encuentroId: encounterToCancel.id,
        usuarioId: currentUser.id,
      });
      setEncounterToCancel(null);
      await Promise.all([loadAgenda(true), loadAssignmentState(true)]);
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo cancelar el encuentro");
    } finally {
      setSubmitting(false);
    }
  };

  const handleCancelRecurrence = async () => {
    if (!currentUser?.id || typeof request.id !== "string") return;
    setSubmitting(true);
    setError("");
    try {
      await servifyApi.cancelServiceRecurrence({
        solicitudId: request.id,
        usuarioId: currentUser.id,
        motivo: "Cancelada desde la app",
      });
      setShowCancelRecurrenceConfirm(false);
      await Promise.all([loadAgenda(true), loadAssignmentState(true)]);
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo cancelar la recurrencia");
    } finally {
      setSubmitting(false);
    }
  };

  const displayStatus = providerInvitationDeclined
    ? "declined"
    : isCancelled
    ? "cancelled"
    : isCompleted
    ? "completed"
    : hasPendingCounterOffer
    ? "counter-offer"
    : hasProposal
    ? "in-progress"
    : hasAcceptedPending || providerInvitationAccepted
    ? "pending-acceptance"
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

        {actionNotice ? (
          <div
            className="servify-action-notice rounded-2xl px-4 py-3"
            role="status"
            style={{ background: providerInvitationDeclined ? "#f8fafc" : "#f0fdf4", color: providerInvitationDeclined ? "#475569" : "#15803d" }}
          >
            <div className="flex items-center gap-2">
              {providerInvitationDeclined ? <XCircle size={17} strokeWidth={2} /> : <CheckCircle size={17} strokeWidth={2} />}
              <p style={{ fontSize: 13, fontWeight: 800, lineHeight: 1.45 }}>{actionNotice}</p>
            </div>
            {providerInvitationDeclined ? (
              <button
                type="button"
                onClick={onBack}
                className="mt-3 w-full rounded-xl py-2.5 transition-all active:scale-95"
                style={{ background: "#eff6ff", color: "#2563eb", border: "1.5px solid #bfdbfe", fontSize: 12, fontWeight: 900 }}
              >
                Volver a solicitudes
              </button>
            ) : null}
          </div>
        ) : null}

        {providerInvitationPending ? (
          <section
            className="servify-card servify-request-response rounded-2xl p-4"
            aria-labelledby="request-response-title"
            style={{ background: "#eff6ff", border: "1.5px solid #bfdbfe" }}
          >
            <p id="request-response-title" style={{ color: "#0f172a", fontSize: 15, fontWeight: 900 }}>
              ¿Querés tomar este trabajo?
            </p>
            <p style={{ color: "#64748b", fontSize: 12, fontWeight: 700, lineHeight: 1.45, marginTop: 4 }}>
              Revisá el pedido y respondé desde acá. El solicitante recibirá tu decisión.
            </p>
            <div className="mt-3 grid grid-cols-2 gap-2">
              <button
                type="button"
                onClick={() => handleDistributionResponse("RECHAZAR")}
                disabled={submitting}
                className="servify-action-button rounded-xl py-3 transition-all active:scale-95"
                style={{ background: "#fef2f2", color: "#b91c1c", border: "1.5px solid #fecaca", fontSize: 13, fontWeight: 900 }}
              >
                Rechazar
              </button>
              <button
                type="button"
                onClick={() => handleDistributionResponse("ACEPTAR")}
                disabled={submitting}
                className="servify-action-success rounded-xl py-3 transition-all active:scale-95"
                style={{ fontSize: 13, fontWeight: 900, opacity: submitting ? 0.72 : 1 }}
              >
                {submitting ? "Procesando..." : "Aceptar solicitud"}
              </button>
            </div>
          </section>
        ) : null}

        <Card title="Solicitud">
          <p style={{ fontSize: 14, color: "#475569", lineHeight: 1.6 }}>{request.description}</p>
          <details className="servify-request-details mt-3 rounded-xl">
            <summary className="flex cursor-pointer list-none items-center justify-between gap-3 px-3 py-2.5">
              <span style={{ color: "#2563eb", fontSize: 12, fontWeight: 900 }}>Ver datos del pedido</span>
              <ChevronDown className="servify-request-details-chevron" size={16} color="#2563eb" strokeWidth={2.2} />
            </summary>
            <div className="flex flex-col gap-3 px-3 pb-3 pt-1">
              <DetailRow icon={<Tag size={15} color="#0891b2" strokeWidth={1.8} />} label="Categoría" value={request.category} />
              <DetailRow icon={<Smartphone size={15} color="#7c3aed" strokeWidth={1.8} />} label="Modalidad" value={request.modal} />
              <DetailRow icon={<MapPin size={15} color="#ef4444" strokeWidth={1.8} />} label="Ubicación" value={request.location} />
              <DetailRow icon={<Calendar size={15} color="#f59e0b" strokeWidth={1.8} />} label="Publicada" value={request.date} />
              <DetailRow icon={<DollarSign size={15} color="#2563eb" strokeWidth={1.8} />} label="Precio sugerido" value={request.price} />
            </div>
          </details>
        </Card>

        <Card title="Agenda del servicio">
          <div className="flex flex-col gap-3">
            <DetailRow icon={<Clock size={15} color="#0891b2" strokeWidth={1.8} />} label="Agenda" value={request.schedule} />
            {loadingAgenda ? (
              <div className="flex items-center gap-2" style={{ color: "#64748b", fontSize: 13, fontWeight: 700 }}>
                <Loader2 size={14} className="animate-spin" />
                Cargando agenda...
              </div>
            ) : null}

            {recurrence ? (
              <div className="servify-agenda-recurrence rounded-2xl px-4 py-3" style={{ background: "#f0fdf4", border: "1px solid #bbf7d0" }}>
                <div className="flex items-start gap-3">
                  <div className="min-w-0 flex-1">
                    <p style={{ fontSize: 13, fontWeight: 900, color: "#166534" }}>
                      {formatRecurrenceTitle(recurrence)}
                    </p>
                    <p style={{ fontSize: 12, fontWeight: 700, color: "#15803d", marginTop: 4 }}>
                      {formatRecurrenceWindow(recurrence)}
                    </p>
                    <p style={{ fontSize: 11, fontWeight: 700, color: "#64748b", marginTop: 6 }}>
                      Estado: {recurrenceStateLabel(recurrence.estado)}
                    </p>
                  </div>
                </div>
                {recurrence.estado !== "CANCELADA" && participantRole && !isCompleted ? (
                  <button
                    type="button"
                    onClick={() => setShowCancelRecurrenceConfirm(true)}
                    disabled={submitting}
                    className="servify-cancel-program mt-3 w-full rounded-xl px-3 py-2 transition-all active:scale-95"
                    style={{ background: "#fee2e2", color: "#b91c1c", fontSize: 12, fontWeight: 900 }}
                  >
                    Cancelar todo el programa
                  </button>
                ) : null}
              </div>
            ) : null}

            {!isRecurringRequest ? (
              <button
              ref={agendaTriggerRef}
              type="button"
              onClick={() => setShowAgendaComposer(true)}
              disabled={!canManageAgenda}
              className="servify-agenda-cta flex w-full items-start gap-3 rounded-2xl p-3 text-left transition-all active:scale-[0.98]"
              style={{ background: canManageAgenda ? "#eff6ff" : "#f8fafc", border: "1.5px solid #dbeafe", opacity: canManageAgenda ? 1 : 0.72 }}
            >
              <span
                className="flex items-center justify-center rounded-xl shrink-0"
                style={{ width: 36, height: 36, background: canManageAgenda ? "#2563eb" : "#e2e8f0" }}
              >
                <CalendarPlus size={18} color={canManageAgenda ? "white" : "#64748b"} strokeWidth={2} />
              </span>
              <span className="min-w-0 flex-1">
                <span className="block" style={{ fontSize: 13, fontWeight: 900, color: "#0f172a" }}>Programar segunda visita</span>
                <span className="mt-1 block" style={{ fontSize: 12, fontWeight: 700, color: "#64748b", lineHeight: 1.45 }}>
                  {canManageAgenda
                    ? "Elegí fecha y horario en el siguiente paso."
                    : agendaUnavailableReason}
                </span>
              </span>
              {canManageAgenda ? <ChevronDown size={18} color="#2563eb" strokeWidth={2.2} /> : null}
              </button>
            ) : null}
            {upcomingEncounters.length === 0 && !loadingAgenda ? (
              <ServisHint
                compact
                tone="quiet"
                pose="peek"
                title="Sin visitas próximas"
                detail="Las visitas canceladas o completadas quedan guardadas abajo, en el historial."
              />
            ) : null}

            {upcomingEncounters.map((encounter) => {
              const ownProposal = currentUser?.id && encounter.propuestoPorId === currentUser.id;
              const canResolve = participantRole && !isCancelled && encounter.estado === "PROPUESTO" && !ownProposal && !submitting;
              const canCancel = participantRole && !isCancelled && !isClosedEncounter(encounter.estado) && !submitting;
              return (
                <EncounterCard
                  key={encounter.id}
                  encounter={encounter}
                  recurring={isRecurringRequest}
                  closesRequest={isPrimaryScheduledEncounter(request, encounter)}
                  canResolve={Boolean(canResolve)}
                  canCancel={Boolean(canCancel && !canResolve)}
                  onResolve={handleResolveEncounter}
                  onRequestCancel={setEncounterToCancel}
                />
              );
            })}

            {encounterHistory.length > 0 ? (
              <details className="servify-agenda-history rounded-xl">
                <summary className="flex cursor-pointer list-none items-center justify-between gap-3 px-3 py-2.5">
                  <span style={{ color: "#64748b", fontSize: 12, fontWeight: 900 }}>
                    Ver historial ({encounterHistory.length})
                  </span>
                  <ChevronDown className="servify-request-details-chevron" size={16} color="#64748b" strokeWidth={2.2} />
                </summary>
                <div className="flex flex-col gap-2 px-2 pb-2">
                  {encounterHistory.map((encounter) => (
                    <EncounterCard key={encounter.id} encounter={encounter} />
                  ))}
                </div>
              </details>
            ) : null}

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

        {hasChatContext && currentUser?.id && (
          <Card title="Chat">
            <button
              type="button"
              onClick={() => {
                if (!chatClosed) setShowChat(true);
              }}
              disabled={chatClosed}
              className="flex w-full items-center justify-center gap-2 rounded-2xl py-3 transition-all active:scale-95"
              style={{
                background: chatClosed ? "#f1f5f9" : "#eff6ff",
                color: chatClosed ? "#64748b" : "#2563eb",
                border: chatClosed ? "1.5px solid #e2e8f0" : "1.5px solid #bfdbfe",
                fontSize: 14,
                fontWeight: 900,
              }}
            >
              <MessageSquare size={17} strokeWidth={2.2} />
              {chatClosed ? "Chat finalizado" : `Abrir chat con ${chatCounterpartRole}`}
              {!chatClosed ? <Maximize2 size={15} strokeWidth={2.1} /> : null}
            </button>
          </Card>
        )}

        {hasProposal && !isCompleted && !isCancelled && (
          <Card title={isRecurringRequest ? "Pago y confirmación del encuentro" : "Pago y finalización"}>
            <div className="servify-form-surface rounded-2xl p-4" style={{ background: "#ffffff", border: "1.5px solid #dfe7f2" }}>
              {isRecurringRequest && activeRecurringEncounter ? (
                <div className="mb-4 rounded-xl px-3 py-2.5" style={{ background: "#eff6ff", border: "1px solid #bfdbfe" }}>
                  <p style={{ color: "#1d4ed8", fontSize: 11, fontWeight: 900 }}>Encuentro actual</p>
                  <p style={{ color: "#0f172a", fontSize: 13, fontWeight: 900, marginTop: 3 }}>
                    {formatEncounterDate(activeRecurringEncounter)}
                  </p>
                </div>
              ) : null}
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

              {bothConfirmed ? (
                <div
                  className="mt-4 rounded-2xl px-4 py-3 flex items-center gap-2"
                  style={{ background: "#dcfce7", border: "1px solid #bbf7d0" }}
                >
                  <CheckCircle size={18} color="#16a34a" strokeWidth={2} />
                  <p style={{ fontWeight: 700, fontSize: 13, color: "#15803d", lineHeight: 1.4 }}>
                    {isRecurringRequest ? "Encuentro confirmado." : "¡Servicio completado!"}
                  </p>
                </div>
              ) : null}

              {loadingState && (
                <div className="mt-3 flex items-center gap-2 text-sm" style={{ color: "#64748b" }}>
                  <Loader2 size={14} className="animate-spin" />
                  Cargando estado real del servicio...
                </div>
              )}

              {!loadingState && confirmationRole && needsAgreedPrice ? (
                <div className="servify-price-agreement">
                  <div className="servify-price-agreement__intro">
                    <div className="servify-price-agreement__icon" aria-hidden="true">
                      <DollarSign size={18} strokeWidth={2.3} />
                    </div>
                    <div>
                      <strong>{isRecurringRequest ? "Definir precio por encuentro" : "Definir precio del servicio"}</strong>
                      <p>
                        {confirmationRole === "SOLICITANTE"
                          ? "Coordiná con el prestador y cargá el importe para habilitar Mercado Pago."
                          : "El solicitante debe cargar el importe acordado antes de habilitar el pago."}
                      </p>
                    </div>
                  </div>

                  {confirmationRole === "SOLICITANTE" && !showPriceAgreement ? (
                    <div className="servify-price-agreement__actions">
                      <button
                        type="button"
                        className="servify-price-agreement__primary"
                        onClick={() => setShowPriceAgreement(true)}
                        disabled={!canAgreePrice}
                      >
                        Acordar precio
                      </button>
                      {chatAvailable ? (
                        <button
                          type="button"
                          className="servify-price-agreement__secondary"
                          onClick={() => setShowChat(true)}
                        >
                          <MessageSquare size={15} strokeWidth={2.1} />
                          Consultar por chat
                        </button>
                      ) : null}
                    </div>
                  ) : null}

                  {confirmationRole === "SOLICITANTE" && showPriceAgreement ? (
                    <form className="servify-price-agreement__form" onSubmit={handleAgreePrice}>
                      <label htmlFor={`price-agreement-${assignment?.id}`}>Precio acordado</label>
                      <div className="servify-price-agreement__field">
                        <span aria-hidden="true">$</span>
                        <input
                          ref={priceAgreementInputRef}
                          id={`price-agreement-${assignment?.id}`}
                          type="text"
                          inputMode="decimal"
                          autoComplete="off"
                          placeholder="20.000"
                          value={priceAgreement}
                          onChange={(event) => setPriceAgreement(event.target.value)}
                          disabled={submitting}
                        />
                        <small>ARS</small>
                      </div>
                      <p>Revisá el importe: una vez guardado se usará para crear el pago.</p>
                      <div className="servify-price-agreement__form-actions">
                        <button
                          type="button"
                          className="servify-price-agreement__secondary"
                          onClick={() => {
                            setShowPriceAgreement(false);
                            setPriceAgreement("");
                          }}
                          disabled={submitting}
                        >
                          Cancelar
                        </button>
                        <button
                          type="submit"
                          className="servify-price-agreement__primary"
                          disabled={submitting || parseMoney(priceAgreement) <= 0}
                        >
                          {submitting ? <Loader2 className="servify-payment__spinner" size={16} /> : null}
                          {submitting ? "Guardando..." : "Guardar y continuar"}
                        </button>
                      </div>
                    </form>
                  ) : null}

                  {confirmationRole === "PRESTADOR" && chatAvailable ? (
                    <button
                      type="button"
                      className="servify-price-agreement__secondary"
                      onClick={() => setShowChat(true)}
                    >
                      <MessageSquare size={15} strokeWidth={2.1} />
                      Escribir al solicitante
                    </button>
                  ) : null}
                </div>
              ) : null}

              {!loadingState
                && confirmationRole
                && currentUser?.id
                && assignment?.id
                && assignmentState?.solicitanteId
                && !needsAgreedPrice
                && (!isRecurringRequest || (paymentEncounterId && recurringEncounterReadyForPayment)) ? (
                  <ServicePaymentCard
                    solicitudId={String(request.id)}
                    asignacionServicioId={assignment.id}
                    encuentroId={paymentEncounterId}
                    solicitanteId={assignmentState.solicitanteId}
                    role={confirmationRole}
                    amount={paymentAmount}
                    alreadyConfirmed={currentUserAlreadyConfirmed}
                    disabled={submitting || !canConfirm}
                    onProviderConfirm={handleConfirm}
                    onStateRefresh={handlePaymentStateRefresh}
                  />
                ) : null}

              {!loadingState && confirmationRole && isRecurringRequest && !paymentEncounterId ? (
                <p className="servify-payment-waiting" role="status">
                  Estamos preparando el próximo encuentro antes de habilitar su pago.
                </p>
              ) : null}

              {!loadingState
                && confirmationRole
                && isRecurringRequest
                && activeRecurringEncounter
                && !recurringEncounterReadyForPayment ? (
                  <p className="servify-payment-waiting" role="status">
                    Próximo encuentro: {formatEncounterDate(activeRecurringEncounter)}. El pago y la confirmación se habilitan cuando termine.
                  </p>
                ) : null}

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
      <AnimatePresence>
        {showAgendaComposer && canManageAgenda ? (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 z-50 flex items-end justify-center bg-black/40 px-4 pb-6"
          >
            <motion.div
              initial={{ y: 28, opacity: 0 }}
              animate={{ y: 0, opacity: 1 }}
              exit={{ y: 28, opacity: 0 }}
              className="servify-agenda-dialog w-full max-w-sm rounded-3xl bg-white p-5"
              role="dialog"
              aria-modal="true"
              aria-labelledby="second-visit-title"
              aria-describedby="second-visit-description"
            >
              <div className="flex items-start justify-between gap-3">
                <div>
                  <h2 id="second-visit-title" style={{ color: "#0f172a", fontSize: 17, fontWeight: 900 }}>
                    Programar segunda visita
                  </h2>
                  <p id="second-visit-description" style={{ color: "#64748b", fontSize: 12, fontWeight: 700, lineHeight: 1.45, marginTop: 4 }}>
                    Proponé una fecha y un horario. La otra persona deberá aceptarlos.
                  </p>
                </div>
                <button
                  type="button"
                  onClick={closeAgendaComposer}
                  disabled={submitting}
                  aria-label="Cerrar programación de segunda visita"
                  className="flex shrink-0 items-center justify-center rounded-xl"
                  style={{ width: 38, height: 38, background: "#f1f5f9", color: "#64748b" }}
                >
                  <X size={18} strokeWidth={2.2} />
                </button>
              </div>

              <div className="mt-4 flex flex-col gap-3">
                <label className="flex flex-col gap-1.5">
                  <span style={{ color: "#475569", fontSize: 12, fontWeight: 900 }}>Fecha</span>
                  <input
                    ref={agendaDateInputRef}
                    type="date"
                    value={agendaDate}
                    onChange={(event) => setAgendaDate(event.target.value)}
                    min={todayInputValue()}
                    className="rounded-xl px-3 py-2.5 outline-none"
                    style={{ border: "1px solid #bfdbfe", color: "#0f172a", fontSize: 13, fontWeight: 700 }}
                  />
                </label>
                <div className="grid grid-cols-2 gap-2">
                  <label className="flex flex-col gap-1.5">
                    <span style={{ color: "#475569", fontSize: 12, fontWeight: 900 }}>Desde</span>
                    <select
                      value={agendaFrom}
                      onChange={(event) => setAgendaFrom(event.target.value)}
                      className="rounded-xl px-3 py-2.5 outline-none"
                      style={{ border: "1px solid #bfdbfe", color: "#0f172a", fontSize: 13, fontWeight: 700 }}
                    >
                      {TIME_OPTIONS.map((time) => <option key={time} value={time}>{time}</option>)}
                    </select>
                  </label>
                  <label className="flex flex-col gap-1.5">
                    <span style={{ color: "#475569", fontSize: 12, fontWeight: 900 }}>Hasta</span>
                    <select
                      value={agendaTo}
                      onChange={(event) => setAgendaTo(event.target.value)}
                      className="rounded-xl px-3 py-2.5 outline-none"
                      style={{ border: "1px solid #bfdbfe", color: "#0f172a", fontSize: 13, fontWeight: 700 }}
                    >
                      {TIME_OPTIONS.map((time) => <option key={time} value={time}>{time}</option>)}
                    </select>
                  </label>
                </div>
                {!agendaTimeRangeValid ? (
                  <p role="alert" style={{ fontSize: 11, color: "#b91c1c", fontWeight: 800 }}>
                    La hora de fin debe ser posterior a la hora de inicio.
                  </p>
                ) : null}
                <label className="flex flex-col gap-1.5">
                  <span style={{ color: "#475569", fontSize: 12, fontWeight: 900 }}>Mensaje (opcional)</span>
                  <textarea
                    value={agendaMessage}
                    onChange={(event) => setAgendaMessage(event.target.value)}
                    rows={2}
                    maxLength={500}
                    placeholder="Ej.: necesito revisar el avance"
                    className="w-full resize-none rounded-xl px-3 py-2.5 outline-none"
                    style={{ border: "1px solid #bfdbfe", color: "#0f172a", fontSize: 13, background: "white" }}
                  />
                </label>
              </div>

              <div className="mt-4 grid grid-cols-2 gap-2">
                <button
                  type="button"
                  onClick={closeAgendaComposer}
                  disabled={submitting}
                  className="rounded-2xl py-3 transition-all active:scale-95"
                  style={{ background: "#f1f5f9", color: "#475569", fontSize: 13, fontWeight: 900 }}
                >
                  Volver
                </button>
                <button
                  type="button"
                  onClick={handleProposeEncounter}
                  disabled={!canProposeAgenda}
                  className={canProposeAgenda ? "servify-action-primary rounded-2xl py-3 transition-all active:scale-95" : "servify-action-muted rounded-2xl py-3"}
                  style={{ fontSize: 13, fontWeight: 900 }}
                >
                  {submitting ? "Enviando..." : "Proponer visita"}
                </button>
              </div>
            </motion.div>
          </motion.div>
        ) : null}
      </AnimatePresence>

      <AnimatePresence>
        {encounterToCancel ? (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 z-50 flex items-end justify-center bg-black/40 px-4 pb-6"
          >
            <motion.div
              initial={{ y: 24, opacity: 0 }}
              animate={{ y: 0, opacity: 1 }}
              exit={{ y: 24, opacity: 0 }}
              className="servify-agenda-dialog w-full max-w-sm rounded-3xl bg-white p-5"
              role="dialog"
              aria-modal="true"
              aria-label={cancelsScheduledRequest ? "Confirmar cancelación del servicio programado" : "Confirmar cancelación de una visita"}
            >
              <ServisHint
                compact
                pose="lean"
                title={cancelsScheduledRequest ? "¿Cancelar el servicio programado?" : "¿Cancelar solo esta visita?"}
                detail={cancelsScheduledRequest
                  ? "Se cancelará el servicio programado y sus visitas pendientes. La solicitud quedará cancelada."
                  : `Se cancelará ${formatEncounterDate(encounterToCancel)}. Las demás visitas seguirán activas.`}
              />
              {chatAvailable ? (
                <button
                  type="button"
                  onClick={handleContactFromConfirmation}
                  disabled={submitting}
                  className="mt-4 flex w-full items-center justify-center gap-2 rounded-2xl py-3 transition-all active:scale-95"
                  style={{ background: "#eff6ff", color: "#2563eb", border: "1.5px solid #bfdbfe", fontSize: 13, fontWeight: 900 }}
                >
                  <MessageSquare size={16} strokeWidth={2.2} />
                  Contactar antes de cancelar
                </button>
              ) : null}
              <div className="mt-2 grid grid-cols-2 gap-2">
                <button
                  ref={cancelEncounterBackButtonRef}
                  type="button"
                  onClick={() => setEncounterToCancel(null)}
                  disabled={submitting}
                  className="rounded-2xl py-3 transition-all active:scale-95"
                  style={{ background: "#f1f5f9", color: "#475569", fontSize: 13, fontWeight: 900 }}
                >
                  Conservar visita
                </button>
                <button
                  type="button"
                  onClick={handleCancelEncounter}
                  disabled={submitting}
                  className="servify-action-danger rounded-2xl py-3 transition-all active:scale-95"
                  style={{ fontSize: 13, fontWeight: 900 }}
                >
                  {submitting ? "Cancelando..." : cancelsScheduledRequest ? "Cancelar servicio" : "Cancelar esta visita"}
                </button>
              </div>
            </motion.div>
          </motion.div>
        ) : null}
      </AnimatePresence>

      <AnimatePresence>
        {showCancelRecurrenceConfirm ? (
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
              aria-label="Confirmar cancelación del servicio recurrente"
            >
              <ServisHint
                compact
                pose="wave"
                title="¿Estás seguro?"
                detail={participantRole === "PRESTADOR"
                  ? "Avisale al solicitante antes de continuar. Se cancelarán todo el programa, sus visitas abiertas y la solicitud."
                  : "Avisale al prestador antes de continuar. Se cancelarán todo el programa, sus visitas abiertas y la solicitud."
                }
              />
              {chatAvailable ? (
                <button
                  type="button"
                  onClick={handleContactFromConfirmation}
                  disabled={submitting}
                  className="mt-4 flex w-full items-center justify-center gap-2 rounded-2xl py-3 transition-all active:scale-95"
                  style={{ background: "#eff6ff", color: "#2563eb", border: "1.5px solid #bfdbfe", fontSize: 13, fontWeight: 900 }}
                >
                  <MessageSquare size={16} strokeWidth={2.2} />
                  Contactar
                </button>
              ) : null}
              <div className="mt-2 grid grid-cols-2 gap-2">
                <button
                  ref={cancelRecurrenceBackButtonRef}
                  type="button"
                  onClick={() => setShowCancelRecurrenceConfirm(false)}
                  disabled={submitting}
                  className="rounded-2xl py-3 transition-all active:scale-95"
                  style={{ background: "#f1f5f9", color: "#475569", fontSize: 13, fontWeight: 900 }}
                >
                  Conservar programa
                </button>
                <button
                  type="button"
                  onClick={handleCancelRecurrence}
                  disabled={submitting}
                  className="servify-action-danger rounded-2xl py-3 transition-all active:scale-95"
                  style={{ fontSize: 13, fontWeight: 900 }}
                >
                  {submitting ? "Cancelando..." : "Cancelar programa"}
                </button>
              </div>
            </motion.div>
          </motion.div>
        ) : null}
      </AnimatePresence>
      <ServiceConnectionCelebration
        visible={showConnectionCelebration}
        requesterName={displayRequesterName}
        requesterInitials={displayRequesterInitials}
        providerName={displayProviderName}
        providerInitials={displayProviderInitials}
        onFinished={dismissConnectionCelebration}
      />
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

  useEffect(() => {
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === "Escape") onClose();
    };
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [onClose]);

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
    <div
      className="fixed inset-0 z-50 flex flex-col bg-white"
      role="dialog"
      aria-modal="true"
      aria-label={`Chat con ${counterpartName}`}
    >
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
          autoFocus
          aria-label="Cerrar chat"
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
            <ServisHint
              compact
              tone="quiet"
              title="Todavia no hay mensajes"
              detail="Usen este chat para coordinar detalles del servicio y dejar la conversacion dentro de Servify."
            />
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
            aria-label="Enviar mensaje"
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

interface EncounterCardProps {
  encounter: ApiServiceEncounter;
  recurring?: boolean;
  closesRequest?: boolean;
  canResolve?: boolean;
  canCancel?: boolean;
  onResolve?: (encounter: ApiServiceEncounter, decision: "ACEPTAR" | "RECHAZAR") => void;
  onRequestCancel?: (encounter: ApiServiceEncounter) => void;
}

function EncounterCard({
  encounter,
  recurring = false,
  closesRequest = false,
  canResolve = false,
  canCancel = false,
  onResolve,
  onRequestCancel,
}: EncounterCardProps) {
  return (
    <article className="servify-encounter-card rounded-2xl px-4 py-3" style={{ background: "#f8fafc", border: "1px solid #e2e8f0" }}>
      <div className="flex items-start justify-between gap-3">
        <div>
          {recurring ? (
            <p style={{ fontSize: 10, fontWeight: 900, color: "#7c3aed", marginBottom: 3 }}>Próxima sesión</p>
          ) : null}
          <p style={{ fontSize: 13, fontWeight: 900, color: "#0f172a" }}>{formatEncounterDate(encounter)}</p>
          <p style={{ fontSize: 12, fontWeight: 800, color: encounterStatusColor(encounter.estado), marginTop: 4 }}>
            {encounterStatusLabel(encounter.estado)}
          </p>
        </div>
        <CalendarPlus size={18} color="#2563eb" strokeWidth={2} />
      </div>
      {encounter.mensaje ? (
        <p style={{ fontSize: 12, color: "#64748b", fontWeight: 700, marginTop: 8, lineHeight: 1.45 }}>{encounter.mensaje}</p>
      ) : null}
      {canResolve && onResolve ? (
        <div className="mt-3 grid grid-cols-2 gap-2">
          <button
            type="button"
            onClick={() => onResolve(encounter, "RECHAZAR")}
            className="rounded-xl py-2.5 transition-all active:scale-95"
            style={{ background: "#fef2f2", color: "#b91c1c", fontSize: 12, fontWeight: 900 }}
          >
            Rechazar visita
          </button>
          <button
            type="button"
            onClick={() => onResolve(encounter, "ACEPTAR")}
            className="rounded-xl py-2.5 transition-all active:scale-95"
            style={{ background: "#dcfce7", color: "#15803d", fontSize: 12, fontWeight: 900 }}
          >
            Aceptar visita
          </button>
        </div>
      ) : null}
      {canCancel && onRequestCancel ? (
        <button
          type="button"
          onClick={() => onRequestCancel(encounter)}
          className="mt-3 w-full rounded-xl px-3 py-2.5 transition-all active:scale-95"
          style={{ background: "#fff7ed", color: "#c2410c", border: "1px solid #fed7aa", fontSize: 12, fontWeight: 900 }}
        >
          {closesRequest ? "Cancelar servicio programado" : "Cancelar esta visita · las demás continúan"}
        </button>
      ) : null}
    </article>
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

function formatEncounterDate(encounter: ApiServiceEncounter): string {
  const start = new Date(encounter.fechaInicio);
  const end = new Date(encounter.fechaFin);
  if (Number.isNaN(start.getTime())) return "Fecha a coordinar";
  const date = start.toLocaleDateString("es-AR", { day: "2-digit", month: "2-digit" });
  const from = start.toLocaleTimeString("es-AR", { hour: "2-digit", minute: "2-digit" });
  const to = Number.isNaN(end.getTime())
    ? ""
    : end.toLocaleTimeString("es-AR", { hour: "2-digit", minute: "2-digit" });
  return `${date} ${from}${to ? `-${to}` : ""}`;
}

function hasEncounterEnded(encounter: ApiServiceEncounter): boolean {
  const end = new Date(encounter.fechaFin).getTime();
  return Number.isFinite(end) && end <= Date.now();
}

function isPrimaryScheduledEncounter(request: ServiceRequest, encounter: ApiServiceEncounter): boolean {
  if (request.scheduleType !== "PROGRAMADA" || encounter.recurrenciaServicioId) return false;
  return sameDateTime(request.scheduledStart, encounter.fechaInicio)
    && sameDateTime(request.scheduledEnd, encounter.fechaFin);
}

function sameDateTime(left?: string, right?: string): boolean {
  if (!left || !right) return false;
  const leftTime = new Date(left).getTime();
  const rightTime = new Date(right).getTime();
  return Number.isFinite(leftTime) && Number.isFinite(rightTime) && leftTime === rightTime;
}

function encounterStatusLabel(status: ApiServiceEncounter["estado"]): string {
  if (status === "PROPUESTO") return "Pendiente de aceptacion";
  if (status === "CONFIRMADO") return "Confirmado";
  if (status === "RECHAZADO") return "Rechazado";
  if (status === "CANCELADO") return "Cancelado";
  return "Completado";
}

function encounterStatusColor(status: ApiServiceEncounter["estado"]): string {
  if (status === "PROPUESTO") return "#0891b2";
  if (status === "CONFIRMADO") return "#16a34a";
  if (status === "RECHAZADO" || status === "CANCELADO") return "#b91c1c";
  return "#64748b";
}

function formatRecurrenceTitle(recurrence: ApiServiceRecurrence): string {
  return `Encuentros ${recurrenceFrequencyLabel(recurrence.frecuencia)}`;
}

function formatRecurrenceWindow(recurrence: ApiServiceRecurrence): string {
  const day = weekDayLabel(recurrence.diaSemana);
  const from = timeShort(recurrence.horaDesde);
  const to = timeShort(recurrence.horaHasta);
  const start = formatDateLabel(recurrence.fechaInicio);
  const end = recurrence.fechaFin ? `hasta ${formatDateLabel(recurrence.fechaFin)}` : "sin fecha final";
  return `${day} de ${from} a ${to}, desde ${start} ${end}.`;
}

function recurrenceFrequencyLabel(frequency?: string): string {
  if (frequency === "QUINCENAL") return "quincenales";
  if (frequency === "MENSUAL") return "mensuales";
  return "semanales";
}

function recurrenceStateLabel(status?: ApiServiceRecurrence["estado"]): string {
  if (status === "ACTIVA") return "Programa activo";
  if (status === "FINALIZADA") return "Programa finalizado";
  if (status === "CANCELADA") return "Programa cancelado";
  return "Buscando prestador";
}

function weekDayLabel(day?: string): string {
  const labels: Record<string, string> = {
    MONDAY: "Lunes",
    TUESDAY: "Martes",
    WEDNESDAY: "Miercoles",
    THURSDAY: "Jueves",
    FRIDAY: "Viernes",
    SATURDAY: "Sabado",
    SUNDAY: "Domingo",
  };
  return day ? labels[day] ?? formatBackendState(day) : "Dia a coordinar";
}

function timeShort(value?: string): string {
  return value ? value.slice(0, 5) : "--:--";
}

function formatDateLabel(value?: string): string {
  if (!value) return "fecha a coordinar";
  const date = new Date(`${value.slice(0, 10)}T00:00:00`);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleDateString("es-AR", { day: "2-digit", month: "2-digit", year: "numeric" });
}

function formatBackendState(value?: string): string {
  if (!value) return "sin estado";
  return value.toLowerCase().replace(/_/g, " ");
}

function isClosedEncounter(status: ApiServiceEncounter["estado"]): boolean {
  return status === "RECHAZADO" || status === "CANCELADO" || status === "COMPLETADO";
}

function isTimeRangeValid(from: string, to: string): boolean {
  return Boolean(from && to && to > from);
}

function toLocalDateTime(date: string, time: string): string | undefined {
  if (!date) return undefined;
  return `${date}T${time.length === 5 ? `${time}:00` : time}`;
}

function todayInputValue(): string {
  const today = new Date();
  const month = String(today.getMonth() + 1).padStart(2, "0");
  const day = String(today.getDate()).padStart(2, "0");
  return `${today.getFullYear()}-${month}-${day}`;
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
