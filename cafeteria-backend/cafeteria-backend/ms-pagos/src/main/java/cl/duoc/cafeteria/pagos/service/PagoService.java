package cl.duoc.cafeteria.pagos.service;

import cl.duoc.cafeteria.pagos.dto.PagoRequest;
import cl.duoc.cafeteria.pagos.dto.PagoResponse;

import java.util.List;

/** Reglas de negocio del CRUD de pagos (ver docs/EP2_PLAN.md seccion 5). */
public interface PagoService {

    List<PagoResponse> listar();

    PagoResponse obtener(Long id);

    PagoResponse crear(PagoRequest request);

    PagoResponse actualizar(Long id, PagoRequest request);

    void eliminar(Long id);

    PagoResponse anular(Long id);
}
