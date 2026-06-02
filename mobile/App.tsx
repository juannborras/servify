import React, { useCallback, useEffect, useMemo, useState } from "react";
import {
  ActivityIndicator,
  Alert,
  Image,
  KeyboardAvoidingView,
  Modal,
  Platform,
  Pressable,
  RefreshControl,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
} from "react-native";
import { SafeAreaProvider, SafeAreaView } from "react-native-safe-area-context";
import { StatusBar } from "expo-status-bar";
import * as ImagePicker from "expo-image-picker";
import {
  AlignLeft,
  Bell,
  Briefcase,
  Calendar,
  Camera,
  CheckCircle,
  ChevronLeft,
  ChevronRight,
  Clock,
  DollarSign,
  Edit3,
  Eye,
  EyeOff,
  FileText,
  Home,
  ListChecks,
  LogOut,
  Mail,
  MapPin,
  Pause,
  Plus,
  Search,
  Send,
  Settings,
  Star,
  Trash2,
  User,
  Wrench,
  X,
} from "lucide-react-native";
import {
  ACCOUNT_ROLES,
  LOCATION_OPTIONS,
  TIME_OPTIONS,
  WEEK_DAYS,
  formatMoney,
  fromApiModality,
  servifyApi,
  storage,
  type ApiPublication,
  type ApiRatingSummary,
  type ApiReceivedRequest,
  type ApiRequest,
  type RoleType,
  type SessionUser,
} from "./src/api";

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
type RequestTab = "my-requests" | "my-proposals" | "explore";

interface ServiceRequest {
  id: string;
  viewerRole?: "SOLICITANTE" | "PRESTADOR" | null;
  title: string;
  description: string;
  category: string;
  location: string;
  proposals: number;
  price: string;
  schedule: string;
  date: string;
  status: "open" | "completed" | "cancelled" | "in-progress";
  requesterName: string;
  requesterInitials: string;
  modal: "Presencial" | "Virtual" | "Ambas";
}

const categories = [
  { label: "Oficios", icon: "W", color: "#0891b2", bg: "#ecfeff" },
  { label: "Clases particulares", icon: "C", color: "#7c3aed", bg: "#f5f3ff" },
  { label: "Soporte tecnico", icon: "S", color: "#2563eb", bg: "#eff6ff" },
  { label: "Limpieza", icon: "L", color: "#0f766e", bg: "#ecfdf5" },
  { label: "Diseno", icon: "D", color: "#db2777", bg: "#fdf2f8" },
  { label: "Reparaciones", icon: "R", color: "#d97706", bg: "#fffbeb" },
  { label: "Fotografia", icon: "F", color: "#16a34a", bg: "#f0fdf4" },
  { label: "Salud y bienestar", icon: "B", color: "#059669", bg: "#ecfdf5" },
  { label: "Otro", icon: "O", color: "#7c3aed", bg: "#f5f3ff" },
];

const modalities = ["Presencial", "Virtual", "Ambas"];
const PUBLISH_DRAFT_PREFIX = "servify.publish-draft.";

export default function App() {
  const [screen, setScreen] = useState<AppScreen>("splash");
  const [user, setUser] = useState<SessionUser | null>(null);
  const [booting, setBooting] = useState(true);
  const [activeTab, setActiveTab] = useState<BottomTab>("explore");
  const [selectedRequest, setSelectedRequest] = useState<ServiceRequest | null>(null);
  const [selectedCategory, setSelectedCategory] = useState("");
  const [showRating, setShowRating] = useState(false);
  const [ratingTarget, setRatingTarget] = useState("");
  const [showNewRequest, setShowNewRequest] = useState(false);

  useEffect(() => {
    let mounted = true;
    servifyApi.getStoredSession().then((stored) => {
      if (!mounted) return;
      setUser(stored);
      setBooting(false);
    });
    return () => {
      mounted = false;
    };
  }, []);

  const handleSplashDone = () => setScreen(user ? "explore" : "auth");

  const handleAuth = (nextUser: SessionUser) => {
    setUser(nextUser);
    setScreen("explore");
    setActiveTab("explore");
  };

  const handleLogout = async () => {
    await servifyApi.clearSession();
    setUser(null);
    setScreen("auth");
  };

  const handleProfileUpdated = async (patch: Partial<SessionUser>) => {
    if (!user) return;
    const next = { ...user, ...patch };
    setUser(next);
    await servifyApi.storeSession(next);
  };

  const handleTabChange = (tab: BottomTab) => {
    setActiveTab(tab);
    setScreen(tab === "explore" ? "explore" : tab === "requests" ? "requests" : tab === "my-services" ? "my-services" : tab);
  };

  const body = (() => {
    if (screen === "splash") return <SplashScreen booting={booting} onDone={handleSplashDone} />;
    if (screen === "auth") return <AuthScreen onAuth={handleAuth} />;
    if (screen === "explore") {
      return (
        <ExploreScreen
          user={user}
          userName={user?.name ?? "Usuario"}
          onCreateRequest={() => setShowNewRequest(true)}
          onCategoryPress={(category) => {
            setSelectedCategory(category);
            setScreen("category-publications");
          }}
        />
      );
    }
    if (screen === "category-publications") {
      return (
        <CategoryPublicationsScreen
          categoryName={selectedCategory}
          onBack={() => {
            setScreen("explore");
            setActiveTab("explore");
          }}
        />
      );
    }
    if (screen === "requests") {
      return (
        <RequestsScreen
          userId={user?.id}
          onNewRequest={() => setShowNewRequest(true)}
          onRequestPress={(req) => {
            setSelectedRequest(req);
            setScreen("request-detail");
          }}
        />
      );
    }
    if (screen === "request-detail" && selectedRequest) {
      return (
        <RequestDetail
          request={selectedRequest}
          onBack={() => {
            setScreen("requests");
            setActiveTab("requests");
          }}
          onRate={(name) => {
            setRatingTarget(name);
            setShowRating(true);
          }}
        />
      );
    }
    if (screen === "my-services") {
      return (
        <MyPublications
          userId={user?.id}
          onNew={() => {
            setScreen("publish");
            setActiveTab("publish");
          }}
        />
      );
    }
    if (screen === "publish") {
      return (
        <PublishScreen
          userId={user?.id}
          onPublished={() => {
            setActiveTab("my-services");
            setScreen("my-services");
          }}
        />
      );
    }
    return (
      <ProfileScreen
        user={user}
        onLogout={handleLogout}
        onLogin={() => setScreen("auth")}
        onUserUpdated={handleProfileUpdated}
      />
    );
  })();

  const showNav = !["splash", "auth", "request-detail"].includes(screen);

  return (
    <SafeAreaProvider>
      <StatusBar style="dark" />
      <SafeAreaView style={styles.appShell} edges={["top", "left", "right"]}>
        <View style={styles.appBody}>{body}</View>
        {showNav ? <BottomNav activeTab={activeTab} onTabChange={handleTabChange} /> : null}
        <RatingModal visible={showRating} providerName={ratingTarget} onClose={() => setShowRating(false)} />
        <NewRequestModal
          visible={showNewRequest}
          userId={user?.id}
          onClose={() => setShowNewRequest(false)}
          onCreated={() => {
            setShowNewRequest(false);
            setActiveTab("requests");
            setScreen("requests");
          }}
        />
      </SafeAreaView>
    </SafeAreaProvider>
  );
}

function SplashScreen({ booting, onDone }: { booting: boolean; onDone: () => void }) {
  useEffect(() => {
    if (booting) return;
    const timeout = setTimeout(onDone, 900);
    return () => clearTimeout(timeout);
  }, [booting, onDone]);

  return (
    <View style={styles.splash}>
      <Image source={require("./assets/icon.png")} style={styles.splashMark} resizeMode="contain" />
      <Text style={styles.splashTitle}>Servify</Text>
      <Text style={styles.splashSubtitle}>Servicios cerca tuyo</Text>
      <ActivityIndicator color="#2563eb" style={{ marginTop: 28 }} />
    </View>
  );
}

