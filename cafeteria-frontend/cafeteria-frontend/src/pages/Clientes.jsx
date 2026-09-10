import ResourcePage from '../components/ResourcePage';

export default function Clientes() {
  return (
    <ResourcePage
      title="Clientes"
      subtitle="Clientes registrados y su fidelización."
      endpoint="/clientes"
      emptyTitle="Todavía no hay clientes registrados"
      emptyText="Registra clientes desde el backend para verlos aquí."
      columns={[
        { key: 'nombre', label: 'Nombre', render: (c) => <strong>{c.nombre}</strong> },
        { key: 'email', label: 'Email' },
        { key: 'telefono', label: 'Teléfono' },
        {
          key: 'puntosFidelizacion',
          label: 'Puntos',
          render: (c) => <span className="badge badge--success">{c.puntosFidelizacion ?? 0} pts</span>,
        },
      ]}
    />
  );
}
