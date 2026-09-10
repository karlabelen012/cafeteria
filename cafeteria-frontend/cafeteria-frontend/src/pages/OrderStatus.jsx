import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useApiClient } from '../services/apiClient';
import CoffeeIcon from '../components/CoffeeIcon';
import { badgeClassForEstado } from '../utils/badges';

const PASOS = ['Pendiente', 'En preparación', 'Listo', 'Entregado'];

export default function OrderStatus() {
  const { id } = useParams();
  const { callApi } = useApiClient();
  const [pedido, setPedido] = useState(null);
  const [items, setItems] = useState([]);
  const [productos, setProductos] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    Promise.all([callApi(`/pedidos/${id}`), callApi(`/pedidos/${id}/items`), callApi('/productos')])
      .then(([pedidoData, itemsData, productosData]) => {
        setPedido(pedidoData);
        setItems(itemsData);
        setProductos(productosData);
      })
      .catch((err) => {
        console.error(err);
        setError('No encontramos ese pedido. Verifica el número o que el backend esté corriendo.');
      })
      .finally(() => setCargando(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  const nombreProducto = (productoId) =>
    productos.find((p) => p.id === productoId)?.nombre || `Producto #${productoId}`;

  const pasoActual = pedido
    ? PASOS.findIndex((p) => p.toLowerCase() === (pedido.estado || '').toLowerCase())
    : -1;

  return (
    <div className="app-shell">
      <header className="navbar">
        <Link to="/" className="navbar__brand">
          <CoffeeIcon />
          CafeGestión360
        </Link>
      </header>
      <div className="page">
        {cargando && (
          <div className="state-block">
            <div className="spinner" />
            <p>Buscando tu pedido...</p>
          </div>
        )}

        {error && <div className="alert alert-error">{error}</div>}

        {pedido && (
          <>
            <div className="page__header">
              <div>
                <h2>Pedido #{pedido.id}</h2>
                <p>Gracias por tu compra. Así va tu pedido:</p>
              </div>
              <span className={badgeClassForEstado(pedido.estado)}>{pedido.estado}</span>
            </div>

            {pasoActual >= 0 && (
              <div className="order-timeline">
                {PASOS.map((paso, idx) => (
                  <div
                    key={paso}
                    className={`order-timeline__step${idx <= pasoActual ? ' order-timeline__step--done' : ''}`}
                  >
                    <span className="order-timeline__dot" />
                    <span>{paso}</span>
                  </div>
                ))}
              </div>
            )}

            <div className="table-wrap" style={{ marginTop: 24 }}>
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Producto</th>
                    <th>Cantidad</th>
                    <th>Subtotal</th>
                  </tr>
                </thead>
                <tbody>
                  {items.map((i) => (
                    <tr key={i.id}>
                      <td>{nombreProducto(i.productoId)}</td>
                      <td>{i.cantidad}</td>
                      <td>${i.precioUnitario * i.cantidad}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            <div className="checkout-summary__total" style={{ marginTop: 16 }}>
              <span>Total</span>
              <strong>${pedido.total}</strong>
            </div>

            <Link to="/" className="btn btn-outline" style={{ marginTop: 24, display: 'inline-flex' }}>
              Volver a la tienda
            </Link>
          </>
        )}
      </div>
    </div>
  );
}
