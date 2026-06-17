import { useEffect, useMemo, useState, type ReactNode } from "react";
import {
  ArrowLeft,
  Ban,
  Briefcase,
  CheckCircle2,
  ChevronRight,
  Clock,
  DollarSign,
  Eye,
  FileWarning,
  Mail,
  MapPin,
  Plus,
  RefreshCcw,
  Search,
  Shield,
  Star,
  Trash2,
  UserRound,
  UserX,
} from "lucide-react";
import { motion } from "motion/react";
import {
  WEEK_DAYS,
  formatMoney,
  fromApiModality,
  servifyApi,
  type ApiAdminUser,
  type ApiCategory,
  type ApiPublicProvider,
  type ApiPublication,
  type ApiPublicationState,
  type ApiRatingSummary,
  type ApiUserProfile,
  type ApiUserState,
} from "../api";

const userStates: ApiUserState[] = ["ACTIVO", "SUSPENDIDO", "BLOQUEADO", "INACTIVO"];

const emptyRating = (usuarioId: string): ApiRatingSummary => ({
  usuarioId,
  cantidadValoraciones: 0,
  promedioEstrellas: 0,
});

interface AdminPanelScreenProps {
  onBack: () => void;
  onProviderPress: (provider: ApiPublicProvider) => void;
}

interface AdminUserView extends ApiAdminUser {
  profile: ApiUserProfile | null;
  rating: ApiRatingSummary;
  publications: ApiPublication[];
  activePublicationCount: number;
  displayName: string;
  searchText: string;
}

