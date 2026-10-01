package cl.duoc.cafeteria.inventario.service;

import cl.duoc.cafeteria.inventario.dto.RecetaItemRequest;
import cl.duoc.cafeteria.inventario.dto.RecetaItemResponse;

import java.util.List;

/** Reglas de negocio de las recetas (insumos que consume cada producto). */
public interface RecetaItemService {

    List<RecetaItemResponse> listar(Long productoId);

    RecetaItemResponse obtener(Long id);

    RecetaItemResponse crear(RecetaItemRequest request);

    RecetaItemResponse actualizar(Long id, RecetaItemRequest request);

    void eliminar(Long id);
}
