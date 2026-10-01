package cl.duoc.cafeteria.empleados.service;

import cl.duoc.cafeteria.empleados.dto.EmpleadoRequest;
import cl.duoc.cafeteria.empleados.dto.EmpleadoResponse;

import java.util.List;

/**
 * Reglas de negocio del modulo de empleados (ver docs/EP2_PLAN.md seccion 5).
 * Los controllers no contienen logica, solo delegan aqui.
 */
public interface EmpleadoService {

    List<EmpleadoResponse> listar();

    EmpleadoResponse obtener(Long id);

    EmpleadoResponse crear(EmpleadoRequest request);

    EmpleadoResponse actualizar(Long id, EmpleadoRequest request);

    void eliminar(Long id);

    EmpleadoResponse desactivar(Long id);
}
