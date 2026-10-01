package cl.duoc.cafeteria.common.evento;

import java.time.Instant;
import java.util.UUID;

/**
 * Publicado por ms-pedidos (exchange cafeteria.pedidos.exchange, routing key
 * "pedido.estado.actualizado") cada vez que un barista avanza el estado de un
 * pedido (PAGADO -> EN_PREPARACION -> LISTO -> ENTREGADO). Lo consumen
 * ms-notificaciones (alerta "pedido listo") y ms-reportes (conteo por estado).
 */
public record PedidoEstadoActualizadoEvent(
        UUID eventId,
        Instant ocurridoEn,
        int version,
        Long pedidoId,
        String codigoSeguimiento,
        String estadoAnterior,
        String estadoNuevo) {
}
