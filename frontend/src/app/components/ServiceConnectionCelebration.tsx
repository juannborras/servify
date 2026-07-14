import { useEffect } from "react";
import { AnimatePresence, motion, useReducedMotion } from "motion/react";
import { ServisMascot } from "./ServisMascot";
import "../../styles/service-connection-celebration.css";

interface ServiceConnectionCelebrationProps {
  visible: boolean;
  requesterName: string;
  requesterInitials: string;
  providerName: string;
  providerInitials: string;
  onFinished: () => void;
}

export function ServiceConnectionCelebration({
  visible,
  requesterName,
  requesterInitials,
  providerName,
  providerInitials,
  onFinished,
}: ServiceConnectionCelebrationProps) {
  const reduceMotion = useReducedMotion();

  useEffect(() => {
    if (!visible) return;
    const timeoutId = window.setTimeout(onFinished, reduceMotion ? 1500 : 2300);
    return () => window.clearTimeout(timeoutId);
  }, [onFinished, reduceMotion, visible]);

  const transition = reduceMotion
    ? { duration: 0.12 }
    : { type: "spring" as const, stiffness: 260, damping: 21 };

  return (
    <AnimatePresence>
      {visible ? (
        <motion.div
          className="servify-connection-celebration"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          transition={{ duration: reduceMotion ? 0.1 : 0.2 }}
        >
          <motion.div
            className="servify-connection-card"
            role="status"
            aria-live="polite"
            aria-atomic="true"
            initial={reduceMotion ? { opacity: 0 } : { opacity: 0, y: -14, scale: 0.96 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={reduceMotion ? { opacity: 0 } : { opacity: 0, y: -8, scale: 0.98 }}
            transition={transition}
          >
            <p className="servify-connection-eyebrow">Conexión confirmada</p>
            <div className="servify-connection-people" aria-hidden="true">
              <ConnectionPerson
                align="left"
                initials={requesterInitials}
                name={requesterName}
                reduceMotion={Boolean(reduceMotion)}
              />
              <div className="servify-connection-bridge">
                <motion.span
                  className="servify-connection-line"
                  initial={{ scaleX: reduceMotion ? 1 : 0, opacity: reduceMotion ? 1 : 0.4 }}
                  animate={{ scaleX: 1, opacity: 1 }}
                  transition={{ delay: reduceMotion ? 0 : 0.25, duration: reduceMotion ? 0.1 : 0.48 }}
                />
                <motion.span
                  className="servify-connection-servis"
                  initial={reduceMotion ? { opacity: 0 } : { opacity: 0, scale: 0.65, rotate: -8 }}
                  animate={{ opacity: 1, scale: 1, rotate: 0 }}
                  transition={{ delay: reduceMotion ? 0 : 0.4, ...transition }}
                >
                  <ServisMascot size="xs" pose="wave" decorative />
                </motion.span>
              </div>
              <ConnectionPerson
                align="right"
                initials={providerInitials}
                name={providerName}
                reduceMotion={Boolean(reduceMotion)}
              />
            </div>
            <p className="servify-connection-title">¡Ya pueden coordinar el servicio!</p>
          </motion.div>
        </motion.div>
      ) : null}
    </AnimatePresence>
  );
}

interface ConnectionPersonProps {
  align: "left" | "right";
  initials: string;
  name: string;
  reduceMotion: boolean;
}

function ConnectionPerson({ align, initials, name, reduceMotion }: ConnectionPersonProps) {
  const offset = align === "left" ? -20 : 20;
  return (
    <motion.span
      className="servify-connection-person"
      initial={reduceMotion ? { opacity: 0 } : { opacity: 0, x: offset }}
      animate={{ opacity: 1, x: 0 }}
      transition={{ delay: reduceMotion ? 0 : 0.12, duration: reduceMotion ? 0.1 : 0.36 }}
    >
      <span className={`servify-connection-avatar servify-connection-avatar-${align}`}>{initials}</span>
      <span className="servify-connection-name">{name}</span>
    </motion.span>
  );
}
