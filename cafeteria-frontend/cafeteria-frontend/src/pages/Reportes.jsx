import ResourcePage from '../components/ResourcePage';

function resumen(ventas) {
  const totalVentas = ventas.reduce((acc, v) => acc + (v.totalVentas || 0), 0);
  const totalPedidos = ventas.reduce((acc, v) => acc + (v.cantidadPedidos || 0), 0);
  const promedioDiario = ventas.length ? totalVentas / ventas.length : 0;

  return (
    <div className="stat-grid">
      <div className="stat-card">
        <div className="stat-card__label">Ventas totales</div>
        <div className="stat-card__value">${totalVentas.toLocaleString('es-CL')}</div>
      </div>
      <div className="stat-card">
        <div className="stat-card__label">Pedidos totales</div>
        <div className="stat-card__value">{totalPedidos}</div>
      </div>
      <div className="stat-card">
        <div className="stat-card__label">Promedio diario</div>
        <div className="stat-card__value">${Math.round(promedioDiario).toLocaleString('es-CL')}</div>
      </div>
    </div>
  );
}

export default function Reportes() {
  return (
    <ResourcePage
      title="Reportes"
      subtitle="Ventas diarias agregadas — visibilidad real para el dueño."
      endpoint="/reportes"
      emptyTitle="Todavía no hay reportes generados"
      emptyText="Registra ventas diarias desde el backend para ver el resumen aquí."
      renderAbove={(ventas) => (ventas.length ? resumen(ventas) : null)}
      columns={[
        { key: 'fecha', label: 'Fecha', render: (v) => <strong>{v.fecha}</strong> },
        { key: 'totalVentas', label: 'Ventas del día', render: (v) => `$${v.totalVentas}` },
        { key: 'cantidadPedidos', label: 'Pedidos del día' },
      ]}
    />
  );
}
