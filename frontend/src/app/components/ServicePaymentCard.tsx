import { useCallback, useEffect, useRef, useState } from "react";
import {
  AlertCircle,
  CheckCircle2,
  Clock3,
  ExternalLink,
  Loader2,
  RefreshCw,
  ShieldCheck,
  WalletCards,
  XCircle,
} from "lucide-react";
import { formatMoney, servifyApi, type ApiServicePayment, type ApiServicePaymentStatus } from "../api";
import {
  clearStoredPaymentReturn,
  readStoredPaymentReturn,
  storePaymentReturn,
  type PaymentReturnContext,
} from "../paymentReturn";

interface ServicePaymentCardProps {
  solicitudId: string;
  asignacionServicioId: string;
  encuentroId?: string;
  solicitanteId: string;
  role: "SOLICITANTE" | "PRESTADOR";
  amount: number;
  alreadyConfirmed: boolean;
  disabled?: boolean;
  onProviderConfirm: () => Promise<void>;
  onStateRefresh: () => Promise<void>;
}

type PaymentAction = "checkout" | "sync" | "confirm" | null;

const PAYMENT_STATUS_CONFIG: Record<ApiServicePaymentStatus, {
  label: string;
  tone: string;
  icon: typeof Clock3;
}> = {
  PENDIENTE: { label: "Pago pendiente", tone: "pending", icon: Clock3 },
  APROBADO: { label: "Pago aprobado", tone: "approved", icon: CheckCircle2 },
  RECHAZADO: { label: "Pago rechazado", tone: "rejected", icon: XCircle },
  CANCELADO: { label: "Pago cancelado", tone: "cancelled", icon: XCircle },
  ERROR: { label: "Revisar pago", tone: "error", icon: AlertCircle },
};

