package cl.duoc.cafeteria.notificaciones.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Snapshot minimo del pedido que necesita ms-notificaciones para armar el
 * ticket (ver docs/EP2_PLAN.md seccion 5), tal como lo expone
 * GET /api/pedidos/{id} de ms-pedidos (dto PedidoResponse). Se ignora el
 * resto de los campos de la respuesta (clienteId, empleadoId, canal, estado,
 * total, fechaCreacion): el total y el metodo de pago del ticket vienen del
 * propio PagoProcesadoEvent, no de aqui.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PedidoDto(
        Long id,
        String codigoSeguimiento,
        String clienteNombre,
        String clienteEmail,
        List<ItemDto> items) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ItemDto(Long productoId, String nombreProducto, Integer cantidad, Double precioUnitario) {
    }
}
