import ResourcePage from '../components/ResourcePage';

export default function Inventario() {
  return (
    <ResourcePage
      title="Inventario"
      subtitle="Insumos y niveles de stock de la cafetería."
      endpoint="/inventario"
      emptyTitle="Todavía no hay insumos cargados"
      emptyText="Registra insumos desde el backend para verlos aquí."
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
