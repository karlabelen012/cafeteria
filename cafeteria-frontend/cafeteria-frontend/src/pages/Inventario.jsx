import CrudPage from '../components/CrudPage.jsx';
import { useUserRole } from '../hooks/useUserRole';

const UNIDADES = ['g', 'ml', 'unidad'];

const CAMPOS = [
  { name: 'nombre', label: 'Nombre', type: 'text', required: true },
  {
    name: 'unidadMedida',
    label: 'Unidad de medida',
    type: 'select',
    required: true,
    options: UNIDADES.map((u) => ({ value: u, label: u })),
  },
  { name: 'stockActual', label: 'Stock actual', type: 'number', required: true, min: 0, step: 0.1 },
  { name: 'stockMinimo', label: 'Stock mínimo', type: 'number', required: true, min: 0, step: 0.1 },
];

export default function Inventario() {
  const role = useUserRole();
  const puedeEditar = role === 'ADMIN' || role === 'BODEGUERO';

  return (
    <CrudPage
      title="Inventario"
      subtitle="Insumos y niveles de stock de la cafetería."
      endpoint="/inventario"
      nombreSingular="insumo"
      searchKeys={['nombre']}
      puedeCrear={puedeEditar}
      puedeEditar={puedeEditar}
      puedeEliminar={role === 'ADMIN'}
      valoresPorDefecto={{ nombre: '', unidadMedida: 'g', stockActual: 0, stockMinimo: 0 }}
      fields={CAMPOS}
      columns={[
        { key: 'nombre', label: 'Insumo', render: (i) => <strong>{i.nombre}</strong> },
        { key: 'unidadMedida', label: 'Unidad' },
        { key: 'stockActual', label: 'Stock actual' },
        { key: 'stockMinimo', label: 'Stock mínimo' },
        {
          key: 'nivel',
          label: 'Nivel',
          render: (i) =>
            i.stockActual <= i.stockMinimo ? (
              <span className="badge badge--error">Bajo stock</span>
            ) : (
              <span className="badge badge--success">OK</span>
            ),
        },
      ]}
    />
  );
}
