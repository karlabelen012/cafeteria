package cl.duoc.cafeteria.common.evento;

import java.time.Instant;
import java.util.UUID;

/**
 * Publicado por ms-pagos (exchange cafeteria.pagos.exchange, routing key
 * "pago.aprobado" o "pago.rechazado" segun {@code aprobado}) al terminar de
 * simular la pasarela. Lo consumen ms-pedidos, ms-inventario, ms-clientes,
 * ms-reportes y ms-notificaciones (ver docs/EP2_PLAN.md seccion 3.3).
 */
public record PagoProcesadoEvent(
        UUID eventId,
        Instant ocurridoEn,
        int version,
        Long pedidoId,
        Long pagoId,
        boolean aprobado,
        double monto,
        String metodoPago) {
}