export function AdminPanelScreen({ onBack, onProviderPress }: AdminPanelScreenProps) {
  const [adminUser, setAdminUser] = useState<ApiAdminUser | null>(null);
  const [users, setUsers] = useState<AdminUserView[]>([]);
  const [categories, setCategories] = useState<ApiCategory[]>([]);
  const [selectedUserId, setSelectedUserId] = useState("");
  const [selectedPublication, setSelectedPublication] = useState<ApiPublication | null>(null);
  const [query, setQuery] = useState("");
  const [categoryName, setCategoryName] = useState("");
  const [categoryDescription, setCategoryDescription] = useState("");
  const [loading, setLoading] = useState(false);
  const [actionLoading, setActionLoading] = useState("");
  const [error, setError] = useState("");

  const loadUsers = async () => {
    setLoading(true);
    setError("");
    try {
      const admin = await servifyApi.getAdminSession();
      setAdminUser(admin);
      const groupedUsers = await Promise.all(userStates.map((state) => servifyApi.listAdminUsers(state)));
      const adminCategories = await servifyApi.listAdminCategories().catch(() => []);
      const uniqueUsers = uniqueById(groupedUsers.flat());
      const enrichedUsers = await Promise.all(uniqueUsers.map(enrichAdminUser));
      setCategories(adminCategories);
      setUsers(enrichedUsers.sort(compareUsersByRisk));
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo cargar administracion");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadUsers();
  }, []);

  const selectedUser = useMemo(
    () => users.find((user) => user.id === selectedUserId) ?? null,
    [selectedUserId, users]
  );

  const filteredUsers = useMemo(() => {
    const normalizedQuery = normalize(query);
    if (!normalizedQuery) return users;
    return users.filter((user) => user.searchText.includes(normalizedQuery));
  }, [query, users]);

  const createCategory = async () => {
    if (!categoryName.trim()) {
      setError("Indica el nombre de la categoria.");
      return;
    }
    setActionLoading("category-create");
    setError("");
    try {
      await servifyApi.createAdminCategory({
        nombre: categoryName.trim(),
        descripcion: categoryDescription.trim() || `Servicios de ${categoryName.trim()}`,
      });
      setCategoryName("");
      setCategoryDescription("");
      await loadUsers();
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo crear la categoria");
    } finally {
      setActionLoading("");
    }
  };

  const changeCategoryState = async (category: ApiCategory, active: boolean) => {
    const motivo = window.prompt(active ? "Motivo para reactivar la categoria" : "Motivo para desactivar la categoria");
    if (!motivo?.trim()) return;
    setActionLoading(`${category.id}-${active ? "ACTIVA" : "INACTIVA"}`);
    setError("");
    try {
      await servifyApi.changeAdminCategoryState(category.id, {
        estadoDestino: active ? "ACTIVA" : "INACTIVA",
        motivo: motivo.trim(),
      });
      await loadUsers();
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo actualizar la categoria");
    } finally {
      setActionLoading("");
    }
  };

  const changeUserState = async (targetUser: AdminUserView, nuevoEstado: ApiUserState) => {
    const motivo = window.prompt(`Motivo para cambiar la cuenta a ${nuevoEstado.toLowerCase()}`);
    if (!motivo?.trim()) return;
    setActionLoading(`${targetUser.id}-${nuevoEstado}`);
    setError("");
    try {
      await servifyApi.changeAdminUserState(targetUser.id, { nuevoEstado, motivo: motivo.trim() });
      await loadUsers();
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo actualizar el usuario");
    } finally {
      setActionLoading("");
    }
  };

  const moderatePublication = async (
    owner: AdminUserView,
    publication: ApiPublication,
    estadoDestino: ApiPublicationState
  ) => {
    const motivo = window.prompt(`Motivo para cambiar la publicacion a ${estadoDestino.toLowerCase()}`);
    if (!motivo?.trim()) return;
    setActionLoading(`${publication.id}-${estadoDestino}`);
    setError("");
    try {
      await servifyApi.moderatePublication(publication.id, { estadoDestino, motivo: motivo.trim() });
      setSelectedPublication(null);
      await loadUsers();
      setSelectedUserId(owner.id);
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo moderar la publicacion");
    } finally {
      setActionLoading("");
    }
  };

  const openProviderProfile = (targetUser: AdminUserView) => {
    onProviderPress(toPublicProvider(targetUser));
  };

  const goBackToUsers = () => {
    setSelectedUserId("");
    setSelectedPublication(null);
  };

  if (selectedUser) {
    return (
      <AdminUserDetail
        adminUserId={adminUser?.id}
        user={selectedUser}
        publication={selectedPublication}
        error={error}
        loading={loading}
        actionLoading={actionLoading}
        onBack={goBackToUsers}
        onOpenPublication={setSelectedPublication}
        onClosePublication={() => setSelectedPublication(null)}
        onOpenProviderProfile={() => openProviderProfile(selectedUser)}
        onSuspend={() => changeUserState(selectedUser, "SUSPENDIDO")}
        onBlock={() => changeUserState(selectedUser, "BLOQUEADO")}
        onReactivate={() => changeUserState(selectedUser, "ACTIVO")}
        onBlockPublication={(publication) => moderatePublication(selectedUser, publication, "BLOQUEADA")}
        onReactivatePublication={(publication) => moderatePublication(selectedUser, publication, "ACTIVA")}
        onDeletePublication={(publication) => moderatePublication(selectedUser, publication, "ELIMINADA")}
      />
    );
  }

  return (
    <div className="servify-dark-screen flex h-full flex-col" style={{ background: "#f8fafc" }}>
      <div className="servify-page-header bg-white px-5 pb-5 pt-12">
        <div className="flex items-center gap-3">
          <button
            type="button"
            onClick={onBack}
            className="flex items-center justify-center rounded-xl"
            style={{ width: 38, height: 38, background: "#f1f5f9" }}
          >
            <ArrowLeft size={18} color="#475569" strokeWidth={2} />
          </button>
          <div className="min-w-0 flex-1">
            <p style={{ fontSize: 12, color: "#64748b", fontWeight: 800 }}>Servify</p>
            <h1 style={{ fontSize: 22, color: "#0f172a", fontWeight: 900, lineHeight: 1.15 }}>
              Moderacion
            </h1>
          </div>
          <div
            className="flex items-center justify-center rounded-2xl"
            style={{ width: 42, height: 42, background: "#eff6ff", color: "#2563eb" }}
          >
            <Shield size={20} strokeWidth={2.1} />
          </div>
        </div>

        <div
          className="mt-4 flex items-center gap-3 rounded-2xl px-4 py-3"
          style={{ background: "#f1f5f9", border: "1.5px solid #e2e8f0" }}
        >
          <Search size={18} color="#94a3b8" strokeWidth={1.8} />
          <input
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            placeholder="Buscar usuario, email o @usuario"
            className="min-w-0 flex-1 bg-transparent outline-none"
            style={{ fontSize: 14, color: "#0f172a" }}
          />
        </div>
      </div>

      <div className="flex-1 overflow-y-auto px-5 pb-7 pt-4">
        <CategoryAdminPanel
          categories={categories}
          categoryName={categoryName}
          categoryDescription={categoryDescription}
          loading={actionLoading}
          onNameChange={setCategoryName}
          onDescriptionChange={setCategoryDescription}
          onCreate={createCategory}
          onActivate={(category) => changeCategoryState(category, true)}
          onDeactivate={(category) => changeCategoryState(category, false)}
        />

        <div className="mb-4 flex items-center justify-between gap-3">
          <div>
            <p style={{ color: "#0f172a", fontSize: 18, fontWeight: 900 }}>Usuarios</p>
            <p style={{ color: "#64748b", fontSize: 12, fontWeight: 700 }}>
              Ordenados de peor calificacion a mejor
            </p>
          </div>
          <span className="servify-chip rounded-full px-3 py-1.5" style={{ background: "#eff6ff", color: "#2563eb", fontSize: 11, fontWeight: 900 }}>
            {filteredUsers.length} resultado{filteredUsers.length === 1 ? "" : "s"}
          </span>
        </div>

        {error ? <ErrorNotice message={error} /> : null}

        {loading ? (
          <p style={{ color: "#64748b", fontSize: 13, fontWeight: 800 }}>Cargando usuarios...</p>
        ) : null}

        {!loading && filteredUsers.length === 0 ? (
          <div className="servify-empty-state rounded-3xl px-5 py-10 text-center">
            <UserRound size={34} color="#94a3b8" strokeWidth={1.7} />
            <p style={{ color: "#0f172a", fontSize: 16, fontWeight: 900, marginTop: 12 }}>
              No hay usuarios para mostrar
            </p>
            <p style={{ color: "#64748b", fontSize: 13, fontWeight: 700, lineHeight: 1.4, marginTop: 5 }}>
              Cambia la busqueda o revisa que el backend este cargando usuarios.
            </p>
          </div>
        ) : null}

        <div className="flex flex-col gap-3">
          {filteredUsers.map((user, index) => (
            <UserRiskCard
              key={user.id}
              user={user}
              index={index}
              onClick={() => {
                setSelectedUserId(user.id);
                setSelectedPublication(null);
              }}
            />
          ))}
        </div>
      </div>
    </div>
  );
}

