package cl.duoc.cafeteria.pedidos.messaging.consumer.dlq;

import cl.duoc.cafeteria.common.messaging.AckHandler;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * Escucha la DLQ de ms-pedidos solo para dejar evidencia en el log (payload +
 * x-death, con la cantidad de reintentos y el motivo del ultimo fallo). No
 * reprocesa nada: confirma (ack) para sacar el mensaje de la cola una vez
 * logueado (ver docs/EP2_PLAN.md seccion 3.5).
 */
@Component
public class PedidosDlqListener {

    private static final Logger log = LoggerFactory.getLogger(PedidosDlqListener.class);

    @RabbitListener(queues = "${app.rabbitmq.queues.pago-resultado}.dlq")
    public void manejar(Message mensaje, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        Object xDeath = mensaje.getMessageProperties().getHeaders().get("x-death");
        log.error("Mensaje en DLQ {}: payload={}, x-death={}",
                mensaje.getMessageProperties().getConsumerQueue(),
                new String(mensaje.getBody()),
                xDeath);
        AckHandler.ack(channel, deliveryTag);
    }
}
