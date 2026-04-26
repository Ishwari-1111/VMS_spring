import { useState, useEffect } from "react";
import { volunteersAPI } from "../../api/volunteers";
import LoadingSpinner from "../../components/LoadingSpinner";
import ErrorAlert from "../../components/ErrorAlert";
import SuccessAlert from "../../components/SuccessAlert";
import EmptyState from "../../components/EmptyState";
import Modal from "../../components/Modal";

export default function VolunteersList() {
  const [volunteers, setVolunteers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [deleteConfirm, setDeleteConfirm] = useState(null);

  useEffect(() => {
    fetchVolunteers();
  }, []);

  const fetchVolunteers = async () => {
    try {
      setLoading(true);
      setError(null);
      const { data } = await volunteersAPI.getAll();
      setVolunteers(data);
    } catch (err) {
      setError("Failed to load volunteers");
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    try {
      setError(null);
      await volunteersAPI.delete(id);
      setVolunteers((prev) => prev.filter((v) => v.volunteerId !== id));
      setSuccess("Volunteer deleted successfully");
      setDeleteConfirm(null);
      setTimeout(() => setSuccess(null), 3000);
    } catch (err) {
      setError("Failed to delete volunteer");
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
          <h1 className="text-3xl font-bold text-neutral-900">Volunteers</h1>
          <p className="text-neutral-600 mt-1">
            Manage all registered volunteers
          </p>
        </div>
      </div>

      {loading ? (
        <div className="flex justify-center py-12">
          <LoadingSpinner />
        </div>
      ) : volunteers.length === 0 ? (
        <div className="card">
          <EmptyState
            title="No Volunteers Yet"
            description="Volunteers will appear here after signup"
          />
        </div>
      ) : (
        <div className="card overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-neutral-50 border-b border-neutral-200">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-neutral-700">
                    ID
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-neutral-700">
                    Name
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-neutral-700">
                    Email
                  </th>
                  <th className="px-6 py-3 text-right text-xs font-semibold text-neutral-700">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-neutral-200">
                {volunteers.map((volunteer) => (
                  <tr
                    key={volunteer.volunteerId}
                    className="hover:bg-neutral-50 transition-colors"
                  >
                    <td className="px-6 py-4 text-sm font-mono text-neutral-600">
                      {volunteer.volunteerId}
                    </td>
                    <td className="px-6 py-4 text-sm font-medium text-neutral-900">
                      {volunteer.name}
                    </td>
                    <td className="px-6 py-4 text-sm text-neutral-600">
                      {volunteer.email || "-"}
                    </td>
                    <td className="px-6 py-4 text-right">
                      <button
                        onClick={() => setDeleteConfirm(volunteer.volunteerId)}
                        className="text-red-600 hover:text-red-700 text-sm font-medium transition-colors"
                      >
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      <Modal
        isOpen={!!deleteConfirm}
        onClose={() => setDeleteConfirm(null)}
        title="Delete Volunteer"
        children={
          <p className="text-neutral-600">
            Are you sure you want to delete this volunteer? This action cannot
            be undone.
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
    </div>
  );
}
