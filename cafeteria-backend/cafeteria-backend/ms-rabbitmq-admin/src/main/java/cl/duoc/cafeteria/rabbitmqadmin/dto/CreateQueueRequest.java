package cl.duoc.cafeteria.rabbitmqadmin.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos para crear una cola nueva (ver docs/EP2_PLAN.md seccion 3.8). Se crea
 * siempre como quorum + durable. ttlMs/maxLength/deliveryLimit son opcionales:
 * si no vienen, se usan los mismos valores por defecto que el resto del
 * sistema (ver docs/EP2_PLAN.md seccion 3.4).
 */
public record CreateQueueRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 3, max = 120, message = "El nombre debe tener entre 3 y 120 caracteres")
        @Pattern(regexp = "^(?!amq\\.)[a-z0-9]+([._-][a-z0-9]+)*$",
                message = "El nombre debe ser minusculas/numeros separados por . _ o -, y no puede empezar con 'amq.' (reservado)")
        String name,

        @Min(value = 1000, message = "ttlMs debe ser al menos 1000")
        @Max(value = 604800000, message = "ttlMs no puede superar 604800000 (7 dias)")
        Long ttlMs,

        @Min(value = 1, message = "maxLength debe ser al menos 1")
        @Max(value = 1000000, message = "maxLength no puede superar 1000000")
        Integer maxLength,

        @Min(value = 1, message = "deliveryLimit debe ser al menos 1")
        @Max(value = 20, message = "deliveryLimit no puede superar 20")
        Integer deliveryLimit) {
}