function CategoryAdminPanel({
  categories,
  categoryName,
  categoryDescription,
  loading,
  onNameChange,
  onDescriptionChange,
  onCreate,
  onActivate,
  onDeactivate,
}: {
  categories: ApiCategory[];
  categoryName: string;
  categoryDescription: string;
  loading: string;
  onNameChange: (value: string) => void;
  onDescriptionChange: (value: string) => void;
  onCreate: () => void;
  onActivate: (category: ApiCategory) => void;
  onDeactivate: (category: ApiCategory) => void;
}) {
  return (
    <section
      className="servify-card servify-request-card mb-5 rounded-2xl bg-white p-4"
      style={{ border: "1px solid rgba(0,0,0,0.06)", boxShadow: "0 1px 4px rgba(0,0,0,0.04)" }}
    >
      <div className="mb-3 flex items-start justify-between gap-3">
        <div>
          <h2 style={{ color: "#0f172a", fontSize: 17, fontWeight: 900 }}>Categorias</h2>
          <p style={{ color: "#64748b", fontSize: 12, fontWeight: 700 }}>
            Alta y baja logica para el catalogo de servicios
          </p>
        </div>
        <span className="servify-chip rounded-full px-3 py-1.5" style={{ background: "#ecfeff", color: "#0891b2", fontSize: 11, fontWeight: 900 }}>
          {categories.length}
        </span>
      </div>

      <div className="grid grid-cols-1 gap-2">
        <input
          value={categoryName}
          onChange={(event) => onNameChange(event.target.value)}
          placeholder="Nueva categoria"
          className="servify-form-surface rounded-xl px-3 py-2.5 outline-none"
          style={{ border: "1.5px solid #e2e8f0", color: "#0f172a", fontSize: 13, fontWeight: 700 }}
        />
        <input
          value={categoryDescription}
          onChange={(event) => onDescriptionChange(event.target.value)}
          placeholder="Descripcion opcional"
          className="servify-form-surface rounded-xl px-3 py-2.5 outline-none"
          style={{ border: "1.5px solid #e2e8f0", color: "#0f172a", fontSize: 13, fontWeight: 700 }}
        />
        <button
          type="button"
          onClick={onCreate}
          disabled={loading === "category-create"}
          className="servify-action-button servify-action-teal flex items-center justify-center gap-1.5 rounded-xl py-2.5 transition-all active:scale-95"
          style={{ fontSize: 12, fontWeight: 900, opacity: loading === "category-create" ? 0.65 : 1 }}
        >
          <Plus size={14} strokeWidth={2.2} />
          {loading === "category-create" ? "Agregando..." : "Agregar categoria"}
        </button>
      </div>

      <div className="mt-4 flex max-h-60 flex-col gap-2 overflow-y-auto">
        {categories.length === 0 ? (
          <p className="rounded-2xl px-4 py-3" style={{ background: "#f8fafc", color: "#64748b", fontSize: 13, fontWeight: 800 }}>
            No hay categorias cargadas.
          </p>
        ) : null}
        {categories.map((category) => {
          const active = (category.estado ?? "").toUpperCase() === "ACTIVA";
          return (
            <div
              key={category.id}
              className="servify-form-surface flex items-center justify-between gap-3 rounded-2xl px-3 py-3"
              style={{ background: "#f8fafc", border: "1px solid #e2e8f0" }}
            >
              <div className="min-w-0">
                <p style={{ color: "#0f172a", fontSize: 13, fontWeight: 900 }}>{category.nombre}</p>
                <p style={{ color: active ? "#16a34a" : "#94a3b8", fontSize: 11, fontWeight: 900, marginTop: 2 }}>
                  {active ? "Activa" : "Inactiva"}
                </p>
              </div>
              {active ? (
                <button
                  type="button"
                  onClick={() => onDeactivate(category)}
                  disabled={loading.startsWith(category.id)}
                  className="servify-action-button servify-action-warning flex items-center gap-1 rounded-xl px-3 py-2 transition-all active:scale-95"
                  style={{ fontSize: 11, fontWeight: 900, opacity: loading.startsWith(category.id) ? 0.55 : 1 }}
                >
                  <Trash2 size={13} strokeWidth={2.2} />
                  Desactivar
                </button>
              ) : (
                <button
                  type="button"
                  onClick={() => onActivate(category)}
                  disabled={loading.startsWith(category.id)}
                  className="servify-action-button servify-action-success flex items-center gap-1 rounded-xl px-3 py-2 transition-all active:scale-95"
                  style={{ fontSize: 11, fontWeight: 900, opacity: loading.startsWith(category.id) ? 0.55 : 1 }}
                >
                  <RefreshCcw size={13} strokeWidth={2.2} />
                  Reactivar
                </button>
              )}
            </div>
          );
        })}
      </div>
    </section>
  );
}

