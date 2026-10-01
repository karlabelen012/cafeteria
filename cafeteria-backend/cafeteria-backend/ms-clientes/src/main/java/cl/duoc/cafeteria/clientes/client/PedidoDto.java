package cl.duoc.cafeteria.clientes.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Snapshot minimo del pedido que necesita ms-clientes para el upsert por
 * email (ver docs/EP2_PLAN.md seccion 5): solo clienteNombre/clienteEmail,
 * tal como los expone GET /api/pedidos/{id} de ms-pedidos (entidad
 * cl.duoc.cafeteria.pedidos.model.Pedido). Se ignoran el resto de los campos
 * de la respuesta (id, estado, total, etc.), no se necesitan aqui.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PedidoDto(Long id, String clienteNombre, String clienteEmail) {
}
