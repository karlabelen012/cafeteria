package cl.duoc.cafeteria.pedidos.dto;

import cl.duoc.cafeteria.pedidos.model.Pedido;

import java.time.Instant;
import java.util.List;

/** Representacion completa de un pedido, para uso interno/staff. */
public record PedidoResponse(
        Long id,
        String codigoSeguimiento,
        Long clienteId,
        String clienteNombre,
        String clienteEmail,
        Long empleadoId,
        String canal,
        String estado,
        Double total,
        Instant fechaCreacion,
        List<ItemResponse> items) {

    public static PedidoResponse desde(Pedido pedido, List<ItemResponse> items) {
        return new PedidoResponse(
                pedido.getId(),
                pedido.getCodigoSeguimiento(),
                pedido.getClienteId(),
                pedido.getClienteNombre(),
                pedido.getClienteEmail(),
                pedido.getEmpleadoId(),
                pedido.getCanal(),
                pedido.getEstado(),
                pedido.getTotal(),
                pedido.getFechaCreacion(),
                items);
    }
}
