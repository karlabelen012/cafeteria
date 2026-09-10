import ResourcePage from '../components/ResourcePage';

export default function Proveedores() {
  return (
    <ResourcePage
      title="Proveedores"
      subtitle="Proveedores de insumos y sus datos de contacto."
      endpoint="/proveedores"
      emptyTitle="Todavía no hay proveedores registrados"
      emptyText="Registra proveedores desde el backend para verlos aquí."
      columns={[
        { key: 'nombre', label: 'Proveedor', render: (p) => <strong>{p.nombre}</strong> },
        { key: 'contacto', label: 'Contacto' },
        { key: 'telefono', label: 'Teléfono' },
        { key: 'email', label: 'Email' },
      ]}
    />
  );
}
