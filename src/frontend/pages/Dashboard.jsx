import { useState, useEffect } from "react";
import { volunteersAPI } from "../api/volunteers";
import { eventsAPI } from "../api/events";
import LoadingSpinner from "../components/LoadingSpinner";
import ErrorAlert from "../components/ErrorAlert";
import { useAuth } from "../hooks/useAuth";

export default function Dashboard() {
  const { user } = useAuth();
  const [stats, setStats] = useState({
    volunteerCount: 0,
    eventCount: 0,
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        setLoading(true);
        setError(null);

        const [volunteerRes, eventsRes] = await Promise.all([
          volunteersAPI.getCount(),
          eventsAPI.getAll(),
        ]);

        setStats({
          volunteerCount: volunteerRes.data,
          eventCount: eventsRes.data.length,
        });
      } catch (err) {
        setError("Failed to load dashboard statistics");
        console.error(err);
      } finally {
        setLoading(false);
      }
    };

    fetchStats();
  }, []);

  const statCards = [
    {
      label: "Total Events",
      value: stats.eventCount,
      iconLabel: "EVT",
      color: "bg-green-100 text-green-700",
    },
  ];

  const roleValue = String(user?.role || "").toLowerCase();
  const isAdmin = roleValue === "admin";
  const resolvedStatCards = isAdmin
    ? [
        {
          label: "Total Volunteers",
          value: stats.volunteerCount,
          iconLabel: "VOL",
          color: "bg-blue-100 text-blue-700",
        },
        ...statCards,
      ]
    : statCards;

  return (
    <div className="space-y-8">
      {error && <ErrorAlert message={error} onClose={() => setError(null)} />}

      <div>
        <h1 className="text-3xl font-bold text-neutral-900">Dashboard</h1>
        <p className="text-neutral-600 mt-2">
          Overview of your volunteer management system
        </p>
      </div>

      {loading ? (
        <div className="flex justify-center py-12">
          <LoadingSpinner />
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {resolvedStatCards.map((card, idx) => (
            <div key={idx} className="card p-6">
              <div className="flex items-start justify-between">
                <div>
                  <p className="text-neutral-600 text-sm font-medium">
                    {card.label}
                  </p>
                  <p className="text-3xl font-bold text-neutral-900 mt-2">
                    {card.value}
                  </p>
                </div>
                <div className={`${card.color} p-3 rounded-lg text-2xl`}>
                  <span className="text-xs font-semibold tracking-wide">
                    {card.iconLabel}
                  </span>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      <div className="card p-6">
        <h2 className="text-lg font-semibold text-neutral-900 mb-4">
          Quick Access
        </h2>
        <div
          className={`grid grid-cols-1 ${isAdmin ? "md:grid-cols-3" : "md:grid-cols-2"} gap-4`}
        >
          {isAdmin && (
            <a
              href="/volunteers"
              className="flex items-center gap-3 p-4 rounded-lg border border-neutral-200 hover:border-primary-600 hover:bg-primary-50 transition-all"
            >
              <div>
                <h3 className="font-medium text-neutral-900">Volunteers</h3>
                <p className="text-xs text-neutral-600">Manage volunteers</p>
              </div>
            </a>
          )}
          <a
            href="/events"
            className="flex items-center gap-3 p-4 rounded-lg border border-neutral-200 hover:border-primary-600 hover:bg-primary-50 transition-all"
          >
            <div>
              <h3 className="font-medium text-neutral-900">Events</h3>
              <p className="text-xs text-neutral-600">
                {isAdmin ? "Manage events" : "Enroll in events for volunteer"}
              </p>
            </div>
          </a>
          <a
            href="/certificates"
            className="flex items-center gap-3 p-4 rounded-lg border border-neutral-200 hover:border-primary-600 hover:bg-primary-50 transition-all"
          >
            <div>
              <h3 className="font-medium text-neutral-900">
                {isAdmin ? "Certificates" : "My Certificates"}
              </h3>
              <p className="text-xs text-neutral-600">
                {isAdmin ? "View certificates" : "View your certificates"}
              </p>
            </div>
          </a>
        </div>
      </div>
    </div>
  );
}
