import { useEffect, useState } from 'react';
import { useApiClient } from '../services/apiClient';
import NavBar from './NavBar';

// Vista genérica de "listar recurso": la usan Inventario, Clientes, Empleados,
// Proveedores y Pagos, que comparten el mismo patrón CRUD en el backend
// (GET lista -> BFF -> microservicio). Cada página solo define columnas.
export default function ResourcePage({
  title,
  subtitle,
  endpoint,
  columns,
  emptyTitle,
  emptyText,
  renderAbove,
}) {
  const { callApi } = useApiClient();
  const [items, setItems] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    callApi(endpoint)
      .then(setItems)
      .catch((err) => {
        console.error(err);
        setError(`No se pudo cargar la información (revisa el token o el backend).`);
      })
      .finally(() => setCargando(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [endpoint]);

  return (
    <div className="app-shell">
      <NavBar />
      <div className="page">
        <div className="page__header">
          <div>
            <h2>{title}</h2>
            <p>{subtitle}</p>
          </div>
        </div>

        {error && <div className="alert alert-error">{error}</div>}

        {cargando && (
          <div className="state-block">
            <div className="spinner" />
            <p>Cargando...</p>
          </div>
        )}

        {!cargando && !error && renderAbove ? renderAbove(items) : null}

        {!cargando && !error && items.length === 0 && (
          <div className="state-block">
            <h3>{emptyTitle}</h3>
            <p>{emptyText}</p>
          </div>
        )}

        {!cargando && items.length > 0 && (
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  {columns.map((col) => (
                    <th key={col.key}>{col.label}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {items.map((item) => (
                  <tr key={item.id}>
                    {columns.map((col) => (
                      <td key={col.key}>{col.render ? col.render(item) : item[col.key]}</td>
                    ))}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