function AdminUserDetail({
  adminUserId,
  user,
  publication,
  error,
  loading,
  actionLoading,
  onBack,
  onOpenPublication,
  onClosePublication,
  onOpenProviderProfile,
  onSuspend,
  onBlock,
  onReactivate,
  onBlockPublication,
  onReactivatePublication,
  onDeletePublication,
}: {
  adminUserId?: string;
  user: AdminUserView;
  publication: ApiPublication | null;
  error: string;
  loading: boolean;
  actionLoading: string;
  onBack: () => void;
  onOpenPublication: (publication: ApiPublication) => void;
  onClosePublication: () => void;
  onOpenProviderProfile: () => void;
  onSuspend: () => void;
  onBlock: () => void;
  onReactivate: () => void;
  onBlockPublication: (publication: ApiPublication) => void;
  onReactivatePublication: (publication: ApiPublication) => void;
  onDeletePublication: (publication: ApiPublication) => void;
}) {
  const isSelf = adminUserId === user.id;
  const active = user.estado === "ACTIVO";

  return (
    <div className="servify-dark-screen flex h-full flex-col" style={{ background: "#f8fafc" }}>
      <div className="servify-page-header bg-white px-5 pb-5 pt-12">
        <div className="flex items-center gap-3">
          <button
            type="button"
            onClick={onBack}
            className="flex items-center justify-center rounded-xl"
            style={{ width: 38, height: 38, background: "#f1f5f9" }}
          >
            <ArrowLeft size={18} color="#475569" strokeWidth={2} />
          </button>
          <div className="min-w-0 flex-1">
            <p style={{ color: "#64748b", fontSize: 12, fontWeight: 800 }}>Usuario</p>
            <h1 style={{ color: "#0f172a", fontSize: 21, fontWeight: 900, lineHeight: 1.15 }}>
              {user.displayName}
            </h1>
          </div>
          <StatusBadge label={user.estado ?? "SIN_ESTADO"} />
        </div>
      </div>

      <div className="flex-1 overflow-y-auto px-5 pb-7 pt-4">
        {error ? <ErrorNotice message={error} /> : null}

        <section
          className="servify-card servify-request-card rounded-2xl bg-white p-4"
          style={{ border: "1px solid rgba(0,0,0,0.06)", boxShadow: "0 1px 4px rgba(0,0,0,0.04)" }}
        >
          <div className="flex items-start gap-3">
            <Avatar user={user} size={56} />
            <div className="min-w-0 flex-1">
              <p style={{ color: "#0f172a", fontSize: 16, fontWeight: 900, lineHeight: 1.2 }}>
                {user.displayName}
              </p>
              <p style={{ color: "#0f766e", fontSize: 12, fontWeight: 900, marginTop: 3 }}>
                {user.nombreUsuario ? `@${user.nombreUsuario}` : "Sin usuario publico"}
              </p>
              <div className="mt-3 flex flex-wrap gap-2">
                <Metric icon={<Star size={13} />} label={ratingText(user.rating)} />
                <Metric icon={<Briefcase size={13} />} label={`${user.activePublicationCount} activas`} />
                <Metric icon={<Mail size={13} />} label={user.email ?? "Sin email"} />
              </div>
            </div>
          </div>

          <div className="servify-card-footer mt-4 grid grid-cols-2 gap-2 pt-3" style={{ borderTop: "1px solid #f1f5f9" }}>
            <ActionButton
              label="Ver perfil"
              icon={<Eye size={13} />}
              variant="teal"
              onClick={onOpenProviderProfile}
            />
            <ActionButton
              label="Reactivar"
              icon={<CheckCircle2 size={13} />}
              variant="success"
              disabled={loading || active}
              onClick={onReactivate}
            />
          </div>
          <div className="mt-2 grid grid-cols-2 gap-2">
            <ActionButton
              label="Suspender"
              icon={<UserX size={13} />}
              variant="warning"
              disabled={loading || !active || isSelf}
              onClick={onSuspend}
            />
            <ActionButton
              label="Bloquear"
              icon={<Ban size={13} />}
              variant="danger"
              disabled={loading || user.estado === "BLOQUEADO" || isSelf}
              onClick={onBlock}
            />
          </div>
          {isSelf ? (
            <p style={{ color: "#94a3b8", fontSize: 11, fontWeight: 800, marginTop: 10 }}>
              Tu propia cuenta administradora no puede suspenderse ni bloquearse desde este panel.
            </p>
          ) : null}
        </section>

        {publication ? (
          <PublicationDetailPanel
            owner={user}
            publication={publication}
            loading={actionLoading.startsWith(publication.id)}
            onClose={onClosePublication}
            onOpenProviderProfile={onOpenProviderProfile}
            onBlock={() => onBlockPublication(publication)}
            onReactivate={() => onReactivatePublication(publication)}
            onDelete={() => onDeletePublication(publication)}
          />
        ) : null}

        <div className="mt-5">
          <div className="mb-3 flex items-center justify-between gap-3">
            <div>
              <h2 style={{ color: "#0f172a", fontSize: 17, fontWeight: 900 }}>Publicaciones</h2>
              <p style={{ color: "#64748b", fontSize: 12, fontWeight: 700 }}>
                Click para ver detalle completo y moderar
              </p>
            </div>
            <span style={{ color: "#94a3b8", fontSize: 12, fontWeight: 900 }}>{user.publications.length}</span>
          </div>

          {user.publications.length === 0 ? (
            <p className="rounded-2xl px-4 py-3" style={{ background: "white", color: "#64748b", fontSize: 13, fontWeight: 700 }}>
              Este usuario todavia no tiene publicaciones.
            </p>
          ) : null}

          <div className="flex flex-col gap-3">
            {user.publications.map((item, index) => (
              <PublicationModerationCard
                key={item.id}
                owner={user}
                publication={item}
                index={index}
                loading={actionLoading.startsWith(item.id)}
                onOpen={() => onOpenPublication(item)}
                onBlock={() => onBlockPublication(item)}
                onReactivate={() => onReactivatePublication(item)}
                onDelete={() => onDeletePublication(item)}
              />
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}

function UserRiskCard({ user, index, onClick }: { user: AdminUserView; index: number; onClick: () => void }) {
  return (
    <motion.button
      type="button"
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ delay: index * 0.025, duration: 0.2 }}
      onClick={onClick}
      className="servify-card servify-request-card w-full rounded-2xl bg-white p-4 text-left transition-all active:scale-[0.98]"
      style={{ border: "1px solid rgba(0,0,0,0.06)", boxShadow: "0 1px 4px rgba(0,0,0,0.04)" }}
    >
      <div className="flex items-start gap-3">
        <Avatar user={user} size={50} />
        <div className="min-w-0 flex-1">
          <div className="flex items-start justify-between gap-2">
            <div className="min-w-0">
              <p style={{ color: "#0f172a", fontSize: 15, fontWeight: 900, lineHeight: 1.2 }}>
                {user.displayName}
              </p>
              <p style={{ color: "#0f766e", fontSize: 12, fontWeight: 900, marginTop: 3 }}>
                {user.nombreUsuario ? `@${user.nombreUsuario}` : user.email}
              </p>
            </div>
            <ChevronRight size={18} color="#cbd5e1" strokeWidth={2} />
          </div>
          <div className="mt-3 flex flex-wrap items-center gap-2">
            <Metric icon={<Star size={13} />} label={ratingText(user.rating)} tone={ratingTone(user.rating)} />
            <Metric icon={<Briefcase size={13} />} label={`${user.activePublicationCount} activas`} />
            <StatusBadge label={user.estado ?? "SIN_ESTADO"} />
          </div>
          <p style={{ color: "#64748b", fontSize: 12, fontWeight: 700, marginTop: 8 }}>
            {user.publications.length} publicacion{user.publications.length === 1 ? "" : "es"} total{user.publications.length === 1 ? "" : "es"} - ID {shortId(user.id)}
          </p>
        </div>
      </div>
    </motion.button>
  );
}

function PublicationModerationCard({
  owner,
  publication,
  index,
  loading,
  onOpen,
  onBlock,
  onReactivate,
  onDelete,
}: {
  owner: AdminUserView;
  publication: ApiPublication;
  index: number;
  loading: boolean;
  onOpen: () => void;
  onBlock: () => void;
  onReactivate: () => void;
  onDelete: () => void;
}) {
  const blocked = publication.estado === "BLOQUEADA";
  const deleted = publication.estado === "ELIMINADA";

  return (
    <motion.article
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ delay: index * 0.025, duration: 0.2 }}
      className="servify-card servify-publication-card rounded-2xl bg-white"
      style={{ border: "1px solid rgba(0,0,0,0.06)", boxShadow: "0 1px 4px rgba(0,0,0,0.04)" }}
    >
      <button type="button" onClick={onOpen} className="w-full p-4 text-left">
        <div className="flex items-start justify-between gap-2">
          <div className="min-w-0">
            <p style={{ color: "#0f172a", fontSize: 15, fontWeight: 900, lineHeight: 1.25 }}>
              {publication.titulo}
            </p>
            <p style={{ color: "#64748b", fontSize: 12, fontWeight: 700, marginTop: 4 }}>
              Dueño: {owner.displayName}
            </p>
          </div>
          <StatusBadge label={publication.estado ?? "SIN_ESTADO"} />
        </div>
        <p style={{ color: "#64748b", fontSize: 12, lineHeight: 1.45, fontWeight: 600, marginTop: 9 }}>
          {publication.descripcion}
        </p>
        <div className="mt-3 flex flex-wrap gap-2">
          <Metric icon={<Briefcase size={13} />} label={publication.categoriaServicio?.nombre ?? "Sin categoria"} />
          <Metric icon={<MapPin size={13} />} label={formatAreas(publication)} />
          <Metric icon={<DollarSign size={13} />} label={formatMoney(publication.precioBase)} />
        </div>
      </button>

      <div className="servify-card-footer grid grid-cols-3 gap-2 px-4 py-3" style={{ borderTop: "1px solid #f1f5f9" }}>
        <ActionButton label="Bloquear" icon={<Ban size={13} />} variant="danger" disabled={loading || blocked || deleted} onClick={onBlock} />
        <ActionButton label="Reactivar" icon={<RefreshCcw size={13} />} variant="success" disabled={loading || !blocked} onClick={onReactivate} />
        <ActionButton label="Eliminar" icon={<FileWarning size={13} />} variant="warning" disabled={loading || deleted} onClick={onDelete} />
      </div>
    </motion.article>
  );
}

