package cl.duoc.cafeteria.common.evento;

import java.time.Instant;
import java.util.UUID;

/**
 * Publicado por ms-inventario (exchange cafeteria.inventario.exchange,
 * routing key "stock.bajo") cuando, tras un movimiento de stock, un insumo
 * queda con stockActual <= stockMinimo. Lo consume ms-notificaciones para
 * generar una alerta en el dashboard (ver docs/EP2_PLAN.md seccion 3.1).
 */
public record StockBajoEvent(
        UUID eventId,
        Instant ocurridoEn,
        int version,
        Long insumoId,
        String nombreInsumo,
        double stockActual,
        double stockMinimo) {
}
