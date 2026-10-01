package cl.duoc.cafeteria.inventario.dto;

/** Datos de salida de un item de receta. */
public record RecetaItemResponse(
        Long id,
        Long productoId,
        Long insumoId,
        Double cantidad
) {
}
