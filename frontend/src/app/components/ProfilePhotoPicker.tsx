import { useCallback, useEffect, useRef, useState } from "react";
import type { ChangeEvent } from "react";
import { createPortal } from "react-dom";
import { Camera, ImagePlus, X } from "lucide-react";

const MAX_PHOTO_EDGE = 1024;
const PHOTO_JPEG_QUALITY = 0.82;

interface ProfilePhotoPickerProps {
  value: string;
  onChange: (photoDataUrl: string) => void;
  onError: (message: string) => void;
  galleryLabel?: string;
  cameraLabel?: string;
}

export function ProfilePhotoPicker({
  value,
  onChange,
  onError,
  galleryLabel = "Elegir foto",
  cameraLabel = "Sacar foto",
}: ProfilePhotoPickerProps) {
  const galleryInputRef = useRef<HTMLInputElement | null>(null);
  const cameraInputRef = useRef<HTMLInputElement | null>(null);
  const videoRef = useRef<HTMLVideoElement | null>(null);
  const cameraStreamRef = useRef<MediaStream | null>(null);
  const mountedRef = useRef(true);
  const [cameraOpen, setCameraOpen] = useState(false);
  const [cameraReady, setCameraReady] = useState(false);

  useEffect(() => {
    if (cameraOpen && videoRef.current && cameraStreamRef.current) {
      videoRef.current.srcObject = cameraStreamRef.current;
    }
  }, [cameraOpen]);

  useEffect(() => {
    mountedRef.current = true;
    return () => {
      mountedRef.current = false;
      cameraStreamRef.current?.getTracks().forEach((track) => track.stop());
      cameraStreamRef.current = null;
    };
  }, []);

  const closeCamera = useCallback(() => {
    cameraStreamRef.current?.getTracks().forEach((track) => track.stop());
    cameraStreamRef.current = null;
    setCameraReady(false);
    setCameraOpen(false);
  }, []);

  useEffect(() => {
    if (!cameraOpen) return;
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === "Escape") closeCamera();
    };
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [cameraOpen, closeCamera]);

  const openCamera = async () => {
    onError("");
    setCameraReady(false);

    if (!navigator.mediaDevices?.getUserMedia) {
      cameraInputRef.current?.click();
      return;
    }

    try {
      const stream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: { ideal: "user" } },
        audio: false,
      });

      if (!mountedRef.current) {
        stream.getTracks().forEach((track) => track.stop());
        return;
      }

      cameraStreamRef.current = stream;
      setCameraOpen(true);
    } catch {
      if (!mountedRef.current) return;
      onError("No se pudo abrir la camara. Revisa el permiso e intenta nuevamente.");
      cameraInputRef.current?.click();
    }
  };

  const capturePhoto = () => {
    const video = videoRef.current;
    if (!video?.videoWidth || !video.videoHeight) {
      onError("La camara todavia no esta lista para capturar.");
      return;
    }

    const nextPhoto = resizePhoto(video, video.videoWidth, video.videoHeight);
    if (!nextPhoto) {
      onError("No se pudo capturar la imagen de la camara.");
      return;
    }

    onChange(nextPhoto);
    onError("");
    closeCamera();
  };

  const handleFileSelected = async (event: ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    event.target.value = "";
    if (!file) return;
    if (!file.type.startsWith("image/")) {
      onError("Selecciona una imagen valida para tu perfil.");
      return;
    }

    try {
      const nextPhoto = await resizePhotoFile(file);
      if (!nextPhoto) {
        onError("No se pudo leer la foto seleccionada.");
        return;
      }
      onChange(nextPhoto);
      onError("");
    } catch {
      onError("No se pudo leer la foto seleccionada.");
    }
  };

  return (
    <div className="flex flex-col items-center gap-2 py-2 w-full">
      <div
        className="flex items-center justify-center rounded-full relative overflow-hidden"
        style={{ width: 76, height: 76, background: "#f1f5f9", border: "2px dashed #cbd5e1" }}
      >
        {value ? (
          <img src={value} alt="Foto de perfil" style={{ width: "100%", height: "100%", objectFit: "cover" }} />
        ) : (
          <Camera size={22} color="#94a3b8" strokeWidth={1.7} />
        )}
      </div>

      <div className="grid grid-cols-2 gap-2 w-full">
        <button
          type="button"
          onClick={() => galleryInputRef.current?.click()}
          className="py-2.5 rounded-xl flex items-center justify-center gap-2"
          style={{ border: "1px solid #cbd5e1", color: "#0f172a", fontSize: 13, fontWeight: 700 }}
        >
          <ImagePlus size={15} strokeWidth={1.8} />
          {galleryLabel}
        </button>
        <button
          type="button"
          onClick={openCamera}
          className="py-2.5 rounded-xl flex items-center justify-center gap-2"
          style={{ border: "1px solid #cbd5e1", color: "#0f172a", fontSize: 13, fontWeight: 700 }}
        >
          <Camera size={15} strokeWidth={1.8} />
          {cameraLabel}
        </button>
      </div>

      <input ref={galleryInputRef} type="file" accept="image/*" className="hidden" onChange={handleFileSelected} />
      <input ref={cameraInputRef} type="file" accept="image/*" capture="user" className="hidden" onChange={handleFileSelected} />

      {cameraOpen
        ? createPortal(
            <div
              className="fixed inset-0 z-[70] flex flex-col"
              style={{ background: "#020617" }}
              role="dialog"
              aria-modal="true"
              aria-label="Camara para foto de perfil"
            >
              <div className="flex items-center justify-between px-5 py-4">
                <span style={{ color: "white", fontSize: 15, fontWeight: 800 }}>Camara</span>
                <button
                  type="button"
                  onClick={closeCamera}
                  autoFocus
                  className="flex items-center justify-center rounded-full"
                  style={{ width: 38, height: 38, background: "rgba(255,255,255,0.14)" }}
                  aria-label="Cerrar camara"
                >
                  <X size={19} color="white" strokeWidth={1.8} />
                </button>
              </div>
              <video
                ref={videoRef}
                autoPlay
                playsInline
                muted
                onCanPlay={() => setCameraReady(true)}
                className="flex-1 min-h-0 object-cover"
              />
              <div className="px-6 py-5">
                <button
                  type="button"
                  onClick={capturePhoto}
                  disabled={!cameraReady}
                  className="w-full py-3.5 rounded-2xl"
                  style={{
                    background: cameraReady ? "#ffffff" : "#64748b",
                    color: cameraReady ? "#0f172a" : "#e2e8f0",
                    fontWeight: 800,
                  }}
                >
                  {cameraReady ? "Usar foto" : "Preparando camara..."}
                </button>
              </div>
            </div>,
            document.body
          )
        : null}
    </div>
  );
}

