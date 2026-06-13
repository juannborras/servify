import { useEffect, useState } from "react";
import type React from "react";
import { AnimatePresence, motion } from "motion/react";
import { Bell, ChevronRight, HelpCircle, Lock, LogOut, Moon, Shield, User, X } from "lucide-react";
import {
  ACCOUNT_ROLES,
  LOCATION_OPTIONS,
  servifyApi,
  type RoleType,
  type SessionUser,
} from "../api";

type SettingsView = "menu" | "account" | "appearance" | "notifications" | "privacy" | "help";
type AppearanceMode = "light" | "dark" | "system";

interface SettingsDrawerProps {
  open: boolean;
  user: SessionUser | null;
  onClose: () => void;
  onLogout: () => void;
  onOpenAdmin: () => void;
  onUserUpdated: (patch: Partial<SessionUser>) => void;
}

const SETTINGS_KEY = "servify.settings";

interface StoredSettings {
  appearance: AppearanceMode;
  notifications: {
    compatibleRequests: boolean;
    counterOffers: boolean;
    activitySummary: boolean;
  };
  privacy: {
    showLocation: boolean;
    showPrices: boolean;
    showAvailability: boolean;
  };
}

const defaultSettings: StoredSettings = {
  appearance: "light",
  notifications: {
    compatibleRequests: true,
    counterOffers: true,
    activitySummary: true,
  },
  privacy: {
    showLocation: true,
    showPrices: true,
    showAvailability: true,
  },
};

