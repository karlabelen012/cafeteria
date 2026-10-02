import CrudPage from '../components/CrudPage.jsx';
import { useUserRole } from '../hooks/useUserRole';

const CATEGORIAS = ['Bebidas calientes', 'Bebidas frías', 'Pastelería', 'Galletas'];

const CAMPOS = [
  { name: 'nombre', label: 'Nombre', type: 'text', required: true },
  { name: 'descripcion', label: 'Descripción', type: 'textarea' },
  { name: 'precio', label: 'Precio', type: 'number', required: true, min: 0, step: 1 },
  {
    name: 'categoria',
    label: 'Categoría',
    type: 'select',
    required: true,
    options: CATEGORIAS.map((c) => ({ value: c, label: c })),
  },
  { name: 'disponible', label: 'Disponible', type: 'checkbox' },
  { name: 'imagenUrl', label: 'URL de la imagen', type: 'text' },
];

export default function Productos() {
  const role = useUserRole();
  const esAdmin = role === 'ADMIN';

  return (
    <CrudPage
      title="Menú de productos"
      subtitle="Lo que hoy se puede pedir en barra."
      endpoint="/productos"
      nombreSingular="producto"
      searchKeys={['nombre', 'categoria']}
      puedeCrear={esAdmin}
      puedeEditar={esAdmin}
      puedeEliminar={esAdmin}
      valoresPorDefecto={{ nombre: '', descripcion: '', precio: '', categoria: '', disponible: true, imagenUrl: '' }}
      fields={CAMPOS}
      columns={[
        { key: 'nombre', label: 'Nombre', render: (p) => <strong>{p.nombre}</strong> },
        { key: 'categoria', label: 'Categoría' },
        { key: 'precio', label: 'Precio', render: (p) => `$${p.precio}` },
        {
          key: 'disponible',
          label: 'Estado',
          render: (p) => (
            <span className={`badge ${p.disponible ? 'badge--success' : 'badge--neutral'}`}>
              {p.disponible ? 'Disponible' : 'No disponible'}
            </span>
          ),
        },
      ]}
    />
  );
}
