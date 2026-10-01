package cl.duoc.cafeteria.clientes.messaging.consumer.dlq;

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
 * Escucha clientes.pago-aprobado.queue.dlq (ver docs/EP2_PLAN.md seccion 3.5):
 * solo deja constancia en el log (payload + cabecera x-death) para diagnostico
 * manual. No reprocesa ni reintenta, simplemente confirma (ack) para sacar el
 * mensaje de la cola una vez queda registrado.
 */
@Component
public class ClientesDlqListener {

    private static final Logger log = LoggerFactory.getLogger(ClientesDlqListener.class);

    @RabbitListener(queues = "${app.rabbitmq.queues.pago-aprobado}.dlq")
    public void recibir(Message mensaje, Channel channel,
                         @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        Object xDeath = mensaje.getMessageProperties().getHeaders().get("x-death");
        String payload = new String(mensaje.getBody(), StandardCharsets.UTF_8);
        log.error("Mensaje en DLQ {}: payload={}, x-death={}",
                mensaje.getMessageProperties().getConsumerQueue(), payload, xDeath);
        AckHandler.ack(channel, deliveryTag);
    }
}