function PublicationDetailPanel({
  owner,
  publication,
  loading,
  onClose,
  onOpenProviderProfile,
  onBlock,
  onReactivate,
  onDelete,
}: {
  owner: AdminUserView;
  publication: ApiPublication;
  loading: boolean;
  onClose: () => void;
  onOpenProviderProfile: () => void;
  onBlock: () => void;
  onReactivate: () => void;
  onDelete: () => void;
}) {
  const blocked = publication.estado === "BLOQUEADA";
  const deleted = publication.estado === "ELIMINADA";

  return (
    <motion.section
      initial={{ opacity: 0, y: 12 }}
      animate={{ opacity: 1, y: 0 }}
      className="servify-card servify-publication-card mt-4 rounded-2xl bg-white p-4"
      style={{ border: "1px solid rgba(37,99,235,0.2)", boxShadow: "0 14px 34px rgba(15,23,42,0.12)" }}
    >
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0">
          <p style={{ color: "#64748b", fontSize: 12, fontWeight: 800 }}>Detalle de publicacion</p>
          <h2 style={{ color: "#0f172a", fontSize: 17, fontWeight: 900, lineHeight: 1.2, marginTop: 3 }}>
            {publication.titulo}
          </h2>
        </div>
        <button
          type="button"
          onClick={onClose}
          className="rounded-xl px-3 py-2"
          style={{ background: "#f1f5f9", color: "#475569", fontSize: 12, fontWeight: 900 }}
        >
          Cerrar
        </button>
      </div>

      <div className="mt-3 grid grid-cols-2 gap-2">
        <DetailLine label="Dueño" value={owner.displayName} />
        <DetailLine label="Usuario" value={owner.nombreUsuario ? `@${owner.nombreUsuario}` : "Sin usuario"} />
        <DetailLine label="Estado" value={publication.estado ?? "Sin estado"} />
        <DetailLine label="Categoria" value={publication.categoriaServicio?.nombre ?? "Sin categoria"} />
        <DetailLine label="Modalidad" value={fromApiModality(publication.modalidadServicio)} />
        <DetailLine label="Precio" value={formatMoney(publication.precioBase)} />
        <DetailLine label="Zonas" value={formatAreas(publication)} />
        <DetailLine label="Disponibilidad" value={formatAvailability(publication)} />
        <DetailLine label="Creada" value={formatDate(publication.fechaCreacion)} />
        <DetailLine label="Actualizada" value={formatDate(publication.fechaUltimaModificacion)} />
      </div>

      <div className="mt-3 rounded-2xl p-3" style={{ background: "#f8fafc", border: "1px solid #e2e8f0" }}>
        <p style={{ color: "#94a3b8", fontSize: 11, fontWeight: 900, marginBottom: 6 }}>DESCRIPCION</p>
        <p style={{ color: "#475569", fontSize: 13, lineHeight: 1.45, fontWeight: 700 }}>
          {publication.descripcion || "Sin descripcion"}
        </p>
      </div>

      <div className="mt-3 flex flex-wrap gap-2">
        <Metric icon={<Clock size={13} />} label={`ID ${shortId(publication.id)}`} />
        <Metric
          icon={<CheckCircle2 size={13} />}
          label={publication.puedeParticiparEnDistribucion ? "Distribuible" : "No distribuible"}
        />
      </div>

      <div className="servify-card-footer mt-4 grid grid-cols-2 gap-2 pt-3" style={{ borderTop: "1px solid #f1f5f9" }}>
        <ActionButton label="Ver perfil" icon={<Eye size={13} />} variant="teal" onClick={onOpenProviderProfile} />
        <ActionButton label="Bloquear" icon={<Ban size={13} />} variant="danger" disabled={loading || blocked || deleted} onClick={onBlock} />
        <ActionButton label="Reactivar" icon={<RefreshCcw size={13} />} variant="success" disabled={loading || !blocked} onClick={onReactivate} />
        <ActionButton label="Eliminar" icon={<FileWarning size={13} />} variant="warning" disabled={loading || deleted} onClick={onDelete} />
      </div>
    </motion.section>
  );
}

