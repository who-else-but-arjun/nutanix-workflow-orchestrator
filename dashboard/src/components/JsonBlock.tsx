export function JsonBlock({ title, value }: { title: string; value: unknown }) {
  return (
    <div className="json-block">
      <div className="json-title">{title}</div>
      <pre>{JSON.stringify(value ?? {}, null, 2)}</pre>
    </div>
  );
}
