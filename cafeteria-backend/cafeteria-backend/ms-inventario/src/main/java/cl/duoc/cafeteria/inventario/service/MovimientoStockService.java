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
}
