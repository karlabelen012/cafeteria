import CrudPage from '../components/CrudPage.jsx';
import { useUserRole } from '../hooks/useUserRole';

const CAMPOS = [
  { name: 'nombre', label: 'Nombre', type: 'text', required: true },
  { name: 'rut', label: 'RUT', type: 'text', required: true, placeholder: '11.111.111-1' },
  { name: 'email', label: 'Email', type: 'email', required: true },
  { name: 'telefono', label: 'Teléfono', type: 'text', required: true },
  { name: 'insumosQueProvee', label: 'Insumos que provee', type: 'textarea' },
];

export default function Proveedores() {
  const role = useUserRole();
  const puedeEditar = role === 'ADMIN' || role === 'BODEGUERO';

  return (
    <CrudPage
      title="Proveedores"
      subtitle="Proveedores de insumos y sus datos de contacto."
      endpoint="/proveedores"
      nombreSingular="proveedor"
      searchKeys={['nombre', 'rut', 'email']}
      puedeCrear={puedeEditar}
      puedeEditar={puedeEditar}
      puedeEliminar={role === 'ADMIN'}
      valoresPorDefecto={{ nombre: '', rut: '', email: '', telefono: '', insumosQueProvee: '' }}
      fields={CAMPOS}
      columns={[
        { key: 'nombre', label: 'Proveedor', render: (p) => <strong>{p.nombre}</strong> },
        { key: 'rut', label: 'RUT' },
        { key: 'telefono', label: 'Teléfono' },
        { key: 'email', label: 'Email' },
      ]}
    />
  );
}
