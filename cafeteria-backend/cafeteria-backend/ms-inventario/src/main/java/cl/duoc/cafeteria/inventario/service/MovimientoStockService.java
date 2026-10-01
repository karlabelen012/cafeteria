package cl.duoc.cafeteria.inventario.service;

import cl.duoc.cafeteria.inventario.dto.MovimientoStockRequest;
import cl.duoc.cafeteria.inventario.dto.MovimientoStockResponse;

import java.util.List;

/**
 * Reglas de negocio de los movimientos de stock: aplica el movimiento sobre
 * el stockActual del insumo y deja constancia del movimiento en la
 * auditoria (ver docs/EP2_PLAN.md seccion 5).
 */
public interface MovimientoStockService {

    List<MovimientoStockResponse> listar(Long insumoId);

    MovimientoStockResponse registrar(Long insumoId, MovimientoStockRequest request);

    /**
     * Salida de stock que NUNCA falla por stock insuficiente: la usa
     * exclusivamente DescuentoStockService al descontar insumos por un pedido
     * pagado (ver docs/EP2_PLAN.md seccion 5). A diferencia de
     * {@link #registrar} con tipo SALIDA (que rechaza todo el movimiento si
     * dejaria el stock negativo), aqui si "cantidad" supera el stock
     * disponible, el stock queda en 0 y el movimiento se audita como AJUSTE
     * (no SALIDA), dejando claro que el resultado fue un piso forzado a 0 y
     * no una resta exacta.
     */
    MovimientoStockResponse registrarSalidaForzada(Long insumoId, Double cantidad, String motivo);
}
