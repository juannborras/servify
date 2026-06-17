import React, { useCallback, useEffect, useRef, useState } from "react";
import { motion, AnimatePresence } from "motion/react";
import {
  Mail,
  Lock,
  User,
  MapPin,
  Calendar,
  Clock,
  Camera,
  ImagePlus,
  Eye,
  EyeOff,
  ArrowLeft,
} from "lucide-react";
import { LOCATION_OPTIONS, TIME_OPTIONS, WEEK_DAYS, servifyApi, type RoleType, type SessionUser } from "../api";
import servifySymbol from "../../imports/servify-symbol.png";

type AuthTab = "login" | "register" | "forgot" | "reset";

type GoogleCredentialResponse = {
  credential?: string;
};

type GoogleButtonText = "signin_with" | "signup_with";

declare global {
  interface Window {
    google?: {
      accounts: {
        id: {
          initialize: (config: {
            client_id: string;
            callback: (response: GoogleCredentialResponse) => void;
            ux_mode?: "popup" | "redirect";
          }) => void;
          renderButton: (
            parent: HTMLElement,
            options: {
              theme: "outline" | "filled_blue" | "filled_black";
              size: "large" | "medium" | "small";
              type: "standard" | "icon";
              shape: "pill" | "rectangular" | "circle" | "square";
              text: GoogleButtonText;
              logo_alignment: "left" | "center";
              locale?: string;
              width: number;
            }
          ) => void;
        };
      };
    };
  }
}

const GOOGLE_IDENTITY_SCRIPT_ID = "google-identity-services";
const GOOGLE_IDENTITY_SCRIPT_SRC = "https://accounts.google.com/gsi/client";
const GOOGLE_CLIENT_ID = import.meta.env.VITE_GOOGLE_CLIENT_ID?.trim() ?? "";

function loadGoogleIdentityScript(): Promise<void> {
  if (window.google?.accounts?.id) {
    return Promise.resolve();
  }

  const existing = document.getElementById(GOOGLE_IDENTITY_SCRIPT_ID) as HTMLScriptElement | null;
  if (existing) {
    return new Promise((resolve, reject) => {
      existing.addEventListener("load", () => resolve(), { once: true });
      existing.addEventListener("error", () => reject(new Error("No se pudo cargar Google")), { once: true });
    });
  }

  return new Promise((resolve, reject) => {
    const script = document.createElement("script");
    script.id = GOOGLE_IDENTITY_SCRIPT_ID;
    script.src = GOOGLE_IDENTITY_SCRIPT_SRC;
    script.async = true;
    script.defer = true;
    script.onload = () => resolve();
    script.onerror = () => reject(new Error("No se pudo cargar Google"));
    document.head.appendChild(script);
  });
}

interface AuthScreenProps {
  onAuth: (user: SessionUser) => void;
}

const roleOptions: { id: RoleType; label: string; sub: string; emoji: string; color: string; bg: string; border: string }[] = [
  { id: "client", label: "Soy cliente", sub: "Busco y contrato servicios", emoji: "🔎", color: "#2563eb", bg: "#eff6ff", border: "#2563eb" },
  { id: "provider", label: "Soy prestador", sub: "Ofrezco servicios profesionales", emoji: "🛠️", color: "#0f766e", bg: "#ecfeff", border: "#0d9488" },
  { id: "both", label: "Ambos roles", sub: "Ofrezco y también contrato", emoji: "⚡", color: "#7c3aed", bg: "#f5f3ff", border: "#7c3aed" },
];

