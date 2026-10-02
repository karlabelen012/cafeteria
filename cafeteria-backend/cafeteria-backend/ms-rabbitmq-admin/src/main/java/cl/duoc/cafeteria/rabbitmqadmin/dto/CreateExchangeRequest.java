package cl.duoc.cafeteria.rabbitmqadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Datos para crear un exchange nuevo (ver docs/EP2_PLAN.md seccion 3.8). */
public record CreateExchangeRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 3, max = 120, message = "El nombre debe tener entre 3 y 120 caracteres")
        @Pattern(regexp = "^(?!amq\\.)[a-z0-9]+([._-][a-z0-9]+)*$",
                message = "El nombre debe ser minusculas/numeros separados por . _ o -, y no puede empezar con 'amq.' (reservado)")
        String name,

        @NotBlank(message = "El tipo es obligatorio")
        @Pattern(regexp = "direct|topic|fanout|headers", message = "Tipo invalido (direct|topic|fanout|headers)")
        String type) {
}
