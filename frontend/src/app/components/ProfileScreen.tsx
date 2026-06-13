import React, { useEffect, useRef, useState } from "react";
import {
  MapPin,
  Mail,
  Briefcase,
  Clock,
  Edit3,
  LogOut,
  User,
  Camera,
  Save,
  ImagePlus,
  Star,
  X,
} from "lucide-react";
import {
  ACCOUNT_ROLES,
  LOCATION_OPTIONS,
  TIME_OPTIONS,
  WEEK_DAYS,
  servifyApi,
  type ApiRatingSummary,
  type RoleType,
  type SessionUser,
} from "../api";

interface ProfileScreenProps {
  user: SessionUser | null;
  onLogout: () => void;
  onLogin: () => void;
  onUserUpdated: (patch: Partial<SessionUser>) => void;
}

const roleLabel: Record<Exclude<RoleType, null>, string> = {
  client: "Cliente",
  provider: "Prestador",
  both: "Cliente y prestador",
};

const emptyRating = (usuarioId: string): ApiRatingSummary => ({
  usuarioId,
  cantidadValoraciones: 0,
  promedioEstrellas: 0,
});

export function ProfileScreen({ user, onLogout, onLogin, onUserUpdated }: ProfileScreenProps) {
  const galleryInputRef = useRef<HTMLInputElement | null>(null);
  const cameraInputRef = useRef<HTMLInputElement | null>(null);
  const videoRef = useRef<HTMLVideoElement | null>(null);
  const cameraStreamRef = useRef<MediaStream | null>(null);

  const [editing, setEditing] = useState(false);
  const [loadingProfile, setLoadingProfile] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [photoUrl, setPhotoUrl] = useState("");
  const [localidad, setLocalidad] = useState(LOCATION_OPTIONS[0]);
  const [availabilityDayFrom, setAvailabilityDayFrom] = useState(WEEK_DAYS[0].value);
  const [availabilityDayTo, setAvailabilityDayTo] = useState(WEEK_DAYS[4].value);
  const [availabilityFrom, setAvailabilityFrom] = useState("09:00");
  const [availabilityTo, setAvailabilityTo] = useState("18:00");
  const [accountRole, setAccountRole] = useState<Exclude<RoleType, null>>("client");
  const [ratingSummary, setRatingSummary] = useState<ApiRatingSummary | null>(null);
  const [cameraOpen, setCameraOpen] = useState(false);

  useEffect(() => {
    if (!user) return;

    let cancelled = false;

    const loadProfile = async () => {
      setLoadingProfile(true);
      setError(null);
      setSuccess(null);

      try {
        const [accountConfig, profile, ratings] = await Promise.all([
          servifyApi.getAccountConfig(user.id).catch(() => null),
          servifyApi.getUserProfile(user.id).catch(() => null),
          servifyApi.getUserRatingSummary(user.id).catch(() => emptyRating(user.id)),
        ]);

        if (cancelled) return;

        const prefs = servifyApi.getProfilePreferences(user.id);
        const storedPhoto = servifyApi.getStoredProfilePhoto(user.id);
        const resolvedName = profile?.nombre || user.name.split(" ")[0] || "";
        const resolvedLastName = profile?.apellido || user.name.split(" ").slice(1).join(" ") || "";
        const resolvedLocation = profile?.ubicacion?.localidad || LOCATION_OPTIONS[0];
        const candidateRole = (prefs.role ?? user.role ?? "client") as Exclude<RoleType, null>;
        const resolvedRole = ACCOUNT_ROLES.some((role) => role.id === candidateRole) ? candidateRole : "client";

        setFirstName(resolvedName);
        setLastName(resolvedLastName);
        setPhotoUrl(storedPhoto || profile?.fotoPerfilUrl || "");
        setLocalidad(LOCATION_OPTIONS.includes(resolvedLocation) ? resolvedLocation : LOCATION_OPTIONS[0]);
        setEmail(prefs.email || accountConfig?.usuario?.email || user.email || "");
        setUsername(accountConfig?.usuario?.nombreUsuario || user.username || "");
        setAvailabilityDayFrom(prefs.availabilityDayFrom || WEEK_DAYS[0].value);
        setAvailabilityDayTo(prefs.availabilityDayTo || WEEK_DAYS[4].value);
        setAvailabilityFrom(prefs.availabilityFrom || "09:00");
        setAvailabilityTo(prefs.availabilityTo || "18:00");
        setAccountRole(resolvedRole);
        setRatingSummary(ratings);
      } finally {
        if (!cancelled) {
          setLoadingProfile(false);
        }
      }
    };

    loadProfile();

    return () => {
      cancelled = true;
    };
  }, [user]);

  useEffect(() => {
    if (cameraOpen && videoRef.current && cameraStreamRef.current) {
      videoRef.current.srcObject = cameraStreamRef.current;
    }
  }, [cameraOpen]);

  useEffect(() => {
    return () => {
      stopCamera();
    };
  }, []);

  if (!user) {
    return (
      <div className="flex flex-col h-full items-center justify-center gap-5 px-8" style={{ background: "#f8fafc" }}>
        <div
          className="flex items-center justify-center rounded-3xl"
          style={{ width: 80, height: 80, background: "#eff6ff" }}
        >
          <User size={36} color="#2563eb" strokeWidth={1.6} />
        </div>
        <div className="text-center">
          <p style={{ fontSize: 20, fontWeight: 800, color: "#0f172a" }}>Sin cuenta</p>
          <p style={{ fontSize: 14, color: "#64748b", marginTop: 6, lineHeight: 1.5 }}>
            Inicia sesion para ver tu perfil, gestionar solicitudes y publicar servicios
          </p>
        </div>
        <button
          onClick={onLogin}
          className="px-8 py-3.5 rounded-2xl transition-all active:scale-95"
          style={{ background: "#2563eb", color: "white", fontWeight: 700, fontSize: 15 }}
        >
          Ingresar
        </button>
      </div>
    );
  }

  const displayName = `${firstName} ${lastName}`.trim() || user.name;
  const initials = displayName
    .split(" ")
    .map((n) => n[0])
    .join("")
    .slice(0, 2)
    .toUpperCase();
  const availabilityDayFromLabel = WEEK_DAYS.find((day) => day.value === availabilityDayFrom)?.label ?? "Lunes";
  const availabilityDayToLabel = WEEK_DAYS.find((day) => day.value === availabilityDayTo)?.label ?? "Viernes";
  const availabilityLabel = `${availabilityDayFromLabel} a ${availabilityDayToLabel}, ${availabilityFrom} - ${availabilityTo}`;
  const canSave = Boolean(firstName.trim() && lastName.trim() && username.trim() && email.trim() && availabilityDayFrom && availabilityDayTo && availabilityFrom && availabilityTo);

  const handleProfilePhotoSelected = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    event.target.value = "";
    if (!file) return;

    if (!file.type.startsWith("image/")) {
      setError("Selecciona un archivo de imagen valido");
      return;
    }

    const reader = new FileReader();
    reader.onload = () => {
      const nextPhoto = typeof reader.result === "string" ? reader.result : "";
      setPhotoUrl(nextPhoto);
      servifyApi.saveProfilePhoto(user.id, nextPhoto);
      setError(null);
    };
    reader.onerror = () => setError("No se pudo leer la imagen seleccionada");
    reader.readAsDataURL(file);
  };

  function stopCamera() {
    cameraStreamRef.current?.getTracks().forEach((track) => track.stop());
    cameraStreamRef.current = null;
    setCameraOpen(false);
  }

  const openCamera = async () => {
    setError(null);
    if (!navigator.mediaDevices?.getUserMedia) {
      cameraInputRef.current?.click();
      return;
    }

    try {
      const stream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: { ideal: "environment" } },
        audio: false,
      });
      cameraStreamRef.current = stream;
      setCameraOpen(true);
    } catch {
      setError("No se pudo abrir la camara. Revisa permisos o usa la fototeca.");
      cameraInputRef.current?.click();
    }
  };

  const captureCameraPhoto = () => {
    const video = videoRef.current;
    if (!video) return;

    const canvas = document.createElement("canvas");
    canvas.width = video.videoWidth || 720;
    canvas.height = video.videoHeight || 960;
    const context = canvas.getContext("2d");
    if (!context) {
      setError("No se pudo capturar la imagen de la camara");
      return;
    }

    context.drawImage(video, 0, 0, canvas.width, canvas.height);
    const nextPhoto = canvas.toDataURL("image/jpeg", 0.9);
    setPhotoUrl(nextPhoto);
    servifyApi.saveProfilePhoto(user.id, nextPhoto);
    setError(null);
    stopCamera();
  };

  const handleSaveProfile = async () => {
    if (!canSave) return;

    setSaving(true);
    setError(null);
    setSuccess(null);

    try {
      const remotePhotoUrl = /^https?:\/\//i.test(photoUrl.trim()) ? photoUrl.trim() : "";

      await servifyApi.updateUserProfile(user.id, {
        nombre: firstName.trim(),
        apellido: lastName.trim(),
        fotoPerfilUrl: remotePhotoUrl,
        localidad,
        descripcionPersonal: `Disponibilidad ${availabilityFrom}-${availabilityTo}`,
      });

      const account = await servifyApi.updateAccount(user.id, {
        nombreUsuario: username.trim(),
      });

      servifyApi.saveProfilePreferences(user.id, {
        email: email.trim(),
        availabilityDayFrom,
        availabilityDayTo,
        availabilityFrom,
        availabilityTo,
        role: accountRole,
      });

      if (!photoUrl) {
        servifyApi.saveProfilePhoto(user.id, "");
      }

      onUserUpdated({
        name: `${firstName.trim()} ${lastName.trim()}`.trim(),
        email: email.trim(),
        username: account.nombreUsuario,
        role: accountRole,
      });

      setSuccess("Perfil actualizado correctamente");
      setEditing(false);
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo actualizar el perfil");
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="flex flex-col h-full" style={{ background: "#f8fafc" }}>
      <div
        className="px-5 pt-12 pb-8 relative overflow-hidden"
        style={{ background: "linear-gradient(160deg, #1d4ed8 0%, #2563eb 50%, #0891b2 100%)" }}
      >
        <div className="relative flex items-start justify-between mb-5">
          <div className="flex items-center gap-4 min-w-0">
            <div
              className="flex items-center justify-center rounded-full overflow-hidden"
              style={{
                width: 68,
                height: 68,
                background: "rgba(255,255,255,0.2)",
                border: "3px solid rgba(255,255,255,0.5)",
                flexShrink: 0,
              }}
            >
              {photoUrl ? (
                <img
                  src={photoUrl}
                  alt="Avatar"
                  style={{ width: "100%", height: "100%", objectFit: "cover" }}
                />
              ) : (
                <span style={{ fontSize: 24, fontWeight: 800, color: "white" }}>{initials}</span>
              )}
            </div>
            <div className="min-w-0">
              <p style={{ fontSize: 19, fontWeight: 800, color: "white", lineHeight: 1.2 }}>
                {displayName}
              </p>
              <div className="flex items-center gap-1 mt-1">
                <MapPin size={12} color="rgba(255,255,255,0.75)" strokeWidth={1.8} />
                <span style={{ fontSize: 12, color: "rgba(255,255,255,0.75)", fontWeight: 500 }}>
                  {localidad || LOCATION_OPTIONS[0]}
                </span>
              </div>
            </div>
          </div>

          <button
            onClick={() => {
              setEditing((prev) => !prev);
              setError(null);
              setSuccess(null);
            }}
            className="flex items-center justify-center rounded-xl"
            style={{ width: 38, height: 38, background: "rgba(255,255,255,0.2)", flexShrink: 0 }}
          >
            <Edit3 size={17} color="white" strokeWidth={1.8} />
          </button>
        </div>

        <div className="rounded-2xl px-4 py-3" style={{ background: "rgba(255,255,255,0.15)" }}>
          <div className="flex items-center justify-between gap-3">
            <RatingStars value={ratingSummary?.promedioEstrellas ?? 0} color="white" />
            <span style={{ fontSize: 12, color: "rgba(255,255,255,0.9)", fontWeight: 700 }}>
              {formatRatingLabel(ratingSummary)}
            </span>
          </div>
        </div>
      </div>

      <div className="flex-1 overflow-y-auto px-5 pt-5 pb-8 flex flex-col gap-4">
        <div
          className="bg-white rounded-2xl p-4"
          style={{ border: "1px solid rgba(0,0,0,0.06)", boxShadow: "0 1px 4px rgba(0,0,0,0.04)" }}
        >
          {loadingProfile ? (
            <p style={{ fontSize: 13, color: "#64748b", marginBottom: 12 }}>Cargando perfil...</p>
          ) : null}

          {error ? (
            <div className="rounded-xl px-3 py-2 mb-3" style={{ background: "#fef2f2", border: "1px solid #fecaca" }}>
              <p style={{ fontSize: 12, color: "#dc2626", fontWeight: 600 }}>{error}</p>
            </div>
          ) : null}

          {success ? (
            <div className="rounded-xl px-3 py-2 mb-3" style={{ background: "#ecfdf5", border: "1px solid #bbf7d0" }}>
              <p style={{ fontSize: 12, color: "#15803d", fontWeight: 700 }}>{success}</p>
            </div>
          ) : null}

          <p style={{ fontSize: 12, fontWeight: 700, color: "#94a3b8", letterSpacing: "0.08em", textTransform: "uppercase", marginBottom: 14 }}>
            Informacion
          </p>
          <div className="flex flex-col gap-3.5">
            <InfoRow icon={<Mail size={15} color="#0891b2" strokeWidth={1.8} />} label="Email" value={email || user.email} />
            <InfoRow icon={<User size={15} color="#2563eb" strokeWidth={1.8} />} label="Usuario" value={username ? `@${username}` : "Sin usuario"} />
            <InfoRow icon={<MapPin size={15} color="#ef4444" strokeWidth={1.8} />} label="Localidad" value={localidad || LOCATION_OPTIONS[0]} />
            <InfoRow icon={<Briefcase size={15} color="#7c3aed" strokeWidth={1.8} />} label="Tipo de cuenta" value={roleLabel[accountRole]} />
            <InfoRow icon={<Clock size={15} color="#d97706" strokeWidth={1.8} />} label="Horario" value={availabilityLabel} />
            <InfoRow
              icon={<Star size={15} color="#eab308" strokeWidth={1.8} />}
              label="Calificaciones"
              value={<RatingSummary summary={ratingSummary} />}
            />
          </div>

          {editing ? (
            <div className="mt-4 pt-4" style={{ borderTop: "1px solid #e2e8f0" }}>
              <p style={{ fontSize: 12, fontWeight: 800, color: "#475569", marginBottom: 10 }}>Editar perfil</p>

              <div className="grid grid-cols-2 gap-2">
                <ProfileInput label="Nombre" value={firstName} onChange={setFirstName} />
                <ProfileInput label="Apellido" value={lastName} onChange={setLastName} />
              </div>

              <div className="mt-2">
                <ProfileInput label="Nombre de usuario" value={username} onChange={setUsername} />
              </div>

              <div className="mt-2">
                <ProfileInput label="Email" value={email} onChange={setEmail} type="email" />
                <p style={{ fontSize: 11, color: "#94a3b8", marginTop: 4 }}>
                  En este MVP el cambio de email queda guardado en el front hasta que exista endpoint dedicado.
                </p>
              </div>

              <div className="mt-3">
                <span style={{ fontSize: 11, fontWeight: 700, color: "#64748b" }}>Foto de perfil</span>
                <div className="grid grid-cols-2 gap-2 mt-1">
                  <button
                    type="button"
                    onClick={() => galleryInputRef.current?.click()}
                    className="py-2.5 rounded-xl flex items-center justify-center gap-2"
                    style={{ border: "1px solid #cbd5e1", color: "#0f172a", fontSize: 13, fontWeight: 700 }}
                  >
                    <ImagePlus size={15} strokeWidth={1.8} />
                    Fototeca
                  </button>
                  <button
                    type="button"
                    onClick={openCamera}
                    className="py-2.5 rounded-xl flex items-center justify-center gap-2"
                    style={{ border: "1px solid #cbd5e1", color: "#0f172a", fontSize: 13, fontWeight: 700 }}
                  >
                    <Camera size={15} strokeWidth={1.8} />
                    Camara
                  </button>
                </div>
                <input ref={galleryInputRef} type="file" accept="image/*" className="hidden" onChange={handleProfilePhotoSelected} />
                <input ref={cameraInputRef} type="file" accept="image/*" capture="environment" className="hidden" onChange={handleProfilePhotoSelected} />
              </div>

              <div className="mt-3">
                <span style={{ fontSize: 11, fontWeight: 700, color: "#64748b" }}>Tipo de cuenta</span>
                <div className="grid grid-cols-3 gap-2 mt-1">
                  {ACCOUNT_ROLES.map((role) => {
                    const selected = accountRole === role.id;
                    return (
                      <button
                        type="button"
                        key={role.id}
                        onClick={() => setAccountRole(role.id)}
                        className="py-2.5 rounded-xl"
                        style={{
                          border: selected ? "1.5px solid #2563eb" : "1px solid #cbd5e1",
                          background: selected ? "#eff6ff" : "white",
                          color: selected ? "#2563eb" : "#475569",
                          fontSize: 12,
                          fontWeight: 800,
                        }}
                      >
                        {role.label}
                      </button>
                    );
                  })}
                </div>
              </div>

              <div className="mt-2">
                <ProfileSelect label="Localidad" value={localidad} onChange={setLocalidad} options={LOCATION_OPTIONS} />
              </div>

              <div className="grid grid-cols-2 gap-2 mt-2">
                <ProfileSelect label="Desde dia" value={availabilityDayFrom} onChange={setAvailabilityDayFrom} options={WEEK_DAYS.map((day) => day.value)} formatOption={(value) => WEEK_DAYS.find((day) => day.value === value)?.label ?? value} />
                <ProfileSelect label="Hasta dia" value={availabilityDayTo} onChange={setAvailabilityDayTo} options={WEEK_DAYS.map((day) => day.value)} formatOption={(value) => WEEK_DAYS.find((day) => day.value === value)?.label ?? value} />
              </div>

              <div className="grid grid-cols-2 gap-2 mt-2">
                <ProfileSelect label="Desde" value={availabilityFrom} onChange={setAvailabilityFrom} options={TIME_OPTIONS} />
                <ProfileSelect label="Hasta" value={availabilityTo} onChange={setAvailabilityTo} options={TIME_OPTIONS} />
              </div>

              <button
                onClick={handleSaveProfile}
                disabled={!canSave || saving}
                className="w-full mt-3 py-3 rounded-xl flex items-center justify-center gap-2"
                style={{
                  background: canSave ? "#2563eb" : "#cbd5e1",
                  color: "white",
                  fontWeight: 700,
                }}
              >
                {saving ? <Camera size={16} strokeWidth={1.8} /> : <Save size={16} strokeWidth={1.8} />}
                {saving ? "Guardando..." : "Guardar cambios"}
              </button>
            </div>
          ) : null}
        </div>

        <button
          onClick={onLogout}
          className="w-full py-3.5 rounded-2xl flex items-center justify-center gap-2 mt-2 transition-all active:scale-95"
          style={{
            background: "#fef2f2",
            color: "#ef4444",
            fontWeight: 700,
            fontSize: 14,
            border: "1.5px solid #fecaca",
          }}
        >
          <LogOut size={16} strokeWidth={1.8} />
          Cerrar sesion
        </button>
      </div>
      {cameraOpen ? (
        <div className="fixed inset-0 z-50 flex flex-col" style={{ background: "#020617" }}>
          <div className="flex items-center justify-between px-5 py-4">
            <span style={{ color: "white", fontSize: 15, fontWeight: 800 }}>Camara</span>
            <button
              type="button"
              onClick={stopCamera}
              className="flex items-center justify-center rounded-full"
              style={{ width: 38, height: 38, background: "rgba(255,255,255,0.14)" }}
            >
              <X size={19} color="white" strokeWidth={1.8} />
            </button>
          </div>
          <video ref={videoRef} autoPlay playsInline muted className="flex-1 min-h-0 object-cover" />
          <div className="px-6 py-5">
            <button
              type="button"
              onClick={captureCameraPhoto}
              className="w-full py-3.5 rounded-2xl"
              style={{ background: "#ffffff", color: "#0f172a", fontWeight: 800 }}
            >
              Usar foto
            </button>
          </div>
        </div>
      ) : null}
    </div>
  );
}