export function AuthScreen({ onAuth }: AuthScreenProps) {
  const registerGalleryInputRef = useRef<HTMLInputElement | null>(null);
  const registerCameraInputRef = useRef<HTMLInputElement | null>(null);

  const [tab, setTab] = useState<AuthTab>("login");
  const [showPass, setShowPass] = useState(false);
  const [showRegisterPass, setShowRegisterPass] = useState(false);
  const [showRegisterConfirmPass, setShowRegisterConfirmPass] = useState(false);
  const [selectedRole, setSelectedRole] = useState<RoleType>(null);
  const [loginEmail, setLoginEmail] = useState("");
  const [loginPass, setLoginPass] = useState("");
  const [recoveryEmail, setRecoveryEmail] = useState("");
  const [recoveryMessage, setRecoveryMessage] = useState("");
  const [resetToken, setResetToken] = useState("");
  const [resetPass, setResetPass] = useState("");
  const [resetPassConfirm, setResetPassConfirm] = useState("");
  const [showResetPass, setShowResetPass] = useState(false);
  const [showResetConfirmPass, setShowResetConfirmPass] = useState(false);
  const [name, setName] = useState("");
  const [lastName, setLastName] = useState("");
  const [username, setUsername] = useState("");
  const [registerEmail, setRegisterEmail] = useState("");
  const [registerPass, setRegisterPass] = useState("");
  const [registerPassConfirm, setRegisterPassConfirm] = useState("");
  const [birthDate, setBirthDate] = useState("");
  const [profilePhotoDataUrl, setProfilePhotoDataUrl] = useState("");
  const [locality, setLocality] = useState(LOCATION_OPTIONS[0]);
  const [availabilityDayFrom, setAvailabilityDayFrom] = useState(WEEK_DAYS[0].value);
  const [availabilityDayTo, setAvailabilityDayTo] = useState(WEEK_DAYS[4].value);
  const [availabilityFrom, setAvailabilityFrom] = useState("09:00");
  const [availabilityTo, setAvailabilityTo] = useState("18:00");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const passwordRules = getPasswordRules(registerPass);
  const passwordIsStrong = passwordRules.every((rule) => rule.valid);
  const passwordConfirmationMatches = Boolean(registerPassConfirm && registerPass === registerPassConfirm);
  const resetPasswordRules = getPasswordRules(resetPass);
  const resetPasswordIsStrong = resetPasswordRules.every((rule) => rule.valid);
  const resetPasswordConfirmationMatches = Boolean(resetPassConfirm && resetPass === resetPassConfirm);
  const canAttemptRegister = Boolean(
    selectedRole &&
      registerEmail &&
      username &&
      registerPass &&
      registerPassConfirm &&
      passwordIsStrong &&
      passwordConfirmationMatches
  );

  useEffect(() => {
    const token = new URLSearchParams(window.location.search).get("resetToken");
    if (!token) return;
    setResetToken(token);
    setTab("reset");
    setError("");
    setRecoveryMessage("");
  }, []);

  const handleGoogleCredential = useCallback(
    async (idToken: string) => {
      setError("");
      setLoading(true);
      try {
        onAuth(await servifyApi.loginWithGoogle(idToken));
      } catch (err) {
        setError(err instanceof Error ? err.message : "No se pudo ingresar con Google");
      } finally {
        setLoading(false);
      }
    },
    [onAuth]
  );

  const handleGoogleError = useCallback((message: string) => {
    setError(message);
  }, []);

  const handleLogin = async () => {
    setError("");
    setLoading(true);
    try {
      onAuth(await servifyApi.login(loginEmail, loginPass));
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo iniciar sesion");
    } finally {
      setLoading(false);
    }
  };

  const handleRequestPasswordReset = async () => {
    setError("");
    setRecoveryMessage("");
    setLoading(true);
    try {
      const result = await servifyApi.requestPasswordReset(recoveryEmail);
      setRecoveryMessage(result.mensaje || "Si existe una cuenta asociada a ese email, enviaremos instrucciones.");
      if (result.debugToken) {
        setResetToken(result.debugToken);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo solicitar la recuperacion");
    } finally {
      setLoading(false);
    }
  };

  const handleResetPassword = async () => {
    if (!resetPasswordIsStrong) {
      setError("La contrasena debe tener mayuscula, minuscula, numero y caracter especial.");
      return;
    }
    if (resetPass !== resetPassConfirm) {
      setError("Las contrasenas no coinciden");
      return;
    }
    setError("");
    setLoading(true);
    try {
      await servifyApi.resetPassword(resetToken, resetPass);
      setRecoveryMessage("Contrasena actualizada. Ya podes iniciar sesion.");
      setResetPass("");
      setResetPassConfirm("");
      setLoginEmail(recoveryEmail || loginEmail);
      setLoginPass("");
      setTab("login");
      window.history.replaceState({}, document.title, window.location.pathname);
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo actualizar la contrasena");
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async () => {
    if (!selectedRole || !registerEmail || !registerPass) return;
    if (!username.trim()) {
      setError("Ingresa un nombre de usuario");
      return;
    }
    if (!passwordIsStrong) {
      setError("La contrasena debe tener mayuscula, minuscula, numero y caracter especial.");
      return;
    }
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
          nombreUsuario: username,
          email: registerEmail,
          password: registerPass,
          fechaNacimiento: birthDate,
          profilePhotoDataUrl,
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

  const handleProfilePhotoSelected = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    event.target.value = "";
    if (!file) return;
    if (!file.type.startsWith("image/")) {
      setError("Selecciona una imagen valida para tu perfil");
      return;
    }

    const reader = new FileReader();
    reader.onload = () => {
      setProfilePhotoDataUrl(typeof reader.result === "string" ? reader.result : "");
      setError("");
    };
    reader.onerror = () => setError("No se pudo leer la foto seleccionada");
    reader.readAsDataURL(file);
  };

  return (
    <div className="flex flex-col h-full bg-white">
      <div className="relative flex flex-col items-start pt-10 pb-4 px-6 overflow-hidden" style={{ background: "#ffffff" }}>
        <img
          src={servifySymbol}
          alt=""
          aria-hidden="true"
          className="pointer-events-none absolute"
          style={{ width: 150, height: 150, right: -30, top: -18, opacity: 0.09, objectFit: "contain" }}
        />
        <div className="flex items-center gap-3 mb-1">
          <div className="flex items-center justify-center rounded-2xl" style={{ width: 42, height: 42, background: "#f8fafc", border: "1px solid #e2e8f0" }}>
            <img src={servifySymbol} alt="Servify" style={{ width: 30, height: 30, objectFit: "contain" }} />
          </div>
          <span style={{ fontSize: 22, fontWeight: 800, color: "#0f172a" }}>Servify</span>
        </div>
        <p style={{ fontSize: 13, color: "#64748b", fontWeight: 500, marginLeft: 55 }}>
          Encontrá el servicio que necesitás
        </p>

        <div className="flex mt-5 rounded-2xl p-1" style={{ background: "#eef2ff", width: "100%" }}>
          {(["login", "register"] as const).map((t) => (
            <button
              key={t}
              onClick={() => {
                setTab(t);
                setError("");
                setRecoveryMessage("");
              }}
              className="flex-1 py-2 rounded-lg transition-all"
              style={{
                background: tab === t ? "white" : "transparent",
                color: tab === t ? "#2563eb" : "#64748b",
                fontWeight: 700,
                fontSize: 14,
                boxShadow: tab === t ? "0 1px 3px rgba(15,23,42,0.08)" : "none",
              }}
            >
              {t === "login" ? "Iniciar sesion" : "Registrarse"}
            </button>
          ))}
        </div>
      </div>

      <div className="flex-1 overflow-y-auto px-6 pt-6 pb-8">
        <AnimatePresence mode="wait">
          {tab === "login" ? (
            <motion.div
              key="login"
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: 20 }}
              transition={{ duration: 0.22 }}
              className="flex flex-col gap-4"
            >
              <div>
                <p style={{ fontSize: 22, fontWeight: 800, color: "#0f172a" }}>Bienvenido de nuevo</p>
                <p style={{ fontSize: 14, color: "#64748b", marginTop: 2 }}>
                  Ingresa a tu cuenta de Servify
                </p>
              </div>

              {error && <ErrorMessage message={error} />}
              {recoveryMessage && <SuccessMessage message={recoveryMessage} />}

              <GoogleAuthButton
                text="signin_with"
                onCredential={handleGoogleCredential}
                onError={handleGoogleError}
              />
              <AuthDivider />

              <InputField
                icon={<Mail size={17} color="#94a3b8" strokeWidth={1.8} />}
                placeholder="Email o nombre de usuario"
                value={loginEmail}
                onChange={setLoginEmail}
                type="text"
              />
              <InputField
                icon={<Lock size={17} color="#94a3b8" strokeWidth={1.8} />}
                placeholder="Contraseña"
                value={loginPass}
                onChange={setLoginPass}
                type={showPass ? "text" : "password"}
                suffix={
                  <button onClick={() => setShowPass(!showPass)}>
                    {showPass ? (
                      <EyeOff size={17} color="#94a3b8" strokeWidth={1.8} />
                    ) : (
                      <Eye size={17} color="#94a3b8" strokeWidth={1.8} />
                    )}
                  </button>
                }
              />

              <button
                type="button"
                onClick={() => {
                  setRecoveryEmail(loginEmail.includes("@") ? loginEmail : "");
                  setError("");
                  setRecoveryMessage("");
                  setTab("forgot");
                }}
                className="self-end transition-all active:scale-95"
                style={{ color: "#2563eb", fontSize: 13, fontWeight: 800 }}
              >
                Olvide mi contrasena
              </button>

              <button
                onClick={handleLogin}
                disabled={loading}
                className="w-full py-3.5 rounded-2xl mt-2 transition-all active:scale-95"
                style={{ background: "#2563eb", color: "white", fontWeight: 700, fontSize: 15 }}
              >
                {loading ? "Conectando..." : "Iniciar sesion"}
              </button>
            </motion.div>
          ) : tab === "forgot" ? (
            <motion.div
              key="forgot"
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: 20 }}
              transition={{ duration: 0.22 }}
              className="flex flex-col gap-4"
            >
              <button
                type="button"
                onClick={() => {
                  setTab("login");
                  setError("");
                  setRecoveryMessage("");
                }}
                className="flex items-center gap-2 self-start transition-all active:scale-95"
                style={{ color: "#2563eb", fontSize: 13, fontWeight: 800 }}
              >
                <ArrowLeft size={16} strokeWidth={2} />
                Volver
              </button>
              <div>
                <p style={{ fontSize: 22, fontWeight: 800, color: "#0f172a" }}>Recuperar cuenta</p>
                <p style={{ fontSize: 14, color: "#64748b", marginTop: 2 }}>
                  Ingresa el email registrado y te enviaremos un enlace temporal.
                </p>
              </div>

              {error && <ErrorMessage message={error} />}
              {recoveryMessage && <SuccessMessage message={recoveryMessage} />}

              <InputField
                icon={<Mail size={17} color="#94a3b8" strokeWidth={1.8} />}
                placeholder="Email registrado"
                value={recoveryEmail}
                onChange={setRecoveryEmail}
                type="email"
              />

              <button
                onClick={handleRequestPasswordReset}
                disabled={loading || !recoveryEmail.trim()}
                className="w-full py-3.5 rounded-2xl mt-2 transition-all active:scale-95"
                style={{
                  background: recoveryEmail.trim() ? "#2563eb" : "#cbd5e1",
                  color: "white",
                  fontWeight: 700,
                  fontSize: 15,
                }}
              >
                {loading ? "Enviando..." : "Enviar enlace"}
              </button>

              {resetToken && (
                <button
                  type="button"
                  onClick={() => {
                    setTab("reset");
                    setError("");
                  }}
                  className="w-full py-3 rounded-2xl transition-all active:scale-95"
                  style={{ background: "#eef2ff", color: "#2563eb", fontWeight: 800, fontSize: 14 }}
                >
                  Ya tengo un enlace de recuperacion
                </button>
              )}
            </motion.div>
          ) : tab === "reset" ? (
            <motion.div
              key="reset"
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: 20 }}
              transition={{ duration: 0.22 }}
              className="flex flex-col gap-4"
            >
              <button
                type="button"
                onClick={() => {
                  setTab("login");
                  setError("");
                  setRecoveryMessage("");
                }}
                className="flex items-center gap-2 self-start transition-all active:scale-95"
                style={{ color: "#2563eb", fontSize: 13, fontWeight: 800 }}
              >
                <ArrowLeft size={16} strokeWidth={2} />
                Volver
              </button>
              <div>
                <p style={{ fontSize: 22, fontWeight: 800, color: "#0f172a" }}>Nueva contrasena</p>
                <p style={{ fontSize: 14, color: "#64748b", marginTop: 2 }}>
                  Crea una contrasena segura para volver a entrar.
                </p>
              </div>

              {error && <ErrorMessage message={error} />}

              {!resetToken && (
                <InputField
                  icon={<Lock size={17} color="#94a3b8" strokeWidth={1.8} />}
                  placeholder="Token de recuperacion"
                  value={resetToken}
                  onChange={setResetToken}
                />
              )}
              <InputField
                icon={<Lock size={17} color="#94a3b8" strokeWidth={1.8} />}
                placeholder="Nueva contrasena"
                value={resetPass}
                onChange={setResetPass}
                type={showResetPass ? "text" : "password"}
                suffix={
                  <button onClick={() => setShowResetPass(!showResetPass)}>
                    {showResetPass ? (
                      <EyeOff size={17} color="#94a3b8" strokeWidth={1.8} />
                    ) : (
                      <Eye size={17} color="#94a3b8" strokeWidth={1.8} />
                    )}
                  </button>
                }
              />
              <PasswordRules rules={resetPasswordRules} />
              <InputField
                icon={<Lock size={17} color="#94a3b8" strokeWidth={1.8} />}
                placeholder="Confirmar nueva contrasena"
                value={resetPassConfirm}
                onChange={setResetPassConfirm}
                type={showResetConfirmPass ? "text" : "password"}
                suffix={
                  <button onClick={() => setShowResetConfirmPass(!showResetConfirmPass)}>
                    {showResetConfirmPass ? (
                      <EyeOff size={17} color="#94a3b8" strokeWidth={1.8} />
                    ) : (
                      <Eye size={17} color="#94a3b8" strokeWidth={1.8} />
                    )}
                  </button>
                }
              />
              {resetPassConfirm && !resetPasswordConfirmationMatches ? (
                <p style={{ color: "#dc2626", fontSize: 12, fontWeight: 700, marginTop: -8 }}>
                  Las contrasenas no coinciden.
                </p>
              ) : null}

              <button
                onClick={handleResetPassword}
                disabled={loading || !resetToken || !resetPasswordIsStrong || !resetPasswordConfirmationMatches}
                className="w-full py-3.5 rounded-2xl mt-1 transition-all active:scale-95"
                style={{
                  background:
                    resetToken && resetPasswordIsStrong && resetPasswordConfirmationMatches ? "#2563eb" : "#cbd5e1",
                  color: "white",
                  fontWeight: 700,
                  fontSize: 15,
                }}
              >
                {loading ? "Actualizando..." : "Guardar nueva contrasena"}
              </button>
            </motion.div>
          ) : (
            <motion.div
              key="register"
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: -20 }}
              transition={{ duration: 0.22 }}
              className="flex flex-col gap-4"
            >
              <div>
                <p style={{ fontSize: 22, fontWeight: 800, color: "#0f172a" }}>Crear cuenta</p>
                <p style={{ fontSize: 14, color: "#64748b", marginTop: 2 }}>
                  Como vas a usar Servify?
                </p>
              </div>

              {error && <ErrorMessage message={error} />}

              <GoogleAuthButton
                text="signup_with"
                onCredential={handleGoogleCredential}
                onError={handleGoogleError}
              />
              <AuthDivider />

              <div className="grid grid-cols-3 gap-2">
                {roleOptions.map(({ id, label, sub, emoji, color, bg, border }) => {
                  const selected = selectedRole === id;
                  return (
                    <button
                      key={id}
                      onClick={() => setSelectedRole(id)}
                      className="flex flex-col items-center justify-center gap-1.5 p-3 rounded-2xl text-center transition-all"
                      style={{
                        border: selected ? `2px solid ${border}` : "1.5px solid #e2e8f0",
                        background: selected ? bg : "white",
                        minHeight: 122,
                      }}
                    >
                      <div style={{ fontSize: 29, lineHeight: 1 }}>{emoji}</div>
                      <p style={{ fontWeight: 700, fontSize: 15, color: selected ? color : "#0f172a", lineHeight: 1.2 }}>{label}</p>
                      <p style={{ fontSize: 11, color: "#64748b", lineHeight: 1.3 }}>{sub}</p>
                    </button>
                  );
                })}
              </div>

              <div className="flex flex-col items-center gap-2 py-2">
                <div
                  className="flex items-center justify-center rounded-full relative overflow-hidden"
                  style={{ width: 76, height: 76, background: "#f1f5f9", border: "2px dashed #cbd5e1" }}
                >
                  {profilePhotoDataUrl ? (
                    <img src={profilePhotoDataUrl} alt="Foto de perfil" style={{ width: "100%", height: "100%", objectFit: "cover" }} />
                  ) : (
                    <Camera size={22} color="#94a3b8" strokeWidth={1.7} />
                  )}
                </div>
                <div className="grid grid-cols-2 gap-2 w-full">
                  <button
                    type="button"
                    onClick={() => registerGalleryInputRef.current?.click()}
                    className="py-2.5 rounded-xl flex items-center justify-center gap-2"
                    style={{ border: "1px solid #cbd5e1", color: "#0f172a", fontSize: 13, fontWeight: 700 }}
                  >
                    <ImagePlus size={15} strokeWidth={1.8} />
                    Elegir foto
                  </button>
                  <button
                    type="button"
                    onClick={() => registerCameraInputRef.current?.click()}
                    className="py-2.5 rounded-xl flex items-center justify-center gap-2"
                    style={{ border: "1px solid #cbd5e1", color: "#0f172a", fontSize: 13, fontWeight: 700 }}
                  >
                    <Camera size={15} strokeWidth={1.8} />
                    Sacar foto
                  </button>
                </div>
                <input ref={registerGalleryInputRef} type="file" accept="image/*" className="hidden" onChange={handleProfilePhotoSelected} />
                <input ref={registerCameraInputRef} type="file" accept="image/*" capture="environment" className="hidden" onChange={handleProfilePhotoSelected} />
              </div>

              <div className="flex flex-col gap-3">
                <InputField icon={<User size={17} color="#94a3b8" strokeWidth={1.8} />} placeholder="Nombre" value={name} onChange={setName} />
                <InputField icon={<User size={17} color="#94a3b8" strokeWidth={1.8} />} placeholder="Apellido" value={lastName} onChange={setLastName} />
              </div>
              <InputField icon={<User size={17} color="#94a3b8" strokeWidth={1.8} />} placeholder="Nombre de usuario" value={username} onChange={setUsername} />
              <InputField icon={<Mail size={17} color="#94a3b8" strokeWidth={1.8} />} placeholder="Email" value={registerEmail} onChange={setRegisterEmail} type="email" />
              <InputField
                icon={<Lock size={17} color="#94a3b8" strokeWidth={1.8} />}
                placeholder="Contrasena"
                value={registerPass}
                onChange={setRegisterPass}
                type={showRegisterPass ? "text" : "password"}
                suffix={
                  <button onClick={() => setShowRegisterPass(!showRegisterPass)}>
                    {showRegisterPass ? (
                      <EyeOff size={17} color="#94a3b8" strokeWidth={1.8} />
                    ) : (
                      <Eye size={17} color="#94a3b8" strokeWidth={1.8} />
                    )}
                  </button>
                }
              />
              <PasswordRules rules={passwordRules} />
              <InputField
                icon={<Lock size={17} color="#94a3b8" strokeWidth={1.8} />}
                placeholder="Confirmar contrasena"
                value={registerPassConfirm}
                onChange={setRegisterPassConfirm}
                type={showRegisterConfirmPass ? "text" : "password"}
                suffix={
                  <button onClick={() => setShowRegisterConfirmPass(!showRegisterConfirmPass)}>
                    {showRegisterConfirmPass ? (
                      <EyeOff size={17} color="#94a3b8" strokeWidth={1.8} />
                    ) : (
                      <Eye size={17} color="#94a3b8" strokeWidth={1.8} />
                    )}
                  </button>
                }
              />
              {registerPassConfirm && !passwordConfirmationMatches ? (
                <p style={{ color: "#dc2626", fontSize: 12, fontWeight: 700, marginTop: -8 }}>
                  Las contrasenas no coinciden.
                </p>
              ) : null}
              <SelectField
                icon={<MapPin size={17} color="#94a3b8" strokeWidth={1.8} />}
                value={locality}
                onChange={setLocality}
                options={LOCATION_OPTIONS}
              />
              <div className="flex items-center gap-3 px-4 py-3.5 rounded-2xl" style={{ background: "#f8fafc", border: "1.5px solid #e2e8f0" }}>
                <Clock size={17} color="#94a3b8" strokeWidth={1.8} />
                <div className="grid grid-cols-2 gap-2 flex-1">
                  <select value={availabilityDayFrom} onChange={(e) => setAvailabilityDayFrom(e.target.value)} className="bg-transparent outline-none min-w-0" style={{ fontSize: 13, color: "#0f172a" }}>
                    {WEEK_DAYS.map((day) => <option key={day.value} value={day.value}>Desde {day.label}</option>)}
                  </select>
                  <select value={availabilityDayTo} onChange={(e) => setAvailabilityDayTo(e.target.value)} className="bg-transparent outline-none min-w-0" style={{ fontSize: 13, color: "#0f172a" }}>
                    {WEEK_DAYS.map((day) => <option key={day.value} value={day.value}>Hasta {day.label}</option>)}
                  </select>
                  <select value={availabilityFrom} onChange={(e) => setAvailabilityFrom(e.target.value)} className="bg-transparent outline-none min-w-0" style={{ fontSize: 13, color: "#0f172a" }}>
                    {TIME_OPTIONS.map((time) => <option key={time} value={time}>Desde {time}</option>)}
                  </select>
                  <select value={availabilityTo} onChange={(e) => setAvailabilityTo(e.target.value)} className="bg-transparent outline-none min-w-0" style={{ fontSize: 13, color: "#0f172a" }}>
                    {TIME_OPTIONS.map((time) => <option key={time} value={time}>Hasta {time}</option>)}
                  </select>
                </div>
              </div>
              <LabeledInputField
                icon={<Calendar size={17} color="#94a3b8" strokeWidth={1.8} />}
                label="Fecha de nacimiento"
                value={birthDate}
                onChange={setBirthDate}
                type="date"
              />

              <button
                onClick={handleRegister}
                disabled={loading || !canAttemptRegister}
                className="w-full py-3.5 rounded-2xl mt-1 transition-all active:scale-95"
                style={{
                  background: canAttemptRegister ? "#2563eb" : "#cbd5e1",
                  color: "white",
                  fontWeight: 700,
                  fontSize: 15,
                }}
              >
                {loading ? "Creando cuenta..." : selectedRole ? "Crear cuenta" : "Selecciona tu rol"}
              </button>
            </motion.div>
          )}
        </AnimatePresence>
      </div>
    </div>
  );
}

