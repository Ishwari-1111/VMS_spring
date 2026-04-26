import { useState } from "react";
import { eventsAPI } from "../../api/events";
import LoadingSpinner from "../../components/LoadingSpinner";
import ErrorAlert from "../../components/ErrorAlert";

export default function EventForm({ onSuccess, onCancel, nextEventId }) {
  const [formData, setFormData] = useState({
    eventName: "",
    date: "",
    finishDate: "",
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    if (!formData.eventName || !formData.date || !formData.finishDate) {
      setError("Please fill in all fields");
      return;
    }

    setLoading(true);
    try {
      await eventsAPI.create(
        formData.eventName,
        formData.date,
        formData.finishDate,
      );
      onSuccess();
    } catch (err) {
      setError(
        err.response?.data?.error ||
          err.response?.data?.message ||
          err.response?.data?.details ||
          err.message ||
          "Failed to create event",
      );
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      {error && <ErrorAlert message={error} onClose={() => setError(null)} />}

      <div>
        <label className="block text-sm font-medium text-neutral-700 mb-2">
          Event ID
        </label>
        <input
          type="text"
          value={nextEventId || "Auto-generated"}
          className="input-field bg-neutral-100"
          disabled
        />
        <p className="text-xs text-neutral-500 mt-1">
          The server assigns the final ID when the event is created.
        </p>
      </div>

      <div>
        <label
          htmlFor="eventName"
          className="block text-sm font-medium text-neutral-700 mb-2"
        >
          Event Name *
        </label>
        <input
          id="eventName"
          type="text"
          name="eventName"
          value={formData.eventName}
          onChange={handleChange}
          placeholder="Event name"
          className="input-field"
          disabled={loading}
        />
      </div>

      <div>
        <label
          htmlFor="date"
          className="block text-sm font-medium text-neutral-700 mb-2"
        >
          Start Date *
        </label>
        <input
          id="date"
          type="date"
          name="date"
          value={formData.date}
          onChange={handleChange}
          className="input-field"
          disabled={loading}
        />
      </div>

      <div>
        <label
          htmlFor="finishDate"
          className="block text-sm font-medium text-neutral-700 mb-2"
        >
          End Date *
        </label>
        <input
          id="finishDate"
          type="date"
          name="finishDate"
          value={formData.finishDate}
          onChange={handleChange}
          className="input-field"
          disabled={loading}
        />
      </div>

      <div className="flex gap-3 pt-4">
        <button
          type="button"
          onClick={onCancel}
          className="btn-secondary flex-1"
          disabled={loading}
        >
          Cancel
        </button>
        <button
          type="submit"
          className="btn-primary flex-1 flex items-center justify-center"
          disabled={loading}
        >
          {loading ? <LoadingSpinner size="sm" /> : "Create Event"}
        </button>
      </div>
    </form>
  );
}
