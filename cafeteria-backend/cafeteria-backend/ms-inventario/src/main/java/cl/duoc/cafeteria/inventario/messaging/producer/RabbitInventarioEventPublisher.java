package cl.duoc.cafeteria.inventario.messaging.producer;

import cl.duoc.cafeteria.common.evento.StockBajoEvent;
import cl.duoc.cafeteria.inventario.messaging.config.RabbitProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitInventarioEventPublisher implements InventarioEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitProperties props;

    public RabbitInventarioEventPublisher(RabbitTemplate rabbitTemplate, RabbitProperties props) {
        this.rabbitTemplate = rabbitTemplate;
        this.props = props;
    }

    @Override
    public void publicarStockBajo(StockBajoEvent evento) {
        rabbitTemplate.convertAndSend(
                props.getExchanges().get("inventario"), props.getRoutingKeys().get("stock-bajo"), evento);
    }
}
