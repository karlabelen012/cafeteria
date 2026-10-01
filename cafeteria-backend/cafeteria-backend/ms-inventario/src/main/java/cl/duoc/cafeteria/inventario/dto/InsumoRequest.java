package cl.duoc.cafeteria.inventario.dto;

import cl.duoc.cafeteria.inventario.model.Insumo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Datos de entrada para crear/actualizar un insumo. El controller nunca
 * expone la entidad JPA directamente (ver docs/EP2_PLAN.md seccion 3.7).
 */
public record InsumoRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        @Pattern(regexp = Insumo.UNIDADES_REGEX, message = "La unidad de medida debe ser una de: g, ml, unidad")
        String unidadMedida,

        @NotNull(message = "El stock actual es obligatorio")
        @PositiveOrZero(message = "El stock actual debe ser mayor o igual a 0")
        Double stockActual,

        @NotNull(message = "El stock minimo es obligatorio")
        @PositiveOrZero(message = "El stock minimo debe ser mayor o igual a 0")
        Double stockMinimo
) {
}
