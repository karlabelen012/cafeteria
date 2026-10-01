package cl.duoc.cafeteria.pedidos.messaging.producer;

import cl.duoc.cafeteria.common.evento.PedidoCreadoEvent;
import cl.duoc.cafeteria.common.evento.PedidoEstadoActualizadoEvent;

/** Productor de los eventos que publica ms-pedidos. El service depende de esta interfaz, no de Rabbit directamente. */
public interface PedidoEventPublisher {

    void publicarPedidoCreado(PedidoCreadoEvent evento);

    void publicarEstadoActualizado(PedidoEstadoActualizadoEvent evento);
}