function GoogleAuthButton({
  text,
  onCredential,
  onError,
}: {
  text: GoogleButtonText;
  onCredential: (idToken: string) => void;
  onError: (message: string) => void;
}) {
  const [ready, setReady] = useState(false);
  const [themeVersion, setThemeVersion] = useState(0);
  const buttonRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    const targets = [document.documentElement, document.body, document.getElementById("root")].filter(
      Boolean
    ) as Element[];
    const observer = new MutationObserver(() => setThemeVersion((value) => value + 1));

    targets.forEach((target) => observer.observe(target, { attributes: true, attributeFilter: ["class"] }));
    return () => observer.disconnect();
  }, []);

  useEffect(() => {
    let cancelled = false;

    async function renderGoogleButton() {
      if (!buttonRef.current) return;
      setReady(false);

      if (!GOOGLE_CLIENT_ID) {
        return;
      }

      try {
        await loadGoogleIdentityScript();
        if (cancelled || !window.google?.accounts?.id || !buttonRef.current) return;

        window.google.accounts.id.initialize({
          client_id: GOOGLE_CLIENT_ID,
          ux_mode: "popup",
          callback: (response) => {
            if (!response.credential) {
              onError("Google no devolvio una credencial valida");
              return;
            }
            onCredential(response.credential);
          },
        });

        const width = Math.max(
          240,
          Math.min(360, Math.floor(buttonRef.current.getBoundingClientRect().width || 320))
        );

        buttonRef.current.innerHTML = "";
        window.google.accounts.id.renderButton(buttonRef.current, {
          type: "standard",
          theme: isDarkModeEnabled() ? "filled_black" : "outline",
          size: "large",
          shape: "pill",
          text,
          logo_alignment: "left",
          locale: "es",
          width,
        });
        setReady(true);
      } catch {
        if (!cancelled) {
          setReady(false);
          onError("No se pudo cargar el inicio con Google");
        }
      }
    }

    renderGoogleButton();

    return () => {
      cancelled = true;
      if (buttonRef.current) {
        buttonRef.current.innerHTML = "";
      }
    };
  }, [onCredential, onError, text, themeVersion]);

  return (
    <div
      className="servify-google-auth-shell flex items-center justify-center rounded-2xl overflow-hidden"
      style={{
        minHeight: 44,
        background: ready ? "transparent" : "rgba(248, 250, 252, 0.88)",
        border: ready ? "1px solid transparent" : "1.5px solid #e2e8f0",
      }}
    >
      {!ready && (
        <span className="servify-google-auth-placeholder" style={{ color: "#64748b", fontSize: 14, fontWeight: 700 }}>
          {GOOGLE_CLIENT_ID ? "Cargando Google..." : "Configurar Google"}
        </span>
      )}
      <div ref={buttonRef} className="flex w-full justify-center" />
    </div>
  );
}

