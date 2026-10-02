package cl.duoc.cafeteria.rabbitmqadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos para crear o eliminar un binding cola<->exchange (ver
 * docs/EP2_PLAN.md seccion 3.8). routingKey puede venir vacio (exchanges
 * fanout) y admite los comodines "*"/"#" de los exchanges topic.
 */
public record BindingRequest(
        @NotBlank(message = "La cola es obligatoria") String queue,
        @NotBlank(message = "El exchange es obligatorio") String exchange,
        @Size(max = 255, message = "La routing key no puede superar 255 caracteres")
        @Pattern(regexp = "^[a-zA-Z0-9_.*#-]*$", message = "Routing key invalida")
        String routingKey) {
}
