package cl.duoc.cafeteria.inventario.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Subconjunto de los campos de ItemPedido (ms-pedidos) que necesita
 * DescuentoStockService: que producto y cuantas unidades. Se ignoran el resto
 * de los campos reales (id, pedidoId, precioUnitario) con
 * {@code ignoreUnknown}, porque este DTO es deliberadamente un parseo parcial
 * de la respuesta de GET /api/pedidos/{id} (ver PedidoClient).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ItemDto(Long productoId, Integer cantidad) {
}
