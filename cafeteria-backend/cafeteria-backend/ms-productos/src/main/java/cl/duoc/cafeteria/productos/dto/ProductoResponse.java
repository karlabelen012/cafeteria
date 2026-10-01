package cl.duoc.cafeteria.productos.dto;

/** Datos de salida de un producto. */
public record ProductoResponse(
        Long id,
        String nombre,
        String descripcion,
        Double precio,
        String categoria,
        Boolean disponible,
        String imagenUrl
) {
}
