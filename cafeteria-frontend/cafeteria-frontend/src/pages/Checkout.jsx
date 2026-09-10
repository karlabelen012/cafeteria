import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useApiClient } from '../services/apiClient';
import { useCart } from '../context/CartContext';
import CoffeeIcon from '../components/CoffeeIcon';

export default function Checkout() {
  const { callApi } = useApiClient();
  const { items, totalPrecio, clear } = useCart();
  const navigate = useNavigate();

  const [form, setForm] = useState({
    nombre: '',
    email: '',
    telefono: '',
    numeroTarjeta: '',
    vencimiento: '',
    cvv: '',
  });
  const [procesando, setProcesando] = useState(false);
  const [error, setError] = useState('');

  const update = (campo) => (e) => setForm((f) => ({ ...f, [campo]: e.target.value }));

  if (items.length === 0) {
    return (
      <div className="app-shell">
        <header className="navbar">
          <Link to="/" className="navbar__brand">
            <CoffeeIcon />
            CafeGestión360
          </Link>
        </header>
        <div className="page">
          <div className="state-block">
            <h3>No tienes productos en tu pedido</h3>
            <p>Vuelve a la tienda para agregar algo del menú.</p>
            <Link to="/" className="btn btn-primary">
              Ir a la tienda
            </Link>
          </div>
        </div>
      </div>
    );
  }

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setProcesando(true);
    try {
      // 1) Buscar (o crear) el cliente por email — así queda registrado con
      // sus puntos de fidelización en ms-clientes.
      const clientes = await callApi('/clientes');
      let cliente = clientes.find((c) => c.email?.toLowerCase() === form.email.toLowerCase());
      if (!cliente) {
        cliente = await callApi('/clientes', {
          method: 'POST',
          body: JSON.stringify({
            nombre: form.nombre,
            email: form.email,
            telefono: form.telefono,
            puntosFidelizacion: 0,
          }),
        });
      }

      // 2) Crear el pedido en ms-pedidos (sin empleado asignado: lo toma un
      // barista cuando lo prepare).
      const pedido = await callApi('/pedidos', {
        method: 'POST',
        body: JSON.stringify({
          clienteId: cliente.id,
          empleadoId: null,
          estado: 'Pendiente',
          total: totalPrecio,
        }),
      });

      // 3) Registrar cada linea del carrito como item del pedido.
      for (const item of items) {
        await callApi(`/pedidos/${pedido.id}/items`, {
          method: 'POST',
          body: JSON.stringify({
            productoId: item.productoId,
            cantidad: item.cantidad,
            precioUnitario: item.precio,
          }),
        });
      }

      // 4) Simular el pago: la tarjeta nunca sale del navegador ni se valida
      // contra nada real, solo se simula el "procesando..." y se guarda el
      // resultado en ms-pagos.
      await new Promise((resolve) => setTimeout(resolve, 1300));
      await callApi('/pagos', {
        method: 'POST',
        body: JSON.stringify({
          pedidoId: pedido.id,
          monto: totalPrecio,
          metodoPago: 'Tarjeta (simulada)',
          estado: 'Aprobado',
        }),
      });

      // 5) El pago se aprobó: el pedido pasa a preparación.
      await callApi(`/pedidos/${pedido.id}`, {
        method: 'PUT',
        body: JSON.stringify({ ...pedido, estado: 'En preparación' }),
      });

      clear();
      try {
        localStorage.setItem('cg360_ultimo_pedido', String(pedido.id));
      } catch {
        // no bloquea el flujo si localStorage no esta disponible
      }
      navigate(`/pedido/${pedido.id}`);
    } catch (err) {
      console.error(err);
      setError('No se pudo procesar el pedido. Revisa que el backend esté corriendo e inténtalo de nuevo.');
    } finally {
      setProcesando(false);
    }
  };

  return (
    <div className="app-shell">
      <header className="navbar">
        <Link to="/" className="navbar__brand">
          <CoffeeIcon />
          CafeGestión360
        </Link>
      </header>
      <div className="page checkout-page">
        <h2>Confirma tu pedido</h2>

        <div className="checkout-layout">
          <form className="checkout-form" onSubmit={handleSubmit}>
            <h3>Tus datos</h3>
            <label>
              Nombre completo
              <input required value={form.nombre} onChange={update('nombre')} />
            </label>
            <label>
              Email
              <input required type="email" value={form.email} onChange={update('email')} />
            </label>
            <label>
              Teléfono
              <input required value={form.telefono} onChange={update('telefono')} />
            </label>

            <h3>Pago con tarjeta</h3>
            <p className="checkout-form__note">
              Esto es una simulación para fines académicos: no se procesa ni se envía a ninguna
              pasarela de pago real.
            </p>
            <label>
              Número de tarjeta
              <input
                required
                inputMode="numeric"
                maxLength={19}
                placeholder="4111 1111 1111 1111"
                value={form.numeroTarjeta}
                onChange={update('numeroTarjeta')}
              />
            </label>
            <div className="checkout-form__row">
              <label>
                Vencimiento
                <input
                  required
                  placeholder="MM/AA"
                  value={form.vencimiento}
                  onChange={update('vencimiento')}
                />
              </label>
              <label>
                CVV
                <input required inputMode="numeric" maxLength={4} value={form.cvv} onChange={update('cvv')} />
              </label>
            </div>

            {error && <div className="alert alert-error">{error}</div>}

            <button className="btn btn-primary" type="submit" disabled={procesando}>
              {procesando ? 'Procesando pago...' : `Pagar $${totalPrecio}`}
            </button>
          </form>

          <div className="checkout-summary">
            <h3>Resumen</h3>
            {items.map((i) => (
              <div className="checkout-summary__line" key={i.productoId}>
                <span>
                  {i.cantidad} × {i.nombre}
                </span>
                <span>${i.precio * i.cantidad}</span>
              </div>
            ))}
            <div className="checkout-summary__total">
              <span>Total</span>
              <strong>${totalPrecio}</strong>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