export function SettingsDrawer({ open, user, onClose, onLogout, onOpenAdmin, onUserUpdated }: SettingsDrawerProps) {
  const [view, setView] = useState<SettingsView>("menu");
  const [settings, setSettings] = useState<StoredSettings>(() => loadSettings());

  useEffect(() => {
    if (open) setView("menu");
  }, [open]);

  useEffect(() => {
    saveSettings(settings);
    applyAppearance(settings.appearance);

    if (settings.appearance !== "system") return;

    const query = window.matchMedia?.("(prefers-color-scheme: dark)");
    if (!query) return;

    const handleChange = () => applyAppearance("system");
    query.addEventListener?.("change", handleChange);
    return () => query.removeEventListener?.("change", handleChange);
  }, [settings]);

  const updateSettings = (patch: Partial<StoredSettings>) => {
    setSettings((current) => ({ ...current, ...patch }));
  };

  return (
    <AnimatePresence>
      {open ? (
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          transition={{ duration: 0.18 }}
          className="absolute inset-0 z-50 flex justify-end"
          style={{ background: "rgba(15,23,42,0.42)" }}
          onClick={onClose}
        >
          <motion.aside
            initial={{ x: "100%" }}
            animate={{ x: 0 }}
            exit={{ x: "100%" }}
            transition={{ type: "spring", damping: 30, stiffness: 280 }}
            className="servify-settings-panel h-full w-[86%] max-w-[340px] bg-white flex flex-col"
            style={{ boxShadow: "-18px 0 50px rgba(15,23,42,0.22)" }}
            onClick={(event) => event.stopPropagation()}
          >
            <div className="flex items-center justify-between px-5 pt-12 pb-4" style={{ borderBottom: "1px solid #e2e8f0" }}>
              <div>
                <AnimatePresence mode="wait" initial={false}>
                  <motion.p
                    key={view}
                    initial={{ opacity: 0, y: 8 }}
                    animate={{ opacity: 1, y: 0 }}
                    exit={{ opacity: 0, y: -8 }}
                    transition={{ duration: 0.16, ease: [0.16, 1, 0.3, 1] }}
                    style={{ color: "#0f172a", fontSize: 18, fontWeight: 900 }}
                  >
                    {viewTitle(view)}
                  </motion.p>
                </AnimatePresence>
                <p style={{ color: "#64748b", fontSize: 12, fontWeight: 700, marginTop: 2 }}>{user?.username ? `@${user.username}` : "Servify"}</p>
              </div>
              <motion.button
                whileTap={{ scale: 0.92 }}
                onClick={onClose}
                className="flex items-center justify-center rounded-xl"
                style={{ width: 36, height: 36, background: "#f1f5f9" }}
              >
                <X size={18} color="#475569" strokeWidth={2} />
              </motion.button>
            </div>

            <div className="flex-1 overflow-y-auto px-5 py-4">
              <AnimatePresence mode="wait" initial={false}>
                <motion.div
                  key={view}
                  initial={{ opacity: 0, x: 18, filter: "blur(2px)" }}
                  animate={{ opacity: 1, x: 0, filter: "blur(0px)" }}
                  exit={{ opacity: 0, x: -14, filter: "blur(2px)" }}
                  transition={{ duration: 0.2, ease: [0.16, 1, 0.3, 1] }}
                >
              {view === "menu" ? (
                <MenuView
                  showAdmin={user?.apiRole === "ADMIN"}
                  onSelect={setView}
                  onLogout={onLogout}
                  onOpenAdmin={onOpenAdmin}
                />
              ) : null}
              {view === "account" ? (
                <AccountSettings user={user} onBack={() => setView("menu")} onUserUpdated={onUserUpdated} />
              ) : null}
              {view === "appearance" ? (
                <AppearanceSettings
                  value={settings.appearance}
                  onBack={() => setView("menu")}
                  onChange={(appearance) => updateSettings({ appearance })}
                />
              ) : null}
              {view === "notifications" ? (
                <ToggleSettings
                  title="Notificaciones"
                  onBack={() => setView("menu")}
                  items={[
                    {
                      label: "Solicitudes compatibles",
                      description: "Avisos cuando aparece una solicitud para tus servicios.",
                      value: settings.notifications.compatibleRequests,
                      onChange: (value) => updateSettings({ notifications: { ...settings.notifications, compatibleRequests: value } }),
                    },
                    {
                      label: "Contraofertas",
                      description: "Avisos cuando una contraoferta necesita respuesta.",
                      value: settings.notifications.counterOffers,
                      onChange: (value) => updateSettings({ notifications: { ...settings.notifications, counterOffers: value } }),
                    },
                    {
                      label: "Resumen de actividad",
                      description: "Mostrar alertas resumidas en el inicio.",
                      value: settings.notifications.activitySummary,
                      onChange: (value) => updateSettings({ notifications: { ...settings.notifications, activitySummary: value } }),
                    },
                  ]}
                />
              ) : null}
              {view === "privacy" ? (
                <ToggleSettings
                  title="Privacidad"
                  onBack={() => setView("menu")}
                  items={[
                    {
                      label: "Mostrar zona",
                      description: "Tu perfil publico muestra zonas de cobertura.",
                      value: settings.privacy.showLocation,
                      onChange: (value) => updateSettings({ privacy: { ...settings.privacy, showLocation: value } }),
                    },
                    {
                      label: "Mostrar precios",
                      description: "Tu perfil publico muestra precio desde.",
                      value: settings.privacy.showPrices,
                      onChange: (value) => updateSettings({ privacy: { ...settings.privacy, showPrices: value } }),
                    },
                    {
                      label: "Mostrar disponibilidad",
                      description: "Tu perfil publico puede mostrar rangos horarios cuando esten publicados.",
                      value: settings.privacy.showAvailability,
                      onChange: (value) => updateSettings({ privacy: { ...settings.privacy, showAvailability: value } }),
                    },
                  ]}
                />
              ) : null}
              {view === "help" ? <HelpView onBack={() => setView("menu")} /> : null}
                </motion.div>
              </AnimatePresence>
            </div>
          </motion.aside>
        </motion.div>
      ) : null}
    </AnimatePresence>
  );
}

function MenuView({
  showAdmin,
  onSelect,
  onLogout,
  onOpenAdmin,
}: {
  showAdmin: boolean;
  onSelect: (view: SettingsView) => void;
  onLogout: () => void;
  onOpenAdmin: () => void;
}) {
  const items = [
    { icon: <User size={18} />, label: "Cuenta", detail: "Datos, usuario y perfil", view: "account" as SettingsView },
    { icon: <Moon size={18} />, label: "Apariencia", detail: "Modo claro, oscuro o sistema", view: "appearance" as SettingsView },
    { icon: <Bell size={18} />, label: "Notificaciones", detail: "Alertas de actividad", view: "notifications" as SettingsView },
    { icon: <Lock size={18} />, label: "Privacidad", detail: "Datos visibles en tu perfil", view: "privacy" as SettingsView },
    { icon: <HelpCircle size={18} />, label: "Ayuda", detail: "Soporte y version", view: "help" as SettingsView },
  ];

  return (
    <div className="flex flex-col gap-2">
      {items.map((item, index) => (
        <MenuItem
          key={item.view}
          icon={item.icon}
          label={item.label}
          detail={item.detail}
          index={index}
          onClick={() => onSelect(item.view)}
        />
      ))}
      {showAdmin ? (
        <MenuItem
          icon={<Shield size={18} />}
          label="Administracion"
          detail="Moderacion y cuentas"
          index={items.length}
          onClick={onOpenAdmin}
        />
      ) : null}
      <motion.button
        initial={{ opacity: 0, y: 10 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: items.length * 0.035, duration: 0.18 }}
        whileTap={{ scale: 0.98 }}
        onClick={onLogout}
        className="mt-4 w-full py-3 rounded-2xl flex items-center justify-center gap-2"
        style={{ background: "#fef2f2", color: "#dc2626", fontSize: 14, fontWeight: 900, border: "1px solid #fecaca" }}
      >
        <LogOut size={17} strokeWidth={2} />
        Cerrar sesion
      </motion.button>
    </div>
  );
}