function resizePhoto(
  source: CanvasImageSource,
  sourceWidth: number,
  sourceHeight: number
): string {
  const scale = Math.min(1, MAX_PHOTO_EDGE / Math.max(sourceWidth, sourceHeight));
  const width = Math.max(1, Math.round(sourceWidth * scale));
  const height = Math.max(1, Math.round(sourceHeight * scale));
  const canvas = document.createElement("canvas");
  canvas.width = width;
  canvas.height = height;
  const context = canvas.getContext("2d");
  if (!context) return "";

  // El frame de video se dibuja sin invertirlo: la foto final conserva izquierda y derecha reales.
  context.drawImage(source, 0, 0, width, height);
  return canvas.toDataURL("image/jpeg", PHOTO_JPEG_QUALITY);
}

async function resizePhotoFile(file: File): Promise<string> {
  if (typeof createImageBitmap === "function") {
    try {
      const bitmap = await createImageBitmap(file, { imageOrientation: "from-image" });
      try {
        return resizePhoto(bitmap, bitmap.width, bitmap.height);
      } finally {
        bitmap.close();
      }
    } catch {
      // Algunos navegadores exponen createImageBitmap pero no soportan la opcion de orientacion.
    }
  }

  const objectUrl = URL.createObjectURL(file);
  try {
    const image = await new Promise<HTMLImageElement>((resolve, reject) => {
      const nextImage = new Image();
      nextImage.onload = () => resolve(nextImage);
      nextImage.onerror = () => reject(new Error("No se pudo decodificar la imagen"));
      nextImage.src = objectUrl;
    });
    return resizePhoto(image, image.naturalWidth, image.naturalHeight);
  } finally {
    URL.revokeObjectURL(objectUrl);
  }
}
