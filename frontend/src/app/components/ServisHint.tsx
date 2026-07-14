import { ServisMascot, type ServisMascotPose } from "./ServisMascot";

type ServisHintTone = "info" | "success" | "quiet";

const AUTO_POSES = ["coach", "wave", "peek", "lean", "bounce"] as const satisfies readonly ServisMascotPose[];

interface ServisHintProps {
  title: string;
  detail: string;
  tone?: ServisHintTone;
  pose?: ServisMascotPose;
  compact?: boolean;
  className?: string;
}

export function ServisHint({
  title,
  detail,
  tone = "info",
  pose,
  compact = false,
  className = "",
}: ServisHintProps) {
  const resolvedPose = pose ?? pickPose(`${title}|${detail}`);

  return (
    <div
      className={`servis-hint servis-hint-${tone} servis-hint-${resolvedPose} ${compact ? "servis-hint-compact" : ""} ${className}`.trim()}
      data-servis-pose={resolvedPose}
    >
      <span className="servis-hint-character" aria-hidden="true">
        <ServisMascot size={compact ? "xs" : "sm"} pose={resolvedPose} className="servis-hint-mascot" decorative />
      </span>
      <div className="servis-speech-bubble">
        <p className="servis-hint-title">{title}</p>
        <p className="servis-hint-detail">{detail}</p>
      </div>
    </div>
  );
}

function pickPose(seed: string): ServisMascotPose {
  let hash = 0;
  for (let index = 0; index < seed.length; index += 1) {
    hash = (hash * 31 + seed.charCodeAt(index)) >>> 0;
  }
  return AUTO_POSES[hash % AUTO_POSES.length] ?? "coach";
}
