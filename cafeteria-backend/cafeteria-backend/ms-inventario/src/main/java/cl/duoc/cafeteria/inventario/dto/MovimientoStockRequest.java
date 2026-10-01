package cl.duoc.cafeteria.inventario.dto;

import cl.duoc.cafeteria.inventario.model.MovimientoStock;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

/**
 * Datos de entrada para registrar un movimiento de stock. insumoId NO va
 * aqui: viene del path (/api/inventario/{insumoId}/movimientos). fecha y
 * usuario tampoco: los fija el servidor (ver MovimientoStockService).
 */
public record MovimientoStockRequest(
        @Pattern(regexp = MovimientoStock.TIPOS_REGEX, message = "El tipo debe ser uno de: ENTRADA, SALIDA, AJUSTE")
        String tipo,

        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor que 0")
        Double cantidad,

        @NotBlank(message = "El motivo es obligatorio")
        String motivo
) {
}
