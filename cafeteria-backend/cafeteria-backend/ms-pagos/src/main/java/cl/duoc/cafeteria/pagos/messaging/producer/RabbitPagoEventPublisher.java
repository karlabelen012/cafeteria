package cl.duoc.cafeteria.pagos.messaging.producer;

import cl.duoc.cafeteria.common.evento.PagoProcesadoEvent;
import cl.duoc.cafeteria.pagos.messaging.config.RabbitProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Publica el resultado de un pago en cafeteria.pagos.exchange, con la
 * routing key pago.aprobado o pago.rechazado segun el campo "aprobado" del
 * evento (ver docs/EP2_PLAN.md seccion 3.2/3.3).
 */
@Component
public class RabbitPagoEventPublisher implements PagoEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitProperties props;

    public RabbitPagoEventPublisher(RabbitTemplate rabbitTemplate, RabbitProperties props) {
        this.rabbitTemplate = rabbitTemplate;
        this.props = props;
    }

    @Override
    public void publicar(PagoProcesadoEvent evento) {
        String routingKey = evento.aprobado()
                ? props.getRoutingKeys().get("pago-aprobado")
                : props.getRoutingKeys().get("pago-rechazado");
        rabbitTemplate.convertAndSend(props.getExchanges().get("pagos"), routingKey, evento);
    }
}