function isDarkModeEnabled() {
  return Boolean(
    document.documentElement.classList.contains("dark") ||
      document.body.classList.contains("dark") ||
      document.getElementById("root")?.classList.contains("dark") ||
      document.querySelector(".dark")
  );
}

function AuthDivider() {
  return (
    <div className="flex items-center gap-3 py-1">
      <div className="h-px flex-1" style={{ background: "#e2e8f0" }} />
      <span style={{ color: "#94a3b8", fontSize: 12, fontWeight: 700 }}>o</span>
      <div className="h-px flex-1" style={{ background: "#e2e8f0" }} />
    </div>
  );
}

function getPasswordRules(password: string) {
  return [
    { label: "8 caracteres minimo", valid: password.length >= 8 },
    { label: "Una mayuscula", valid: /[A-Z]/.test(password) },
    { label: "Una minuscula", valid: /[a-z]/.test(password) },
    { label: "Un numero", valid: /\d/.test(password) },
    { label: "Un caracter especial", valid: /[^A-Za-z0-9]/.test(password) },
  ];
}

function PasswordRules({ rules }: { rules: Array<{ label: string; valid: boolean }> }) {
  return (
    <div className="grid grid-cols-2 gap-2" style={{ marginTop: -6 }}>
      {rules.map((rule) => (
        <div
          key={rule.label}
          className="flex items-center gap-1.5 rounded-xl px-2 py-1.5"
          style={{ background: rule.valid ? "#f0fdf4" : "#f8fafc", border: `1px solid ${rule.valid ? "#bbf7d0" : "#e2e8f0"}` }}
        >
          <span
            className="rounded-full"
            style={{ width: 7, height: 7, background: rule.valid ? "#16a34a" : "#cbd5e1", flexShrink: 0 }}
          />
          <span style={{ fontSize: 11, fontWeight: 700, color: rule.valid ? "#15803d" : "#64748b" }}>
            {rule.label}
          </span>
        </div>
      ))}
    </div>
  );
}

