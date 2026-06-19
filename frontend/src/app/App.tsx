import { useCallback, useEffect, useState } from "react";
import { AnimatePresence, motion } from "motion/react";
import { SplashScreen } from "./components/SplashScreen";
import { AuthScreen } from "./components/AuthScreen";
import { ExploreScreen } from "./components/ExploreScreen";
import { CategoryPublicationsScreen } from "./components/CategoryPublicationsScreen";
import { RequestsScreen, type ServiceRequest } from "./components/RequestsScreen";
import { RequestDetail, type RatingTarget } from "./components/RequestDetail";
import { PublishScreen } from "./components/PublishScreen";
import { MyPublications } from "./components/MyPublications";
import { AdminPanelScreen } from "./components/AdminPanelScreen";
import { PublicProfileScreen } from "./components/PublicProfileScreen";
import { NotificationsScreen } from "./components/NotificationsScreen";
import { SettingsDrawer } from "./components/SettingsDrawer";
import { BottomNav } from "./components/BottomNav";
import { RatingModal } from "./components/RatingModal";
import { NewRequestModal, type NewRequestInitialValues } from "./components/NewRequestModal";
import { servifyApi, type ApiNotification, type ApiPublicProvider, type SessionUser } from "./api";

type AppScreen =
  | "splash"
  | "auth"
  | "explore"
  | "category-publications"
  | "requests"
  | "request-detail"
  | "provider-profile"
  | "notifications"
  | "admin"
  | "my-services"
  | "publish"
  | "profile";

type BottomTab = "explore" | "requests" | "my-services" | "publish" | "profile";
type ProviderBackScreen = "explore" | "category-publications" | "admin" | "requests";