function Avatar({ user, size }: { user: AdminUserView; size: number }) {
  const photo = servifyApi.getStoredProfilePhoto(user.id) || user.profile?.fotoPerfilUrl;
  return (
    <div
      className="flex shrink-0 items-center justify-center overflow-hidden rounded-2xl"
      style={{ width: size, height: size, background: "#ecfdf5" }}
    >
      {photo ? (
        <img src={photo} alt="" className="h-full w-full object-cover" />
      ) : (
        <UserRound size={Math.round(size * 0.46)} color="#0f766e" strokeWidth={1.8} />
      )}
    </div>
  );
}

function Metric({ icon, label, tone = "neutral" }: { icon: ReactNode; label: string; tone?: "neutral" | "warning" | "success" }) {
  const colors = {
    neutral: { bg: "#f1f5f9", fg: "#475569" },
    warning: { bg: "#fffbeb", fg: "#d97706" },
    success: { bg: "#f0fdf4", fg: "#16a34a" },
  }[tone];

  return (
    <span className="servify-chip inline-flex max-w-full items-center gap-1.5 rounded-full px-2.5 py-1" style={{ background: colors.bg, color: colors.fg, fontSize: 11, fontWeight: 900 }}>
      {icon}
      <span className="truncate">{label}</span>
    </span>
  );
}