function ErrorMessage({ message }: { message: string }) {
  return (
    <div className="rounded-2xl px-4 py-3" style={{ background: "#fef2f2", border: "1px solid #fecaca" }}>
      <p style={{ color: "#b91c1c", fontSize: 13, fontWeight: 600 }}>{message}</p>
    </div>
  );
}

function SuccessMessage({ message }: { message: string }) {
  return (
    <div className="rounded-2xl px-4 py-3" style={{ background: "#f0fdf4", border: "1px solid #bbf7d0" }}>
      <p style={{ color: "#166534", fontSize: 13, fontWeight: 700 }}>{message}</p>
    </div>
  );
}

function SelectField({
  icon,
  value,
  onChange,
  options,
}: {
  icon?: React.ReactNode;
  value: string;
  onChange: (v: string) => void;
  options: string[];
}) {
  return (
    <div className="flex items-center gap-3 px-4 py-3.5 rounded-2xl" style={{ background: "#f8fafc", border: "1.5px solid #e2e8f0" }}>
      {icon}
      <select
        value={value}
        onChange={(e) => onChange(e.target.value)}
        className="flex-1 bg-transparent outline-none min-w-0"
        style={{ fontSize: 14, color: "#0f172a" }}
      >
        {options.map((option) => (
          <option key={option} value={option}>{option}</option>
        ))}
      </select>
    </div>
  );
}