export default function App() {
  const storedSession = servifyApi.getStoredSession();
  const [screen, setScreen] = useState<AppScreen>("splash");
  const [user, setUser] = useState<SessionUser | null>(storedSession);
  const [activeTab, setActiveTab] = useState<BottomTab>("explore");
  const [selectedRequest, setSelectedRequest] = useState<ServiceRequest | null>(null);
  const [selectedProvider, setSelectedProvider] = useState<ApiPublicProvider | null>(null);
  const [providerBackScreen, setProviderBackScreen] = useState<ProviderBackScreen>("explore");
  const [selectedCategory, setSelectedCategory] = useState("");
  const [showRating, setShowRating] = useState(false);
  const [ratingTarget, setRatingTarget] = useState<RatingTarget | null>(null);
  const [showNewRequest, setShowNewRequest] = useState(false);
  const [showSettings, setShowSettings] = useState(false);
  const [newRequestInitialValues, setNewRequestInitialValues] = useState<NewRequestInitialValues | null>(null);
  const [unreadNotificationCount, setUnreadNotificationCount] = useState(0);
  const [pendingRequestId, setPendingRequestId] = useState<string | null>(null);
  const [requestsRefreshKey, setRequestsRefreshKey] = useState(0);

  const showNav =
    screen !== "splash" && screen !== "auth";

  const handleSplashDone = () => setScreen(user ? "explore" : "auth");

  const loadUnreadNotificationCount = useCallback(async () => {
    if (!user?.id) {
      setUnreadNotificationCount(0);
      return;
    }
    const notifications = await servifyApi.listNotifications(user.id).catch(() => []);
    setUnreadNotificationCount(notifications.filter((notification) => !notification.leida).length);
  }, [user?.id]);

  useEffect(() => {
    void loadUnreadNotificationCount();
  }, [loadUnreadNotificationCount, screen]);

  const handleAuth = (u: SessionUser) => {
    setUser(u);
    setScreen("explore");
    setActiveTab("explore");
  };

  const handleLogout = () => {
    servifyApi.clearSession();
    setUser(null);
    setScreen("auth");
  };

  const handleProfileUpdated = (patch: Partial<SessionUser>) => {
    setUser((prev) => {
      if (!prev) return prev;
      const next = { ...prev, ...patch };
      servifyApi.storeSession(next);
      return next;
    });
  };

  const handleTabChange = (tab: BottomTab) => {
    setActiveTab(tab);
    if (tab === "explore") setScreen("explore");
    else if (tab === "requests") setScreen("requests");
    else if (tab === "my-services") setScreen("my-services");
    else if (tab === "publish") setScreen("publish");
    else if (tab === "profile") setScreen("profile");
  };

  const handleRequestPress = (req: ServiceRequest) => {
    setSelectedRequest(req);
    setScreen("request-detail");
  };

  const handleOpenNotifications = () => {
    setShowSettings(false);
    setScreen("notifications");
  };

  const handleProviderPress = (provider: ApiPublicProvider, backScreen: ProviderBackScreen = "explore") => {
    setSelectedProvider(provider);
    setProviderBackScreen(backScreen);
    setScreen("provider-profile");
  };

  const handleNotificationReference = (notification: ApiNotification) => {
    const referenceType = (notification.referenciaTipo ?? "").toUpperCase();
    setPendingRequestId(null);
    if (referenceType === "PUBLICACION") {
      setActiveTab("my-services");
      setScreen("my-services");
      return;
    }
    if (referenceType === "USUARIO") {
      setActiveTab("profile");
      setScreen("profile");
      return;
    }
    if (["SOLICITUD", "DISTRIBUCION", "CONTRAOFERTA", "ASIGNACION"].includes(referenceType)) {
      setPendingRequestId(referenceType === "SOLICITUD" ? notification.referenciaId ?? null : null);
      setActiveTab("requests");
      setScreen("requests");
      return;
    }
    setActiveTab("explore");
    setScreen("explore");
  };

  const handleOpenNewRequest = (initialValues?: NewRequestInitialValues) => {
    setNewRequestInitialValues(initialValues ?? null);
    setShowNewRequest(true);
  };

  const handleRate = (target: RatingTarget) => {
    setRatingTarget(target);
    setShowRating(true);
  };

  const handlePublished = () => {
    setActiveTab("my-services");
    setScreen("my-services");
  };

  const handleCategoryPress = (categoryName: string) => {
    setSelectedCategory(categoryName);
    setScreen("category-publications");
  };

  const renderScreen = () => {
    switch (screen) {
      case "splash":
        return <SplashScreen onDone={handleSplashDone} />;
      case "auth":
        return <AuthScreen onAuth={handleAuth} />;
      case "explore":
        return (
          <ExploreScreen
            user={user}
            userName={user?.name ?? "Usuario"}
            notificationCount={unreadNotificationCount}
            onOpenNotifications={handleOpenNotifications}
            onCreateRequest={() => handleOpenNewRequest()}
            onPublishService={() => {
              setScreen("publish");
              setActiveTab("publish");
            }}
            onCategoryPress={handleCategoryPress}
            onAcceptedRequest={handleRequestPress}
            onProviderPress={(provider) => handleProviderPress(provider, "explore")}
          />
        );
      case "category-publications":
        return (
          <CategoryPublicationsScreen
            categoryName={selectedCategory}
            currentUserId={user?.id}
            onBack={() => {
              setScreen("explore");
              setActiveTab("explore");
            }}
            onRequestPublication={(initialValues) => handleOpenNewRequest(initialValues)}
            onProviderPress={(provider) => handleProviderPress(provider, "category-publications")}
          />
        );
      case "requests":
        return (
          <RequestsScreen
            userId={user?.id}
            onRequestPress={handleRequestPress}
            onNewRequest={() => handleOpenNewRequest()}
            onRepeatRequest={(request) => handleOpenNewRequest(toNewRequestInitialValues(request))}
            initialRequestId={pendingRequestId}
            onInitialRequestOpened={() => setPendingRequestId(null)}
            refreshKey={requestsRefreshKey}
          />
        );
      case "request-detail":
        return selectedRequest ? (
          <RequestDetail
            request={selectedRequest}
            currentUser={user}
            onProviderPress={(provider) => handleProviderPress(provider, "requests")}
            onBack={() => {
              setScreen("requests");
              setActiveTab("requests");
            }}
            onRate={handleRate}
          />
        ) : null;
      case "provider-profile":
        return (
          <PublicProfileScreen
            provider={selectedProvider}
            onBack={() => {
              setScreen(providerBackScreen);
              setActiveTab(providerBackScreen === "admin" ? "profile" : providerBackScreen === "requests" ? "requests" : "explore");
            }}
          />
        );
      case "notifications":
        return (
          <NotificationsScreen
            userId={user?.id}
            onBack={() => setScreen(screenForTab(activeTab))}
            onOpenReference={handleNotificationReference}
            onUnreadCountChange={setUnreadNotificationCount}
          />
        );
      case "admin":
        return (
          <AdminPanelScreen
            onProviderPress={(provider) => handleProviderPress(provider, "admin")}
            onBack={() => {
              setScreen("profile");
              setActiveTab("profile");
            }}
          />
        );
      case "my-services":
        return (
          <MyPublications
            userId={user?.id}
            onNew={() => {
              setScreen("publish");
              setActiveTab("publish");
            }}
          />
        );
      case "publish":
        return <PublishScreen userId={user?.id} onPublished={handlePublished} />;
      case "profile":
        return (
          <PublicProfileScreen
            user={user}
            ownProfile
            onOpenSettings={() => setShowSettings(true)}
          />
        );
      default:
        return null;
    }
  };

  return (
    <div className="flex min-h-screen md:items-center md:justify-center" style={{ background: "#f8fafc" }}>
      {/* Full-screen on phones, framed preview only on desktop */}
      <div
        className="relative flex flex-col overflow-hidden w-full min-h-screen md:w-[390px] md:h-[844px] md:min-h-0 md:rounded-[48px] md:[box-shadow:0_0_0_10px_#1e293b,_0_40px_80px_rgba(0,0,0,0.6),_inset_0_0_0_1px_rgba(255,255,255,0.05)]"
        style={{ background: "#f8fafc" }}
      >
        <div
          className="hidden md:block absolute top-0 left-1/2 z-50"
          style={{
            transform: "translateX(-50%)",
            width: 126,
            height: 34,
            background: "#0f172a",
            borderRadius: "0 0 20px 20px",
          }}
        />

        {/* Screen content */}
        <div className="flex flex-col flex-1 overflow-hidden" style={{ paddingTop: 0 }}>
          <AnimatePresence mode="wait">
            <motion.div
              key={screen}
              initial={{ opacity: 0, y: screen === "splash" ? 0 : 16 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -8 }}
              transition={{ duration: 0.22, ease: [0.16, 1, 0.3, 1] }}
              className="flex-1 overflow-hidden flex flex-col"
            >
              {renderScreen()}
            </motion.div>
          </AnimatePresence>
        </div>

        {/* Bottom nav */}
        <AnimatePresence>
          {showNav && (
            <motion.div
              initial={{ y: 80, opacity: 0 }}
              animate={{ y: 0, opacity: 1 }}
              exit={{ y: 80, opacity: 0 }}
              transition={{ duration: 0.3 }}
              className="sticky bottom-0 z-40"
              style={{ flexShrink: 0 }}
            >
              <BottomNav activeTab={activeTab} onTabChange={handleTabChange} />
            </motion.div>
          )}
        </AnimatePresence>

        {/* Modals */}
        <AnimatePresence>
          {showRating && (
            <div className="absolute inset-0 z-50">
              <RatingModal
                providerName={ratingTarget?.name ?? "Usuario"}
                onClose={() => setShowRating(false)}
                onSubmit={async (rating, comment) => {
                  if (!ratingTarget) return;
                  await servifyApi.rateService({
                    solicitudId: ratingTarget.solicitudId,
                    asignacionServicioId: ratingTarget.asignacionServicioId,
                    solicitanteId: ratingTarget.solicitanteId,
                    prestadorId: ratingTarget.prestadorId,
                    calificadorId: ratingTarget.calificadorId,
                    rolCalificador: ratingTarget.rolCalificador,
                    puntaje: rating,
                    comentario: comment,
                  });
                  ratingTarget.onSubmitted?.(rating, comment);
                }}
              />
            </div>
          )}
        </AnimatePresence>

        <AnimatePresence>
          {showNewRequest && (
            <div className="absolute inset-0 z-50">
              <NewRequestModal
                userId={user?.id}
                initialValues={newRequestInitialValues ?? undefined}
                onClose={() => setShowNewRequest(false)}
                onCreated={() => {
                  setRequestsRefreshKey((current) => current + 1);
                  setActiveTab("requests");
                  setScreen("requests");
                  setNewRequestInitialValues(null);
                }}
              />
            </div>
          )}
        </AnimatePresence>

        <SettingsDrawer
          open={showSettings}
          user={user}
          onClose={() => setShowSettings(false)}
          onLogout={() => {
            setShowSettings(false);
            handleLogout();
          }}
          onOpenAdmin={() => {
            setShowSettings(false);
            setScreen("admin");
            setActiveTab("profile");
          }}
          onOpenNotifications={handleOpenNotifications}
          unreadNotificationCount={unreadNotificationCount}
          onUserUpdated={handleProfileUpdated}
        />
      </div>
    </div>
  );
}

function screenForTab(tab: BottomTab): AppScreen {
  if (tab === "requests") return "requests";
  if (tab === "my-services") return "my-services";
  if (tab === "publish") return "publish";
  if (tab === "profile") return "profile";
  return "explore";
}

function toNewRequestInitialValues(request: ServiceRequest): NewRequestInitialValues {
  const title = request.title || "Solicitud de servicio";
  const description = request.description.startsWith(`${title}.`)
    ? request.description.slice(title.length + 1).trim()
    : request.description;

  return {
    title,
    description,
    category: normalizeCategoryName(request.category),
    modality: request.modal,
    location: request.locality ?? request.location,
    price: request.price === "A convenir" ? "" : request.price,
    availabilityDay: request.availabilityDay,
    availabilityFrom: request.availabilityFrom,
    availabilityTo: request.availabilityTo,
  };
}

function normalizeCategoryName(category?: string): string | undefined {
  const raw = category?.trim();
  if (!raw || raw.toLowerCase().startsWith("sin categor")) return undefined;

  const key = raw
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

  if (key === "soportetecnico") return "Soporte tecnico";
  if (key === "diseno") return "Diseno";
  if (key === "fotografia") return "Fotografia";
  return raw;
}
