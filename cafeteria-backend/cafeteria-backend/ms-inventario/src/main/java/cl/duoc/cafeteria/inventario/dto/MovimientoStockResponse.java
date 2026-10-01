package cl.duoc.cafeteria.inventario.dto;

import java.time.Instant;

/** Datos de salida de un movimiento de stock. */
public record MovimientoStockResponse(
        Long id,
        Long insumoId,
        String tipo,
        Double cantidad,
        String motivo,
        Instant fecha,
        String usuario
) {
}