function ProfileInput({
  label,
  value,
  onChange,
  type = "text",
}: {
  label: string;
  value: string;
  onChange: (next: string) => void;
  type?: string;
}) {
  return (
    <label className="flex flex-col gap-1">
      <span style={{ fontSize: 11, fontWeight: 700, color: "#64748b" }}>{label}</span>
      <input
        type={type}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        className="px-3 py-2.5 rounded-xl"
        style={{ border: "1px solid #cbd5e1", fontSize: 13, color: "#0f172a", background: "white" }}
      />
    </label>
  );
}

function ProfileSelect({
  label,
  value,
  onChange,
  options,
  formatOption,
}: {
  label: string;
  value: string;
  onChange: (next: string) => void;
  options: string[];
  formatOption?: (value: string) => string;
}) {
  return (
    <label className="flex flex-col gap-1">
      <span style={{ fontSize: 11, fontWeight: 700, color: "#64748b" }}>{label}</span>
      <select
        value={value}
        onChange={(e) => onChange(e.target.value)}
        className="px-3 py-2.5 rounded-xl"
        style={{ border: "1px solid #cbd5e1", fontSize: 13, color: "#0f172a", background: "white" }}
      >
        {options.map((option) => (
          <option key={option} value={option}>{formatOption ? formatOption(option) : option}</option>
        ))}
      </select>
    </label>
  );
}

