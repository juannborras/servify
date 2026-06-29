import { useRef, useState, type TouchEvent } from "react";
import { Loader2 } from "lucide-react";

type RefreshHandler = () => Promise<void> | void;

export function usePullToRefresh(onRefresh: RefreshHandler, enabled = true) {
  const [pullDistance, setPullDistance] = useState(0);
  const [refreshing, setRefreshing] = useState(false);
  const startYRef = useRef<number | null>(null);
  const trackingRef = useRef(false);
  const distanceRef = useRef(0);
  const refreshingRef = useRef(false);

  const updateDistance = (value: number) => {
    distanceRef.current = value;
    setPullDistance(value);
  };

  const runRefresh = async () => {
    if (refreshingRef.current) return;
    refreshingRef.current = true;
    setRefreshing(true);
    try {
      await onRefresh();
    } finally {
      refreshingRef.current = false;
      setRefreshing(false);
    }
  };

  const onTouchStart = (event: TouchEvent<HTMLElement>) => {
    if (!enabled || event.touches.length !== 1 || refreshingRef.current) return;
    if (event.currentTarget.scrollTop > 0) return;
    trackingRef.current = true;
    startYRef.current = event.touches[0].clientY;
  };

  const onTouchMove = (event: TouchEvent<HTMLElement>) => {
    if (!trackingRef.current || startYRef.current == null || refreshingRef.current) return;
    if (event.currentTarget.scrollTop > 0) {
      trackingRef.current = false;
      updateDistance(0);
      return;
    }

    const delta = event.touches[0].clientY - startYRef.current;
    updateDistance(delta > 0 ? Math.min(74, delta * 0.55) : 0);
  };

  const onTouchEnd = () => {
    if (!trackingRef.current) return;
    const shouldRefresh = distanceRef.current >= 48;
    trackingRef.current = false;
    startYRef.current = null;
    updateDistance(0);
    if (shouldRefresh) void runRefresh();
  };

  return {
    pullDistance,
    refreshing,
    pullHandlers: {
      onTouchStart,
      onTouchMove,
      onTouchEnd,
      onTouchCancel: onTouchEnd,
    },
  };
}

export function PullToRefreshIndicator({
  pullDistance,
  refreshing,
}: {
  pullDistance: number;
  refreshing: boolean;
}) {
  const visible = refreshing || pullDistance > 8;

  return (
    <div
      className="flex shrink-0 items-center justify-center gap-2 overflow-hidden transition-all"
      style={{
        height: visible ? 42 : 0,
        opacity: visible ? 1 : 0,
        transform: visible ? "translateY(0)" : "translateY(-8px)",
      }}
    >
      <Loader2
        size={16}
        color="#2563eb"
        strokeWidth={2.2}
        className={refreshing ? "animate-spin" : ""}
        style={{ transform: refreshing ? undefined : `rotate(${pullDistance * 4}deg)` }}
      />
      <span style={{ color: "#64748b", fontSize: 12, fontWeight: 800 }}>
        {refreshing ? "Actualizando..." : "Solta para actualizar"}
      </span>
    </div>
  );
}
