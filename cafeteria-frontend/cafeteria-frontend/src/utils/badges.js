export function badgeClassForEstado(estado = '') {
  const normalizado = (estado || '').toLowerCase();
  if (['listo', 'entregado', 'completado', 'pagado', 'aprobado', 'exitoso'].includes(normalizado)) {
    return 'badge badge--success';
  }
  if (['cancelado', 'anulado', 'rechazado', 'fallido', 'pago_rechazado'].includes(normalizado)) {
    return 'badge badge--error';
  }
  if (!estado) {
    return 'badge badge--neutral';
  }
  return 'badge';
}
