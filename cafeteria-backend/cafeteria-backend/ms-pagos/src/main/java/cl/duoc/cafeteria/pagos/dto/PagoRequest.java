package cl.duoc.cafeteria.pagos.dto;

import cl.duoc.cafeteria.pagos.model.Pago;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

/**
 * Datos de entrada para crear/actualizar un pago. El controller nunca expone
 * la entidad JPA directamente (ver docs/EP2_PLAN.md seccion 3.7). NUNCA
 * incluye el numero completo de la tarjeta ni el CVV: solo los ultimos4
 * digitos (regla de seguridad del proyecto).
 */
public record PagoRequest(
        @NotNull(message = "El id del pedido es obligatorio")
        Long pedidoId,

        @NotNull(message = "El monto es obligatorio")
        @Positive(message = "El monto debe ser mayor que 0")
        Double monto,

        @Pattern(regexp = Pago.METODOS_REGEX, message = "El metodo de pago debe ser uno de: EFECTIVO, DEBITO, CREDITO, TRANSFERENCIA")
        String metodoPago,

        @Pattern(regexp = Pago.ESTADOS_REGEX, message = "El estado debe ser uno de: APROBADO, RECHAZADO, ANULADO")
        String estado,

        @Pattern(regexp = "^\\d{4}$", message = "ultimos4 debe tener exactamente 4 digitos")
        String ultimos4
) {
}