function AuthScreen({ onAuth }: { onAuth: (user: SessionUser) => void }) {
  const [tab, setTab] = useState<"login" | "register">("login");
  const [showPass, setShowPass] = useState(false);
  const [selectedRole, setSelectedRole] = useState<RoleType>(null);
  const [loginEmail, setLoginEmail] = useState("");
  const [loginPass, setLoginPass] = useState("");
  const [name, setName] = useState("");
  const [lastName, setLastName] = useState("");
  const [registerEmail, setRegisterEmail] = useState("");
  const [registerPass, setRegisterPass] = useState("");
  const [registerPassConfirm, setRegisterPassConfirm] = useState("");
  const [locality, setLocality] = useState(LOCATION_OPTIONS[0]);
  const [availabilityDayFrom, setAvailabilityDayFrom] = useState(WEEK_DAYS[0].value);
  const [availabilityDayTo, setAvailabilityDayTo] = useState(WEEK_DAYS[4].value);
  const [availabilityFrom, setAvailabilityFrom] = useState("09:00");
  const [availabilityTo, setAvailabilityTo] = useState("18:00");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleLogin = async () => {
    setError("");
    setLoading(true);
    try {
      onAuth(await servifyApi.login(loginEmail.trim(), loginPass));
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo iniciar sesion");
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async () => {
    if (!selectedRole || !registerEmail || !registerPass) return;
    if (registerPass !== registerPassConfirm) {
      setError("Las contrasenas no coinciden");
      return;
    }
    setError("");
    setLoading(true);
    try {
      onAuth(
        await servifyApi.register({
          nombre: name,
          apellido: lastName,
          email: registerEmail.trim(),
          password: registerPass,
          localidad: locality,
          disponibilidadDiaDesde: availabilityDayFrom,
          disponibilidadDiaHasta: availabilityDayTo,
          horaDesde: availabilityFrom,
          horaHasta: availabilityTo,
          role: selectedRole,
        })
      );
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo crear la cuenta");
    } finally {
      setLoading(false);
    }
  };

  return (
    <KeyboardAvoidingView style={styles.screen} behavior={Platform.OS === "ios" ? "padding" : undefined}>
      <View style={styles.authHeader}>
        <View style={styles.brandRow}>
          <Image source={require("./assets/icon.png")} style={styles.brandIcon} />
          <View>
            <Text style={styles.brandName}>Servify</Text>
            <Text style={styles.mutedText}>Encontra el servicio que necesitas</Text>
          </View>
        </View>
        <View style={styles.segment}>
          <SegmentButton label="Iniciar sesion" active={tab === "login"} onPress={() => setTab("login")} />
          <SegmentButton label="Registrarse" active={tab === "register"} onPress={() => setTab("register")} />
        </View>
      </View>

      <ScrollView contentContainerStyle={styles.formScroll} keyboardShouldPersistTaps="handled">
        <Text style={styles.screenTitle}>{tab === "login" ? "Bienvenido de nuevo" : "Crear cuenta"}</Text>
        <Text style={styles.screenSubtitle}>{tab === "login" ? "Ingresa a tu cuenta de Servify" : "Como vas a usar Servify?"}</Text>
        {error ? <ErrorBox message={error} /> : null}

        {tab === "login" ? (
          <>
            <Input icon={<Mail size={18} color="#94a3b8" />} placeholder="Email" value={loginEmail} onChangeText={setLoginEmail} autoCapitalize="none" keyboardType="email-address" />
            <Input
              icon={<Eye size={18} color="#94a3b8" />}
              placeholder="Contrasena"
              value={loginPass}
              onChangeText={setLoginPass}
              secureTextEntry={!showPass}
              suffix={<IconButton onPress={() => setShowPass((value) => !value)} icon={showPass ? <EyeOff size={18} color="#64748b" /> : <Eye size={18} color="#64748b" />} />}
            />
            <PrimaryButton label={loading ? "Conectando..." : "Iniciar sesion"} disabled={loading || !loginEmail || !loginPass} onPress={handleLogin} />
          </>
        ) : (
          <>
            <View style={styles.roleGrid}>
              {[
                { id: "client" as const, title: "Cliente", sub: "Busco servicios" },
                { id: "provider" as const, title: "Prestador", sub: "Ofrezco servicios" },
                { id: "both" as const, title: "Ambos", sub: "Uso completo" },
              ].map((role) => (
                <TouchableOpacity
                  key={role.id}
                  onPress={() => setSelectedRole(role.id)}
                  style={[styles.roleCard, selectedRole === role.id && styles.roleCardActive]}
                >
                  <Text style={[styles.roleTitle, selectedRole === role.id && { color: "#2563eb" }]}>{role.title}</Text>
                  <Text style={styles.roleSub}>{role.sub}</Text>
                </TouchableOpacity>
              ))}
            </View>
            <View style={styles.row}>
              <Input placeholder="Nombre" value={name} onChangeText={setName} style={{ flex: 1 }} />
              <Input placeholder="Apellido" value={lastName} onChangeText={setLastName} style={{ flex: 1 }} />
            </View>
            <Input icon={<Mail size={18} color="#94a3b8" />} placeholder="Email" value={registerEmail} onChangeText={setRegisterEmail} autoCapitalize="none" keyboardType="email-address" />
            <Input placeholder="Contrasena" value={registerPass} onChangeText={setRegisterPass} secureTextEntry />
            <Input placeholder="Confirmar contrasena" value={registerPassConfirm} onChangeText={setRegisterPassConfirm} secureTextEntry />
            <ChoiceField label="Localidad" value={locality} options={LOCATION_OPTIONS} onChange={setLocality} />
            <ChoiceField label="Desde dia" value={availabilityDayFrom} options={WEEK_DAYS.map((day) => day.value)} onChange={setAvailabilityDayFrom} format={(value) => WEEK_DAYS.find((day) => day.value === value)?.label ?? value} />
            <ChoiceField label="Hasta dia" value={availabilityDayTo} options={WEEK_DAYS.map((day) => day.value)} onChange={setAvailabilityDayTo} format={(value) => WEEK_DAYS.find((day) => day.value === value)?.label ?? value} />
            <View style={styles.row}>
              <ChoiceField compact label="Desde" value={availabilityFrom} options={TIME_OPTIONS} onChange={setAvailabilityFrom} />
              <ChoiceField compact label="Hasta" value={availabilityTo} options={TIME_OPTIONS} onChange={setAvailabilityTo} />
            </View>
            <PrimaryButton label={loading ? "Creando cuenta..." : selectedRole ? "Crear cuenta" : "Selecciona tu rol"} disabled={loading || !selectedRole || !registerEmail || !registerPass} onPress={handleRegister} />
          </>
        )}
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

function ExploreScreen({ user, userName, onCreateRequest, onCategoryPress }: { user?: SessionUser | null; userName: string; onCreateRequest: () => void; onCategoryPress: (cat: string) => void }) {
  const [search, setSearch] = useState("");
  const [activityOpen, setActivityOpen] = useState(false);
  const [received, setReceived] = useState<ApiReceivedRequest[]>([]);
  const [ownRequests, setOwnRequests] = useState<ApiRequest[]>([]);
  const [ownPublications, setOwnPublications] = useState<ApiPublication[]>([]);
  const [loading, setLoading] = useState(false);

  const loadActivity = useCallback(async () => {
    if (!user) return;
    setLoading(true);
    const shouldLoadProviderData = user.role === "provider" || user.role === "both";
    const [nextReceived, nextRequests, nextPublications] = await Promise.all([
      shouldLoadProviderData ? servifyApi.listReceivedRequests(user.id).catch(() => []) : Promise.resolve([]),
      servifyApi.listUserRequests(user.id).catch(() => []),
      shouldLoadProviderData ? servifyApi.listUserPublications(user.id).catch(() => []) : Promise.resolve([]),
    ]);
    setReceived(nextReceived);
    setOwnRequests(nextRequests);
    setOwnPublications(nextPublications);
    setLoading(false);
  }, [user]);

  useEffect(() => {
    loadActivity();
  }, [loadActivity]);

  const firstName = userName.split(" ")[0];
  const filtered = search ? categories.filter((cat) => cat.label.toLowerCase().includes(search.toLowerCase())) : categories;
  const activity = useMemo(() => buildActivitySummary(received, ownRequests, ownPublications), [received, ownRequests, ownPublications]);

  return (
    <View style={styles.screen}>
      <View style={styles.headerWhite}>
        <View style={styles.headerRow}>
          <View>
            <Text style={styles.mutedText}>Hola</Text>
            <Text style={styles.headerTitle}>Que necesitas,{`\n`}{firstName}?</Text>
          </View>
          <TouchableOpacity style={styles.iconBadgeButton} onPress={() => setActivityOpen((open) => !open)}>
            <Bell size={21} color="#475569" />
            {activity.badgeCount > 0 ? <Text style={styles.badge}>{activity.badgeCount > 9 ? "9+" : activity.badgeCount}</Text> : null}
          </TouchableOpacity>
        </View>
        {activityOpen ? (
          <View style={styles.activityPanel}>
            <Text style={styles.cardTitle}>Actividad</Text>
            <Text style={styles.cardSubtitle}>{loading ? "Actualizando..." : activity.items[0]?.detail}</Text>
          </View>
        ) : null}
        <Input icon={<Search size={18} color="#94a3b8" />} placeholder="Buscar categoria..." value={search} onChangeText={setSearch} />
      </View>
      <ScrollView contentContainerStyle={styles.content} refreshControl={<RefreshControl refreshing={loading} onRefresh={loadActivity} />}>
        {(user?.role === "provider" || user?.role === "both") && received.length > 0 ? (
          <Section title="Recomendados para vos">
            {received.map((request) => (
              <CardButton key={request.id} title={(request.descripcionNecesidad || "Solicitud de servicio").split(".")[0]} subtitle={request.descripcionNecesidad} onPress={() => onCategoryPress(request.descripcionNecesidad ?? "")} />
            ))}
          </Section>
        ) : null}
        {!search ? (
          <View style={styles.featureCard}>
            <Text style={styles.featureEyebrow}>Buscas un experto?</Text>
            <Text style={styles.featureTitle}>Encontra el experto que necesitas</Text>
            <TouchableOpacity style={styles.featureButton} onPress={onCreateRequest}>
              <Text style={styles.featureButtonText}>Crear solicitud</Text>
              <ChevronRight size={16} color="#0891b2" />
            </TouchableOpacity>
          </View>
        ) : null}
        <Section title="Categorias" aside={`${filtered.length} disponibles`}>
          {filtered.map((cat) => (
            <CardButton
              key={cat.label}
              left={<View style={[styles.categoryMark, { backgroundColor: cat.bg }]}><Text style={[styles.categoryLetter, { color: cat.color }]}>{cat.icon}</Text></View>}
              title={cat.label}
              subtitle="Ver servicios disponibles"
              onPress={() => onCategoryPress(cat.label)}
            />
          ))}
        </Section>
      </ScrollView>
    </View>
  );
}

function CategoryPublicationsScreen({ categoryName, onBack }: { categoryName: string; onBack: () => void }) {
  const [items, setItems] = useState<ApiPublication[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let mounted = true;
    setLoading(true);
    servifyApi
      .listCategoryPublications(categoryName)
      .then((next) => mounted && setItems(next))
      .catch((err) => mounted && setError(err instanceof Error ? err.message : "No se pudieron cargar las publicaciones"))
      .finally(() => mounted && setLoading(false));
    return () => {
      mounted = false;
    };
  }, [categoryName]);

  return (
    <View style={styles.screen}>
      <HeaderWithBack title={categoryName || "Publicaciones"} subtitle="Servicios disponibles" onBack={onBack} />
      <ScrollView contentContainerStyle={styles.content}>
        {error ? <ErrorBox message={error} /> : null}
        {loading ? <LoadingText /> : null}
        {!loading && items.length === 0 ? <EmptyState title="Sin publicaciones" subtitle="Todavia no hay servicios activos en esta categoria." /> : null}
        {items.map((pub) => (
          <View key={pub.id} style={styles.card}>
            <Text style={styles.cardTitle}>{pub.titulo}</Text>
            <Text style={styles.cardSubtitle}>{pub.descripcion}</Text>
            <View style={styles.chipRow}>
              <Chip label={fromApiModality(pub.modalidadServicio)} />
              <Chip label={pub.ubicacion?.localidad || "CABA"} tone="gray" />
              <Chip label={formatMoney(pub.precioBase)} tone="blue" />
            </View>
          </View>
        ))}
      </ScrollView>
    </View>
  );
}

function RequestsScreen({ userId, onRequestPress, onNewRequest }: { userId?: string; onRequestPress: (req: ServiceRequest) => void; onNewRequest: () => void }) {
  const [tab, setTab] = useState<RequestTab>("my-requests");
  const [own, setOwn] = useState<ServiceRequest[]>([]);
  const [received, setReceived] = useState<ServiceRequest[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const load = useCallback(async () => {
    if (!userId) return;
    setLoading(true);
    setError("");
    try {
      const [nextOwn, nextReceived] = await Promise.all([
        servifyApi.listUserRequests(userId).catch(() => []),
        servifyApi.listReceivedRequests(userId).catch(() => []),
      ]);
      setOwn(nextOwn.map((req) => mapRequest(req, "SOLICITANTE")));
      setReceived(nextReceived.map((req) => mapRequest(req, "PRESTADOR")));
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudieron cargar las solicitudes");
    } finally {
      setLoading(false);
    }
  }, [userId]);

  useEffect(() => {
    load();
  }, [load]);

  const data = tab === "my-requests" ? own : tab === "my-proposals" ? received : [];

  return (
    <View style={styles.screen}>
      <View style={styles.headerWhite}>
        <View style={styles.headerRow}>
          <Text style={styles.headerTitle}>Solicitudes</Text>
          <SmallAction label="Nueva" icon={<Plus size={15} color="white" />} onPress={onNewRequest} />
        </View>
        <View style={styles.tabs}>
          <SegmentButton label="Mis pedidos" active={tab === "my-requests"} onPress={() => setTab("my-requests")} />
          <SegmentButton label="Mis propuestas" active={tab === "my-proposals"} onPress={() => setTab("my-proposals")} />
          <SegmentButton label="Explorar" active={tab === "explore"} onPress={() => setTab("explore")} />
        </View>
      </View>
      <ScrollView contentContainerStyle={styles.content} refreshControl={<RefreshControl refreshing={loading} onRefresh={load} />}>
        {error ? <ErrorBox message={error} /> : null}
        {!loading && data.length === 0 ? <EmptyState title="No hay resultados" subtitle="Cuando tengas solicitudes van a aparecer aca." /> : null}
        {data.map((req) => <RequestCard key={req.id} request={req} onPress={() => onRequestPress(req)} />)}
      </ScrollView>
    </View>
  );
}

function RequestDetail({ request, onBack, onRate }: { request: ServiceRequest; onBack: () => void; onRate: (name: string) => void }) {
  return (
    <View style={styles.screen}>
      <HeaderWithBack title={request.title} subtitle={request.status === "completed" ? "Solicitud completada" : "Detalle de solicitud"} onBack={onBack} />
      <ScrollView contentContainerStyle={styles.content}>
        <View style={styles.card}>
          <Text style={styles.cardTitle}>Necesidad</Text>
          <Text style={styles.cardSubtitle}>{request.description}</Text>
          <View style={styles.chipRow}>
            <Chip label={request.category} />
            <Chip label={request.modal} tone="purple" />
            <Chip label={request.price} tone="blue" />
          </View>
        </View>
        <InfoCard icon={<MapPin size={20} color="#ef4444" />} title="Ubicacion" value={request.location} />
        <InfoCard icon={<Clock size={20} color="#7c3aed" />} title="Horario" value={request.schedule} />
        <InfoCard icon={<User size={20} color="#2563eb" />} title="Solicitante" value={request.requesterName} />
        <PrimaryButton label="Calificar servicio" onPress={() => onRate(request.requesterName)} />
      </ScrollView>
    </View>
  );
}

function MyPublications({ userId, onNew }: { userId?: string; onNew: () => void }) {
  const [pubs, setPubs] = useState<ApiPublication[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const load = useCallback(async () => {
    if (!userId) return;
    setLoading(true);
    setError("");
    try {
      const next = await servifyApi.listUserPublications(userId);
      setPubs(next.filter((item) => (item.estado ?? "").toUpperCase() !== "ELIMINADA"));
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudieron cargar las publicaciones");
    } finally {
      setLoading(false);
    }
  }, [userId]);

  useEffect(() => {
    load();
  }, [load]);

  const toggleActive = async (pub: ApiPublication) => {
    if (!userId) return;
    const active = !["PAUSADA", "INACTIVA"].includes((pub.estado ?? "").toUpperCase());
    try {
      const updated = await servifyApi.changePublicationState(pub.id, userId, !active);
      setPubs((current) => current.map((item) => (item.id === pub.id ? updated : item)));
    } catch (err) {
      Alert.alert("Servify", err instanceof Error ? err.message : "No se pudo cambiar el estado");
    }
  };

  const remove = async (pub: ApiPublication) => {
    if (!userId) return;
    try {
      await servifyApi.deletePublication(pub.id, userId);
      setPubs((current) => current.filter((item) => item.id !== pub.id));
    } catch (err) {
      Alert.alert("Servify", err instanceof Error ? err.message : "No se pudo eliminar la publicacion");
    }
  };

  const activeCount = pubs.filter((pub) => !["PAUSADA", "INACTIVA"].includes((pub.estado ?? "").toUpperCase())).length;

  return (
    <View style={styles.screen}>
      <View style={styles.headerWhite}>
        <View style={styles.headerRow}>
          <View>
            <Text style={styles.headerTitle}>Mis publicaciones</Text>
            <Text style={styles.mutedText}>{pubs.length} servicios publicados - {activeCount} activos</Text>
          </View>
          <SmallAction label="Nuevo" icon={<Plus size={15} color="white" />} onPress={onNew} />
        </View>
      </View>
      <ScrollView contentContainerStyle={styles.content} refreshControl={<RefreshControl refreshing={loading} onRefresh={load} />}>
        {error ? <ErrorBox message={error} /> : null}
        {!loading && pubs.length === 0 ? <EmptyState title="Sin publicaciones" subtitle="Publica tu primer servicio para recibir pedidos." action="Publicar servicio" onAction={onNew} /> : null}
        {pubs.map((pub) => {
          const active = !["PAUSADA", "INACTIVA"].includes((pub.estado ?? "").toUpperCase());
          return (
            <View key={pub.id} style={styles.card}>
              <View style={styles.headerRow}>
                <Text style={[styles.cardTitle, { flex: 1 }]}>{pub.titulo}</Text>
                <Chip label={active ? "Activo" : "Pausado"} tone={active ? "green" : "gray"} />
              </View>
              <Text style={styles.cardSubtitle}>{pub.descripcion}</Text>
              <View style={styles.chipRow}>
                <Chip label={pub.categoriaServicio?.nombre ?? "Categoria"} />
                <Chip label={formatMoney(pub.precioBase)} tone="blue" />
                <Chip label={pub.ubicacion?.localidad || "CABA"} tone="gray" />
              </View>
              <View style={styles.actionRow}>
                <OutlineButton label={active ? "Pausar" : "Activar"} icon={active ? <Pause size={15} color="#d97706" /> : <CheckCircle size={15} color="#16a34a" />} onPress={() => toggleActive(pub)} />
                <OutlineButton danger label="Eliminar" icon={<Trash2 size={15} color="#ef4444" />} onPress={() => remove(pub)} />
              </View>
            </View>
          );
        })}
      </ScrollView>
    </View>
  );
}

function PublishScreen({ userId, onPublished }: { userId?: string; onPublished: () => void }) {
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [selectedCategory, setSelectedCategory] = useState<string | null>(null);
  const [selectedModality, setSelectedModality] = useState<string | null>(null);
  const [price, setPrice] = useState("");
  const [selectedAreas, setSelectedAreas] = useState<string[]>([LOCATION_OPTIONS[0]]);
  const [address, setAddress] = useState("");
  const [availabilityDayFrom, setAvailabilityDayFrom] = useState(WEEK_DAYS[0].value);
  const [availabilityDayTo, setAvailabilityDayTo] = useState(WEEK_DAYS[4].value);
  const [availabilityFrom, setAvailabilityFrom] = useState("09:00");
  const [availabilityTo, setAvailabilityTo] = useState("18:00");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const draftKey = userId ? `${PUBLISH_DRAFT_PREFIX}${userId}` : "";
  const canPublish = Boolean(userId && title && description && selectedCategory && selectedModality && price && selectedAreas.length > 0);

  useEffect(() => {
    if (!draftKey) return;
    storage.get(draftKey).then((raw) => {
      if (!raw) return;
      try {
        const draft = JSON.parse(raw) as Record<string, unknown>;
        setTitle(String(draft.title ?? ""));
        setDescription(String(draft.description ?? ""));
        setSelectedCategory((draft.selectedCategory as string | null) ?? null);
        setSelectedModality((draft.selectedModality as string | null) ?? null);
        setPrice(String(draft.price ?? ""));
        setSelectedAreas(Array.isArray(draft.selectedAreas) ? draft.selectedAreas as string[] : [LOCATION_OPTIONS[0]]);
        setAddress(String(draft.address ?? ""));
      } catch {
        storage.remove(draftKey);
      }
    });
  }, [draftKey]);

  useEffect(() => {
    if (!draftKey) return;
    storage.set(draftKey, JSON.stringify({ title, description, selectedCategory, selectedModality, price, selectedAreas, address }));
  }, [address, description, draftKey, price, selectedAreas, selectedCategory, selectedModality, title]);

  const publish = async () => {
    if (!canPublish) return;
    setLoading(true);
    setError("");
    try {
      await servifyApi.createPublication({
        usuarioId: userId!,
        categoria: selectedCategory!,
        titulo: title,
        descripcion: description,
        modalidad: selectedModality!,
        localidades: selectedAreas,
        direccion: address,
        precio: price,
        disponibilidadDiaDesde: availabilityDayFrom,
        disponibilidadDiaHasta: availabilityDayTo,
        horaDesde: availabilityFrom,
        horaHasta: availabilityTo,
      });
      if (draftKey) await storage.remove(draftKey);
      onPublished();
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo publicar el servicio");
    } finally {
      setLoading(false);
    }
  };

  const toggleArea = (area: string) => {
    setSelectedAreas((current) => current.includes(area) ? (current.length === 1 ? current : current.filter((item) => item !== area)) : [...current, area]);
  };

  return (
    <View style={styles.screen}>
      <View style={styles.headerWhite}>
        <Text style={styles.headerTitle}>Publicar servicio</Text>
        <Text style={styles.mutedText}>Ofrece tus habilidades a la comunidad</Text>
      </View>
      <ScrollView contentContainerStyle={styles.content} keyboardShouldPersistTaps="handled">
        {!userId ? <ErrorBox message="Inicia sesion para publicar servicios." /> : null}
        {error ? <ErrorBox message={error} /> : null}
        <LabeledInput label="Titulo del servicio" icon={<FileText size={16} color="#0891b2" />} value={title} onChangeText={setTitle} placeholder="Ej: Plomeria y reparaciones generales" />
        <LabeledInput label="Descripcion" icon={<AlignLeft size={16} color="#0891b2" />} value={description} onChangeText={setDescription} placeholder="Describe tu servicio..." multiline />
        <ChoiceChips label="Categoria" options={categories.map((cat) => cat.label)} value={selectedCategory} onChange={setSelectedCategory} />
        <ChoiceChips label="Modalidad" options={modalities} value={selectedModality} onChange={setSelectedModality} />
        <LabeledInput label="Precio base (ARS)" icon={<DollarSign size={16} color="#2563eb" />} value={price} onChangeText={setPrice} keyboardType="numeric" placeholder="Ej: 8000" />
        <MultiChoiceChips label="Areas de trabajo" options={LOCATION_OPTIONS} values={selectedAreas} onToggle={toggleArea} />
        <LabeledInput label="Direccion exacta (opcional)" icon={<MapPin size={16} color="#ef4444" />} value={address} onChangeText={setAddress} placeholder="Ej: Av. Santa Fe 1234" />
        <ChoiceField label="Desde dia" value={availabilityDayFrom} options={WEEK_DAYS.map((day) => day.value)} onChange={setAvailabilityDayFrom} format={(value) => WEEK_DAYS.find((day) => day.value === value)?.label ?? value} />
        <ChoiceField label="Hasta dia" value={availabilityDayTo} options={WEEK_DAYS.map((day) => day.value)} onChange={setAvailabilityDayTo} format={(value) => WEEK_DAYS.find((day) => day.value === value)?.label ?? value} />
        <View style={styles.row}>
          <ChoiceField compact label="Desde" value={availabilityFrom} options={TIME_OPTIONS} onChange={setAvailabilityFrom} />
          <ChoiceField compact label="Hasta" value={availabilityTo} options={TIME_OPTIONS} onChange={setAvailabilityTo} />
        </View>
        <PrimaryButton label={loading ? "Publicando..." : "Publicar servicio"} disabled={!canPublish || loading} onPress={publish} />
      </ScrollView>
    </View>
  );
}

function ProfileScreen({ user, onLogout, onLogin, onUserUpdated }: { user: SessionUser | null; onLogout: () => void; onLogin: () => void; onUserUpdated: (patch: Partial<SessionUser>) => void }) {
  const [editing, setEditing] = useState(false);
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [email, setEmail] = useState("");
  const [photoUrl, setPhotoUrl] = useState("");
  const [localidad, setLocalidad] = useState(LOCATION_OPTIONS[0]);
  const [role, setRole] = useState<Exclude<RoleType, null>>("client");
  const [rating, setRating] = useState<ApiRatingSummary | null>(null);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState("");

  useEffect(() => {
    if (!user) return;
    let mounted = true;
    Promise.all([
      servifyApi.getAccountConfig(user.id).catch(() => null),
      servifyApi.getUserProfile(user.id).catch(() => null),
      servifyApi.getUserRatingSummary(user.id).catch(() => null),
      servifyApi.getProfilePreferences(user.id),
      servifyApi.getStoredProfilePhoto(user.id),
    ]).then(([account, profile, nextRating, prefs, photo]) => {
      if (!mounted) return;
      setFirstName(profile?.nombre || user.name.split(" ")[0] || "");
      setLastName(profile?.apellido || user.name.split(" ").slice(1).join(" ") || "");
      setEmail(prefs.email || account?.usuario.email || user.email);
      setLocalidad(profile?.ubicacion?.localidad || LOCATION_OPTIONS[0]);
      setRole((prefs.role ?? user.role ?? "client") as Exclude<RoleType, null>);
      setPhotoUrl(photo || profile?.fotoPerfilUrl || "");
      setRating(nextRating);
    });
    return () => {
      mounted = false;
    };
  }, [user]);

  if (!user) {
    return (
      <View style={[styles.screen, styles.centered]}>
        <User size={42} color="#2563eb" />
        <Text style={styles.screenTitle}>Sin cuenta</Text>
        <Text style={[styles.screenSubtitle, { textAlign: "center" }]}>Inicia sesion para ver tu perfil y gestionar servicios.</Text>
        <PrimaryButton label="Ingresar" onPress={onLogin} />
      </View>
    );
  }

  const displayName = `${firstName} ${lastName}`.trim() || user.name;
  const initials = displayName.split(" ").map((chunk) => chunk[0]).join("").slice(0, 2).toUpperCase();

  const pickPhoto = async () => {
    const permission = await ImagePicker.requestMediaLibraryPermissionsAsync();
    if (!permission.granted) {
      Alert.alert("Servify", "Necesitamos permiso para acceder a tu galeria.");
      return;
    }
    const result = await ImagePicker.launchImageLibraryAsync({ mediaTypes: ImagePicker.MediaTypeOptions.Images, quality: 0.8, base64: false });
    if (!result.canceled) {
      const uri = result.assets[0]?.uri ?? "";
      setPhotoUrl(uri);
      await servifyApi.saveProfilePhoto(user.id, uri);
    }
  };

  const save = async () => {
    setSaving(true);
    setMessage("");
    try {
      await servifyApi.updateUserProfile(user.id, {
        nombre: firstName.trim(),
        apellido: lastName.trim(),
        fotoPerfilUrl: /^https?:\/\//i.test(photoUrl) ? photoUrl : "",
        localidad,
        descripcionPersonal: "Perfil actualizado desde app movil",
      });
      await servifyApi.saveProfilePreferences(user.id, { email, role });
      onUserUpdated({ name: `${firstName.trim()} ${lastName.trim()}`.trim(), email, role });
      setMessage("Perfil actualizado correctamente");
      setEditing(false);
    } catch (err) {
      setMessage(err instanceof Error ? err.message : "No se pudo actualizar el perfil");
    } finally {
      setSaving(false);
    }
  };

  return (
    <View style={styles.screen}>
      <View style={styles.profileHero}>
        <View style={styles.headerRow}>
          <View style={styles.profileUserRow}>
            <TouchableOpacity style={styles.avatar} onPress={editing ? pickPhoto : undefined}>
              {photoUrl ? <Image source={{ uri: photoUrl }} style={styles.avatarImage} /> : <Text style={styles.avatarText}>{initials}</Text>}
            </TouchableOpacity>
            <View style={{ flex: 1 }}>
              <Text style={styles.profileName}>{displayName}</Text>
              <Text style={styles.profileLocation}>{localidad}</Text>
            </View>
          </View>
          <TouchableOpacity style={styles.profileEdit} onPress={() => setEditing((value) => !value)}>
            <Edit3 size={18} color="white" />
          </TouchableOpacity>
        </View>
        <Text style={styles.profileRating}>{formatRatingLabel(rating)}</Text>
      </View>
      <ScrollView contentContainerStyle={styles.content}>
        {message ? <InfoMessage message={message} /> : null}
        <View style={styles.card}>
          <InfoLine icon={<Mail size={16} color="#0891b2" />} label="Email" value={email || user.email} />
          <InfoLine icon={<MapPin size={16} color="#ef4444" />} label="Localidad" value={localidad} />
          <InfoLine icon={<Briefcase size={16} color="#7c3aed" />} label="Tipo de cuenta" value={ACCOUNT_ROLES.find((item) => item.id === role)?.label ?? "Cliente"} />
          <InfoLine icon={<Star size={16} color="#eab308" />} label="Calificaciones" value={rating?.cantidadValoraciones ? `${rating.promedioEstrellas.toFixed(1)} (${rating.cantidadValoraciones})` : "Sin valoraciones"} />
        </View>
        {editing ? (
          <View style={styles.card}>
            <View style={styles.row}>
              <Input placeholder="Nombre" value={firstName} onChangeText={setFirstName} style={{ flex: 1 }} />
              <Input placeholder="Apellido" value={lastName} onChangeText={setLastName} style={{ flex: 1 }} />
            </View>
            <Input placeholder="Email" value={email} onChangeText={setEmail} autoCapitalize="none" />
            <ChoiceField label="Localidad" value={localidad} options={LOCATION_OPTIONS} onChange={setLocalidad} />
            <ChoiceChips label="Tipo de cuenta" options={ACCOUNT_ROLES.map((item) => item.id)} value={role} onChange={(next) => setRole(next as Exclude<RoleType, null>)} format={(value) => ACCOUNT_ROLES.find((item) => item.id === value)?.label ?? value} />
            <PrimaryButton label={saving ? "Guardando..." : "Guardar cambios"} disabled={saving || !firstName || !lastName || !email} onPress={save} />
          </View>
        ) : null}
        <OutlineButton danger label="Cerrar sesion" icon={<LogOut size={16} color="#ef4444" />} onPress={onLogout} />
      </ScrollView>
    </View>
  );
}

function NewRequestModal({ visible, userId, onClose, onCreated }: { visible: boolean; userId?: string; onClose: () => void; onCreated: () => void }) {
  const [category, setCategory] = useState(categories[0].label);
  const [description, setDescription] = useState("");
  const [modality, setModality] = useState("Presencial");
  const [locality, setLocality] = useState(LOCATION_OPTIONS[0]);
  const [price, setPrice] = useState("");
  const [loading, setLoading] = useState(false);

  const create = async () => {
    if (!userId || !description) return;
    setLoading(true);
    try {
      await servifyApi.createServiceRequest({
        solicitanteId: userId,
        categoria: category,
        descripcion: description,
        modalidad: modality,
        localidad: locality,
        precio: price,
      });
      setDescription("");
      onCreated();
    } catch (err) {
      Alert.alert("Servify", err instanceof Error ? err.message : "No se pudo crear la solicitud");
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal visible={visible} animationType="slide" transparent onRequestClose={onClose}>
      <View style={styles.modalBackdrop}>
        <KeyboardAvoidingView behavior={Platform.OS === "ios" ? "padding" : undefined} style={styles.sheet}>
          <View style={styles.headerRow}>
            <Text style={styles.sheetTitle}>Nueva solicitud</Text>
            <IconButton onPress={onClose} icon={<X size={20} color="#475569" />} />
          </View>
          {!userId ? <ErrorBox message="Inicia sesion para crear solicitudes." /> : null}
          <ChoiceChips label="Categoria" options={categories.map((cat) => cat.label)} value={category} onChange={setCategory} />
          <LabeledInput label="Descripcion" icon={<AlignLeft size={16} color="#0891b2" />} value={description} onChangeText={setDescription} multiline placeholder="Que necesitas resolver?" />
          <ChoiceChips label="Modalidad" options={modalities} value={modality} onChange={setModality} />
          <ChoiceField label="Localidad" value={locality} options={LOCATION_OPTIONS} onChange={setLocality} />
          <Input placeholder="Precio de referencia" value={price} onChangeText={setPrice} keyboardType="numeric" />
          <PrimaryButton label={loading ? "Creando..." : "Crear solicitud"} disabled={!userId || !description || loading} onPress={create} />
        </KeyboardAvoidingView>
      </View>
    </Modal>
  );
}

function RatingModal({ visible, providerName, onClose }: { visible: boolean; providerName: string; onClose: () => void }) {
  const [rating, setRating] = useState(0);
  const [sent, setSent] = useState(false);

  return (
    <Modal visible={visible} animationType="slide" transparent onRequestClose={onClose}>
      <View style={styles.modalBackdrop}>
        <View style={styles.sheet}>
          <View style={styles.headerRow}>
            <View>
              <Text style={styles.sheetTitle}>Calificar servicio</Text>
              <Text style={styles.cardSubtitle}>{providerName}</Text>
            </View>
            <IconButton onPress={onClose} icon={<X size={20} color="#475569" />} />
          </View>
          {sent ? (
            <EmptyState title="Gracias por tu calificacion" subtitle="Tu opinion ayuda a mejorar Servify." />
          ) : (
            <>
              <View style={styles.starRow}>
                {[1, 2, 3, 4, 5].map((value) => (
                  <TouchableOpacity key={value} onPress={() => setRating(value)}>
                    <Star size={34} color="#eab308" fill={value <= rating ? "#eab308" : "transparent"} />
                  </TouchableOpacity>
                ))}
              </View>
              <PrimaryButton label="Enviar calificacion" disabled={rating === 0} onPress={() => setSent(true)} />
            </>
          )}
        </View>
      </View>
    </Modal>
  );
}

function BottomNav({ activeTab, onTabChange }: { activeTab: BottomTab; onTabChange: (tab: BottomTab) => void }) {
  const tabs: { id: BottomTab; label: string; icon: React.ReactNode }[] = [
    { id: "explore", label: "Explorar", icon: <Home size={20} /> },
    { id: "requests", label: "Pedidos", icon: <ListChecks size={20} /> },
    { id: "my-services", label: "Servicios", icon: <Wrench size={20} /> },
    { id: "publish", label: "Publicar", icon: <Send size={20} /> },
    { id: "profile", label: "Perfil", icon: <Settings size={20} /> },
  ];
  return (
    <SafeAreaView edges={["bottom"]} style={styles.nav}>
      {tabs.map((tab) => {
        const active = activeTab === tab.id;
        return (
          <TouchableOpacity key={tab.id} style={styles.navItem} onPress={() => onTabChange(tab.id)}>
            {React.cloneElement(tab.icon as React.ReactElement, { color: active ? "#2563eb" : "#94a3b8", strokeWidth: active ? 2.4 : 1.8 })}
            <Text style={[styles.navLabel, active && { color: "#2563eb", fontWeight: "800" }]}>{tab.label}</Text>
          </TouchableOpacity>
        );
      })}
    </SafeAreaView>
  );
}

function RequestCard({ request, onPress }: { request: ServiceRequest; onPress: () => void }) {
  const st = statusConfig[request.status];
  return (
    <TouchableOpacity style={styles.card} onPress={onPress}>
      <View style={styles.headerRow}>
        <Text style={[styles.cardTitle, { flex: 1 }]}>{request.title}</Text>
        <Chip label={st.label} customBg={st.bg} customColor={st.color} />
      </View>
      <Text style={styles.cardSubtitle}>{request.description}</Text>
      <View style={styles.chipRow}>
        <Chip label={request.category} />
        <Chip label={request.modal} tone="purple" />
      </View>
      <View style={styles.metaRow}>
        <Text style={styles.metaText}>{request.location}</Text>
        <Text style={styles.metaText}>{request.schedule}</Text>
        <Text style={styles.metaPrice}>{request.price}</Text>
      </View>
    </TouchableOpacity>
  );
}

function HeaderWithBack({ title, subtitle, onBack }: { title: string; subtitle?: string; onBack: () => void }) {
  return (
    <View style={styles.headerWhite}>
      <View style={styles.headerRow}>
        <TouchableOpacity onPress={onBack} style={styles.backButton}>
          <ChevronLeft size={22} color="#0f172a" />
        </TouchableOpacity>
        <View style={{ flex: 1 }}>
          <Text style={styles.headerTitle} numberOfLines={2}>{title}</Text>
          {subtitle ? <Text style={styles.mutedText}>{subtitle}</Text> : null}
        </View>
      </View>
    </View>
  );
}

function Section({ title, aside, children }: { title: string; aside?: string; children: React.ReactNode }) {
  return (
    <View>
      <View style={styles.sectionHeader}>
        <Text style={styles.sectionTitle}>{title}</Text>
        {aside ? <Text style={styles.mutedText}>{aside}</Text> : null}
      </View>
      <View style={{ gap: 10 }}>{children}</View>
    </View>
  );
}

function CardButton({ title, subtitle, left, onPress }: { title: string; subtitle?: string; left?: React.ReactNode; onPress: () => void }) {
  return (
    <TouchableOpacity style={styles.cardButton} onPress={onPress}>
      {left}
      <View style={{ flex: 1 }}>
        <Text style={styles.cardTitle}>{title}</Text>
        {subtitle ? <Text style={styles.cardSubtitle} numberOfLines={2}>{subtitle}</Text> : null}
      </View>
      <ChevronRight size={19} color="#cbd5e1" />
    </TouchableOpacity>
  );
}

function Input(props: React.ComponentProps<typeof TextInput> & { icon?: React.ReactNode; suffix?: React.ReactNode; style?: object }) {
  const { icon, suffix, style, ...rest } = props;
  return (
    <View style={[styles.inputWrap, style]}>
      {icon}
      <TextInput placeholderTextColor="#94a3b8" style={styles.input} {...rest} />
      {suffix}
    </View>
  );
}

function LabeledInput({ label, icon, ...props }: React.ComponentProps<typeof TextInput> & { label: string; icon: React.ReactNode }) {
  return (
    <View>
      <Text style={styles.fieldLabel}>{label}</Text>
      <Input icon={icon} {...props} style={props.multiline ? styles.textAreaWrap : undefined} />
    </View>
  );
}

function ChoiceField({ label, value, options, onChange, format, compact }: { label: string; value: string; options: string[]; onChange: (value: string) => void; format?: (value: string) => string; compact?: boolean }) {
  return (
    <View style={compact ? { flex: 1 } : undefined}>
      <Text style={styles.fieldLabel}>{label}</Text>
      <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={styles.choiceRow}>
        {options.map((option) => (
          <TouchableOpacity key={option} onPress={() => onChange(option)} style={[styles.choiceChip, value === option && styles.choiceChipActive]}>
            <Text style={[styles.choiceText, value === option && styles.choiceTextActive]}>{format ? format(option) : option}</Text>
          </TouchableOpacity>
        ))}
      </ScrollView>
    </View>
  );
}

function ChoiceChips({ label, options, value, onChange, format }: { label: string; options: string[]; value: string | null; onChange: (value: string) => void; format?: (value: string) => string }) {
  return (
    <View>
      <Text style={styles.fieldLabel}>{label}</Text>
      <View style={styles.wrapRow}>
        {options.map((option) => (
          <TouchableOpacity key={option} onPress={() => onChange(option)} style={[styles.choiceChip, value === option && styles.choiceChipActive]}>
            <Text style={[styles.choiceText, value === option && styles.choiceTextActive]}>{format ? format(option) : option}</Text>
          </TouchableOpacity>
        ))}
      </View>
    </View>
  );
}

function MultiChoiceChips({ label, options, values, onToggle }: { label: string; options: string[]; values: string[]; onToggle: (value: string) => void }) {
  return (
    <View>
      <Text style={styles.fieldLabel}>{label}</Text>
      <View style={styles.wrapRow}>
        {options.map((option) => {
          const selected = values.includes(option);
          return (
            <TouchableOpacity key={option} onPress={() => onToggle(option)} style={[styles.choiceChip, selected && styles.choiceChipActive]}>
              <Text style={[styles.choiceText, selected && styles.choiceTextActive]}>{option}</Text>
            </TouchableOpacity>
          );
        })}
      </View>
    </View>
  );
}

function PrimaryButton({ label, disabled, onPress }: { label: string; disabled?: boolean; onPress: () => void }) {
  return (
    <TouchableOpacity disabled={disabled} onPress={onPress} style={[styles.primaryButton, disabled && styles.disabledButton]}>
      <Text style={styles.primaryText}>{label}</Text>
    </TouchableOpacity>
  );
}

function OutlineButton({ label, icon, danger, onPress }: { label: string; icon?: React.ReactNode; danger?: boolean; onPress: () => void }) {
  return (
    <TouchableOpacity onPress={onPress} style={[styles.outlineButton, danger && styles.outlineDanger]}>
      {icon}
      <Text style={[styles.outlineText, danger && { color: "#ef4444" }]}>{label}</Text>
    </TouchableOpacity>
  );
}

function SmallAction({ label, icon, onPress }: { label: string; icon: React.ReactNode; onPress: () => void }) {
  return (
    <TouchableOpacity onPress={onPress} style={styles.smallAction}>
      {icon}
      <Text style={styles.smallActionText}>{label}</Text>
    </TouchableOpacity>
  );
}

function IconButton({ icon, onPress }: { icon: React.ReactNode; onPress: () => void }) {
  return <TouchableOpacity style={styles.iconButton} onPress={onPress}>{icon}</TouchableOpacity>;
}

function SegmentButton({ label, active, onPress }: { label: string; active: boolean; onPress: () => void }) {
  return (
    <TouchableOpacity onPress={onPress} style={[styles.segmentButton, active && styles.segmentActive]}>
      <Text style={[styles.segmentText, active && styles.segmentTextActive]}>{label}</Text>
    </TouchableOpacity>
  );
}

function Chip({ label, tone = "cyan", customBg, customColor }: { label: string; tone?: "cyan" | "purple" | "blue" | "gray" | "green"; customBg?: string; customColor?: string }) {
  const colors = {
    cyan: ["#ecfeff", "#0891b2"],
    purple: ["#f5f3ff", "#7c3aed"],
    blue: ["#eff6ff", "#2563eb"],
    gray: ["#f1f5f9", "#64748b"],
    green: ["#f0fdf4", "#16a34a"],
  }[tone];
  return <Text style={[styles.chip, { backgroundColor: customBg ?? colors[0], color: customColor ?? colors[1] }]}>{label}</Text>;
}

function ErrorBox({ message }: { message: string }) {
  return <Text style={styles.errorBox}>{message}</Text>;
}

function InfoMessage({ message }: { message: string }) {
  return <Text style={styles.infoBox}>{message}</Text>;
}

function EmptyState({ title, subtitle, action, onAction }: { title: string; subtitle: string; action?: string; onAction?: () => void }) {
  return (
    <View style={styles.empty}>
      <Text style={styles.emptyTitle}>{title}</Text>
      <Text style={styles.emptySubtitle}>{subtitle}</Text>
      {action && onAction ? <PrimaryButton label={action} onPress={onAction} /> : null}
    </View>
  );
}

function LoadingText() {
  return <Text style={styles.mutedText}>Cargando...</Text>;
}

function InfoCard({ icon, title, value }: { icon: React.ReactNode; title: string; value: string }) {
  return (
    <View style={styles.card}>
      <View style={styles.infoCardRow}>
        {icon}
        <View>
          <Text style={styles.cardTitle}>{title}</Text>
          <Text style={styles.cardSubtitle}>{value}</Text>
        </View>
      </View>
    </View>
  );
}

function InfoLine({ icon, label, value }: { icon: React.ReactNode; label: string; value: string }) {
  return (
    <View style={styles.infoLine}>
      <View style={styles.infoCardRow}>{icon}<Text style={styles.infoLabel}>{label}</Text></View>
      <Text style={styles.infoValue}>{value}</Text>
    </View>
  );
}

function buildActivitySummary(receivedRequests: ApiReceivedRequest[], ownRequests: ApiRequest[], ownPublications: ApiPublication[]) {
  const pendingReceived = receivedRequests.filter((request) => ["ENVIADA", "CONTRAOFERTADA"].includes((request.estadoDistribucion ?? request.estado ?? "").toUpperCase()));
  const activeOwnRequests = ownRequests.filter((request) => ["BUSCANDO_PRESTADOR", "ASIGNADA"].includes((request.estado ?? "").toUpperCase()));
  const activePublications = ownPublications.filter((publication) => (publication.estado ?? "").toUpperCase() === "ACTIVA");
  const items = pendingReceived.length > 0
    ? [{ detail: "Tenes pedidos compatibles esperando respuesta." }]
    : activeOwnRequests.length > 0
      ? [{ detail: "Tus pedidos siguen buscando o ya tienen asignacion." }]
      : [{ detail: activePublications.length > 0 ? `${activePublications.length} publicacion activa.` : "Sin actividad pendiente." }];
  return { badgeCount: pendingReceived.length + activeOwnRequests.length, items };
}

const statusConfig = {
  open: { label: "Abierta", bg: "#eff6ff", color: "#2563eb" },
  completed: { label: "Completada", bg: "#f0fdf4", color: "#16a34a" },
  cancelled: { label: "Cancelada", bg: "#fef2f2", color: "#ef4444" },
  "in-progress": { label: "En curso", bg: "#fffbeb", color: "#d97706" },
};

function mapRequest(req: ApiRequest, viewerRole: "SOLICITANTE" | "PRESTADOR" | null): ServiceRequest {
  const description = req.descripcionNecesidad ?? "Solicitud de servicio";
  const title = description.split(".")[0] || "Solicitud de servicio";
  const locality = req.ubicacion?.localidad || req.ubicacion?.ciudad || "CABA";
  const requesterId = req.solicitanteId || "";
  const requesterName = requesterId ? `Usuario ${requesterId.slice(0, 6)}` : "Solicitante";
  const date = req.fechaSolicitud ?? req.createdAt;
  return {
    id: req.id,
    viewerRole,
    title,
    description,
    category: req.categoriaServicioId ? `Categoria ${req.categoriaServicioId.slice(0, 6)}` : "Sin categoria",
    location: locality,
    proposals: 0,
    price: formatMoney(req.precioReferencia),
    schedule: formatAvailability(req),
    date: date ? new Date(date).toLocaleDateString("es-AR") : "Sin fecha",
    status: toUiStatus(req.estado),
    requesterName,
    requesterInitials: requesterName.split(" ").map((chunk) => chunk[0]).join("").slice(0, 2).toUpperCase(),
    modal: fromApiModality(req.modalidadServicio),
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
  if (status === "ASIGNADA" || status === "EN_CURSO" || status === "ACEPTADA" || status === "CONTRAOFERTADA" || status === "CERRADA") return "in-progress";
  return "open";
}

function formatRatingLabel(summary: ApiRatingSummary | null) {
  const count = summary?.cantidadValoraciones ?? 0;
  if (count === 0) return "Sin valoraciones todavia";
  const average = summary?.promedioEstrellas ?? 0;
  return `${average.toFixed(1)} promedio sobre ${count} valoraciones`;
}

const styles = StyleSheet.create({
  appShell: { flex: 1, backgroundColor: "#f8fafc" },
  appBody: { flex: 1, overflow: "hidden" },
  screen: { flex: 1, backgroundColor: "#f8fafc" },
  centered: { alignItems: "center", justifyContent: "center", padding: 28, gap: 12 },
  splash: { flex: 1, alignItems: "center", justifyContent: "center", backgroundColor: "#ffffff" },
  splashMark: { width: 104, height: 104 },
  splashTitle: { marginTop: 18, fontSize: 34, fontWeight: "900", color: "#0f172a" },
  splashSubtitle: { marginTop: 5, fontSize: 15, fontWeight: "600", color: "#64748b" },
  authHeader: { backgroundColor: "#ffffff", paddingHorizontal: 22, paddingTop: 18, paddingBottom: 16 },
  brandRow: { flexDirection: "row", alignItems: "center", gap: 12 },
  brandIcon: { width: 44, height: 44 },
  brandName: { fontSize: 22, fontWeight: "900", color: "#0f172a" },
  headerWhite: { backgroundColor: "#ffffff", paddingHorizontal: 20, paddingTop: 18, paddingBottom: 16, gap: 14 },
  headerRow: { flexDirection: "row", alignItems: "center", justifyContent: "space-between", gap: 12 },
  headerTitle: { fontSize: 23, fontWeight: "900", color: "#0f172a", lineHeight: 29 },
  screenTitle: { fontSize: 22, fontWeight: "900", color: "#0f172a" },
  screenSubtitle: { fontSize: 14, color: "#64748b", fontWeight: "600", marginTop: 3, marginBottom: 10 },
  mutedText: { fontSize: 13, color: "#64748b", fontWeight: "600" },
  formScroll: { padding: 22, gap: 14 },
  content: { padding: 20, gap: 14, paddingBottom: 28 },
  row: { flexDirection: "row", gap: 10 },
  segment: { marginTop: 18, flexDirection: "row", gap: 6, backgroundColor: "#eef2ff", padding: 4, borderRadius: 16 },
  tabs: { flexDirection: "row", gap: 6 },
  segmentButton: { flex: 1, paddingVertical: 10, paddingHorizontal: 8, borderRadius: 12, alignItems: "center" },
  segmentActive: { backgroundColor: "#ffffff", shadowColor: "#0f172a", shadowOpacity: 0.08, shadowRadius: 4, elevation: 1 },
  segmentText: { fontSize: 12, color: "#64748b", fontWeight: "700" },
  segmentTextActive: { color: "#2563eb", fontWeight: "900" },
  inputWrap: { minHeight: 48, borderRadius: 16, backgroundColor: "#f8fafc", borderWidth: 1.5, borderColor: "#e2e8f0", paddingHorizontal: 14, flexDirection: "row", alignItems: "center", gap: 10 },
  input: { flex: 1, color: "#0f172a", fontSize: 14, fontWeight: "600", paddingVertical: Platform.OS === "ios" ? 12 : 8 },
  textAreaWrap: { minHeight: 96, alignItems: "flex-start", paddingTop: 12 },
  fieldLabel: { marginBottom: 8, fontSize: 13, fontWeight: "800", color: "#475569" },
  roleGrid: { flexDirection: "row", gap: 8 },
  roleCard: { flex: 1, minHeight: 92, borderRadius: 16, backgroundColor: "#ffffff", borderWidth: 1.5, borderColor: "#e2e8f0", alignItems: "center", justifyContent: "center", padding: 8 },
  roleCardActive: { borderColor: "#2563eb", backgroundColor: "#eff6ff" },
  roleTitle: { fontSize: 14, fontWeight: "900", color: "#0f172a", textAlign: "center" },
  roleSub: { marginTop: 4, fontSize: 11, color: "#64748b", fontWeight: "600", textAlign: "center" },
  primaryButton: { minHeight: 50, borderRadius: 16, backgroundColor: "#2563eb", alignItems: "center", justifyContent: "center", paddingHorizontal: 18 },
  disabledButton: { backgroundColor: "#cbd5e1" },
  primaryText: { color: "#ffffff", fontWeight: "900", fontSize: 15 },
  smallAction: { flexDirection: "row", alignItems: "center", gap: 6, backgroundColor: "#2563eb", borderRadius: 13, paddingHorizontal: 14, paddingVertical: 9 },
  smallActionText: { color: "#ffffff", fontSize: 13, fontWeight: "900" },
  outlineButton: { flex: 1, minHeight: 44, borderRadius: 14, borderWidth: 1.5, borderColor: "#cbd5e1", alignItems: "center", justifyContent: "center", flexDirection: "row", gap: 7, paddingHorizontal: 12 },
  outlineDanger: { backgroundColor: "#fef2f2", borderColor: "#fecaca" },
  outlineText: { color: "#475569", fontWeight: "900", fontSize: 13 },
  iconButton: { width: 38, height: 38, borderRadius: 12, backgroundColor: "#f1f5f9", alignItems: "center", justifyContent: "center" },
  iconBadgeButton: { width: 46, height: 46, borderRadius: 16, backgroundColor: "#f1f5f9", alignItems: "center", justifyContent: "center" },
  badge: { position: "absolute", top: -4, right: -4, minWidth: 20, height: 20, borderRadius: 10, backgroundColor: "#ef4444", color: "#fff", fontSize: 10, fontWeight: "900", textAlign: "center", overflow: "hidden", paddingTop: 3 },
  activityPanel: { borderRadius: 16, borderWidth: 1, borderColor: "#e2e8f0", padding: 14, backgroundColor: "#ffffff" },
  featureCard: { minHeight: 156, borderRadius: 22, backgroundColor: "#0891b2", padding: 20, justifyContent: "center" },
  featureEyebrow: { color: "rgba(255,255,255,0.78)", fontSize: 13, fontWeight: "700" },
  featureTitle: { color: "#ffffff", fontSize: 21, fontWeight: "900", marginTop: 4, marginBottom: 16, maxWidth: 230 },
  featureButton: { alignSelf: "flex-start", backgroundColor: "#ffffff", borderRadius: 13, flexDirection: "row", alignItems: "center", gap: 6, paddingHorizontal: 14, paddingVertical: 10 },
  featureButtonText: { color: "#0891b2", fontWeight: "900", fontSize: 13 },
  sectionHeader: { flexDirection: "row", justifyContent: "space-between", alignItems: "center", marginBottom: 10 },
  sectionTitle: { fontSize: 16, fontWeight: "900", color: "#0f172a" },
  cardButton: { backgroundColor: "#ffffff", borderRadius: 18, borderWidth: 1, borderColor: "rgba(0,0,0,0.06)", padding: 14, flexDirection: "row", alignItems: "center", gap: 12 },
  card: { backgroundColor: "#ffffff", borderRadius: 18, borderWidth: 1, borderColor: "rgba(0,0,0,0.06)", padding: 15, gap: 10 },
  cardTitle: { fontSize: 15, fontWeight: "900", color: "#0f172a", lineHeight: 20 },
  cardSubtitle: { fontSize: 13, fontWeight: "600", color: "#64748b", lineHeight: 19 },
  categoryMark: { width: 46, height: 46, borderRadius: 14, alignItems: "center", justifyContent: "center" },
  categoryLetter: { fontSize: 19, fontWeight: "900" },
  chipRow: { flexDirection: "row", flexWrap: "wrap", gap: 8 },
  chip: { overflow: "hidden", borderRadius: 999, paddingHorizontal: 10, paddingVertical: 5, fontSize: 11, fontWeight: "900" },
  metaRow: { flexDirection: "row", flexWrap: "wrap", gap: 12, alignItems: "center", justifyContent: "space-between" },
  metaText: { fontSize: 12, color: "#94a3b8", fontWeight: "700" },
  metaPrice: { fontSize: 12, color: "#2563eb", fontWeight: "900" },
  actionRow: { flexDirection: "row", gap: 10, marginTop: 4 },
  choiceRow: { gap: 8, paddingRight: 12 },
  wrapRow: { flexDirection: "row", flexWrap: "wrap", gap: 8 },
  choiceChip: { borderRadius: 999, borderWidth: 1.5, borderColor: "#e2e8f0", backgroundColor: "#f8fafc", paddingHorizontal: 13, paddingVertical: 9 },
  choiceChipActive: { borderColor: "#2563eb", backgroundColor: "#eff6ff" },
  choiceText: { color: "#475569", fontSize: 12, fontWeight: "800" },
  choiceTextActive: { color: "#2563eb" },
  errorBox: { backgroundColor: "#fef2f2", borderColor: "#fecaca", borderWidth: 1, borderRadius: 14, padding: 12, color: "#b91c1c", fontSize: 13, fontWeight: "800", lineHeight: 18 },
  infoBox: { backgroundColor: "#ecfdf5", borderColor: "#bbf7d0", borderWidth: 1, borderRadius: 14, padding: 12, color: "#15803d", fontSize: 13, fontWeight: "800" },
  empty: { alignItems: "center", justifyContent: "center", paddingVertical: 42, gap: 8 },
  emptyTitle: { fontSize: 17, fontWeight: "900", color: "#0f172a" },
  emptySubtitle: { fontSize: 13, color: "#64748b", fontWeight: "600", textAlign: "center", lineHeight: 19 },
  backButton: { width: 38, height: 38, borderRadius: 12, backgroundColor: "#f1f5f9", alignItems: "center", justifyContent: "center" },
  infoCardRow: { flexDirection: "row", alignItems: "center", gap: 10 },
  infoLine: { flexDirection: "row", justifyContent: "space-between", alignItems: "center", gap: 14, paddingVertical: 8 },
  infoLabel: { fontSize: 13, color: "#64748b", fontWeight: "700" },
  infoValue: { flex: 1, textAlign: "right", fontSize: 13, color: "#0f172a", fontWeight: "900" },
  profileHero: { backgroundColor: "#2563eb", paddingHorizontal: 20, paddingTop: 20, paddingBottom: 24, gap: 16 },
  profileUserRow: { flexDirection: "row", alignItems: "center", gap: 13, flex: 1 },
  avatar: { width: 68, height: 68, borderRadius: 34, backgroundColor: "rgba(255,255,255,0.2)", borderWidth: 3, borderColor: "rgba(255,255,255,0.5)", alignItems: "center", justifyContent: "center", overflow: "hidden" },
  avatarImage: { width: "100%", height: "100%" },
  avatarText: { color: "#ffffff", fontSize: 24, fontWeight: "900" },
  profileName: { color: "#ffffff", fontSize: 20, fontWeight: "900" },
  profileLocation: { color: "rgba(255,255,255,0.78)", fontSize: 13, fontWeight: "700", marginTop: 3 },
  profileEdit: { width: 39, height: 39, borderRadius: 13, backgroundColor: "rgba(255,255,255,0.2)", alignItems: "center", justifyContent: "center" },
  profileRating: { color: "rgba(255,255,255,0.9)", fontSize: 13, fontWeight: "800", backgroundColor: "rgba(255,255,255,0.14)", padding: 12, borderRadius: 16 },
  modalBackdrop: { flex: 1, backgroundColor: "rgba(15,23,42,0.35)", justifyContent: "flex-end" },
  sheet: { backgroundColor: "#ffffff", borderTopLeftRadius: 24, borderTopRightRadius: 24, padding: 20, gap: 14, maxHeight: "92%" },
  sheetTitle: { fontSize: 21, fontWeight: "900", color: "#0f172a" },
  starRow: { flexDirection: "row", alignItems: "center", justifyContent: "center", gap: 10, paddingVertical: 22 },
  nav: { flexDirection: "row", backgroundColor: "#ffffff", borderTopWidth: 1, borderTopColor: "#f1f5f9", paddingTop: 8 },
  navItem: { flex: 1, alignItems: "center", justifyContent: "center", gap: 3, paddingBottom: 3 },
  navLabel: { color: "#94a3b8", fontSize: 10, fontWeight: "700" },
});
