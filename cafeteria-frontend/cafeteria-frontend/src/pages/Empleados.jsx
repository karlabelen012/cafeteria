import CrudPage from '../components/CrudPage.jsx';
import { useUserRole } from '../hooks/useUserRole';

const ROLES = ['ADMIN', 'GERENTE', 'BARISTA', 'CAJERO', 'BODEGUERO'];

const CAMPOS = [
  { name: 'nombre', label: 'Nombre', type: 'text', required: true },
  { name: 'email', label: 'Email', type: 'email', required: true },
  {
    name: 'rol',
    label: 'Rol',
    type: 'select',
    required: true,
    options: ROLES.map((r) => ({ value: r, label: r })),
  },
  { name: 'fechaIngreso', label: 'Fecha de ingreso', type: 'date', required: true },
  { name: 'activo', label: 'Activo', type: 'checkbox' },
];

export default function Empleados() {
  const role = useUserRole();
  const esAdmin = role === 'ADMIN';

  return (
    <CrudPage
      title="Empleados"
      subtitle="Equipo de la cafetería, sus roles y estado."
      endpoint="/empleados"
      nombreSingular="empleado"
      searchKeys={['nombre', 'email']}
      puedeCrear={esAdmin}
      puedeEditar={esAdmin}
      puedeEliminar={esAdmin}
      valoresPorDefecto={{ nombre: '', email: '', rol: 'BARISTA', fechaIngreso: '', activo: true }}
      fields={CAMPOS}
      columns={[
        { key: 'nombre', label: 'Nombre', render: (e) => <strong>{e.nombre}</strong> },
        { key: 'email', label: 'Email' },
        { key: 'rol', label: 'Rol', render: (e) => <span className="badge">{e.rol}</span> },
        { key: 'fechaIngreso', label: 'Ingreso' },
        {
          key: 'activo',
          label: 'Estado',
          render: (e) => (
            <span className={`badge ${e.activo ? 'badge--success' : 'badge--neutral'}`}>
              {e.activo ? 'Activo' : 'Inactivo'}
            </span>
          ),
        },
      ]}
    />
  );
}
