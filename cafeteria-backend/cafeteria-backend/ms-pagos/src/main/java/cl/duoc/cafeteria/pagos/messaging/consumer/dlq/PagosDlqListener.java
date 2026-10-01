package cl.duoc.cafeteria.pagos.messaging.consumer.dlq;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Observa la DLQ de ms-pagos y deja registro en el log del payload y del
 * motivo (header x-death) de cada mensaje que termino ahi (ver
 * docs/EP2_PLAN.md seccion 3.5). Solo observa: no reprocesa ni reencola.
 */
@Component
public class PagosDlqListener {

    private static final Logger log = LoggerFactory.getLogger(PagosDlqListener.class);

    @RabbitListener(queues = "${app.rabbitmq.queues.pedido-creado}.dlq")
    public void recibir(Message message, Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        String payload = new String(message.getBody(), StandardCharsets.UTF_8);
        log.error("Mensaje en DLQ [{}]: payload={}, x-death={}",
                message.getMessageProperties().getConsumerQueue(), payload,
                message.getMessageProperties().getXDeathHeader());
        channel.basicAck(deliveryTag, false);
    }
}
