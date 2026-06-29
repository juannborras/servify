import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import {
  Search,
  ChevronRight,
  Bell,
  RefreshCcw,
  X,
  CheckCircle,
  XCircle,
  MessageSquare,
  UserRound,
  Hammer,
  Laptop,
  BookOpen,
  MoreHorizontal,
  Wrench,
  Sparkles,
  Palette,
  Camera,
  HeartPulse,
  Star,
  BriefcaseBusiness,
  type LucideIcon,
} from "lucide-react";
import { motion } from "motion/react";
import { servifyApi, type ApiCategory, type ApiNotification, type ApiPublicProvider, type ApiPublication, type ApiReceivedRequest, type ApiRequest, type SessionUser } from "../api";
import type { ServiceRequest } from "./RequestsScreen";
import { PullToRefreshIndicator, usePullToRefresh } from "./PullToRefresh";
import servifySymbol from "../../imports/servify-symbol.png";

type CategoryImageKey = "home" | "trades" | "classes" | "tech" | "cleaning" | "design" | "repair" | "photo" | "wellness" | "other";

type CategoryItem = {
  id: number;
  label: string;
  shortLabel: string;
  icon: LucideIcon;
  color: string;
  bg: string;
  imageKey: CategoryImageKey;
};

type ServiceHighlightItem = {
  title: string;
  category: string;
  requestCount: number;
  imageKey: CategoryImageKey;
  color: string;
  isDefault?: boolean;
};

const categories: CategoryItem[] = [
  { id: 1, label: "Oficios", shortLabel: "Oficios", icon: Hammer, color: "#0f766e", bg: "#ecfeff", imageKey: "trades" },
  { id: 2, label: "Clases particulares", shortLabel: "Clases", icon: BookOpen, color: "#7c3aed", bg: "#f5f3ff", imageKey: "classes" },
  { id: 3, label: "Soporte tecnico", shortLabel: "Tecnologia", icon: Laptop, color: "#2563eb", bg: "#eff6ff", imageKey: "tech" },
  { id: 4, label: "Limpieza", shortLabel: "Limpieza", icon: Sparkles, color: "#0891b2", bg: "#ecfeff", imageKey: "cleaning" },
  { id: 5, label: "Diseno", shortLabel: "Diseno", icon: Palette, color: "#db2777", bg: "#fdf2f8", imageKey: "design" },
  { id: 6, label: "Reparaciones", shortLabel: "Reparar", icon: Wrench, color: "#d97706", bg: "#fffbeb", imageKey: "repair" },
  { id: 7, label: "Fotografia", shortLabel: "Fotos", icon: Camera, color: "#16a34a", bg: "#f0fdf4", imageKey: "photo" },
  { id: 8, label: "Salud y bienestar", shortLabel: "Bienestar", icon: HeartPulse, color: "#059669", bg: "#ecfdf5", imageKey: "wellness" },
  { id: 9, label: "Otro", shortLabel: "Otros", icon: MoreHorizontal, color: "#7c3aed", bg: "#f5f3ff", imageKey: "other" },
];

const defaultPopularCategoryLabels = ["Oficios", "Clases particulares", "Soporte tecnico"];
const popularCategories = new Set<string>(defaultPopularCategoryLabels);
const minimumRequestsForPopular = 3;

interface ExploreScreenProps {
  userName: string;
  notificationCount?: number;
  onOpenNotifications?: () => void;
  onCreateRequest: () => void;
  onPublishService?: () => void;
  onCategoryPress: (cat: string) => void;
  onAcceptedRequest?: (request: ServiceRequest) => void;
  onProviderPress: (provider: ApiPublicProvider) => void;
}

