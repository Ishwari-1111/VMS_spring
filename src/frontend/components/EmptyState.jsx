export default function EmptyState({ title, description, icon }) {
  return (
    <div className="flex flex-col items-center justify-center py-12 px-4">
      {icon ? <div className="text-5xl mb-4">{icon}</div> : null}
      <h3 className="text-lg font-semibold text-neutral-900">{title}</h3>
      <p className="text-neutral-600 text-sm mt-1 max-w-sm text-center">
        {description}
      </p>
    </div>
  );
}
