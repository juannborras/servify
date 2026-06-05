import { useState } from "react";
import { AnimatePresence, motion } from "motion/react";
import { SplashScreen } from "./components/SplashScreen";
import { AuthScreen } from "./components/AuthScreen";
import { ExploreScreen } from "./components/ExploreScreen";
import { CategoryPublicationsScreen } from "./components/CategoryPublicationsScreen";
import { RequestsScreen, type ServiceRequest } from "./components/RequestsScreen";
import { RequestDetail, type RatingTarget } from "./components/RequestDetail";
import { PublishScreen } from "./components/PublishScreen";
import { MyPublications } from "./components/MyPublications";
import { ProfileScreen } from "./components/ProfileScreen";
import { BottomNav } from "./components/BottomNav";
import { RatingModal } from "./components/RatingModal";
import { NewRequestModal } from "./components/NewRequestModal";
import { servifyApi, type SessionUser } from "./api";

type AppScreen =
  | "splash"
  | "auth"
  | "explore"
  | "category-publications"
  | "requests"
  | "request-detail"
  | "my-services"
  | "publish"
  | "profile";

type BottomTab = "explore" | "requests" | "my-services" | "publish" | "profile";

export default function App() {
  const storedSession = servifyApi.getStoredSession();
  const [screen, setScreen] = useState<AppScreen>("splash");
  const [user, setUser] = useState<SessionUser | null>(storedSession);
  const [activeTab, setActiveTab] = useState<BottomTab>("explore");
  const [selectedRequest, setSelectedRequest] = useState<ServiceRequest | null>(null);
  const [selectedCategory, setSelectedCategory] = useState("");
  const [showRating, setShowRating] = useState(false);
  const [ratingTarget, setRatingTarget] = useState<RatingTarget | null>(null);
  const [showNewRequest, setShowNewRequest] = useState(false);

  const showNav =
    screen !== "splash" && screen !== "auth" && screen !== "request-detail";

  const handleSplashDone = () => setScreen(user ? "explore" : "auth");

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
            onCreateRequest={() => setShowNewRequest(true)}
            onCategoryPress={handleCategoryPress}
            onAcceptedRequest={handleRequestPress}
          />
        );
      case "category-publications":
        return (
          <CategoryPublicationsScreen
            categoryName={selectedCategory}
            onBack={() => {
              setScreen("explore");
              setActiveTab("explore");
            }}
          />
        );
      case "requests":
        return (
          <RequestsScreen
            userId={user?.id}
            onRequestPress={handleRequestPress}
            onNewRequest={() => setShowNewRequest(true)}
          />
        );
      case "request-detail":
        return selectedRequest ? (
          <RequestDetail
            request={selectedRequest}
            currentUser={user}
            onBack={() => {
              setScreen("requests");
              setActiveTab("requests");
            }}
            onRate={handleRate}
          />
        ) : null;
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
          <ProfileScreen
            user={user}
            onLogout={handleLogout}
            onLogin={() => setScreen("auth")}
            onUserUpdated={handleProfileUpdated}
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
                onSubmit={async (rating) => {
                  if (!ratingTarget) return;
                  await servifyApi.rateService({
                    solicitudId: ratingTarget.solicitudId,
                    asignacionServicioId: ratingTarget.asignacionServicioId,
                    solicitanteId: ratingTarget.solicitanteId,
                    prestadorId: ratingTarget.prestadorId,
                    calificadorId: ratingTarget.calificadorId,
                    rolCalificador: ratingTarget.rolCalificador,
                    puntaje: rating,
                  });
                  ratingTarget.onSubmitted?.(rating);
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
                onClose={() => setShowNewRequest(false)}
                onCreated={() => {
                  setActiveTab("requests");
                  setScreen("requests");
                }}
              />
            </div>
          )}
        </AnimatePresence>
      </div>
    </div>
  );
}
