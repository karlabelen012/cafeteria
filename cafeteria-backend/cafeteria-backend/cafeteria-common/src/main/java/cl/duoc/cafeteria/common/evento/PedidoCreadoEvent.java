package cl.duoc.cafeteria.common.evento;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Publicado por ms-pedidos (exchange cafeteria.pedidos.exchange, routing key
 * "pedido.creado") apenas se crea un pedido en estado PENDIENTE_PAGO. Lo
 * consume ms-pagos para simular la pasarela (ver docs/EP2_PLAN.md seccion 3.1
 * y 3.5) y ms-reportes para el modelo de lectura (seccion 5: top productos,
 * pedidos por hora/estado y clientes nuevos). SOLO trae metodoPago + ultimos4
 * de la tarjeta: nunca el numero completo ni el CVV (seccion 5, regla de
 * ms-pagos). clienteEmail puede venir null (venta de mostrador sin cliente
 * identificado).
 */
public record PedidoCreadoEvent(
        UUID eventId,
        Instant ocurridoEn,
        int version,
        Long pedidoId,
        String codigoSeguimiento,
        String clienteEmail,
        String metodoPago,
        String ultimos4,
        double total,
        List<ItemEvent> items) {
}