function DetailLine({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-2xl px-3 py-2" style={{ background: "#f8fafc", border: "1px solid #e2e8f0" }}>
      <p style={{ color: "#94a3b8", fontSize: 10, fontWeight: 900 }}>{label}</p>
      <p style={{ color: "#0f172a", fontSize: 12, fontWeight: 900, lineHeight: 1.25, marginTop: 3 }}>
        {value}
      </p>
    </div>
  );
}

function ActionButton({
  label,
  icon,
  variant,
  disabled,
  onClick,
}: {
  label: string;
  icon: ReactNode;
  variant: "danger" | "success" | "warning" | "teal";
  disabled?: boolean;
  onClick: () => void;
}) {
  return (
    <button
      type="button"
      disabled={disabled}
      onClick={onClick}
      className={`servify-action-button servify-action-${variant} flex items-center justify-center gap-1 rounded-xl py-2 transition-all active:scale-95`}
      style={{ fontSize: 11, fontWeight: 900, opacity: disabled ? 0.45 : 1 }}
    >
      {icon}
      {label}
    </button>
  );
}

function StatusBadge({ label }: { label: string }) {
  return (
    <span className="servify-status-badge rounded-full px-2.5 py-1" style={{ background: "#f1f5f9", color: "#475569", fontSize: 10, fontWeight: 900 }}>
      {label}
    </span>
  );
}

function ErrorNotice({ message }: { message: string }) {
  return (
    <p className="mb-3 rounded-2xl px-4 py-3" style={{ background: "#fef2f2", color: "#b91c1c", fontSize: 13, fontWeight: 800 }}>
      {message}
    </p>
  );
}

async function enrichAdminUser(user: ApiAdminUser): Promise<AdminUserView> {
  const [profile, rating, publications] = await Promise.all([
    servifyApi.getUserProfile(user.id).catch(() => null),
    servifyApi.getUserRatingSummary(user.id).catch(() => emptyRating(user.id)),
    servifyApi.listUserPublications(user.id).catch(() => []),
  ]);
  const activePublications = publications.filter((publication) => (publication.estado ?? "").toUpperCase() === "ACTIVA");
  const displayName = formatAdminUserName(user, profile);
  const searchText = normalize([
    displayName,
    user.nombreUsuario,
    user.email,
    user.telefono,
    user.estado,
    user.rol,
  ].filter(Boolean).join(" "));

  return {
    ...user,
    profile,
    rating,
    publications,
    activePublicationCount: activePublications.length,
    displayName,
    searchText,
  };
}

