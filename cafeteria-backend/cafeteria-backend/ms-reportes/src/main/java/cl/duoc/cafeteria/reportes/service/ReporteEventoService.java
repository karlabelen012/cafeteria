package cl.duoc.cafeteria.reportes.service;

import cl.duoc.cafeteria.common.evento.PagoProcesadoEvent;
import cl.duoc.cafeteria.common.evento.PedidoCreadoEvent;
import cl.duoc.cafeteria.common.evento.PedidoEstadoActualizadoEvent;

/**
 * Aplica eventos de RabbitMQ al modelo de lectura de ms-reportes (ver
 * docs/EP2_PLAN.md seccion 5). No conoce RabbitMQ: los listeners solo
 * traducen mensaje -> llamada a este service + ACK/NACK.
 */
public interface ReporteEventoService {

    /** pedido.creado: pedidos por hora, top productos y clientes nuevos. */
    void registrarPedidoCreado(PedidoCreadoEvent evento);

    /** pedido.estado.actualizado: estado vigente de cada pedido. */
    void registrarEstadoActualizado(PedidoEstadoActualizadoEvent evento);

    /** pago.aprobado / pago.rechazado: ventas diarias (solo si fue aprobado). */
    void registrarPagoProcesado(PagoProcesadoEvent evento);
}
