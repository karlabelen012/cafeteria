package cl.duoc.cafeteria.pedidos.service;

import cl.duoc.cafeteria.pedidos.dto.CheckoutRequest;
import cl.duoc.cafeteria.pedidos.dto.CheckoutResponse;
import cl.duoc.cafeteria.pedidos.dto.PedidoRequest;
import cl.duoc.cafeteria.pedidos.dto.PedidoResponse;
import cl.duoc.cafeteria.pedidos.dto.SeguimientoResponse;

import java.util.List;

/**
 * Logica de negocio del dominio Pedidos (ver docs/EP2_PLAN.md seccion 5). El
 * controller solo delega aqui: la maquina de estados, la resolucion de
 * precios/nombres via ms-productos y la publicacion de eventos viven en la
 * implementacion.
 */
public interface PedidoService {

    List<PedidoResponse> listar();

    PedidoResponse obtener(Long id);

    /** Venta en mostrador (staff). canal debe ser "MOSTRADOR". */
    PedidoResponse crear(PedidoRequest request, String canal);

    /** Checkout publico (sin autenticacion, canal "WEB"). Solo devuelve el codigo de seguimiento. */
    CheckoutResponse checkout(CheckoutRequest request);

    /** Seguimiento publico por codigo (nunca por id secuencial). */
    SeguimientoResponse obtenerPorCodigoSeguimiento(String codigoSeguimiento);

    /**
     * Cambia el estado de un pedido validando la maquina de estados.
     *
     * @throws cl.duoc.cafeteria.pedidos.exception.RecursoNoEncontradoException si el pedido no existe (404)
     * @throws cl.duoc.cafeteria.pedidos.exception.ConflictoDeNegocioException  si la transicion no es valida (409)
     */
    PedidoResponse cambiarEstado(Long id, String nuevoEstado);

    /** Cancela un pedido. El controller ya debe haber validado el rol ADMIN antes de llamar esto. */
    PedidoResponse cancelar(Long id);

    /**
     * Metodo interno (no expuesto por HTTP), usado por PagoResultadoListener al
     * consumir pago.aprobado/pago.rechazado. Busca el pedido por su id
     * secuencial (el evento solo trae ese dato, no el codigoSeguimiento).
     *
     * @throws cl.duoc.cafeteria.pedidos.exception.RecursoNoEncontradoException si el pedido no existe
     * @throws cl.duoc.cafeteria.pedidos.exception.ConflictoDeNegocioException  si el pedido no esta en un estado desde el que se pueda pagar/rechazar
     */
    void actualizarEstadoPorResultadoPago(Long pedidoId, boolean aprobado);
}
