package cl.duoc.cafeteria.clientes.service;

import cl.duoc.cafeteria.clientes.dto.ClienteRequest;
import cl.duoc.cafeteria.clientes.dto.ClienteResponse;

import java.util.List;

/**
 * Logica de negocio del dominio Clientes (ver docs/EP2_PLAN.md seccion 5).
 * El controller solo delega aqui: toda regla (duplicados, existencia,
 * puntos de fidelizacion) vive en la implementacion.
 */
public interface ClienteService {

    List<ClienteResponse> listar();

    ClienteResponse obtener(Long id);

    ClienteResponse crear(ClienteRequest request);

    ClienteResponse actualizar(Long id, ClienteRequest request);

    void eliminar(Long id);

    /**
     * Descuenta puntos de fidelizacion de un cliente.
     *
     * @throws cl.duoc.cafeteria.clientes.exception.RecursoNoEncontradoException si el cliente no existe (404)
     * @throws cl.duoc.cafeteria.clientes.exception.ConflictoDeNegocioException si no tiene puntos suficientes (409)
     */
    ClienteResponse canjearPuntos(Long id, int puntos);
}