function AccountSettings({ user, onBack, onUserUpdated }: { user: SessionUser | null; onBack: () => void; onUserUpdated: (patch: Partial<SessionUser>) => void }) {
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [localidad, setLocalidad] = useState(LOCATION_OPTIONS[0]);
  const [description, setDescription] = useState("");
  const [role, setRole] = useState<Exclude<RoleType, null>>("both");
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState("");

  useEffect(() => {
    if (!user) return;
    let cancelled = false;
    Promise.all([
      servifyApi.getAccountConfig(user.id).catch(() => null),
      servifyApi.getUserProfile(user.id).catch(() => null),
    ]).then(([account, profile]) => {
      if (cancelled) return;
      const prefs = servifyApi.getProfilePreferences(user.id);
      setFirstName(profile?.nombre ?? user.name.split(" ")[0] ?? "");
      setLastName(profile?.apellido ?? user.name.split(" ").slice(1).join(" ") ?? "");
      setUsername(account?.usuario.nombreUsuario ?? user.username ?? "");
      setEmail(prefs.email ?? account?.usuario.email ?? user.email ?? "");
      setLocalidad(profile?.ubicacion?.localidad ?? LOCATION_OPTIONS[0]);
      setDescription(profile?.descripcionPersonal ?? "");
      setRole((prefs.role ?? user.role ?? "both") as Exclude<RoleType, null>);
    });
    return () => {
      cancelled = true;
    };
  }, [user]);

  const save = async () => {
    if (!user) return;
    setSaving(true);
    setMessage("");
    try {
      await servifyApi.updateUserProfile(user.id, {
        nombre: firstName.trim() || "Usuario",
        apellido: lastName.trim() || "Servify",
        localidad,
        descripcionPersonal: description.trim(),
      });
      const account = await servifyApi.updateAccount(user.id, { nombreUsuario: username.trim() });
      servifyApi.saveProfilePreferences(user.id, { email: email.trim(), role });
      onUserUpdated({
        name: `${firstName.trim()} ${lastName.trim()}`.trim(),
        email: email.trim(),
        username: account.nombreUsuario,
        role,
      });
      setMessage("Cuenta actualizada");
    } catch (err) {
      setMessage(err instanceof Error ? err.message : "No se pudo guardar");
    } finally {
      setSaving(false);
    }
  };

  return (
    <SectionStack onBack={onBack}>
      <SettingsInput label="Nombre" value={firstName} onChange={setFirstName} />
      <SettingsInput label="Apellido" value={lastName} onChange={setLastName} />
      <SettingsInput label="Nombre de usuario" value={username} onChange={setUsername} />
      <SettingsInput label="Email" value={email} onChange={setEmail} />
      <SettingsSelect label="Localidad" value={localidad} onChange={setLocalidad} options={LOCATION_OPTIONS} />
      <SettingsTextarea label="Presentacion publica" value={description} onChange={setDescription} />
      <div>
        <p style={{ color: "#64748b", fontSize: 12, fontWeight: 800, marginBottom: 8 }}>Tipo de cuenta</p>
        <div className="grid grid-cols-3 gap-2">
          {ACCOUNT_ROLES.map((item) => (
            <button
              key={item.id}
              onClick={() => setRole(item.id)}
              className="py-2 rounded-xl"
              style={{
                background: role === item.id ? "#eff6ff" : "white",
                border: role === item.id ? "1.5px solid #2563eb" : "1px solid #cbd5e1",
                color: role === item.id ? "#2563eb" : "#475569",
                fontSize: 11,
                fontWeight: 900,
              }}
            >
              {item.label}
            </button>
          ))}
        </div>
      </div>
      {message ? <p style={{ color: message.includes("No se") || message.includes("uso") ? "#dc2626" : "#15803d", fontSize: 12, fontWeight: 800 }}>{message}</p> : null}
      <button onClick={save} disabled={saving} className="w-full py-3 rounded-2xl" style={{ background: "#2563eb", color: "white", fontSize: 14, fontWeight: 900 }}>
        {saving ? "Guardando..." : "Guardar cuenta"}
      </button>
    </SectionStack>
  );
}

