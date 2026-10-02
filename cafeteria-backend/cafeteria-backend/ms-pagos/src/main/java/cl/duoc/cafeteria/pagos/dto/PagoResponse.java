package cl.duoc.cafeteria.pagos.dto;

import java.time.Instant;

/** Datos de salida de un pago. */
public record PagoResponse(
        Long id,
        Long pedidoId,
        Double monto,
        String metodoPago,
        String estado,
        String ultimos4,
        Instant fecha
) {
}
