export interface PaymentReturnContext {
  pagoId: string;
  solicitudId: string;
  encuentroId?: string;
  resultado?: "success" | "pending" | "failure";
  mercadoPagoPaymentId?: string;
}

const PAYMENT_RETURN_STORAGE_KEY = "servify.payment-return";
const PAYMENT_QUERY_KEYS = [
  "pagoId",
  "solicitudId",
  "encuentroId",
  "resultado",
  "payment_id",
  "status",
  "external_reference",
  "collection_id",
  "collection_status",
  "payment_type",
  "merchant_order_id",
  "preference_id",
  "site_id",
  "processing_mode",
  "merchant_account_id",
] as const;

export function readPaymentReturnFromUrl(): PaymentReturnContext | null {
  if (typeof window === "undefined") return null;

  const params = new URLSearchParams(window.location.search);
  const pagoId = params.get("pagoId")?.trim() ?? "";
  const solicitudId = params.get("solicitudId")?.trim() ?? "";
  if (!pagoId || !solicitudId) return null;

  const rawResult = params.get("resultado");
  const resultado = rawResult === "success" || rawResult === "pending" || rawResult === "failure"
    ? rawResult
    : undefined;

  return {
    pagoId,
    solicitudId,
    encuentroId: params.get("encuentroId")?.trim() || undefined,
    resultado,
    mercadoPagoPaymentId: params.get("payment_id")?.trim() || undefined,
  };
}

export function readStoredPaymentReturn(): PaymentReturnContext | null {
  if (typeof sessionStorage === "undefined") return null;
  const raw = sessionStorage.getItem(PAYMENT_RETURN_STORAGE_KEY);
  if (!raw) return null;

  try {
    const parsed = JSON.parse(raw) as Partial<PaymentReturnContext>;
    if (!parsed.pagoId || !parsed.solicitudId) return null;
    return {
      pagoId: parsed.pagoId,
      solicitudId: parsed.solicitudId,
      encuentroId: parsed.encuentroId,
      resultado: parsed.resultado,
      mercadoPagoPaymentId: parsed.mercadoPagoPaymentId,
    };
  } catch {
    sessionStorage.removeItem(PAYMENT_RETURN_STORAGE_KEY);
    return null;
  }
}

export function storePaymentReturn(context: PaymentReturnContext): void {
  if (typeof sessionStorage === "undefined") return;
  sessionStorage.setItem(PAYMENT_RETURN_STORAGE_KEY, JSON.stringify(context));
}

export function clearStoredPaymentReturn(pagoId?: string): void {
  if (typeof sessionStorage === "undefined") return;
  if (pagoId) {
    const stored = readStoredPaymentReturn();
    if (stored && stored.pagoId !== pagoId) return;
  }
  sessionStorage.removeItem(PAYMENT_RETURN_STORAGE_KEY);
}

export function cleanPaymentReturnUrl(): void {
  if (typeof window === "undefined") return;
  const url = new URL(window.location.href);
  PAYMENT_QUERY_KEYS.forEach((key) => url.searchParams.delete(key));
  window.history.replaceState({}, document.title, `${url.pathname}${url.search}${url.hash}`);
}
