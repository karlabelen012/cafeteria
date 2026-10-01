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

    /**
     * Upsert por email a partir de una compra pagada (ver docs/EP2_PLAN.md
     * seccion 5, consumido por PagoAprobadoListener): si no existe un cliente
     * con ese email se crea uno nuevo, y en cualquier caso se suman puntos de
     * fidelizacion segun el monto total (1 punto cada $1.000, redondeando
     * hacia abajo). No se debe confundir con {@link #canjearPuntos}, que resta:
     * son operaciones independientes y no comparten implementacion.
     */
    ClienteResponse registrarCompra(String nombre, String email, double montoTotal);
}
