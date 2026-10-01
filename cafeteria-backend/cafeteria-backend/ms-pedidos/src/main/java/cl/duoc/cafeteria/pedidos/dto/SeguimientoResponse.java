package cl.duoc.cafeteria.pedidos.dto;

import cl.duoc.cafeteria.pedidos.model.Pedido;

import java.time.Instant;
import java.util.List;

/**
 * Respuesta publica de seguimiento (GET /api/public/pedidos/{codigo}): a
 * proposito NO incluye clienteNombre/clienteEmail ni ningun otro dato
 * sensible del cliente (ver docs/EP2_PLAN.md seccion 2, hallazgo #7).
 */
public record SeguimientoResponse(
        String codigoSeguimiento,
        String estado,
        Double total,
        Instant fechaCreacion,
        List<ItemResponse> items) {

    public static SeguimientoResponse desde(Pedido pedido, List<ItemResponse> items) {
        return new SeguimientoResponse(
                pedido.getCodigoSeguimiento(),
                pedido.getEstado(),
                pedido.getTotal(),
                pedido.getFechaCreacion(),
                items);
    }
}