export function ServicePaymentCard({
  solicitudId,
  asignacionServicioId,
  encuentroId,
  solicitanteId,
  role,
  amount,
  alreadyConfirmed,
  disabled = false,
  onProviderConfirm,
  onStateRefresh,
}: ServicePaymentCardProps) {
  const [payment, setPayment] = useState<ApiServicePayment | null>(null);
  const [pendingReturn, setPendingReturn] = useState<PaymentReturnContext | null>(() => {
    const stored = readStoredPaymentReturn();
    return paymentReturnMatchesTarget(stored, solicitudId, encuentroId) ? stored : null;
  });
  const [checkoutUrl, setCheckoutUrl] = useState("");
  const [loading, setLoading] = useState(true);
  const [action, setAction] = useState<PaymentAction>(null);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const notifiedApprovedPaymentRef = useRef("");
  const pollingEnabledRef = useRef(false);
  const pollCountRef = useRef(0);
  const activeTargetRef = useRef("");
  const targetKey = `${solicitudId}:${asignacionServicioId}:${encuentroId ?? "single"}`;

  const applyPayment = useCallback(
    async (nextPayment: ApiServicePayment | null | undefined) => {
      const next = nextPayment ?? null;
      setPayment(next);
      if (next?.checkoutUrl) setCheckoutUrl(next.checkoutUrl);

      if (next && ["APROBADO", "RECHAZADO", "CANCELADO"].includes(next.estado)) {
        clearStoredPaymentReturn(next.id);
        setPendingReturn(null);
      }

      const storedReturn = readStoredPaymentReturn();
      pollingEnabledRef.current = next?.estado === "PENDIENTE"
        || paymentReturnMatchesTarget(storedReturn, solicitudId, encuentroId);

      if (next?.estado === "APROBADO" && notifiedApprovedPaymentRef.current !== next.id) {
        notifiedApprovedPaymentRef.current = next.id;
        await onStateRefresh();
      }
    },
    [encuentroId, onStateRefresh, solicitudId]
  );

  const loadPayment = useCallback(
    async (silent = false) => {
      const requestedTarget = targetKey;
      if (!silent) setLoading(true);
      try {
        const next = await servifyApi.getServicePaymentState({
          solicitudId,
          asignacionServicioId,
          encuentroId,
        });
        if (activeTargetRef.current !== requestedTarget) return;
        await applyPayment(next);
        if (!silent) setError("");
      } catch (loadError) {
        if (!silent && activeTargetRef.current === requestedTarget) {
          setError(loadError instanceof Error ? loadError.message : "No se pudo consultar el pago");
        }
      } finally {
        if (!silent && activeTargetRef.current === requestedTarget) setLoading(false);
      }
    },
    [applyPayment, asignacionServicioId, encuentroId, solicitudId, targetKey]
  );

  useEffect(() => {
    activeTargetRef.current = targetKey;
    pollingEnabledRef.current = false;
    pollCountRef.current = 0;
    setPayment(null);
    setCheckoutUrl("");
    setError("");
    setNotice("");
    notifiedApprovedPaymentRef.current = "";
    const stored = readStoredPaymentReturn();
    const matchingReturn = paymentReturnMatchesTarget(stored, solicitudId, encuentroId);
    setPendingReturn(matchingReturn ? stored : null);
    pollingEnabledRef.current = matchingReturn;

    void loadPayment();
    const intervalId = window.setInterval(() => {
      if (!pollingEnabledRef.current || pollCountRef.current >= 20) return;
      pollCountRef.current += 1;
      void loadPayment(true);
    }, 6000);

    return () => {
      activeTargetRef.current = "";
      window.clearInterval(intervalId);
    };
  }, [asignacionServicioId, encuentroId, loadPayment, solicitudId, targetKey]);

  const handleStartCheckout = async () => {
    const checkoutPopup = window.open("about:blank", "_blank");
    if (checkoutPopup) checkoutPopup.opener = null;
    setAction("checkout");
    setError("");
    setNotice("");
    try {
      const next = await servifyApi.createServicePaymentCheckout({
        solicitudId,
        solicitanteId,
        asignacionServicioId,
        encuentroId,
      });
      await applyPayment(next);
      if (!next.checkoutUrl) {
        throw new Error("Mercado Pago no devolvió un enlace de checkout.");
      }

      const context: PaymentReturnContext = { pagoId: next.id, solicitudId, encuentroId };
      storePaymentReturn(context);
      setPendingReturn(context);
      setCheckoutUrl(next.checkoutUrl);
      const popupAvailable = Boolean(checkoutPopup && !checkoutPopup.closed);
      if (checkoutPopup && popupAvailable) checkoutPopup.location.replace(next.checkoutUrl);
      setNotice(
        popupAvailable
          ? "Mercado Pago se abrió en otra pestaña. Al terminar, volvé acá y verificá el pago."
          : "Si Mercado Pago no se abrió automáticamente, usá el botón “Abrir Mercado Pago”."
      );
    } catch (checkoutError) {
      checkoutPopup?.close();
      setError(checkoutError instanceof Error ? checkoutError.message : "No se pudo iniciar Mercado Pago");
    } finally {
      setAction(null);
    }
  };

  const handleSyncPayment = async () => {
    const pagoId = payment?.id ?? pendingReturn?.pagoId;
    if (!pagoId) {
      await loadPayment();
      return;
    }

    setAction("sync");
    setError("");
    setNotice("");
    try {
      const next = await servifyApi.syncServicePayment({
        pagoId,
        solicitanteId,
        mercadoPagoPaymentId: pendingReturn?.mercadoPagoPaymentId,
      });
      await applyPayment(next);
      setNotice(
        next.estado === "APROBADO"
          ? "Pago aprobado y confirmado por Servify."
          : "Mercado Pago todavía no confirmó la acreditación. Podés volver a verificar en unos segundos."
      );
    } catch (syncError) {
      setError(syncError instanceof Error ? syncError.message : "No se pudo verificar el pago");
    } finally {
      setAction(null);
    }
  };

  const handleProviderConfirm = async () => {
    setAction("confirm");
    try {
      await onProviderConfirm();
    } finally {
      setAction(null);
    }
  };

  const status = payment ? PAYMENT_STATUS_CONFIG[payment.estado] : null;
  const StatusIcon = status?.icon ?? WalletCards;
  const displayedAmount = payment?.monto ?? amount;
  const hasPayableAmount = Number.isFinite(displayedAmount) && displayedAmount > 0;
  const busy = disabled || loading || action !== null;
  const paymentApproved = payment?.estado === "APROBADO";
  const canRetryCheckout = !payment || ["RECHAZADO", "CANCELADO", "ERROR"].includes(payment.estado);
  const canVerify = Boolean(payment?.id || pendingReturn?.pagoId);

  return (
    <section className="servify-payment" aria-labelledby="service-payment-title">
      <div className="servify-payment__header">
        <div className="servify-payment__brand" aria-hidden="true">
          <WalletCards size={20} strokeWidth={2.2} />
        </div>
        <div className="servify-payment__heading">
          <p id="service-payment-title">Pago con Mercado Pago</p>
          <span>{encuentroId ? "Este encuentro" : "Servicio completo"}</span>
        </div>
        <strong className="servify-payment__amount">{formatMoney(displayedAmount)}</strong>
      </div>

      <div className={`servify-payment__status servify-payment__status--${status?.tone ?? "idle"}`} role="status" aria-live="polite">
        <StatusIcon size={18} strokeWidth={2.2} aria-hidden="true" />
        <div>
          <strong>{status?.label ?? (role === "SOLICITANTE" ? "Listo para pagar" : "Esperando el pago")}</strong>
          <p>{paymentStatusDescription(payment?.estado, role, alreadyConfirmed)}</p>
        </div>
      </div>

      {loading ? (
        <div className="servify-payment__loading" role="status">
          <Loader2 className="servify-payment__spinner" size={16} aria-hidden="true" />
          Consultando el estado real...
        </div>
      ) : null}

      {error ? (
        <p className="servify-payment__message servify-payment__message--error" role="alert">
          <AlertCircle size={15} aria-hidden="true" />
          {error}
        </p>
      ) : null}

      {notice ? <p className="servify-payment__message" role="status">{notice}</p> : null}

      {role === "SOLICITANTE" && !alreadyConfirmed ? (
        <div className="servify-payment__actions">
          {canRetryCheckout && hasPayableAmount ? (
            <button type="button" onClick={handleStartCheckout} disabled={busy} className="servify-payment__primary">
              {action === "checkout" ? <Loader2 className="servify-payment__spinner" size={17} aria-hidden="true" /> : <WalletCards size={17} aria-hidden="true" />}
              {action === "checkout" ? "Abriendo Mercado Pago..." : payment ? "Reintentar pago" : "Pagar y confirmar"}
            </button>
          ) : null}

          {!hasPayableAmount ? (
            <p className="servify-payment__message servify-payment__message--error" role="alert">
              <AlertCircle size={15} aria-hidden="true" />
              Falta acordar un monto antes de iniciar el pago.
            </p>
          ) : null}

          {checkoutUrl && !paymentApproved ? (
            <a className="servify-payment__secondary" href={checkoutUrl} target="_blank" rel="noreferrer">
              <ExternalLink size={16} aria-hidden="true" />
              Abrir Mercado Pago
            </a>
          ) : null}

          {canVerify && !paymentApproved ? (
            <button type="button" onClick={handleSyncPayment} disabled={busy} className="servify-payment__verify">
              {action === "sync" ? <Loader2 className="servify-payment__spinner" size={16} aria-hidden="true" /> : <RefreshCw size={16} aria-hidden="true" />}
              {action === "sync" ? "Verificando..." : "Ya pagué, verificar pago"}
            </button>
          ) : null}
        </div>
      ) : null}

      {role === "PRESTADOR" && paymentApproved && payment.canConfirmProvider && !alreadyConfirmed ? (
        <button type="button" onClick={handleProviderConfirm} disabled={busy} className="servify-payment__primary">
          {action === "confirm" ? <Loader2 className="servify-payment__spinner" size={17} aria-hidden="true" /> : <CheckCircle2 size={17} aria-hidden="true" />}
          {action === "confirm" ? "Confirmando..." : "Confirmar cobro y finalización"}
        </button>
      ) : null}

      {role === "PRESTADOR" && !paymentApproved && !alreadyConfirmed ? (
        <button type="button" onClick={() => void loadPayment()} disabled={busy || loading} className="servify-payment__verify">
          {loading ? <Loader2 className="servify-payment__spinner" size={16} aria-hidden="true" /> : <RefreshCw size={16} aria-hidden="true" />}
          {loading ? "Actualizando..." : "Actualizar estado del pago"}
        </button>
      ) : null}

      <div className="servify-payment__security">
        <ShieldCheck size={16} strokeWidth={2.2} aria-hidden="true" />
        <p>Servify no guarda los datos de tu tarjeta. Mercado Pago procesa el cobro y el servidor verifica la acreditación.</p>
      </div>
    </section>
  );
}

