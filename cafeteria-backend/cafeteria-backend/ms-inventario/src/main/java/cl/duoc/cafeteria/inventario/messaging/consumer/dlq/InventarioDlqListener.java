package cl.duoc.cafeteria.inventario.messaging.consumer.dlq;

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
 * Solo deja rastro en el log de lo que cae a la DLQ de inventario.pago-aprobado.queue
 * (payload + el header x-death que agrega RabbitMQ con el historial de rechazos),
 * para que sea facil diagnosticar por que un mensaje no se pudo procesar (ver
 * docs/EP2_PLAN.md seccion 3.5). No reprocesa nada: siempre hace ack.
 */
@Component
public class InventarioDlqListener {

    private static final Logger log = LoggerFactory.getLogger(InventarioDlqListener.class);

    @RabbitListener(queues = "${app.rabbitmq.queues.pago-aprobado}.dlq")
    public void escuchar(Message mensaje, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        String payload = new String(mensaje.getBody(), StandardCharsets.UTF_8);
        Object xDeath = mensaje.getMessageProperties().getHeaders().get("x-death");
        log.error("Mensaje en la DLQ {}: payload={}, x-death={}",
                mensaje.getMessageProperties().getConsumerQueue(), payload, xDeath);
        AckHandler.ack(channel, deliveryTag);
    }
}
