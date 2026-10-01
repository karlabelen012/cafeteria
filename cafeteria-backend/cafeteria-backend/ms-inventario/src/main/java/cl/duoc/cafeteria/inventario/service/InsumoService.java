package cl.duoc.cafeteria.inventario.service;

import cl.duoc.cafeteria.inventario.dto.InsumoRequest;
import cl.duoc.cafeteria.inventario.dto.InsumoResponse;

import java.util.List;

/** Reglas de negocio de los insumos (ver docs/EP2_PLAN.md seccion 5). */
public interface InsumoService {

    List<InsumoResponse> listar();

    InsumoResponse obtener(Long id);

    InsumoResponse crear(InsumoRequest request);

    InsumoResponse actualizar(Long id, InsumoRequest request);

    void eliminar(Long id);

    /** Insumos cuyo stockActual <= stockMinimo (alerta de reposicion). */
    List<InsumoResponse> alertas();
}
