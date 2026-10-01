package cl.duoc.cafeteria.common.evento;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Publicado por ms-pedidos (exchange cafeteria.pedidos.exchange, routing key
 * "pedido.creado") apenas se crea un pedido en estado PENDIENTE_PAGO. Lo
 * consume ms-pagos para simular la pasarela (ver docs/EP2_PLAN.md seccion 3.1
 * y 3.5). SOLO trae metodoPago + ultimos4 de la tarjeta: nunca el numero
 * completo ni el CVV (seccion 5, regla de ms-pagos).
 */
public record PedidoCreadoEvent(
        UUID eventId,
        Instant ocurridoEn,
        int version,
        Long pedidoId,
        String codigoSeguimiento,
        String metodoPago,
        String ultimos4,
        double total,
        List<ItemEvent> items) {
}
