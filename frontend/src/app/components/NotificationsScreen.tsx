import { useEffect, useMemo, useState } from "react";
import { ArrowLeft, Bell, CheckCircle2, ChevronRight, RefreshCcw, ShieldAlert, X } from "lucide-react";
import { motion } from "motion/react";
import { servifyApi, type ApiNotification } from "../api";
import { PullToRefreshIndicator, usePullToRefresh } from "./PullToRefresh";

interface NotificationsScreenProps {
  userId?: string;
  onBack: () => void;
  onOpenReference: (notification: ApiNotification) => void;
  onUnreadCountChange?: (count: number) => void;
}

export function NotificationsScreen({
  userId,
  onBack,
  onOpenReference,
  onUnreadCountChange,
}: NotificationsScreenProps) {
  const [notifications, setNotifications] = useState<ApiNotification[]>([]);
  const [loading, setLoading] = useState(false);
  const [actionLoading, setActionLoading] = useState("");
  const [error, setError] = useState("");

  const unreadCount = useMemo(
    () => notifications.filter((notification) => !notification.leida).length,
    [notifications]
  );

  const loadNotifications = async () => {
    if (!userId) return;
    setLoading(true);
    setError("");
    try {
      const items = await servifyApi.listNotifications(userId);
      setNotifications(items);
      onUnreadCountChange?.(items.filter((item) => !item.leida).length);
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudieron cargar las notificaciones");
    } finally {
      setLoading(false);
    }
  };

  const { pullDistance, refreshing, pullHandlers } = usePullToRefresh(
    async () => {
      await loadNotifications();
    },
    Boolean(userId)
  );

  useEffect(() => {
    void loadNotifications();
  }, [userId]);

  const markRead = async (notification: ApiNotification) => {
    if (!userId || notification.leida) return notification;
    setActionLoading(notification.id);
    setError("");
    try {
      const updated = await servifyApi.markNotificationRead(userId, notification.id);
      setNotifications((current) => {
        const next = current.map((item) => (item.id === updated.id ? updated : item));
        onUnreadCountChange?.(next.filter((item) => !item.leida).length);
        return next;
      });
      return updated;
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo marcar como leida");
      return notification;
    } finally {
      setActionLoading("");
    }
  };

  const markAllRead = async () => {
    if (!userId) return;
    const unread = notifications.filter((notification) => !notification.leida);
    if (unread.length === 0) return;
    setActionLoading("all");
    setError("");
    try {
      const updated = await Promise.all(
        unread.map((notification) => servifyApi.markNotificationRead(userId, notification.id))
      );
      const updatedById = new Map(updated.map((notification) => [notification.id, notification]));
      setNotifications((current) => {
        const next = current.map((item) => updatedById.get(item.id) ?? item);
        onUnreadCountChange?.(next.filter((item) => !item.leida).length);
        return next;
      });
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudieron marcar todas como leidas");
    } finally {
      setActionLoading("");
    }
  };

  const deleteNotification = async (notification: ApiNotification) => {
    if (!userId) return;
    setActionLoading(`delete-${notification.id}`);
    setError("");
    try {
      await servifyApi.deleteNotification(userId, notification.id);
      setNotifications((current) => {
        const next = current.filter((item) => item.id !== notification.id);
        onUnreadCountChange?.(next.filter((item) => !item.leida).length);
        return next;
      });
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo quitar la notificacion");
    } finally {
      setActionLoading("");
    }
  };

  const openReference = async (notification: ApiNotification) => {
    const updated = await markRead(notification);
    onOpenReference(updated);
  };

  return (
    <div className="servify-dark-screen servify-notifications-screen flex h-full flex-col" style={{ background: "#f8fafc" }}>
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
            <p className="servify-text-muted" style={{ color: "#64748b", fontSize: 12, fontWeight: 800 }}>Centro</p>
            <h1 className="servify-text-primary" style={{ color: "#0f172a", fontSize: 22, fontWeight: 900, lineHeight: 1.15 }}>
              Notificaciones
            </h1>
          </div>
          <div
            className="relative flex items-center justify-center rounded-2xl"
            style={{ width: 44, height: 44, background: "#eff6ff", color: "#2563eb" }}
          >
            <Bell size={20} strokeWidth={2.1} />
            {unreadCount > 0 ? <UnreadBadge count={unreadCount} /> : null}
          </div>
        </div>

        <div className="mt-4 flex items-center justify-between gap-3">
          <p className="servify-text-muted" style={{ color: "#64748b", fontSize: 13, fontWeight: 700 }}>
            {unreadCount > 0 ? `${unreadCount} sin leer` : "Todo al dia"}
          </p>
          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={loadNotifications}
              disabled={loading}
              className="flex items-center gap-1.5 rounded-xl px-3 py-2 transition-all active:scale-95"
              style={{ background: "#f1f5f9", color: "#475569", fontSize: 12, fontWeight: 900 }}
            >
              <RefreshCcw size={14} strokeWidth={2} />
              Actualizar
            </button>
            {unreadCount > 0 ? (
              <button
                type="button"
                onClick={markAllRead}
                disabled={actionLoading === "all"}
                className="flex items-center gap-1.5 rounded-xl px-3 py-2 transition-all active:scale-95"
                style={{ background: "#eff6ff", color: "#2563eb", fontSize: 12, fontWeight: 900 }}
              >
                <CheckCircle2 size={14} strokeWidth={2} />
                Leer todas
              </button>
            ) : null}
          </div>
        </div>
      </div>

      <div className="flex-1 overflow-y-auto px-5 pb-7 pt-4" {...pullHandlers}>
        <PullToRefreshIndicator pullDistance={pullDistance} refreshing={refreshing} />
        {error ? (
          <p className="mb-3 rounded-2xl px-4 py-3" style={{ background: "#fef2f2", color: "#b91c1c", fontSize: 13, fontWeight: 800 }}>
            {error}
          </p>
        ) : null}

        {loading ? (
          <p className="servify-text-muted" style={{ color: "#64748b", fontSize: 13, fontWeight: 800 }}>Cargando notificaciones...</p>
        ) : null}

        {!loading && notifications.length === 0 ? (
          <div className="servify-empty-state rounded-3xl px-5 py-10 text-center">
            <Bell size={34} color="#94a3b8" strokeWidth={1.7} />
            <p className="servify-text-primary" style={{ color: "#0f172a", fontSize: 16, fontWeight: 900, marginTop: 12 }}>
              Sin notificaciones
            </p>
            <p className="servify-text-muted" style={{ color: "#64748b", fontSize: 13, fontWeight: 700, lineHeight: 1.4, marginTop: 5 }}>
              Los avisos sobre tu cuenta, publicaciones y solicitudes van a aparecer aca.
            </p>
          </div>
        ) : null}

        <div className="flex flex-col gap-3">
          {notifications.map((notification, index) => (
            <NotificationCard
              key={notification.id}
              notification={notification}
              index={index}
              loading={actionLoading === notification.id}
              deleting={actionLoading === `delete-${notification.id}`}
              onMarkRead={() => markRead(notification)}
              onOpenReference={() => openReference(notification)}
              onDelete={() => deleteNotification(notification)}
            />
          ))}
        </div>
      </div>
    </div>
  );
}

