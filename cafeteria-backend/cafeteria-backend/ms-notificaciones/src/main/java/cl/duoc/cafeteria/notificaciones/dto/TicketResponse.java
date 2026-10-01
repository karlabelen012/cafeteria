package cl.duoc.cafeteria.notificaciones.dto;

import cl.duoc.cafeteria.notificaciones.model.ItemTicket;
import cl.duoc.cafeteria.notificaciones.model.Ticket;

import java.time.Instant;
import java.util.List;

public record TicketResponse(
        Long numero,
        Long pedidoId,
        String codigoSeguimiento,
        String clienteNombre,
        String clienteEmail,
        Double total,
        String metodoPago,
        Instant fecha,
        List<ItemTicketResponse> items) {

    public static TicketResponse desde(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getPedidoId(),
                ticket.getCodigoSeguimiento(),
                ticket.getClienteNombre(),
                ticket.getClienteEmail(),
                ticket.getTotal(),
                ticket.getMetodoPago(),
                ticket.getFecha(),
                ticket.getItems().stream().map(ItemTicketResponse::desde).toList());
    }

    public record ItemTicketResponse(
            Long productoId, String nombreProducto, Integer cantidad, Double precioUnitario) {

        public static ItemTicketResponse desde(ItemTicket item) {
            return new ItemTicketResponse(
                    item.getProductoId(), item.getNombreProducto(), item.getCantidad(), item.getPrecioUnitario());
        }
    }
}