function InfoRow({
  icon,
  label,
  value,
}: {
  icon: React.ReactNode;
  label: string;
  value: React.ReactNode;
}) {
  return (
    <div className="flex items-center justify-between gap-3">
      <div className="flex items-center gap-2.5">
        {icon}
        <span style={{ fontSize: 13, color: "#64748b", fontWeight: 500 }}>{label}</span>
      </div>
      <span style={{ fontSize: 13, color: "#0f172a", fontWeight: 700, textAlign: "right" }}>{value}</span>
    </div>
  );
}

function RatingSummary({ summary }: { summary: ApiRatingSummary | null }) {
  const count = summary?.cantidadValoraciones ?? 0;
  const average = summary?.promedioEstrellas ?? 0;
  if (count === 0) return <span>Sin valoraciones</span>;
  return <span>{average.toFixed(1)} ({count})</span>;
}

function RatingStars({ value, color = "#eab308" }: { value: number; color?: string }) {
  const rounded = Math.round(value);
  return (
    <div className="flex items-center gap-1">
      {Array.from({ length: 5 }).map((_, index) => (
        <Star
          key={index}
          size={15}
          color={color}
          fill={index < rounded ? color : "transparent"}
          strokeWidth={1.8}
        />
      ))}
    </div>
  );
}

function formatRatingLabel(summary: ApiRatingSummary | null) {
  const count = summary?.cantidadValoraciones ?? 0;
  if (count === 0) return "Sin valoraciones todavia";
  const average = summary?.promedioEstrellas ?? 0;
  return `${average.toFixed(1)} promedio sobre ${count} valoraciones`;
}
