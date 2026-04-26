import { Link, useLocation } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";

const menuItems = [
  { label: "Dashboard", path: "/dashboard" },
  { label: "Volunteers", path: "/volunteers" },
  { label: "Events", path: "/events" },
  { label: "Certificates", path: "/certificates" },
];

export default function Sidebar() {
  const location = useLocation();
  const { user } = useAuth();

  const roleValue = String(user?.role || "").toLowerCase();
  const isAdmin = roleValue === "admin";
  const resolvedMenuItems = isAdmin
    ? menuItems
    : menuItems
        .filter((item) => item.path !== "/volunteers")
        .map((item) => {
          if (item.path === "/certificates") {
            return { ...item, label: "My Certificates" };
          }
          return item;
        });

  return (
    <aside className="w-64 bg-slate-50/80 backdrop-blur-sm border-r border-slate-200 hidden md:block h-[calc(100vh-64px)] sticky top-16">
      <nav className="p-4 space-y-2">
        {resolvedMenuItems.map((item) => {
          const isActive = location.pathname.startsWith(item.path);
          return (
            <Link
              key={item.path}
              to={item.path}
              className={`flex items-center gap-3 px-4 py-3 rounded-lg transition-colors ${
                isActive
                  ? "bg-primary-100 text-primary-700 font-medium"
                  : "text-neutral-700 hover:bg-neutral-100"
              }`}
            >
              <span>{item.label}</span>
            </Link>
          );
        })}
      </nav>
    </aside>
  );
}
