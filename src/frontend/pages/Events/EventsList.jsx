import { useState, useEffect } from "react";
import { eventsAPI } from "../../api/events";
import LoadingSpinner from "../../components/LoadingSpinner";
import ErrorAlert from "../../components/ErrorAlert";
import SuccessAlert from "../../components/SuccessAlert";
import EmptyState from "../../components/EmptyState";
import Modal from "../../components/Modal";
import EventForm from "./EventForm";
import { useAuth } from "../../hooks/useAuth";
import { volunteersAPI } from "../../api/volunteers";

export default function EventsList() {
  const { user } = useAuth();
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [deleteConfirm, setDeleteConfirm] = useState(null);
  const [myVolunteerId, setMyVolunteerId] = useState(null);
  const [myVolunteerName, setMyVolunteerName] = useState(null);
  const [enrolledEventIds, setEnrolledEventIds] = useState(new Set());
  const [enrollingEventId, setEnrollingEventId] = useState(null);
  const roleValue = String(user?.role || "").toLowerCase();
  const isAdmin = roleValue === "admin";
  const nextEventId = getNextEventId(events);

  function getNextEventId(eventList) {
    const parsed = eventList
      .map((event) => (event.eventId || "").match(/^([A-Za-z]*)(\d+)$/))
      .filter(Boolean)
      .map((match) => ({
        prefix: match[1] || "EV",
        value: parseInt(match[2], 10),
      }));

    if (parsed.length === 0) {
      return "EV001";
    }

    const highest = parsed.reduce((max, current) =>
      current.value > max.value ? current : max,
    );

    return `${highest.prefix}${String(highest.value + 1).padStart(3, "0")}`;
  }

  useEffect(() => {
    fetchEvents();
  }, []);

  useEffect(() => {
    if (!isAdmin) {
      resolveVolunteerIdentity();
    }
  }, [isAdmin, user?.email, user?.username]);

  useEffect(() => {
    if (!isAdmin && events.length > 0 && myVolunteerName) {
      loadEnrollmentState();
    }
  }, [isAdmin, events, myVolunteerName]);

  const fetchEvents = async () => {
    try {
      setLoading(true);
      setError(null);
      const { data } = await eventsAPI.getAll();
      setEvents(data);
    } catch (err) {
      setError("Failed to load events");
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    try {
      setError(null);
      await eventsAPI.delete(id);
      setEvents((prev) => prev.filter((e) => e.eventId !== id));
      setSuccess("Event deleted successfully");
      setDeleteConfirm(null);
      setTimeout(() => setSuccess(null), 3000);
    } catch (err) {
      setError("Failed to delete event");
      console.error(err);
    }
  };

  const handleEventAdded = () => {
    setSuccess("Event created successfully");
    setShowForm(false);
    fetchEvents();
    setTimeout(() => setSuccess(null), 3000);
  };

  const formatDate = (date) => {
    return new Date(date).toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
    });
  };

  const resolveVolunteerIdentity = async () => {
    try {
      const { data } = await volunteersAPI.getAll();
      const matchedVolunteer = data.find(
        (vol) =>
          (vol.email && vol.email === user?.email) ||
          (vol.name && vol.name === user?.username),
      );
      setMyVolunteerId(matchedVolunteer?.volunteerId || null);
      setMyVolunteerName(matchedVolunteer?.name || user?.username || null);
    } catch (err) {
      console.error(err);
      setMyVolunteerId(null);
      setMyVolunteerName(user?.username || null);
    }
  };

  const loadEnrollmentState = async () => {
    try {
      const checks = await Promise.all(
        events.map(async (event) => {
          const statsResponse = await eventsAPI.getStats(event.eventId);
          const stats = statsResponse.data || {};
          const isEnrolled = Object.prototype.hasOwnProperty.call(
            stats,
            myVolunteerName,
          );
          return { eventId: event.eventId, isEnrolled };
        }),
      );

      const enrolled = new Set(
        checks.filter((item) => item.isEnrolled).map((item) => item.eventId),
      );
      setEnrolledEventIds(enrolled);
    } catch (err) {
      console.error(err);
    }
  };

  const handleToggleEnrollment = async (eventId) => {
    if (!myVolunteerId) {
      setError("Volunteer profile not found for current account");
      return;
    }

    const isCurrentlyEnrolled = enrolledEventIds.has(eventId);

    try {
      setEnrollingEventId(eventId);
      setError(null);

      if (isCurrentlyEnrolled) {
        await eventsAPI.unenrollVolunteer(eventId, myVolunteerId);
        setEnrolledEventIds((prev) => {
          const next = new Set(prev);
          next.delete(eventId);
          return next;
        });
        setSuccess("Unenrolled from event successfully");
      } else {
        await eventsAPI.enrollVolunteer(eventId, myVolunteerId);
        setEnrolledEventIds((prev) => new Set(prev).add(eventId));
        setSuccess("Enrolled in event successfully");
      }

      setTimeout(() => setSuccess(null), 3000);
    } catch (err) {
      setError(err.response?.data?.error || "Failed to update enrollment");
      console.error(err);
    } finally {
      setEnrollingEventId(null);
    }
  };

  const handleCompleteEventDirect = async (event) => {
    if (!event.finishDate) {
      setError("Event end date is required to complete the event");
      return;
    }

    try {
      setError(null);
      await eventsAPI.completeEvent(event.eventId, event.finishDate);
      setSuccess("Event completed and certificates generated");
      fetchEvents();
      setTimeout(() => setSuccess(null), 3000);
    } catch (err) {
      setError(err.response?.data?.error || "Failed to complete event");
      console.error(err);
    }
  };

  return (
    <div className="space-y-6">
      {error && <ErrorAlert message={error} onClose={() => setError(null)} />}
      {success && (
        <SuccessAlert message={success} onClose={() => setSuccess(null)} />
      )}

      <div className="flex items-center justify-between flex-wrap gap-4">
        <div>
          <h1 className="text-3xl font-bold text-neutral-900">Events</h1>
          <p className="text-neutral-600 mt-1">
            Manage and organize volunteer events
          </p>
        </div>
        {isAdmin && (
          <button onClick={() => setShowForm(true)} className="btn-primary">
            Create Event
          </button>
        )}
      </div>

      {isAdmin && (
        <Modal
          isOpen={showForm}
          onClose={() => setShowForm(false)}
          title="Create New Event"
          children={
            <EventForm
              onSuccess={handleEventAdded}
              onCancel={() => setShowForm(false)}
              nextEventId={nextEventId}
            />
          }
        />
      )}

      {loading ? (
        <div className="flex justify-center py-12">
          <LoadingSpinner />
        </div>
      ) : events.length === 0 ? (
        <div className="card">
          <EmptyState
            title="No Events Yet"
            description="Create your first event to start organizing volunteers"
          />
        </div>
      ) : (
        <div className="grid gap-4 md:gap-6">
          {events.map((event) => (
            <div
              key={event.eventId}
              className="card p-6 hover:shadow-lg transition-shadow"
            >
              <div className="flex items-start justify-between gap-4 flex-wrap">
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2 mb-2">
                    <h3 className="text-lg font-semibold text-neutral-900 truncate">
                      {event.eventName}
                    </h3>
                    {event.isCompleted && (
                      <span className="badge-success">Completed</span>
                    )}
                  </div>
                  <div className="space-y-1 text-sm">
                    <p className="text-neutral-600">
                      <span className="font-medium">ID:</span> {event.eventId}
                    </p>
                    <p className="text-neutral-600">
                      <span className="font-medium">Start Date:</span>{" "}
                      {formatDate(event.date)}
                    </p>
                    {event.finishDate && (
                      <p className="text-neutral-600">
                        <span className="font-medium">End Date:</span>{" "}
                        {formatDate(event.finishDate)}
                      </p>
                    )}
                  </div>
                </div>
                <div className="flex gap-2">
                  {isAdmin ? (
                    <>
                      <button
                        onClick={() => handleCompleteEventDirect(event)}
                        className="btn-primary bg-green-600 hover:bg-green-700"
                      >
                        Complete Event
                      </button>
                      <button
                        onClick={() => setDeleteConfirm(event.eventId)}
                        className="btn-danger"
                      >
                        Delete
                      </button>
                    </>
                  ) : (
                    !event.isCompleted && (
                      <button
                        onClick={() => handleToggleEnrollment(event.eventId)}
                        className={
                          enrolledEventIds.has(event.eventId)
                            ? "btn-secondary"
                            : "btn-primary"
                        }
                        disabled={
                          enrollingEventId === event.eventId || !myVolunteerId
                        }
                      >
                        {enrollingEventId === event.eventId
                          ? "Please wait..."
                          : enrolledEventIds.has(event.eventId)
                            ? "Unenroll"
                            : "Enroll"}
                      </button>
                    )
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {isAdmin && (
        <Modal
          isOpen={!!deleteConfirm}
          onClose={() => setDeleteConfirm(null)}
          title="Delete Event"
          children={
            <p className="text-neutral-600">
              Are you sure you want to delete this event? This action cannot be
              undone.
            </p>
          }
          actions={[
            <button
              key="cancel"
              onClick={() => setDeleteConfirm(null)}
              className="btn-secondary"
            >
              Cancel
            </button>,
            <button
              key="delete"
              onClick={() => handleDelete(deleteConfirm)}
              className="btn-danger"
            >
              Delete
            </button>,
          ]}
        />
      )}
    </div>
  );
}
