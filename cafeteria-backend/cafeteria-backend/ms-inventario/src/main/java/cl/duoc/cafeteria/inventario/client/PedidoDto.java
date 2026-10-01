package cl.duoc.cafeteria.inventario.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Parseo parcial de la respuesta de GET /api/pedidos/{id} en ms-pedidos
 * (PedidoController.obtener(), que devuelve el entity Pedido completo como
 * JSON). Solo interesa la lista de items; el resto de los campos del pedido
 * (estado, total, cliente, etc.) se ignoran aqui.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PedidoDto(Long id, List<ItemDto> items) {
}