function toPublicProvider(user: AdminUserView): ApiPublicProvider {
  const activePublications = user.publications.filter((publication) => (publication.estado ?? "").toUpperCase() === "ACTIVA");
  const zonasCobertura = uniqueValues(activePublications.flatMap(publicationAreas));
  const prices = activePublications
    .map((publication) => publication.precioBase)
    .filter((price): price is number => Boolean(price && price > 0))
    .sort((a, b) => a - b);

  return {
    usuarioId: user.id,
    nombreUsuario: user.nombreUsuario ?? `usuario-${shortId(user.id)}`,
    nombre: user.profile?.nombre,
    apellido: user.profile?.apellido,
    fotoPerfilUrl: servifyApi.getStoredProfilePhoto(user.id) || user.profile?.fotoPerfilUrl,
    descripcionPersonal: user.profile?.descripcionPersonal,
    localidad: user.profile?.ubicacion?.localidad ?? user.profile?.ubicacion?.ciudad,
    cantidadPublicacionesActivas: activePublications.length,
    categorias: uniqueValues(activePublications.map((publication) => publication.categoriaServicio?.nombre ?? "")),
    servicios: activePublications.map((publication) => publication.titulo).filter(Boolean),
    zonasCobertura,
    precioDesde: prices[0],
    publicacionesActivas: activePublications.map((publication) => ({
      id: publication.id,
      titulo: publication.titulo,
      descripcion: publication.descripcion,
      categoria: publication.categoriaServicio?.nombre,
      modalidadServicio: publication.modalidadServicio,
      zonasCobertura: publicationAreas(publication),
      precioBase: publication.precioBase,
    })),
    cantidadValoraciones: user.rating.cantidadValoraciones,
    promedioEstrellas: user.rating.promedioEstrellas,
  };
}

function uniqueById(users: ApiAdminUser[]): ApiAdminUser[] {
  const map = new Map<string, ApiAdminUser>();
  users.forEach((user) => map.set(user.id, { ...map.get(user.id), ...user }));
  return Array.from(map.values());
}

function compareUsersByRisk(a: AdminUserView, b: AdminUserView): number {
  const aRating = ratingRank(a.rating);
  const bRating = ratingRank(b.rating);
  if (aRating !== bRating) return aRating - bRating;
  const stateDiff = stateRank(a.estado) - stateRank(b.estado);
  if (stateDiff !== 0) return stateDiff;
  return a.displayName.localeCompare(b.displayName, "es");
}

function ratingRank(rating: ApiRatingSummary): number {
  if (!rating.cantidadValoraciones) return Number.POSITIVE_INFINITY;
  return rating.promedioEstrellas;
}

function stateRank(state?: ApiUserState): number {
  if (state === "BLOQUEADO") return 0;
  if (state === "SUSPENDIDO") return 1;
  if (state === "INACTIVO") return 2;
  return 3;
}

function formatAdminUserName(user: ApiAdminUser, profile?: ApiUserProfile | null): string {
  const profileName = [profile?.nombre, profile?.apellido].filter(Boolean).join(" ").trim();
  if (profileName) return profileName;
  if (user.nombreUsuario) return `@${user.nombreUsuario}`;
  return user.email ?? `Usuario ${shortId(user.id)}`;
}

function ratingText(rating: ApiRatingSummary): string {
  if (!rating.cantidadValoraciones) return "Sin calificaciones";
  return `${rating.promedioEstrellas.toFixed(1)} (${rating.cantidadValoraciones})`;
}

function ratingTone(rating: ApiRatingSummary): "neutral" | "warning" | "success" {
  if (!rating.cantidadValoraciones) return "neutral";
  if (rating.promedioEstrellas < 3.5) return "warning";
  return "success";
}

function formatAreas(publication: ApiPublication): string {
  const areas = publicationAreas(publication);
  if (areas.length <= 2) return areas.join(", ");
  return `${areas.slice(0, 2).join(", ")} +${areas.length - 2}`;
}

function publicationAreas(publication: ApiPublication): string[] {
  const areas = (publication.zonasCobertura?.length ? publication.zonasCobertura : publication.ubicacion ? [publication.ubicacion] : [])
    .map((zona) => zona.localidad || zona.ciudad || "CABA")
    .filter(Boolean);
  return uniqueValues(areas.length ? areas : ["CABA"]);
}

function formatAvailability(publication: ApiPublication): string {
  const availability = publication.disponibilidadesHorarias?.[0];
  if (!availability) return "Horario a coordinar";
  const day = WEEK_DAYS.find((item) => item.value === availability.diaSemana)?.label ?? availability.diaSemana;
  return `${day} ${availability.horaDesde.slice(0, 5)}-${availability.horaHasta.slice(0, 5)}`;
}

function formatDate(value?: string): string {
  if (!value) return "Sin fecha";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "Sin fecha";
  return date.toLocaleDateString("es-AR", { day: "2-digit", month: "2-digit", year: "2-digit" });
}

function uniqueValues(values: string[]): string[] {
  return Array.from(new Set(values.map((value) => value.trim()).filter(Boolean)));
}

function shortId(id: string): string {
  return id.slice(0, 8);
}

function normalize(value: string): string {
  return value
    .toLowerCase()
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/^@/, "")
    .trim();
}
