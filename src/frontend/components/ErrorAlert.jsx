export default function ErrorAlert({ message, onClose }) {
  return (
    <div className="fixed top-4 right-4 bg-red-50 border border-red-200 text-red-800 px-4 py-3 rounded-lg shadow-md max-w-sm z-50">
      <div className="flex items-start justify-between">
        <div>
          <h3 className="font-semibold text-sm">Error</h3>
          <p className="text-sm mt-1">{message}</p>
        </div>
        {onClose && (
          <button
            onClick={onClose}
            className="ml-4 text-red-600 hover:text-red-900 transition-colors"
            aria-label="Close"
          >
            x
          </button>
        )}
      </div>
    </div>
  );
}
