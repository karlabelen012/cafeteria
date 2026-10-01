package cl.duoc.cafeteria.pagos.messaging.consumer.pedido;

import cl.duoc.cafeteria.common.evento.PedidoCreadoEvent;
import cl.duoc.cafeteria.common.exception.NonRecoverableMessageException;
import cl.duoc.cafeteria.common.exception.RecoverableMessageException;
import cl.duoc.cafeteria.common.messaging.AckHandler;
import cl.duoc.cafeteria.pagos.service.PasarelaPagoService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * Consume pedido.creado (pagos.pedido-creado.queue) y delega el trabajo en
 * PasarelaPagoService: este listener SOLO traduce mensaje -> llamada al
 * service + ACK/NACK (ver docs/EP2_PLAN.md seccion 3.5 y 3.7), sin logica de
 * negocio propia.
 */
@Component
public class PedidoCreadoListener {

    private final PasarelaPagoService pasarelaPagoService;

    public PedidoCreadoListener(PasarelaPagoService pasarelaPagoService) {
        this.pasarelaPagoService = pasarelaPagoService;
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.pedido-creado}")
    public void recibir(PedidoCreadoEvent evento, Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            pasarelaPagoService.procesar(evento);
            AckHandler.ack(channel, deliveryTag);
        } catch (NonRecoverableMessageException e) {
            AckHandler.nackSinReintento(channel, deliveryTag, e);
        } catch (RecoverableMessageException e) {
            AckHandler.nackConReintento(channel, deliveryTag, e);
        } catch (Exception e) {
            // Cualquier otro error inesperado (BD caida, bug no previsto, etc.) se
            // trata como transitorio: se reintenta hasta el x-delivery-limit.
            AckHandler.nackConReintento(channel, deliveryTag, e);
        }
    }
}
