import servisMascotImage from "../../imports/servis-mascot.png";

type ServisMascotSize = "xs" | "sm" | "md" | "lg";
export type ServisMascotPose = "peek" | "wave" | "coach" | "lean" | "bounce";

interface ServisMascotProps {
  size?: ServisMascotSize;
  pose?: ServisMascotPose;
  className?: string;
  alt?: string;
  decorative?: boolean;
}

const heightMap: Record<ServisMascotSize, number> = {
  xs: 44,
  sm: 62,
  md: 86,
  lg: 118,
};

const widthRatioByPose: Record<ServisMascotPose, number> = {
  coach: 0.665,
  wave: 0.744,
  peek: 0.734,
  lean: 0.775,
  bounce: 0.756,
};

const poseAssetModules = import.meta.glob<string>("../../imports/servis-mascot-*.png", {
  eager: true,
  query: "?url",
  import: "default",
});

function getPoseImage(pose: ServisMascotPose): string {
  return poseAssetModules[`../../imports/servis-mascot-${pose}.png`] ?? servisMascotImage;
}

export function ServisMascot({
  size = "sm",
  pose = "coach",
  className = "",
  alt = "Servis",
  decorative = false,
}: ServisMascotProps) {
  const height = heightMap[size];
  const width = Math.round(height * widthRatioByPose[pose]);

  return (
    <span
      className={`servis-mascot servis-mascot-${pose} ${className}`}
      data-servis-mascot-pose={pose}
      aria-hidden={decorative ? "true" : undefined}
      style={{
        position: "relative",
        display: "inline-block",
        width,
        height,
        flexShrink: 0,
        pointerEvents: "none",
      }}
    >
      <span
        className="servis-mascot-shadow"
        aria-hidden="true"
        style={{
          position: "absolute",
          left: "16%",
          right: "12%",
          bottom: 1,
          height: Math.max(5, Math.round(height * 0.07)),
          borderRadius: 999,
          background: "rgba(15, 23, 42, 0.12)",
          filter: "blur(3px)",
          transform: "scaleX(1.08)",
        }}
      />
      <img
        className="servis-mascot-image"
        src={getPoseImage(pose)}
        alt={decorative ? "" : alt}
        draggable={false}
        onError={({ currentTarget }) => {
          currentTarget.onerror = null;
          currentTarget.src = servisMascotImage;
        }}
        style={{
          position: "absolute",
          inset: 0,
          width: "100%",
          height: "100%",
          objectFit: "contain",
          filter: "drop-shadow(0 7px 8px rgba(15, 23, 42, 0.14))",
          userSelect: "none",
        }}
      />
    </span>
  );
}