function NotificationCard({
  notification,
  index,
  loading,
  deleting,
  onMarkRead,
  onOpenReference,
  onDelete,
}: {
  notification: ApiNotification;
  index: number;
  loading: boolean;
  deleting: boolean;
  onMarkRead: () => void;
  onOpenReference: () => void;
  onDelete: () => void;
}) {
  const unread = !notification.leida;
  const hasReference = Boolean(notification.referenciaTipo && notification.referenciaId);

  return (
    <motion.article
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ delay: index * 0.025, duration: 0.2 }}
      className="servify-card rounded-2xl bg-white p-4"
      style={{
        border: unread ? "1.5px solid rgba(37,99,235,0.32)" : "1px solid rgba(0,0,0,0.06)",
        boxShadow: unread ? "0 12px 26px rgba(37,99,235,0.10)" : "0 1px 4px rgba(0,0,0,0.04)",
      }}
    >
      <div className="flex items-start gap-3">
        <div
          className="flex shrink-0 items-center justify-center rounded-2xl"
          style={{ width: 44, height: 44, background: unread ? "#eff6ff" : "#f1f5f9", color: unread ? "#2563eb" : "#64748b" }}
        >
          <ShieldAlert size={20} strokeWidth={2} />
        </div>
        <div className="min-w-0 flex-1">
          <div className="flex items-start justify-between gap-2">
            <div className="min-w-0">
              <p className="servify-text-primary" style={{ color: "#0f172a", fontSize: 15, fontWeight: 900, lineHeight: 1.25 }}>
                {notification.titulo}
              </p>
              <p className="servify-text-soft" style={{ color: "#94a3b8", fontSize: 11, fontWeight: 800, marginTop: 3 }}>
                {formatNotificationDate(notification.fechaCreacion)}
              </p>
            </div>
            <div className="flex shrink-0 items-center gap-1">
              {unread ? (
                <span className="rounded-full px-2 py-1" style={{ background: "#dbeafe", color: "#2563eb", fontSize: 10, fontWeight: 900 }}>
                  Nueva
                </span>
              ) : null}
              <button
                type="button"
                aria-label="Quitar notificacion"
                onClick={onDelete}
                disabled={deleting}
                className="flex items-center justify-center rounded-full transition-all active:scale-95"
                style={{ width: 24, height: 24, background: "#f1f5f9", color: "#64748b", opacity: deleting ? 0.55 : 1 }}
              >
                <X size={13} strokeWidth={2.4} />
              </button>
            </div>
          </div>
          <p className="servify-text-secondary" style={{ color: "#475569", fontSize: 13, fontWeight: 700, lineHeight: 1.45, marginTop: 8 }}>
            {notification.mensaje}
          </p>
          <div className="mt-3 flex flex-wrap gap-2">
            {unread ? (
              <button
                type="button"
                onClick={onMarkRead}
                disabled={loading}
                className="rounded-xl px-3 py-2 transition-all active:scale-95"
                style={{ background: "#f1f5f9", color: "#475569", fontSize: 11, fontWeight: 900 }}
              >
                Marcar leida
              </button>
            ) : null}
            {hasReference ? (
              <button
                type="button"
                onClick={onOpenReference}
                className="flex items-center gap-1 rounded-xl px-3 py-2 transition-all active:scale-95"
                style={{ background: "#eff6ff", color: "#2563eb", fontSize: 11, fontWeight: 900 }}
              >
                Ver detalle
                <ChevronRight size={14} strokeWidth={2} />
              </button>
            ) : null}
          </div>
        </div>
      </div>
    </motion.article>
  );
}

function UnreadBadge({ count }: { count: number }) {
  return (
    <div
      className="absolute -right-1 -top-1 flex items-center justify-center rounded-full"
      style={{ minWidth: 18, height: 18, padding: "0 5px", background: "#ef4444", border: "2px solid white" }}
    >
      <span style={{ color: "white", fontSize: 10, fontWeight: 900 }}>{count > 9 ? "9+" : count}</span>
    </div>
  );
}

function formatNotificationDate(value?: string): string {
  if (!value) return "Sin fecha";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "Sin fecha";
  return date.toLocaleString("es-AR", {
    day: "2-digit",
    month: "2-digit",
    year: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
}
