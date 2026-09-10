import { Link } from 'react-router-dom';
import { useCart } from '../context/CartContext';

export default function CartDrawer({ open, onClose }) {
  const { items, setQty, removeItem, totalPrecio } = useCart();

  return (
    <>
      <div
        className={`cart-overlay${open ? ' cart-overlay--visible' : ''}`}
        onClick={onClose}
        aria-hidden={!open}
      />
      <aside className={`cart-drawer${open ? ' cart-drawer--open' : ''}`}>
        <div className="cart-drawer__header">
          <h3>Tu pedido</h3>
          <button className="btn btn-ghost" onClick={onClose} aria-label="Cerrar carrito">
            ✕
          </button>
        </div>

        {items.length === 0 ? (
          <div className="state-block">
            <h3>Tu carrito está vacío</h3>
            <p>Agrega productos del menú para armar tu pedido.</p>
          </div>
        ) : (
          <div className="cart-drawer__lines">
            {items.map((i) => (
              <div className="cart-line" key={i.productoId}>
                <div className="cart-line__info">
                  <strong>{i.nombre}</strong>
                  <span>${i.precio} c/u</span>
                </div>
                <div className="qty-stepper">
                  <button type="button" onClick={() => setQty(i.productoId, i.cantidad - 1)}>
                    −
                  </button>
                  <span>{i.cantidad}</span>
                  <button type="button" onClick={() => setQty(i.productoId, i.cantidad + 1)}>
                    +
                  </button>
                </div>
                <span className="cart-line__subtotal">${i.precio * i.cantidad}</span>
                <button
                  type="button"
                  className="cart-line__remove"
                  onClick={() => removeItem(i.productoId)}
                  aria-label={`Quitar ${i.nombre}`}
                >
                  ✕
                </button>
              </div>
            ))}
          </div>
        )}

        {items.length > 0 && (
          <div className="cart-drawer__footer">
            <div className="cart-drawer__total">
              <span>Total</span>
              <strong>${totalPrecio}</strong>
            </div>
            <Link to="/checkout" className="btn btn-primary cart-drawer__checkout" onClick={onClose}>
              Ir a pagar
            </Link>
          </div>
        )}
      </aside>
    </>
  );
}
