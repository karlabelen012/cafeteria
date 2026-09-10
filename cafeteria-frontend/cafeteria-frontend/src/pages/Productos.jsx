import { useEffect, useState } from 'react';
import { useApiClient } from '../services/apiClient';
import NavBar from '../components/NavBar';

export default function Productos() {
  const { callApi } = useApiClient();
  const [productos, setProductos] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    callApi('/productos')
      .then(setProductos)
      .catch((err) => {
        console.error(err);
        setError('No se pudo cargar el menú (revisa el token o el backend).');
      })
      .finally(() => setCargando(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const categorias = Array.from(
    new Set(productos.map((p) => p.categoria || 'Otros')),
  );

  return (
    <div className="app-shell">
      <NavBar />
      <div className="page">
        <div className="page__header">
          <div>
            <h2>Menú de productos</h2>
            <p>Lo que hoy se puede pedir en barra.</p>
          </div>
        </div>

        {error && <div className="alert alert-error">{error}</div>}

        {cargando && (
          <div className="state-block">
            <div className="spinner" />
            <p>Cargando el menú...</p>
          </div>
        )}

        {!cargando && !error && productos.length === 0 && (
          <div className="state-block">
            <h3>Todavía no hay productos cargados</h3>
            <p>Agrega productos desde el backend para verlos aquí.</p>
          </div>
        )}

        {!cargando &&
          categorias.map((categoria) => (
            <div key={categoria}>
              <h3 className="category-heading">{categoria}</h3>
              <div className="menu-grid">
                {productos
                  .filter((p) => (p.categoria || 'Otros') === categoria)
                  .map((p) => (
                    <article key={p.id} className="product-card">
                      <div className="product-card__top">
                        <span className="product-card__name">{p.nombre}</span>
                        <span className="product-card__price">${p.precio}</span>
                      </div>
                      {p.descripcion && (
                        <p className="product-card__desc">{p.descripcion}</p>
                      )}
                    </article>
                  ))}
              </div>
            </div>
          ))}
      </div>
    </div>
  );
}
