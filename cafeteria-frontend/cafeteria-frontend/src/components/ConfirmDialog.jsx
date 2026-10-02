import Modal from './Modal.jsx';

// Confirmacion antes de eliminar (ver docs/EP2_PLAN.md seccion 6.2:
// "confirmación al eliminar").
export default function ConfirmDialog({ title, message, confirmLabel = 'Eliminar', onConfirm, onCancel, loading }) {
  return (
    <Modal title={title} onClose={onCancel} width={420}>
      <p>{message}</p>
      <div className="dash-modal__actions">
        <button className="dash-btn dash-btn--ghost" onClick={onCancel} disabled={loading}>
          Cancelar
        </button>
        <button className="dash-btn dash-btn--danger" onClick={onConfirm} disabled={loading}>
          {loading ? 'Eliminando...' : confirmLabel}
        </button>
      </div>
    </Modal>
  );
}
