package cl.duoc.cafeteria.empleados.dto;

import java.time.LocalDate;

/** Representacion de salida de un empleado, incluye el id generado. */
public record EmpleadoResponse(
        Long id,
        String nombre,
        String email,
        String rol,
        Boolean activo,
        LocalDate fechaIngreso
) {
}
