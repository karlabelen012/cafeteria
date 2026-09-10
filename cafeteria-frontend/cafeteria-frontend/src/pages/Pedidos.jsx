import { useEffect, useState } from 'react';
import { useApiClient } from '../services/apiClient';
import NavBar from '../components/NavBar';
import { badgeClassForEstado } from '../utils/badges';

const ESTADOS = ['Pendiente', 'En preparación', 'Listo', 'Entregado', 'Cancelado'];

export default function Pedidos() {
  const { callApi } = useApiClient();
  const [pedidos, setPedidos] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');
  const [actualizando, setActualizando] = useState(null);

  useEffect(() => {
    // Esta llamada pasa por: React -> BFF (valida JWT) -> ms-pedidos
    callApi('/pedidos')
      .then((data) => setPedidos(data))
      .catch((err) => {
        console.error(err);
        setError('No se pudieron cargar los pedidos (revisa el token o el backend).');
      })
      .finally(() => setCargando(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const cambiarEstado = async (pedido, nuevoEstado) => {
    setActualizando(pedido.id);
    try {
      const actualizado = await callApi(`/pedidos/${pedido.id}`, {
        method: 'PUT',
        body: JSON.stringify({ ...pedido, estado: nuevoEstado }),
      });
      setPedidos((prev) => prev.map((p) => (p.id === pedido.id ? actualizado : p)));
    } catch (err) {
      console.error(err);
      setError(`No se pudo actualizar el pedido #${pedido.id}.`);
    } finally {
      setActualizando(null);
    }
  };

  return (
    <div className="app-shell">
      <NavBar />
      <div className="page">
        <div className="page__header">
          <div>
            <h2>Pedidos</h2>
            <p>Seguimiento de lo que se está preparando y entregando.</p>
          </div>
        </div>

        {error && <div className="alert alert-error">{error}</div>}

        {cargando && (
          <div className="state-block">
            <div className="spinner" />
            <p>Cargando pedidos...</p>
          </div>
        )}

        {!cargando && !error && pedidos.length === 0 && (
          <div className="state-block">
            <h3>No hay pedidos registrados todavía</h3>
            <p>Los nuevos pedidos aparecerán aquí apenas se registren.</p>
          </div>
        )}

        {!cargando && pedidos.length > 0 && (
          <div className="orders-list">
            {pedidos.map((p) => (
              <div key={p.id} className="order-card">
                <span className="order-card__id">Pedido #{p.id}</span>
                <span className={badgeClassForEstado(p.estado)}>{p.estado || 'Sin estado'}</span>
                <span className="order-card__total">${p.total}</span>
                <select
                  className="role-switcher"
                  value={p.estado || ''}
                  disabled={actualizando === p.id}
                  onChange={(e) => cambiarEstado(p, e.target.value)}
                >
                  {!ESTADOS.includes(p.estado) && p.estado && <option value={p.estado}>{p.estado}</option>}
                  {ESTADOS.map((estado) => (
                    <option key={estado} value={estado}>
                      {estado}
                    </option>
                  ))}
                </select>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
