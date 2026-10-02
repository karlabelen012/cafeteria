import { useEffect } from 'react';

// Modal generico (crear/editar, confirmaciones, etc. — ver docs/EP2_PLAN.md
// seccion 6.2). Cierra con Escape o clic fuera del cuadro.
export default function Modal({ title, onClose, children, width }) {
  useEffect(() => {
    function onKeyDown(e) {
      if (e.key === 'Escape') onClose();
    }
    document.addEventListener('keydown', onKeyDown);
    return () => document.removeEventListener('keydown', onKeyDown);
  }, [onClose]);

  return (
    <div className="dash-modal-overlay" onClick={onClose}>
      <div
        className="dash-modal"
        style={width ? { maxWidth: width } : undefined}
        onClick={(e) => e.stopPropagation()}
        role="dialog"
        aria-modal="true"
      >
        {title && <h3>{title}</h3>}
        {children}
      </div>
    </div>
  );
}
