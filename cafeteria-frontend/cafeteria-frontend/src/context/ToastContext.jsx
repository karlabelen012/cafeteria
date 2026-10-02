import { createContext, useCallback, useContext, useRef, useState } from 'react';

// Toasts de exito/error para las acciones del dashboard (ver docs/EP2_PLAN.md
// seccion 6.2: "toasts de éxito/error"). Un solo contenedor global, montado
// una vez en main.jsx, para que cualquier pagina pueda disparar un toast con
// useToasts() sin tener que renderizar su propio contenedor.
const ToastContext = createContext(null);

let contador = 0;

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([]);
  const timers = useRef({});

  const quitar = useCallback((id) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
    clearTimeout(timers.current[id]);
    delete timers.current[id];
  }, []);

  const mostrar = useCallback(
    (mensaje, tipo = 'success', duracionMs = 4000) => {
      const id = ++contador;
      setToasts((prev) => [...prev, { id, mensaje, tipo }]);
      timers.current[id] = setTimeout(() => quitar(id), duracionMs);
      return id;
    },
    [quitar]
  );

  const valor = {
    success: (mensaje) => mostrar(mensaje, 'success'),
    error: (mensaje) => mostrar(mensaje, 'error', 6000),
  };

  return (
    <ToastContext.Provider value={valor}>
      {children}
      <div className="dash-toasts">
        {toasts.map((t) => (
          <div key={t.id} className={`dash-toast dash-toast--${t.tipo}`} onClick={() => quitar(t.id)}>
            {t.mensaje}
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
}

export function useToasts() {
  const ctx = useContext(ToastContext);
  if (!ctx) {
    throw new Error('useToasts debe usarse dentro de <ToastProvider>');
  }
  return ctx;
}
