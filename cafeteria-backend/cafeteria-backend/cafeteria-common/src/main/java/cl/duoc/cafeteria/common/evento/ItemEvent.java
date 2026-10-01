package cl.duoc.cafeteria.common.evento;

/**
 * Snapshot de un item de pedido en el momento del evento: el nombre y el
 * precio unitario viajan congelados en el mensaje (no se vuelven a consultar
 * a ms-productos), para que el historial de ventas no cambie si el producto
 * cambia de precio despues.
 */
public record ItemEvent(Long productoId, String nombreProducto, int cantidad, double precioUnitario) {
}
