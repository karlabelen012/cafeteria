import CrudPage from '../components/CrudPage.jsx';
import { useUserRole } from '../hooks/useUserRole';

const CAMPOS = [
  { name: 'nombre', label: 'Nombre', type: 'text', required: true },
  { name: 'email', label: 'Email', type: 'email', required: true },
  { name: 'telefono', label: 'Teléfono', type: 'text', placeholder: '+56912345678' },
  { name: 'puntosFidelizacion', label: 'Puntos de fidelización', type: 'number', min: 0, step: 1 },
];

export default function Clientes() {
  const role = useUserRole();
  const puedeEditar = role === 'ADMIN' || role === 'CAJERO';

  return (
    <CrudPage
      title="Clientes"
      subtitle="Clientes registrados y su fidelización."
      endpoint="/clientes"
      nombreSingular="cliente"
      searchKeys={['nombre', 'email']}
      puedeCrear={puedeEditar}
      puedeEditar={puedeEditar}
      puedeEliminar={role === 'ADMIN'}
      valoresPorDefecto={{ nombre: '', email: '', telefono: '', puntosFidelizacion: 0 }}
      aCuerpoPeticion={(v) => ({ ...v, puntosFidelizacion: v.puntosFidelizacion === '' ? null : v.puntosFidelizacion })}
      fields={CAMPOS}
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
