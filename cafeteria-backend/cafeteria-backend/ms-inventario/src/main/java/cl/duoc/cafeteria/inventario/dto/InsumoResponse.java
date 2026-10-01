package cl.duoc.cafeteria.inventario.dto;

/** Datos de salida de un insumo. */
public record InsumoResponse(
        Long id,
        String nombre,
        String unidadMedida,
        Double stockActual,
        Double stockMinimo
) {
}