function AppearanceSettings({ value, onChange, onBack }: { value: AppearanceMode; onChange: (value: AppearanceMode) => void; onBack: () => void }) {
  return (
    <SectionStack onBack={onBack}>
      {(["light", "dark", "system"] as AppearanceMode[]).map((mode) => (
        <motion.button
          key={mode}
          layout
          whileTap={{ scale: 0.98 }}
          onClick={() => onChange(mode)}
          className="w-full rounded-2xl px-4 py-3 text-left"
          style={{ background: value === mode ? "#eff6ff" : "white", border: value === mode ? "1.5px solid #2563eb" : "1px solid #e2e8f0" }}
        >
          <p style={{ color: value === mode ? "#2563eb" : "#0f172a", fontSize: 14, fontWeight: 900 }}>{appearanceLabel(mode)}</p>
        </motion.button>
      ))}
    </SectionStack>
  );
}

function ToggleSettings({ title, items, onBack }: { title: string; items: { label: string; description: string; value: boolean; onChange: (value: boolean) => void }[]; onBack: () => void }) {
  return (
    <SectionStack onBack={onBack}>
      <p style={{ color: "#64748b", fontSize: 13, fontWeight: 800 }}>{title}</p>
      {items.map((item, index) => (
        <motion.div
          key={item.label}
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: index * 0.035, duration: 0.18 }}
          className="flex items-center justify-between gap-3 rounded-2xl bg-white p-4"
          style={{ border: "1px solid #e2e8f0" }}
        >
          <div className="min-w-0">
            <p style={{ color: "#0f172a", fontSize: 14, fontWeight: 900 }}>{item.label}</p>
            <p style={{ color: "#64748b", fontSize: 12, fontWeight: 600, lineHeight: 1.35, marginTop: 2 }}>{item.description}</p>
          </div>
          <motion.button
            whileTap={{ scale: 0.92 }}
            onClick={() => item.onChange(!item.value)}
            className="rounded-full p-0.5 shrink-0"
            style={{ width: 44, height: 26, background: item.value ? "#2563eb" : "#cbd5e1" }}
          >
            <motion.span
              className="settings-toggle-thumb block rounded-full bg-white"
              animate={{ x: item.value ? 18 : 0 }}
              transition={{ type: "spring", damping: 18, stiffness: 360 }}
              style={{ width: 22, height: 22 }}
            />
          </motion.button>
        </motion.div>
      ))}
    </SectionStack>
  );
}

function HelpView({ onBack }: { onBack: () => void }) {
  return (
    <SectionStack onBack={onBack}>
      <InfoCard title="Soporte" text="Para reportar problemas, adjunta captura, usuario afectado y pasos para reproducir." />
      <InfoCard title="Version local" text="Servify MVP - entorno de desarrollo." />
      <InfoCard title="Datos" text="Los ajustes de notificaciones, privacidad y apariencia se guardan en este dispositivo." />
    </SectionStack>
  );
}

function SectionStack({ children, onBack }: { children: React.ReactNode; onBack: () => void }) {
  return (
    <div className="flex flex-col gap-3">
      <motion.button
        whileTap={{ scale: 0.96 }}
        onClick={onBack}
        className="self-start px-3 py-2 rounded-xl"
        style={{ background: "#f1f5f9", color: "#475569", fontSize: 12, fontWeight: 900 }}
      >
        Volver
      </motion.button>
      {children}
    </div>
  );
}