function InputField({
  icon,
  placeholder,
  value,
  onChange,
  type = "text",
  suffix,
}: {
  icon?: React.ReactNode;
  placeholder: string;
  value?: string;
  onChange?: (v: string) => void;
  type?: string;
  suffix?: React.ReactNode;
}) {
  return (
    <div className="flex items-center gap-3 px-4 py-3.5 rounded-2xl" style={{ background: "#f8fafc", border: "1.5px solid #e2e8f0" }}>
      {icon}
      <input
        type={type}
        placeholder={placeholder}
        value={value}
        onChange={(e) => onChange?.(e.target.value)}
        className="flex-1 bg-transparent outline-none min-w-0"
        style={{ fontSize: 14, color: "#0f172a" }}
      />
      {suffix}
    </div>
  );
}

function LabeledInputField({
  icon,
  label,
  value,
  onChange,
  type = "text",
}: {
  icon?: React.ReactNode;
  label: string;
  value?: string;
  onChange?: (v: string) => void;
  type?: string;
}) {
  return (
    <label className="flex flex-col gap-1.5">
      <span style={{ fontSize: 12, fontWeight: 800, color: "#64748b" }}>{label}</span>
      <div className="flex items-center gap-3 px-4 py-3.5 rounded-2xl" style={{ background: "#f8fafc", border: "1.5px solid #e2e8f0" }}>
        {icon}
        <input
          type={type}
          aria-label={label}
          value={value}
          onChange={(e) => onChange?.(e.target.value)}
          className="flex-1 bg-transparent outline-none min-w-0"
          style={{ fontSize: 14, color: "#0f172a" }}
        />
      </div>
    </label>
  );
}