function paymentReturnMatchesTarget(
  context: PaymentReturnContext | null,
  solicitudId: string,
  encuentroId?: string
): context is PaymentReturnContext {
  if (!context || context.solicitudId !== solicitudId) return false;
  return encuentroId ? context.encuentroId === encuentroId : !context.encuentroId;
}

function paymentStatusDescription(
  status: ApiServicePaymentStatus | undefined,
  role: "SOLICITANTE" | "PRESTADOR",
  alreadyConfirmed: boolean
): string {
  if (status === "APROBADO") {
    if (role === "SOLICITANTE") return "Pago verificado. Tu confirmación quedó registrada automáticamente.";
    return alreadyConfirmed
      ? "Cobro y finalización confirmados."
      : "Mercado Pago ya acreditó el pago. Podés confirmar el cobro y la finalización.";
  }
  if (status === "PENDIENTE") return "Mercado Pago todavía no confirmó la acreditación.";
  if (status === "RECHAZADO") return "El pago no fue aprobado. El solicitante puede reintentarlo.";
  if (status === "CANCELADO") return "El checkout se canceló sin registrar un cobro.";
  if (status === "ERROR") return "No pudimos validar el último intento. Verificá el estado o reintentá.";
  if (alreadyConfirmed) return "La confirmación de esta parte ya fue registrada.";
  return role === "SOLICITANTE"
    ? "Pagás el monto acordado y Servify confirma el resultado con Mercado Pago."
    : "La confirmación se habilita únicamente cuando el pago está aprobado.";
}
