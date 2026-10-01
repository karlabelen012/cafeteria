package cl.duoc.cafeteria.inventario.service;

/**
 * Descuenta stock de insumos segun la receta de los productos de un pedido ya
 * pagado (ver docs/EP2_PLAN.md seccion 5). Lo invoca PagoAprobadoListener al
 * recibir un evento pago.aprobado.
 */
public interface DescuentoStockService {

    void descontarPorPedido(Long pedidoId);
}
