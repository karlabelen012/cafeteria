package cl.duoc.cafeteria.clientes.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Datos de entrada para crear/actualizar un cliente (ver docs/EP2_PLAN.md
 * seccion 5). El telefono es opcional, pero si viene debe cumplir el formato
 * de un numero movil chileno.
 */
public record ClienteRequest(

        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email debe tener un formato valido")
        String email,

        @Pattern(regexp = "^\\+?56 ?9\\d{8}$", message = "El telefono debe ser un numero movil chileno valido (ej: +56912345678)")
        String telefono,

        @Min(value = 0, message = "Los puntos de fidelizacion no pueden ser negativos")
        Integer puntosFidelizacion) {
}
