package cl.duoc.cafeteria.empleados.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

/**
 * Datos de entrada para crear o actualizar un empleado (POST/PUT).
 * `activo` es opcional: si no viene, el servicio lo deja en true por defecto
 * al crear.
 */
public record EmpleadoRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato valido")
        String email,

        @NotBlank(message = "El rol es obligatorio")
        @Pattern(regexp = "ADMIN|GERENTE|BARISTA|CAJERO|BODEGUERO",
                message = "El rol debe ser uno de: ADMIN, GERENTE, BARISTA, CAJERO, BODEGUERO")
        String rol,

        Boolean activo,

        @NotNull(message = "La fecha de ingreso es obligatoria")
        LocalDate fechaIngreso
) {
}
