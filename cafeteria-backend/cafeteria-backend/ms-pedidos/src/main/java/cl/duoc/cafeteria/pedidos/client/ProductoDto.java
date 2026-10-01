package cl.duoc.cafeteria.pedidos.client;

/** Subconjunto de GET /api/productos/{id} de ms-productos que necesita ms-pedidos. */
public record ProductoDto(Long id, String nombre, Double precio, Boolean disponible) {
}
