import { useState, useEffect } from "react";
import { eventsAPI } from "../../api/events";
import { volunteersAPI } from "../../api/volunteers";
import { certificatesAPI } from "../../api/certificates";
import LoadingSpinner from "../../components/LoadingSpinner";
import ErrorAlert from "../../components/ErrorAlert";
import SuccessAlert from "../../components/SuccessAlert";
import Modal from "../../components/Modal";
import EmptyState from "../../components/EmptyState";

export default function EventDetail({
  event,
  onClose,
  onEventUpdated,
  initialOpenCompleteModal = false,
}) {
  const [eventData, setEventData] = useState(event);
  const [volunteers, setVolunteers] = useState([]);
  const [allVolunteers, setAllVolunteers] = useState([]);
  const [certificates, setCertificates] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [activeTab, setActiveTab] = useState("volunteers");
  const [showEnrollModal, setShowEnrollModal] = useState(false);
  const [showCompleteModal, setShowCompleteModal] = useState(false);
  const [selectedVolunteer, setSelectedVolunteer] = useState(null);
  const [hoursToLog, setHoursToLog] = useState("");
  const [finishDate, setFinishDate] = useState("");

  useEffect(() => {
    fetchEventDetails();
  }, [event.eventId]);

  useEffect(() => {
    if (initialOpenCompleteModal) {
      setShowCompleteModal(true);
    }
  }, [initialOpenCompleteModal]);

  const fetchEventDetails = async () => {
    try {
      setLoading(true);
      setError(null);

      const [allVols, certs] = await Promise.all([
        volunteersAPI.getAll(),
        certificatesAPI.getByEvent(event.eventId),
      ]);

      setAllVolunteers(allVols.data);
      setCertificates(certs.data?.certificates || []);
      setVolunteers(allVols.data);
    } catch (err) {
      setError("Failed to load event details");
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleEnrollVolunteer = async (volunteerId) => {
    try {
      setError(null);
      await eventsAPI.enrollVolunteer(eventData.eventId, volunteerId);
      setSuccess("Volunteer enrolled successfully");
      setShowEnrollModal(false);
      fetchEventDetails();
      setTimeout(() => setSuccess(null), 3000);
    } catch (err) {
      setError(err.response?.data?.error || "Failed to enroll volunteer");
      console.error(err);
    }
  };

  const handleLogHours = async (volunteerId) => {
    if (!hoursToLog || hoursToLog <= 0) {
      setError("Please enter valid hours");
      return;
    }

    try {
      setError(null);
      await eventsAPI.logHours(
        eventData.eventId,
        volunteerId,
        parseInt(hoursToLog),
      );
      setSuccess("Hours logged successfully");
      setHoursToLog("");
      setSelectedVolunteer(null);
      fetchEventDetails();
      setTimeout(() => setSuccess(null), 3000);
    } catch (err) {
      setError(err.response?.data?.error || "Failed to log hours");
      console.error(err);
    }
  };

  const handleCompleteEvent = async () => {
    if (!finishDate) {
      setError("Please select a finish date");
      return;
    }

    try {
      setError(null);
      await eventsAPI.completeEvent(eventData.eventId, finishDate);
      setSuccess("Event completed and certificates generated");
      setShowCompleteModal(false);
      setFinishDate("");
      const updatedEvent = await eventsAPI.getById(eventData.eventId);
      setEventData(updatedEvent.data);
      onEventUpdated();
      fetchEventDetails();
      setTimeout(() => setSuccess(null), 3000);
    } catch (err) {
      setError(err.response?.data?.error || "Failed to complete event");
      console.error(err);
    }
  };

  const handleMarkIncomplete = async () => {
    try {
      setError(null);
      await eventsAPI.markIncomplete(eventData.eventId);
      setSuccess("Event marked as incomplete");
      const updatedEvent = await eventsAPI.getById(eventData.eventId);
      setEventData(updatedEvent.data);
      onEventUpdated();
      setTimeout(() => setSuccess(null), 3000);
    } catch (err) {
      setError(err.response?.data?.error || "Failed to update event");
      console.error(err);
    }
  };

  const formatDate = (date) => {
    return new Date(date).toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
    });
  };

  if (loading) {
    return (
      <Modal isOpen={true} onClose={onClose} title="Event Details">
        <div className="flex justify-center py-8">
          <LoadingSpinner />
        </div>
      </Modal>
    );
  }

  return (
    <Modal
      isOpen={true}
      onClose={onClose}
      title={eventData.eventName}
      children={
        <div className="space-y-4 max-h-96 overflow-y-auto">
          {error && (
            <ErrorAlert message={error} onClose={() => setError(null)} />
          )}
          {success && (
            <SuccessAlert message={success} onClose={() => setSuccess(null)} />
          )}

          <div className="grid grid-cols-2 gap-4 pb-4 border-b border-neutral-200">
            <div>
              <p className="text-xs text-neutral-600 font-medium">Event ID</p>
              <p className="text-sm font-mono text-neutral-900">
                {eventData.eventId}
              </p>
            </div>
            <div>
              <p className="text-xs text-neutral-600 font-medium">Status</p>
              <p className="text-sm">
                {eventData.isCompleted ? (
                  <span className="badge-success">Completed</span>
                ) : (
                  <span className="badge-warning">Ongoing</span>
                )}
              </p>
            </div>
            <div>
              <p className="text-xs text-neutral-600 font-medium">Start Date</p>
              <p className="text-sm text-neutral-900">
                {formatDate(eventData.date)}
              </p>
            </div>
            {eventData.finishDate && (
              <div>
                <p className="text-xs text-neutral-600 font-medium">End Date</p>
                <p className="text-sm text-neutral-900">
                  {formatDate(eventData.finishDate)}
                </p>
              </div>
            )}
          </div>

          <div className="flex gap-2 border-b border-neutral-200">
            <button
              onClick={() => setActiveTab("volunteers")}
              className={`px-4 py-2 text-sm font-medium transition-colors ${
                activeTab === "volunteers"
                  ? "text-primary-600 border-b-2 border-primary-600"
                  : "text-neutral-600 hover:text-neutral-900"
              }`}
            >
              Volunteers ({volunteers.length})
            </button>
            <button
              onClick={() => setActiveTab("certificates")}
              className={`px-4 py-2 text-sm font-medium transition-colors ${
                activeTab === "certificates"
                  ? "text-primary-600 border-b-2 border-primary-600"
                  : "text-neutral-600 hover:text-neutral-900"
              }`}
            >
              Certificates ({certificates.length})
            </button>
          </div>

          {activeTab === "volunteers" && (
            <div className="space-y-3">
              {volunteers.length === 0 ? (
                <EmptyState
                  title="No Volunteers"
                  description="Enroll volunteers to this event"
                />
              ) : (
                <div className="space-y-2">
                  {volunteers.map((vol) => (
                    <div
                      key={vol.volunteerId}
                      className="flex items-center justify-between p-3 bg-neutral-50 rounded-lg border border-neutral-200"
                    >
                      <div className="min-w-0 flex-1">
                        <p className="text-sm font-medium text-neutral-900">
                          {vol.name}
                        </p>
                        <p className="text-xs text-neutral-600">
                          {vol.volunteerId}
                        </p>
                      </div>
                      <button
                        onClick={() => setSelectedVolunteer(vol)}
                        className="btn-secondary text-xs"
                      >
                        Log Hours
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          {activeTab === "certificates" && (
            <div className="space-y-3">
              {certificates.length === 0 ? (
                <EmptyState
                  title="No Certificates"
                  description="Complete the event to generate certificates"
                />
              ) : (
                <div className="space-y-2">
                  {certificates.map((cert) => (
                    <div
                      key={cert.certificateId}
                      className="p-3 bg-neutral-50 rounded-lg border border-neutral-200"
                    >
                      <p className="text-sm font-medium text-neutral-900">
                        {cert.volunteerName}
                      </p>
                      <p className="text-xs text-neutral-600">
                        {cert.certificateCode}
                      </p>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}
        </div>
      }
      actions={[
        <button key="close" onClick={onClose} className="btn-secondary">
          Close
        </button>,
        !eventData.isCompleted && (
          <button
            key="enroll"
            onClick={() => setShowEnrollModal(true)}
            className="btn-primary"
          >
            Enroll
          </button>
        ),
        !eventData.isCompleted && (
          <button
            key="complete"
            onClick={() => setShowCompleteModal(true)}
            className="btn-primary"
          >
            Complete
          </button>
        ),
        eventData.isCompleted && (
          <button
            key="incomplete"
            onClick={handleMarkIncomplete}
            className="btn-secondary"
          >
            Mark Incomplete
          </button>
        ),
      ]}
    >
      <Modal
        isOpen={showEnrollModal}
        onClose={() => setShowEnrollModal(false)}
        title="Enroll Volunteer"
        children={
          <div className="space-y-3 max-h-60 overflow-y-auto">
            {allVolunteers.map((vol) => (
              <button
                key={vol.volunteerId}
                onClick={() => handleEnrollVolunteer(vol.volunteerId)}
                className="w-full p-3 text-left border border-neutral-200 rounded-lg hover:bg-primary-50 hover:border-primary-600 transition-all"
              >
                <p className="text-sm font-medium text-neutral-900">
                  {vol.name}
                </p>
                <p className="text-xs text-neutral-600">{vol.volunteerId}</p>
              </button>
            ))}
          </div>
        }
      />

      <Modal
        isOpen={!!selectedVolunteer}
        onClose={() => setSelectedVolunteer(null)}
        title={`Log Hours - ${selectedVolunteer?.name}`}
        children={
          <div className="space-y-4">
            <div>
              <label
                htmlFor="hours"
                className="block text-sm font-medium text-neutral-700 mb-2"
              >
                Hours
              </label>
              <input
                id="hours"
                type="number"
                min="1"
                value={hoursToLog}
                onChange={(e) => setHoursToLog(e.target.value)}
                placeholder="Enter hours"
                className="input-field"
              />
            </div>
          </div>
        }
        actions={[
          <button
            key="cancel"
            onClick={() => setSelectedVolunteer(null)}
            className="btn-secondary"
          >
            Cancel
          </button>,
          <button
            key="log"
            onClick={() => handleLogHours(selectedVolunteer.volunteerId)}
            className="btn-primary"
          >
            Log Hours
          </button>,
        ]}
      />

      <Modal
        isOpen={showCompleteModal}
        onClose={() => setShowCompleteModal(false)}
        title="Complete Event"
        children={
          <div className="space-y-4">
            <p className="text-sm text-neutral-600">
              This will mark the event as completed and automatically generate
              certificates for all enrolled volunteers.
            </p>
            <div>
              <label
                htmlFor="finishDate"
                className="block text-sm font-medium text-neutral-700 mb-2"
              >
                Finish Date
              </label>
              <input
                id="finishDate"
                type="date"
                value={finishDate}
                onChange={(e) => setFinishDate(e.target.value)}
                className="input-field"
              />
            </div>
          </div>
        }
        actions={[
          <button
            key="cancel"
            onClick={() => setShowCompleteModal(false)}
            className="btn-secondary"
          >
            Cancel
          </button>,
          <button
            key="complete"
            onClick={handleCompleteEvent}
            className="btn-primary"
          >
            Complete
          </button>,
        ]}
      />
    </Modal>
  );
}
