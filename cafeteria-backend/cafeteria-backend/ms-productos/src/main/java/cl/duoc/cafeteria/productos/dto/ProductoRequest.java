package cl.duoc.cafeteria.productos.dto;

import cl.duoc.cafeteria.productos.model.Producto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

/**
 * Datos de entrada para crear/actualizar un producto. El controller nunca
 * expone la entidad JPA directamente (ver docs/EP2_PLAN.md seccion 3.7).
 */
public record ProductoRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        String descripcion,

        @Positive(message = "El precio debe ser mayor que 0")
        Double precio,

        @Pattern(regexp = Producto.CATEGORIAS_REGEX, message = "La categoria debe ser una de: Bebidas calientes, Bebidas frías, Pastelería, Galletas")
        String categoria,

        Boolean disponible,

        String imagenUrl
) {
}
