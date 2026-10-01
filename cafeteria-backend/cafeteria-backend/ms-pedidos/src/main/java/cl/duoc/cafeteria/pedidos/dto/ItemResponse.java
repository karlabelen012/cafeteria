package cl.duoc.cafeteria.pedidos.dto;

import cl.duoc.cafeteria.pedidos.model.ItemPedido;

public record ItemResponse(
        Long id,
        Long productoId,
        String nombreProducto,
        Integer cantidad,
        Double precioUnitario) {

    public static ItemResponse desde(ItemPedido item) {
        return new ItemResponse(
                item.getId(),
                item.getProductoId(),
                item.getNombreProducto(),
                item.getCantidad(),
                item.getPrecioUnitario());
    }
}
