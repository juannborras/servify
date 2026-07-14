
  import { createRoot } from "react-dom/client";
  import App from "./app/App.tsx";
import "./styles/index.css";

installMobileViewportLock();

createRoot(document.getElementById("root")!).render(<App />);

if ('serviceWorker' in navigator && import.meta.env.PROD) {
  window.addEventListener('load', () => {
    navigator.serviceWorker
      .register('/sw.js')
      .then((registration) => {
        void registration.update();
        if (registration.waiting) {
          registration.waiting.postMessage({ type: 'SKIP_WAITING' });
        }
        registration.addEventListener('updatefound', () => {
          const worker = registration.installing;
          if (!worker) return;
          worker.addEventListener('statechange', () => {
            if (worker.state === 'installed' && navigator.serviceWorker.controller) {
              worker.postMessage({ type: 'SKIP_WAITING' });
            }
          });
        });
      })
      .catch(() => {
        // La app sigue funcionando aunque el navegador no permita instalarla.
      });
  });
}

function installMobileViewportLock() {
  let lastTouchEnd = 0;
  document.documentElement.style.setProperty("-webkit-text-size-adjust", "100%");

  document.addEventListener(
    "gesturestart",
    (event) => {
      event.preventDefault();
    },
    { passive: false }
  );

  document.addEventListener(
    "touchmove",
    (event) => {
      if (event.touches.length > 1) {
        event.preventDefault();
      }
    },
    { passive: false }
  );

  document.addEventListener(
    "touchend",
    (event) => {
      const now = Date.now();
      if (now - lastTouchEnd <= 300) {
        event.preventDefault();
      }
      lastTouchEnd = now;
    },
    { passive: false }
  );

  document.addEventListener(
    "wheel",
    (event) => {
      if (event.ctrlKey) {
        event.preventDefault();
      }
    },
    { passive: false }
  );

  document.addEventListener(
    "keydown",
    (event) => {
      if ((event.ctrlKey || event.metaKey) && ["+", "-", "="].includes(event.key)) {
        event.preventDefault();
      }
    },
    { passive: false }
  );
}