function MenuItem({ icon, label, detail, index, onClick }: { icon: React.ReactNode; label: string; detail: string; index: number; onClick: () => void }) {
  return (
    <motion.button
      initial={{ opacity: 0, x: 18 }}
      animate={{ opacity: 1, x: 0 }}
      transition={{ delay: index * 0.035, duration: 0.18 }}
      whileTap={{ scale: 0.98 }}
      onClick={onClick}
      className="w-full flex items-center gap-3 rounded-2xl bg-white p-4 text-left"
      style={{ border: "1px solid #e2e8f0" }}
    >
      <span className="flex items-center justify-center rounded-xl" style={{ width: 36, height: 36, background: "#eff6ff", color: "#2563eb" }}>{icon}</span>
      <span className="min-w-0 flex-1">
        <span className="block" style={{ color: "#0f172a", fontSize: 14, fontWeight: 900 }}>{label}</span>
        <span className="block" style={{ color: "#64748b", fontSize: 12, fontWeight: 600 }}>{detail}</span>
      </span>
      <ChevronRight size={17} color="#94a3b8" strokeWidth={2} />
    </motion.button>
  );
}

function SettingsInput({ label, value, onChange }: { label: string; value: string; onChange: (value: string) => void }) {
  return (
    <label className="flex flex-col gap-1">
      <span style={{ color: "#64748b", fontSize: 12, fontWeight: 800 }}>{label}</span>
      <input value={value} onChange={(event) => onChange(event.target.value)} className="rounded-xl px-3 py-2.5" style={{ border: "1px solid #cbd5e1", color: "#0f172a", fontSize: 13 }} />
    </label>
  );
}

function SettingsTextarea({ label, value, onChange }: { label: string; value: string; onChange: (value: string) => void }) {
  return (
    <label className="flex flex-col gap-1">
      <span style={{ color: "#64748b", fontSize: 12, fontWeight: 800 }}>{label}</span>
      <textarea
        value={value}
        onChange={(event) => onChange(event.target.value)}
        rows={3}
        className="rounded-xl px-3 py-2.5 resize-none"
        style={{ border: "1px solid #cbd5e1", color: "#0f172a", fontSize: 13 }}
      />
    </label>
  );
}

function SettingsSelect({ label, value, onChange, options }: { label: string; value: string; onChange: (value: string) => void; options: string[] }) {
  return (
    <label className="flex flex-col gap-1">
      <span style={{ color: "#64748b", fontSize: 12, fontWeight: 800 }}>{label}</span>
      <select value={value} onChange={(event) => onChange(event.target.value)} className="rounded-xl px-3 py-2.5" style={{ border: "1px solid #cbd5e1", color: "#0f172a", fontSize: 13, background: "white" }}>
        {options.map((option) => <option key={option} value={option}>{option}</option>)}
      </select>
    </label>
  );
}

function InfoCard({ title, text }: { title: string; text: string }) {
  return (
    <div className="rounded-2xl bg-white p-4" style={{ border: "1px solid #e2e8f0" }}>
      <p style={{ color: "#0f172a", fontSize: 14, fontWeight: 900 }}>{title}</p>
      <p style={{ color: "#64748b", fontSize: 12, fontWeight: 600, lineHeight: 1.4, marginTop: 3 }}>{text}</p>
    </div>
  );
}

function viewTitle(view: SettingsView): string {
  if (view === "account") return "Cuenta";
  if (view === "appearance") return "Apariencia";
  if (view === "notifications") return "Notificaciones";
  if (view === "privacy") return "Privacidad";
  if (view === "help") return "Ayuda";
  return "Configuracion";
}

function appearanceLabel(mode: AppearanceMode): string {
  if (mode === "dark") return "Modo oscuro";
  if (mode === "system") return "Usar configuracion del sistema";
  return "Modo claro";
}

function loadSettings(): StoredSettings {
  const raw = localStorage.getItem(SETTINGS_KEY);
  if (!raw) return defaultSettings;
  try {
    return { ...defaultSettings, ...JSON.parse(raw) } as StoredSettings;
  } catch {
    return defaultSettings;
  }
}

function saveSettings(settings: StoredSettings) {
  localStorage.setItem(SETTINGS_KEY, JSON.stringify(settings));
}

function applyAppearance(mode: AppearanceMode) {
  const prefersDark = window.matchMedia?.("(prefers-color-scheme: dark)").matches ?? false;
  const dark = mode === "dark" || (mode === "system" && prefersDark);
  document.documentElement.classList.toggle("dark", dark);
  document.documentElement.style.colorScheme = dark ? "dark" : "light";
}
