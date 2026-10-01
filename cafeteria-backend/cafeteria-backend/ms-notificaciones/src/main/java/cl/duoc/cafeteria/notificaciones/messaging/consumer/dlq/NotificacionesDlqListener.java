package cl.duoc.cafeteria.notificaciones.messaging.consumer.dlq;

import cl.duoc.cafeteria.common.messaging.AckHandler;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * Observa las dos DLQ de ms-notificaciones y deja registro en el log del
 * payload y del motivo (header x-death) de cada mensaje que termino ahi (ver
 * docs/EP2_PLAN.md seccion 3.5). Solo observa: no reprocesa ni reencola.
 */
@Component
public class NotificacionesDlqListener {

    private static final Logger log = LoggerFactory.getLogger(NotificacionesDlqListener.class);

    @RabbitListener(queues = "${app.rabbitmq.queues.ticket}.dlq")
    public void recibirDlqTicket(Message mensaje, Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        registrarYAck(mensaje, channel, deliveryTag);
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.alertas}.dlq")
    public void recibirDlqAlertas(Message mensaje, Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        registrarYAck(mensaje, channel, deliveryTag);
    }

    private void registrarYAck(Message mensaje, Channel channel, long deliveryTag) {
        String payload = new String(mensaje.getBody(), StandardCharsets.UTF_8);
        log.error("Mensaje en DLQ {}: payload={}, x-death={}",
                mensaje.getMessageProperties().getConsumerQueue(), payload,
                mensaje.getMessageProperties().getXDeathHeader());
        AckHandler.ack(channel, deliveryTag);
    }
}
