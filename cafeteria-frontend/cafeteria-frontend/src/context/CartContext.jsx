import { createContext, useContext, useEffect, useState } from 'react';

const CartContext = createContext(null);
const STORAGE_KEY = 'cg360_cart';

export function CartProvider({ children }) {
  const [items, setItems] = useState(() => {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      return raw ? JSON.parse(raw) : [];
    } catch {
      return [];
    }
  });

  useEffect(() => {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(items));
    } catch {
      // localStorage no disponible (modo privado, etc.): el carrito sigue
      // funcionando solo en memoria durante la sesión.
    }
  }, [items]);

  function addItem(producto) {
    setItems((prev) => {
      const existente = prev.find((i) => i.productoId === producto.id);
      if (existente) {
        return prev.map((i) =>
          i.productoId === producto.id ? { ...i, cantidad: i.cantidad + 1 } : i,
        );
      }
      return [
        ...prev,
        {
          productoId: producto.id,
          nombre: producto.nombre,
          precio: producto.precio,
          categoria: producto.categoria,
          cantidad: 1,
        },
      ];
    });
  }

  function setQty(productoId, cantidad) {
    setItems((prev) => {
      if (cantidad <= 0) return prev.filter((i) => i.productoId !== productoId);
      return prev.map((i) => (i.productoId === productoId ? { ...i, cantidad } : i));
    });
  }

  function removeItem(productoId) {
    setItems((prev) => prev.filter((i) => i.productoId !== productoId));
  }

  function clear() {
    setItems([]);
  }

  const totalItems = items.reduce((acc, i) => acc + i.cantidad, 0);
  const totalPrecio = items.reduce((acc, i) => acc + i.cantidad * i.precio, 0);

  return (
    <CartContext.Provider
      value={{ items, addItem, setQty, removeItem, clear, totalItems, totalPrecio }}
    >
      {children}
    </CartContext.Provider>
  );
}

export function useCart() {
  const ctx = useContext(CartContext);
  if (!ctx) throw new Error('useCart debe usarse dentro de <CartProvider>');
  return ctx;
}
