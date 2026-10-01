package cl.duoc.cafeteria.pedidos.messaging.producer;

import cl.duoc.cafeteria.common.evento.PedidoCreadoEvent;
import cl.duoc.cafeteria.common.evento.PedidoEstadoActualizadoEvent;
import cl.duoc.cafeteria.pedidos.messaging.config.RabbitProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitPedidoEventPublisher implements PedidoEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitProperties props;

    public RabbitPedidoEventPublisher(RabbitTemplate rabbitTemplate, RabbitProperties props) {
        this.rabbitTemplate = rabbitTemplate;
        this.props = props;
    }

    @Override
    public void publicarPedidoCreado(PedidoCreadoEvent evento) {
        rabbitTemplate.convertAndSend(
                props.getExchanges().get("pedidos"), props.getRoutingKeys().get("pedido-creado"), evento);
    }

    @Override
    public void publicarEstadoActualizado(PedidoEstadoActualizadoEvent evento) {
        rabbitTemplate.convertAndSend(
                props.getExchanges().get("pedidos"), props.getRoutingKeys().get("pedido-estado-actualizado"), evento);
    }
}
