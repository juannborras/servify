import { Compass, FileText, Briefcase, PlusSquare, User } from "lucide-react";

type Tab = "explore" | "requests" | "create-request" | "my-services" | "publish" | "profile";

interface BottomNavProps {
  activeTab: Tab;
  onTabChange: (tab: Tab) => void;
}

const tabs = [
  { id: "explore" as Tab, label: "Explorar", icon: Compass },
  { id: "requests" as Tab, label: "Solicitudes", icon: FileText },
  { id: "create-request" as Tab, label: "Solicitar", icon: PlusSquare },
  { id: "my-services" as Tab, label: "Mis servicios", icon: Briefcase },
  { id: "profile" as Tab, label: "Perfil", icon: User },
];

export function BottomNav({ activeTab, onTabChange }: BottomNavProps) {
  return (
    <div className="flex items-center justify-around bg-white border-t border-gray-100 px-2 py-2 safe-area-bottom">
      {tabs.map(({ id, label, icon: Icon }) => {
        const active = activeTab === id;
        const primary = id === "create-request";
        return (
          <button
            key={id}
            onClick={() => onTabChange(id)}
            className="flex flex-col items-center gap-0.5 px-2 py-1 rounded-xl transition-all active:scale-95"
            style={{ minWidth: primary ? 58 : 52, transform: primary ? "translateY(-10px)" : "none" }}
          >
            {primary ? (
              <span
                className="servify-bottom-primary flex items-center justify-center rounded-2xl"
                style={{
                  width: 48,
                  height: 48,
                  background: "linear-gradient(135deg, #2563eb 0%, #0891b2 100%)",
                  boxShadow: "0 10px 24px rgba(37,99,235,0.28)",
                  border: "3px solid white",
                }}
              >
                <Icon size={24} color="white" strokeWidth={2.2} />
              </span>
            ) : (
              <Icon
                size={22}
                strokeWidth={active ? 2.2 : 1.7}
                style={{ color: active ? "#2563eb" : "#94a3b8" }}
              />
            )}
            <span
              style={{
                fontSize: 10,
                fontWeight: active || primary ? 700 : 500,
                color: active ? "#2563eb" : primary ? "#475569" : "#94a3b8",
                letterSpacing: "0.01em",
              }}
            >
              {label}
            </span>
          </button>
        );
      })}
    </div>
  );
}