export function ExploreScreen({
  user,
  userName,
  notificationCount = 0,
  onOpenNotifications,
  onCreateRequest,
  onPublishService,
  onCategoryPress,
  onAcceptedRequest,
  onProviderPress,
}: ExploreScreenProps & { user?: SessionUser | null }) {
  const firstName = userName.split(" ")[0];
  const [remoteRequests, setRemoteRequests] = useState<ApiReceivedRequest[] | null>(null);
  const [ownRequests, setOwnRequests] = useState<ApiRequest[]>([]);
  const [ownAssignmentStates, setOwnAssignmentStates] = useState<Record<string, Awaited<ReturnType<typeof servifyApi.getAssignmentState>> | null>>({});
  const [ownPublications, setOwnPublications] = useState<ApiPublication[]>([]);
  const [activeCategories, setActiveCategories] = useState<ApiCategory[]>([]);
  const [adminNotifications, setAdminNotifications] = useState<ApiNotification[]>([]);
  const [activityOpen, setActivityOpen] = useState(false);
  const [activityLoading, setActivityLoading] = useState(false);
  const [activityError, setActivityError] = useState("");
  const [actionLoading, setActionLoading] = useState("");
  const [counterOfferFor, setCounterOfferFor] = useState<string | null>(null);
  const [counterPrice, setCounterPrice] = useState("");
  const [counterMessage, setCounterMessage] = useState("");
  const [providerSearch, setProviderSearch] = useState("");
  const [providers, setProviders] = useState<ApiPublicProvider[]>([]);
  const [providersLoading, setProvidersLoading] = useState(false);
  const [providersError, setProvidersError] = useState("");
  const [showAllCategories, setShowAllCategories] = useState(false);
  const categoriesPanelRef = useRef<HTMLDivElement | null>(null);

  const loadHomeData = useCallback(async (isCancelled: () => boolean = () => false) => {
    setActivityLoading(Boolean(user));
    setRemoteRequests(null);
    setOwnRequests([]);
    setOwnAssignmentStates({});
    setOwnPublications([]);
    setActiveCategories([]);
    setAdminNotifications([]);
    setActivityError("");

    if (!user) {
      setActivityLoading(false);
      return;
    }

    const shouldLoadProviderData = user.role === "provider" || user.role === "both";

    try {
      const [received, requests, publications, loadedCategories, notifications] = await Promise.all([
        shouldLoadProviderData ? servifyApi.listReceivedRequests(String(user.id)).catch(() => []) : Promise.resolve([]),
        servifyApi.listUserRequests(String(user.id)).catch(() => []),
        shouldLoadProviderData ? servifyApi.listUserPublications(String(user.id)).catch(() => []) : Promise.resolve([]),
        servifyApi.listCategories().catch(() => []),
        servifyApi.listNotifications(String(user.id)).catch(() => []),
      ]);

      if (isCancelled()) return;
      const assignmentEntries = await Promise.all(
        (requests || []).map(async (request) => [
          request.id,
          await servifyApi.getAssignmentState(request.id).catch(() => null),
        ] as const)
      );
      if (isCancelled()) return;

      setRemoteRequests(received || []);
      setOwnRequests(requests || []);
      setOwnAssignmentStates(Object.fromEntries(assignmentEntries));
      setOwnPublications(publications || []);
      setActiveCategories(loadedCategories || []);
      setAdminNotifications(notifications || []);
    } finally {
      if (!isCancelled()) setActivityLoading(false);
    }
  }, [user]);

  useEffect(() => {
    let ignore = false;
    void loadHomeData(() => ignore);

    return () => {
      ignore = true;
    };
  }, [loadHomeData]);

  useEffect(() => {
    let ignore = false;
    const query = providerSearch.trim();
    setProvidersError("");

    if (!query) {
      setProviders([]);
      setProvidersLoading(false);
      return;
    }

    setProvidersLoading(true);
    setProvidersError("");

    const timeoutId = window.setTimeout(() => {
      servifyApi.searchProvidersByUsername(query)
        .then((items) => {
          if (!ignore) setProviders(items);
        })
        .catch((err) => {
          if (!ignore) setProvidersError(err instanceof Error ? err.message : "No se pudieron cargar prestadores");
        })
        .finally(() => {
          if (!ignore) setProvidersLoading(false);
        });
    }, 250);

    return () => {
      ignore = true;
      window.clearTimeout(timeoutId);
    };
  }, [providerSearch]);

  const activity = useMemo(
    () => buildActivitySummary(remoteRequests ?? [], ownRequests, ownPublications, ownAssignmentStates, adminNotifications),
    [adminNotifications, ownAssignmentStates, ownPublications, ownRequests, remoteRequests]
  );
  const compatibleRequests = useMemo(
    () => (remoteRequests ?? []).filter(isCompatibleReceived),
    [remoteRequests]
  );
  const popularServices = useMemo(
    () => buildPopularServices([...ownRequests, ...(remoteRequests ?? [])], activeCategories),
    [activeCategories, ownRequests, remoteRequests]
  );
  const visibleCategories = showAllCategories
    ? categories
    : categories.filter((category) => popularCategories.has(category.label));

  const { pullDistance, refreshing, pullHandlers } = usePullToRefresh(loadHomeData, Boolean(user));

  const reloadReceivedRequests = async () => {
    if (!user || !(user.role === "provider" || user.role === "both")) return;
    const received = await servifyApi.listReceivedRequests(String(user.id)).catch(() => []);
    setRemoteRequests(received || []);
  };

  const markAdminNotificationsRead = async () => {
    if (!user?.id) return;
    const unread = adminNotifications.filter((notification) => !notification.leida);
    if (unread.length === 0) return;
    await Promise.all(
      unread.map((notification) =>
        servifyApi.markNotificationRead(String(user.id), notification.id).catch(() => null)
      )
    );
    setAdminNotifications((current) => current.map((notification) => ({ ...notification, leida: true })));
  };

  const handleDistributionResponse = async (request: ApiReceivedRequest, tipoRespuesta: "ACEPTAR" | "RECHAZAR") => {
    if (!user?.id || !request.distribucionSolicitudId) {
      setActivityError("No se pudo identificar la solicitud compatible.");
      return;
    }
    setActivityError("");
    setActionLoading(`${request.distribucionSolicitudId}-${tipoRespuesta}`);
    try {
      await servifyApi.respondToDistribution({
        distribucionSolicitudId: request.distribucionSolicitudId,
        prestadorId: String(user.id),
        tipoRespuesta,
      });
      await reloadReceivedRequests();
      if (tipoRespuesta === "ACEPTAR") {
        onAcceptedRequest?.(mapReceivedRequestForDetail(request, String(user.id)));
      }
    } catch (err) {
      setActivityError(err instanceof Error ? err.message : "No se pudo responder la solicitud");
    } finally {
      setActionLoading("");
    }
  };

  const handleCounterOffer = async (request: ApiReceivedRequest) => {
    if (!user?.id || !request.distribucionSolicitudId) {
      setActivityError("No se pudo identificar la solicitud compatible.");
      return;
    }
    if (!counterPrice.trim()) {
      setActivityError("Indica un importe para contraofertar.");
      return;
    }
    setActivityError("");
    setActionLoading(`${request.distribucionSolicitudId}-CONTRAOFERTA`);
    try {
      await servifyApi.createCounterOffer({
        distribucionSolicitudId: request.distribucionSolicitudId,
        prestadorId: String(user.id),
        precioPropuesto: counterPrice,
        mensaje: counterMessage,
      });
      setCounterOfferFor(null);
      setCounterPrice("");
      setCounterMessage("");
      await reloadReceivedRequests();
    } catch (err) {
      setActivityError(err instanceof Error ? err.message : "No se pudo emitir la contraoferta");
    } finally {
      setActionLoading("");
    }
  };

  return (
    <div className="servify-home-shell flex flex-col h-full">
      {/* Header */}
      <div className="servify-home-header px-5 pt-11 pb-5">
        <div className="relative mb-5 flex items-center justify-center">
          <div className="servify-home-brand">
            <img src={servifySymbol} alt="" aria-hidden="true" />
            <span>Servify</span>
          </div>
          <button
            type="button"
            onClick={() => onOpenNotifications?.()}
            className="servify-home-bell absolute right-0 top-1/2 flex -translate-y-1/2 items-center justify-center rounded-2xl transition-all active:scale-95"
            aria-label="Abrir notificaciones"
          >
            <Bell size={20} strokeWidth={1.8} />
            {notificationCount > 0 ? (
              <span className="servify-home-badge">
                {notificationCount > 9 ? "9+" : notificationCount}
              </span>
            ) : null}
          </button>
        </div>

        <div className="mb-5">
          <h1 className="servify-home-title">¡Hola, {firstName}!</h1>
          <p className="servify-home-subtitle">Que necesitas hoy?</p>
        </div>

        {activityOpen ? (
          <motion.div
            initial={{ opacity: 0, y: -6 }}
            animate={{ opacity: 1, y: 0 }}
            className="mb-4 rounded-2xl bg-white"
            style={{ border: "1px solid #e2e8f0", boxShadow: "0 12px 28px rgba(15,23,42,0.08)" }}
          >
            <div className="flex items-center justify-between px-4 py-3" style={{ borderBottom: "1px solid #f1f5f9" }}>
              <div>
                <p style={{ color: "#0f172a", fontSize: 14, fontWeight: 800 }}>Actividad</p>
                <p style={{ color: "#64748b", fontSize: 12, fontWeight: 600 }}>
                  Solicitudes y avisos de Servify
                </p>
              </div>
              <div className="flex items-center gap-2">
                {adminNotifications.some((notification) => !notification.leida) ? (
                  <button
                    type="button"
                    onClick={markAdminNotificationsRead}
                    className="rounded-xl px-3 py-2"
                    style={{ background: "#eff6ff", color: "#2563eb", fontSize: 11, fontWeight: 900 }}
                  >
                    Marcar leidas
                  </button>
                ) : null}
                <button
                  type="button"
                  onClick={() => setActivityOpen(false)}
                  className="flex items-center justify-center rounded-xl"
                  style={{ width: 32, height: 32, background: "#f8fafc" }}
                >
                  <X size={15} color="#64748b" strokeWidth={2} />
                </button>
              </div>
            </div>

            <div className="flex flex-col gap-2 px-4 py-3">
              {activityLoading ? (
                <ActivityRow
                  tone="neutral"
                  title="Actualizando actividad"
                  detail="Consultando solicitudes y publicaciones del backend."
                />
              ) : (
                activity.items.map((item, index) => (
                  <ActivityRow key={`${item.title}-${index}`} tone={item.tone} title={item.title} detail={item.detail} />
                ))
              )}
            </div>
          </motion.div>
        ) : null}

        {/* Search */}
        <div className="servify-home-search flex items-center gap-3 px-4 py-3">
          <input
            value={providerSearch}
            onChange={(e) => setProviderSearch(e.target.value)}
            placeholder="Buscar prestadores..."
            className="flex-1 bg-transparent outline-none"
            aria-label="Buscar prestadores por nombre de usuario"
          />
          <Search size={19} strokeWidth={2} />
        </div>
      </div>

      {/* Scrollable content */}
      <div className="flex-1 overflow-y-auto px-5 pt-4 pb-6 flex flex-col gap-5" {...pullHandlers}>
        <PullToRefreshIndicator pullDistance={pullDistance} refreshing={refreshing} />
        <ProviderSearchSection
          search={providerSearch}
          providers={providers}
          loading={providersLoading}
          error={providersError}
          onProviderPress={onProviderPress}
        />

        {!providerSearch.trim() && (
          <QuickCategoryStrip
            categories={categories.filter((category) => popularCategories.has(category.label))}
            showAllCategories={showAllCategories}
            onCategoryPress={onCategoryPress}
            onToggleAll={() => setShowAllCategories((current) => !current)}
          />
        )}

        {!providerSearch.trim() && showAllCategories && (
          <div ref={categoriesPanelRef}>
            <AllCategoriesPanel categories={visibleCategories} onCategoryPress={onCategoryPress} />
          </div>
        )}

        {/* If provider, try to load provider-relevant requests from backend */}
        {!providerSearch.trim() && (user?.role === "provider" || user?.role === "both") && remoteRequests && (
          <div className="mb-3">
            <p style={{ fontSize: 13, color: "#64748b", marginBottom: 6 }}>Recomendados para vos</p>
            {activityError ? (
              <p className="rounded-2xl px-4 py-3 mb-2" style={{ background: "#fef2f2", color: "#b91c1c", fontSize: 13, fontWeight: 700 }}>
                {activityError}
              </p>
            ) : null}
            <div className="flex flex-col gap-2.5">
              {compatibleRequests.length === 0 && <p style={{ color: "#64748b" }}>No hay solicitudes compatibles por ahora</p>}
              {compatibleRequests.map((r, i) => (
                <motion.div
                  key={r.id}
                  initial={{ opacity: 0, y: 8 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ duration: 0.3, delay: i * 0.05 }}
                  className="p-4 rounded-2xl bg-white text-left transition-all"
                  style={{ border: "1px solid rgba(0,0,0,0.06)", boxShadow: "0 1px 4px rgba(0,0,0,0.04)" }}
                >
                  <div className="flex items-start gap-4">
                    <div className="flex-1">
                      <p style={{ fontWeight: 700, fontSize: 14, color: "#0f172a" }}>{(r.descripcionNecesidad || "Solicitud de servicio").split('.')[0]}</p>
                      <p style={{ fontSize: 12, color: "#94a3b8", marginTop: 6 }}>{r.descripcionNecesidad}</p>
                      <p style={{ fontSize: 11, color: "#64748b", marginTop: 6, fontWeight: 700 }}>
                        {(r.ubicacion?.localidad || r.ubicacion?.ciudad || "CABA")} - {statusLabel(r)}
                      </p>
                    </div>
                    <ChevronRight size={18} color="#cbd5e1" strokeWidth={2} />
                  </div>

                  {canRespondReceived(r) ? (
                    <div className="mt-3 pt-3" style={{ borderTop: "1px solid #f1f5f9" }}>
                      <div className="grid grid-cols-3 gap-2">
                        <button
                          type="button"
                          disabled={Boolean(actionLoading)}
                          onClick={() => handleDistributionResponse(r, "ACEPTAR")}
                          className="flex items-center justify-center gap-1 py-2.5 rounded-xl transition-all active:scale-95"
                          style={{ background: "#f0fdf4", color: "#16a34a", border: "1.5px solid #bbf7d0", fontSize: 12, fontWeight: 800 }}
                        >
                          <CheckCircle size={14} strokeWidth={2} />
                          Aceptar
                        </button>
                        <button
                          type="button"
                          disabled={Boolean(actionLoading)}
                          onClick={() => handleDistributionResponse(r, "RECHAZAR")}
                          className="flex items-center justify-center gap-1 py-2.5 rounded-xl transition-all active:scale-95"
                          style={{ background: "#fef2f2", color: "#dc2626", border: "1.5px solid #fecaca", fontSize: 12, fontWeight: 800 }}
                        >
                          <XCircle size={14} strokeWidth={2} />
                          Rechazar
                        </button>
                        <button
                          type="button"
                          disabled={Boolean(actionLoading)}
                          onClick={() => {
                            setCounterOfferFor(counterOfferFor === r.distribucionSolicitudId ? null : r.distribucionSolicitudId ?? null);
                            setCounterPrice("");
                            setCounterMessage("");
                          }}
                          className="flex items-center justify-center gap-1 py-2.5 rounded-xl transition-all active:scale-95"
                          style={{ background: "#fffbeb", color: "#d97706", border: "1.5px solid #fde68a", fontSize: 12, fontWeight: 800 }}
                        >
                          <MessageSquare size={14} strokeWidth={2} />
                          Ofertar
                        </button>
                      </div>
                      {counterOfferFor === r.distribucionSolicitudId ? (
                        <div className="mt-3 flex flex-col gap-2 rounded-2xl p-3" style={{ background: "#f8fafc", border: "1px solid #e2e8f0" }}>
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
                            onClick={() => handleCounterOffer(r)}
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
              ))}
            </div>
          </div>
        )}
        {!providerSearch.trim() && (
          <PopularServicesSection
            services={popularServices}
            onCategoryPress={onCategoryPress}
          />
        )}

        {!providerSearch.trim() && (
          <ProviderPromoCard onPublish={onPublishService ?? onCreateRequest} />
        )}
      </div>
    </div>
  );
}

function QuickCategoryStrip({
  categories,
  showAllCategories,
  onCategoryPress,
  onToggleAll,
}: {
  categories: CategoryItem[];
  showAllCategories: boolean;
  onCategoryPress: (category: string) => void;
  onToggleAll: () => void;
}) {
  return (
    <section className="servify-home-section">
      <div className="servify-quick-categories">
        {categories.slice(0, 3).map((category, index) => {
          const Icon = category.icon;
          return (
            <motion.button
              key={category.id}
              initial={{ opacity: 0, y: 8 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.28, delay: index * 0.04 }}
              type="button"
              onClick={() => onCategoryPress(category.label)}
              className="servify-quick-category transition-all active:scale-95"
            >
              <span
                className="servify-category-symbol servify-quick-category-symbol"
                style={{ color: category.color, background: category.bg, borderColor: `${category.color}28` }}
              >
                <Icon size={24} strokeWidth={2.15} />
              </span>
              <span>{category.shortLabel}</span>
            </motion.button>
          );
        })}

        <motion.button
          initial={{ opacity: 0, y: 8 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.28, delay: 0.12 }}
          type="button"
          onClick={onToggleAll}
          className="servify-quick-category transition-all active:scale-95"
          aria-expanded={showAllCategories}
        >
          <span className="servify-category-symbol servify-quick-category-symbol servify-quick-category-more">
            <MoreHorizontal size={23} strokeWidth={2.3} />
          </span>
          <span>Mas</span>
        </motion.button>
      </div>
    </section>
  );
}

function PopularServicesSection({
  services,
  onCategoryPress,
}: {
  services: ServiceHighlightItem[];
  onCategoryPress: (category: string) => void;
}) {
  const usingDefaults = services.every((service) => service.isDefault);

  return (
    <section className="servify-home-section">
      <div className="servify-section-heading">
        <h2>Servicios populares</h2>
        <span>{usingDefaults ? "Sugeridos" : "Por solicitudes"}</span>
      </div>

      {services.length === 0 ? (
        <p className="servify-popular-empty">
          Aun estamos preparando recomendaciones.
        </p>
      ) : (
      <div className="servify-popular-services">
        {services.map((service, index) => (
            <motion.button
              key={service.title}
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.3, delay: index * 0.05 }}
              type="button"
              onClick={() => onCategoryPress(service.category)}
              className="servify-service-card transition-all active:scale-[0.98]"
            >
              <span className={`servify-service-visual servify-category-photo-${service.imageKey}`}>
                <span className="servify-category-photo-shade" aria-hidden="true" />
              </span>
              <span className="servify-service-title">{service.title}</span>
              <span className="servify-service-price">
                {service.isDefault
                  ? "Categoria sugerida"
                  : `${service.requestCount} solicitud${service.requestCount === 1 ? "" : "es"}`}
              </span>
              <span className="servify-service-rating">
                <Star size={12} fill="currentColor" strokeWidth={0} />
                {service.isDefault ? "Sugerido" : "Popular"}
              </span>
            </motion.button>
        ))}
      </div>
      )}
    </section>
  );
}

function ProviderPromoCard({ onPublish }: { onPublish: () => void }) {
  return (
    <section className="servify-provider-promo">
      <div className="servify-provider-promo-icon">
        <BriefcaseBusiness size={22} strokeWidth={2} />
      </div>
      <div className="min-w-0 flex-1">
        <h2>¿Sos prestador de servicios?</h2>
        <p>Ofrecé tus habilidades y generá ingresos de forma flexible.</p>
      </div>
      <button type="button" onClick={onPublish} className="servify-provider-promo-button transition-all active:scale-95">
        Publicar
      </button>
    </section>
  );
}

function AllCategoriesPanel({
  categories,
  onCategoryPress,
}: {
  categories: CategoryItem[];
  onCategoryPress: (category: string) => void;
}) {
  return (
    <section className="servify-all-categories">
      <div className="servify-section-heading">
        <h2>Todas las categorias</h2>
        <span>{categories.length} disponibles</span>
      </div>
      <div className="servify-all-categories-grid">
        {categories.map((category, index) => {
          const Icon = category.icon;
          return (
            <motion.button
              key={category.id}
              initial={{ opacity: 0, y: 8 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.24, delay: Math.min(index * 0.025, 0.16) }}
              type="button"
              onClick={() => onCategoryPress(category.label)}
              className="servify-all-category-item transition-all active:scale-[0.98]"
            >
              <span
                className="servify-category-symbol servify-all-category-symbol"
                style={{ color: category.color, background: category.bg, borderColor: `${category.color}28` }}
              >
                <Icon size={22} strokeWidth={2.15} />
              </span>
              <strong>{category.shortLabel}</strong>
            </motion.button>
          );
        })}
      </div>
    </section>
  );
}

function buildPopularServices(requests: ApiRequest[], activeCategories: ApiCategory[]): ServiceHighlightItem[] {
  const categoryNameById = new Map(activeCategories.map((category) => [category.id, category.nombre]));
  const fallbackCategory = categories.find((category) => category.label === "Otro") ?? categories[0];
  const counters = new Map<string, { category: CategoryItem; count: number }>();

  requests.forEach((request) => {
    const rawCategoryName = request.categoriaServicioId ? categoryNameById.get(request.categoriaServicioId) : undefined;
    const category = findCategoryByName(rawCategoryName) ?? fallbackCategory;
    const current = counters.get(category.label);
    counters.set(category.label, {
      category,
      count: (current?.count ?? 0) + 1,
    });
  });

  const rankedServices = Array.from(counters.values())
    .sort((a, b) => b.count - a.count || a.category.label.localeCompare(b.category.label))
    .map(({ category, count }) => ({
      title: category.label,
      category: category.label,
      requestCount: count,
      imageKey: category.imageKey,
      color: category.color,
    }));

  const totalRequests = rankedServices.reduce((total, service) => total + service.requestCount, 0);
  if (totalRequests < minimumRequestsForPopular) {
    return buildDefaultPopularServices();
  }

  const topServices = rankedServices.slice(0, 3);
  if (topServices.length >= 3) {
    return topServices;
  }

  const usedCategories = new Set(topServices.map((service) => service.category));
  return [
    ...topServices,
    ...buildDefaultPopularServices(usedCategories, 3 - topServices.length),
  ];
}

function buildDefaultPopularServices(excludedCategories = new Set<string>(), limit = 3): ServiceHighlightItem[] {
  const seen = new Set<string>();
  const defaultCategories = [
    ...defaultPopularCategoryLabels.map((label) => findCategoryByName(label)),
    ...categories,
  ].filter((category): category is CategoryItem => {
    if (!category || seen.has(category.label) || excludedCategories.has(category.label)) {
      return false;
    }
    seen.add(category.label);
    return true;
  });

  return defaultCategories.slice(0, limit).map((category) => ({
    title: category.label,
    category: category.label,
    requestCount: 0,
    imageKey: category.imageKey,
    color: category.color,
    isDefault: true,
  }));
}

function findCategoryByName(name?: string): CategoryItem | undefined {
  const key = normalizeCategoryKey(name);
  if (!key) return undefined;
  return categories.find((category) => normalizeCategoryKey(category.label) === key);
}

function normalizeCategoryKey(value?: string): string {
  return (value ?? "")
    .replace(/tÃ©cnico/gi, "tecnico")
    .replace(/diseÃ±o/gi, "diseno")
    .replace(/fotografÃ­a/gi, "fotografia")
    .replace(/Ã¡/g, "a")
    .replace(/Ã©/g, "e")
    .replace(/Ã­/g, "i")
    .replace(/Ã³/g, "o")
    .replace(/Ãº/g, "u")
    .replace(/Ã±/g, "n")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/[^a-zA-Z0-9]/g, "")
    .toLowerCase();
}

type ActivityTone = "urgent" | "info" | "success" | "neutral";

interface ActivityItem {
  title: string;
  detail: string;
  tone: ActivityTone;
}

function buildActivitySummary(
  receivedRequests: ApiReceivedRequest[],
  ownRequests: ApiRequest[],
  ownPublications: ApiPublication[],
  ownAssignmentStates: Record<string, Awaited<ReturnType<typeof servifyApi.getAssignmentState>> | null>,
  adminNotifications: ApiNotification[]
): { badgeCount: number; items: ActivityItem[] } {
  const unreadAdminNotifications = adminNotifications.filter((notification) => !notification.leida);
  const pendingReceived = receivedRequests.filter((request) =>
    (request.estadoDistribucion ?? request.estado ?? "").toUpperCase() === "ENVIADA"
  );
  const activeOwnRequests = ownRequests.filter((request) =>
    ["BUSCANDO_PRESTADOR", "ASIGNADA"].includes((request.estado ?? "").toUpperCase())
  );
  const ownCounterOffers = ownRequests.filter((request) =>
    (ownAssignmentStates[request.id]?.contraofertasPendientes?.length ?? 0) > 0
  );
  const pausedPublications = ownPublications.filter((publication) =>
    ["PAUSADA", "INACTIVA"].includes((publication.estado ?? "").toUpperCase())
  );
  const activePublications = ownPublications.filter((publication) =>
    (publication.estado ?? "").toUpperCase() === "ACTIVA"
  );

  const items: ActivityItem[] = [];
  unreadAdminNotifications.slice(0, 3).forEach((notification) => {
    items.push({
      title: notification.titulo,
      detail: notification.mensaje,
      tone: "urgent",
    });
  });
  if (pendingReceived.length > 0) {
    items.push({
      title: `${pendingReceived.length} solicitud${pendingReceived.length === 1 ? "" : "es"} para revisar`,
      detail: "Tenes pedidos compatibles esperando respuesta.",
      tone: "urgent",
    });
  }
  if (ownCounterOffers.length > 0) {
    items.push({
      title: `${ownCounterOffers.length} contraoferta${ownCounterOffers.length === 1 ? "" : "s"} pendiente${ownCounterOffers.length === 1 ? "" : "s"}`,
      detail: "Revisala desde Solicitudes para aceptar o rechazar.",
      tone: "urgent",
    });
  }
  if (activeOwnRequests.length > 0) {
    items.push({
      title: `${activeOwnRequests.length} solicitud${activeOwnRequests.length === 1 ? "" : "es"} activa${activeOwnRequests.length === 1 ? "" : "s"}`,
      detail: "Tus pedidos siguen buscando o ya tienen asignacion.",
      tone: "info",
    });
  }
  if (pausedPublications.length > 0) {
    items.push({
      title: `${pausedPublications.length} publicacion${pausedPublications.length === 1 ? "" : "es"} pausada${pausedPublications.length === 1 ? "" : "s"}`,
      detail: "Podrias reactivarlas desde Mis publicaciones.",
      tone: "neutral",
    });
  }
  if (items.length === 0) {
    items.push({
      title: activePublications.length > 0 ? "Todo al dia" : "Sin actividad pendiente",
      detail: activePublications.length > 0
        ? `${activePublications.length} publicacion${activePublications.length === 1 ? "" : "es"} activa${activePublications.length === 1 ? "" : "s"} disponible${activePublications.length === 1 ? "" : "s"}.`
        : "Cuando haya solicitudes o cambios relevantes van a aparecer aca.",
      tone: "success",
    });
  }

  return {
    badgeCount: pendingReceived.length + ownCounterOffers.length + unreadAdminNotifications.length,
    items,
  };
}

function canRespondReceived(request: ApiReceivedRequest): boolean {
  return Boolean(request.distribucionSolicitudId)
    && (request.estadoDistribucion ?? request.estado ?? "").toUpperCase() === "ENVIADA";
}

function isCompatibleReceived(request: ApiReceivedRequest): boolean {
  const status = (request.estadoDistribucion ?? request.estado ?? "").toUpperCase();
  return status === "ENVIADA";
}

function statusLabel(request: ApiReceivedRequest): string {
  const status = (request.estadoDistribucion ?? request.estado ?? "ENVIADA").toUpperCase();
  if (status === "ACEPTADA") return "Aceptada";
  if (status === "RECHAZADA") return "Rechazada";
  if (status === "CONTRAOFERTADA") return "Contraofertada";
  if (status === "EXPIRADA") return "Expirada";
  return "Pendiente";
}

function mapReceivedRequestForDetail(request: ApiReceivedRequest, providerId: string): ServiceRequest {
  const description = request.descripcionNecesidad ?? "Solicitud de servicio";
  const requesterName = "Solicitante";
  const locality = request.ubicacion?.localidad || request.ubicacion?.ciudad || "CABA";
  const availability = request.disponibilidadRequerida;
  return {
    id: request.id,
    viewerRole: "PRESTADOR",
    title: description.split(".")[0] || "Solicitud de servicio",
    description,
    category: "Servicio solicitado",
    location: locality,
    proposals: 1,
    price: request.precioReferencia ? `$${request.precioReferencia}` : "A convenir",
    schedule: availability
      ? `${availability.diaSemana} ${availability.horaDesde.slice(0, 5)}-${availability.horaHasta.slice(0, 5)}`
      : "Horario a coordinar",
    date: request.fechaSolicitud ? new Date(request.fechaSolicitud).toLocaleDateString("es-AR") : "Sin fecha",
    status: "in-progress",
    requesterName,
    requesterInitials: "SO",
    modal: request.modalidadServicio === "VIRTUAL" ? "Virtual" : request.modalidadServicio === "MIXTA" ? "Ambas" : "Presencial",
    locality,
    availabilityDay: availability?.diaSemana,
    availabilityFrom: availability?.horaDesde?.slice(0, 5),
    availabilityTo: availability?.horaHasta?.slice(0, 5),
    distributionId: request.distribucionSolicitudId,
    providerId,
    rawStatus: "ACEPTADA",
  };
}

function ProviderSearchSection({
  search,
  providers,
  loading,
  error,
  onProviderPress,
}: {
  search: string;
  providers: ApiPublicProvider[];
  loading: boolean;
  error: string;
  onProviderPress: (provider: ApiPublicProvider) => void;
}) {
  const hasSearch = search.trim().length > 0;

  if (!hasSearch) {
    return null;
  }

  return (
    <section>
      <div className="flex items-center justify-between mb-3">
        <div>
          <h3 style={{ fontSize: 16, fontWeight: 800, color: "#0f172a" }}>Prestadores encontrados</h3>
          <p style={{ fontSize: 12, color: "#64748b", fontWeight: 600, marginTop: 2 }}>
            Coincidencias para @{search.trim().replace(/^@/, "")}
          </p>
        </div>
        <span style={{ fontSize: 12, color: "#94a3b8", fontWeight: 700 }}>
          {providers.length} resultado{providers.length === 1 ? "" : "s"}
        </span>
      </div>

      {error ? (
        <p className="rounded-2xl px-4 py-3 mb-3" style={{ background: "#fef2f2", color: "#b91c1c", fontSize: 13, fontWeight: 700 }}>
          {error}
        </p>
      ) : null}

      {loading ? (
        <p style={{ color: "#64748b", fontSize: 13, fontWeight: 700 }}>Cargando prestadores...</p>
      ) : null}

      {!loading && !error && providers.length === 0 ? (
        <p className="rounded-2xl px-4 py-3" style={{ background: "#f8fafc", color: "#64748b", fontSize: 13, fontWeight: 700 }}>
          No encontramos prestadores con ese usuario.
        </p>
      ) : null}

      <div className="flex flex-col gap-2.5">
        {providers.slice(0, 5).map((provider) => (
          <ProviderCard key={provider.usuarioId} provider={provider} onClick={() => onProviderPress(provider)} />
        ))}
      </div>
    </section>
  );
}

function ProviderCard({ provider, onClick }: { provider: ApiPublicProvider; onClick: () => void }) {
  const profileName = formatProviderName(provider);
  const displayName = profileName || `@${provider.nombreUsuario}`;
  const activePublications = provider.publicacionesActivas?.length
    ? provider.publicacionesActivas.map((publication) => publication.titulo)
    : provider.servicios;

  return (
    <button
      type="button"
      onClick={onClick}
      className="bg-white rounded-2xl p-4 text-left w-full transition-all active:scale-[0.98]"
      style={{ border: "1px solid rgba(0,0,0,0.06)", boxShadow: "0 1px 4px rgba(0,0,0,0.04)" }}
    >
      <div className="flex items-start gap-3">
        <div
          className="flex items-center justify-center rounded-2xl shrink-0 overflow-hidden"
          style={{ width: 48, height: 48, background: "#ecfdf5" }}
        >
          {provider.fotoPerfilUrl ? (
            <img src={provider.fotoPerfilUrl} alt="" className="h-full w-full object-cover" />
          ) : (
            <UserRound size={22} color="#0f766e" strokeWidth={1.8} />
          )}
        </div>

        <div className="min-w-0 flex-1">
          <div className="flex items-start justify-between gap-2">
            <div className="min-w-0">
              <p style={{ fontSize: 14, fontWeight: 800, color: "#0f172a", lineHeight: 1.2 }}>
                {displayName}
              </p>
              <p style={{ fontSize: 12, fontWeight: 800, color: "#0f766e", marginTop: 2 }}>
                @{provider.nombreUsuario}
              </p>
            </div>
            <ChevronRight size={18} color="#cbd5e1" strokeWidth={2} />
          </div>

          <p style={{ fontSize: 12, color: "#64748b", lineHeight: 1.45, marginTop: 8, fontWeight: 700 }}>
            {provider.cantidadPublicacionesActivas} publicacion{provider.cantidadPublicacionesActivas === 1 ? "" : "es"} activa{provider.cantidadPublicacionesActivas === 1 ? "" : "s"}
          </p>
          {activePublications.length ? (
            <p style={{ fontSize: 12, color: "#475569", lineHeight: 1.45, marginTop: 3 }}>
              {activePublications.slice(0, 3).join(", ")}
            </p>
          ) : null}
        </div>
      </div>
    </button>
  );
}

function formatProviderName(provider: ApiPublicProvider): string {
  return [provider.nombre, provider.apellido].filter(Boolean).join(" ").trim();
}

function ActivityRow({ title, detail, tone }: ActivityItem) {
  const colors: Record<ActivityTone, { bg: string; fg: string }> = {
    urgent: { bg: "#fef2f2", fg: "#dc2626" },
    info: { bg: "#eff6ff", fg: "#2563eb" },
    success: { bg: "#f0fdf4", fg: "#16a34a" },
    neutral: { bg: "#f8fafc", fg: "#64748b" },
  };
  const color = colors[tone];

  return (
    <div className="flex items-start gap-3 rounded-xl px-3 py-2.5" style={{ background: color.bg }}>
      <div
        className="flex items-center justify-center rounded-full mt-0.5"
        style={{ width: 24, height: 24, background: "white", color: color.fg }}
      >
        <RefreshCcw size={13} strokeWidth={2} />
      </div>
      <div className="min-w-0">
        <p style={{ color: "#0f172a", fontSize: 13, fontWeight: 800, lineHeight: 1.25 }}>{title}</p>
        <p style={{ color: "#64748b", fontSize: 12, fontWeight: 600, lineHeight: 1.35, marginTop: 2 }}>{detail}</p>
      </div>
    </div>
  );
}
