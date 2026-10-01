package cl.duoc.cafeteria.proveedores.dto;

import cl.duoc.cafeteria.proveedores.validation.Rut;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Datos de entrada para crear/actualizar un proveedor (ver docs/EP2_PLAN.md
 * seccion 5). "insumosQueProvee" es texto libre (ej: "Cafe en grano, leche").
 */
public record ProveedorRequest(

        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        @NotBlank(message = "El RUT es obligatorio")
        @Rut
        String rut,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato valido")
        String email,

        @NotBlank(message = "El telefono es obligatorio")
        String telefono,

        String insumosQueProvee
) {
}
