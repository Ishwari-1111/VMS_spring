import { useState } from "react";
import { volunteersAPI } from "../../api/volunteers";
import LoadingSpinner from "../../components/LoadingSpinner";
import ErrorAlert from "../../components/ErrorAlert";

export default function VolunteerForm({ onSuccess, onCancel }) {
  const [formData, setFormData] = useState({
    volunteerId: "",
    name: "",
    email: "",
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

    if (!formData.volunteerId || !formData.name) {
      setError("Please fill in all required fields");
      return;
    }

    setLoading(true);
    try {
      await volunteersAPI.create(
        formData.volunteerId,
        formData.name,
        formData.email || null,
      );
      onSuccess();
    } catch (err) {
      setError(err.response?.data?.error || "Failed to add volunteer");
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      {error && <ErrorAlert message={error} onClose={() => setError(null)} />}

      <div>
        <label
          htmlFor="volunteerId"
          className="block text-sm font-medium text-neutral-700 mb-2"
        >
          Volunteer ID *
        </label>
        <input
          id="volunteerId"
          type="text"
          name="volunteerId"
          value={formData.volunteerId}
          onChange={handleChange}
          placeholder="e.g., VOL001"
          className="input-field"
          disabled={loading}
        />
      </div>

      <div>
        <label
          htmlFor="name"
          className="block text-sm font-medium text-neutral-700 mb-2"
        >
          Name *
        </label>
        <input
          id="name"
          type="text"
          name="name"
          value={formData.name}
          onChange={handleChange}
          placeholder="Full name"
          className="input-field"
          disabled={loading}
        />
      </div>

      <div>
        <label
          htmlFor="email"
          className="block text-sm font-medium text-neutral-700 mb-2"
        >
          Email
        </label>
        <input
          id="email"
          type="email"
          name="email"
          value={formData.email}
          onChange={handleChange}
          placeholder="email@example.com"
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
          {loading ? <LoadingSpinner size="sm" /> : "Add Volunteer"}
        </button>
      </div>
    </form>
  );
}
