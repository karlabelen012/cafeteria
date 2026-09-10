import { useEffect, useState } from 'react';
import { useApiClient } from '../services/apiClient';
import NavBar from '../components/NavBar';
import { badgeClassForEstado } from '../utils/badges';

const ESTADOS = ['Pendiente', 'Aprobado', 'Rechazado', 'Reembolsado'];

export default function Pagos() {
  const { callApi } = useApiClient();
  const [pagos, setPagos] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');
  const [actualizando, setActualizando] = useState(null);

  useEffect(() => {
    callApi('/pagos')
      .then(setPagos)
      .catch((err) => {
        console.error(err);
        setError('No se pudieron cargar los pagos (revisa el token o el backend).');
      })
      .finally(() => setCargando(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const cambiarEstado = async (pago, nuevoEstado) => {
    setActualizando(pago.id);
    try {
      const actualizado = await callApi(`/pagos/${pago.id}`, {
        method: 'PUT',
        body: JSON.stringify({ ...pago, estado: nuevoEstado }),
      });
      setPagos((prev) => prev.map((p) => (p.id === pago.id ? actualizado : p)));
    } catch (err) {
      console.error(err);
      setError(`No se pudo actualizar el pago #${pago.id}.`);
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
            <h2>Pagos</h2>
            <p>Registro de pagos por pedido, método y estado.</p>
          </div>
        </div>

        {error && <div className="alert alert-error">{error}</div>}

        {cargando && (
          <div className="state-block">
            <div className="spinner" />
            <p>Cargando pagos...</p>
          </div>
        )}

        {!cargando && !error && pagos.length === 0 && (
          <div className="state-block">
            <h3>Todavía no hay pagos registrados</h3>
            <p>Los pagos hechos desde la tienda aparecerán aquí.</p>
          </div>
        )}

        {!cargando && pagos.length > 0 && (
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Pago</th>
                  <th>Pedido</th>
                  <th>Monto</th>
                  <th>Método</th>
                  <th>Estado</th>
                </tr>
              </thead>
              <tbody>
                {pagos.map((p) => (
                  <tr key={p.id}>
                    <td>
                      <strong>#{p.id}</strong>
                    </td>
                    <td>#{p.pedidoId}</td>
                    <td>${p.monto}</td>
                    <td>{p.metodoPago}</td>
                    <td>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                        <span className={badgeClassForEstado(p.estado)}>{p.estado || 'Sin estado'}</span>
                        <select
                          className="role-switcher"
                          value={p.estado || ''}
                          disabled={actualizando === p.id}
                          onChange={(e) => cambiarEstado(p, e.target.value)}
                        >
                          {!ESTADOS.includes(p.estado) && p.estado && (
                            <option value={p.estado}>{p.estado}</option>
                          )}
                          {ESTADOS.map((estado) => (
                            <option key={estado} value={estado}>
                              {estado}
                            </option>
                          ))}
                        </select>
                      </div>
                    </td>
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
