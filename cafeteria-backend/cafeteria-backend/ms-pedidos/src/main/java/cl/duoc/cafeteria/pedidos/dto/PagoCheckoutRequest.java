package cl.duoc.cafeteria.pedidos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Datos de pago que llegan desde el checkout publico. numeroTarjeta NUNCA se
 * persiste ni se publica completo: PedidoServiceImpl.checkout() solo extrae
 * los ultimos 4 digitos antes de descartar el resto (regla de seguridad del
 * proyecto, ver docs/EP2_PLAN.md seccion 2 y 5). El CVV directamente no se
 * recibe aqui.
 */
public record PagoCheckoutRequest(

        @NotBlank(message = "El metodo de pago es obligatorio")
        String metodo,

        @NotBlank(message = "El numero de tarjeta es obligatorio")
        @Pattern(regexp = "\\d{4,19}", message = "El numero de tarjeta debe contener solo digitos")
        String numeroTarjeta) {
}
