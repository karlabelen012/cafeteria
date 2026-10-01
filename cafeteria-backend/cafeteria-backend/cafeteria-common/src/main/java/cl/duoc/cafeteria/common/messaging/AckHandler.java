package cl.duoc.cafeteria.common.messaging;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Centraliza el try/catch de ACK manual que, si no, se repetiria en cada
 * {@code @RabbitListener} (ver docs/EP2_PLAN.md seccion 3.5): los listeners
 * solo deciden QUE hacer (ack / nack sin reintento / nack con reintento), no
 * COMO hacerlo ni como loguearlo.
 */
public final class AckHandler {

    private static final Logger log = LoggerFactory.getLogger(AckHandler.class);

    private AckHandler() {
    }

    /** Mensaje procesado correctamente (o ya procesado antes, idempotencia): sale de la cola. */
    public static void ack(Channel channel, long deliveryTag) {
        try {
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            log.error("No se pudo confirmar (ACK) el mensaje con deliveryTag={}", deliveryTag, e);
        }
    }

    /** Error de negocio o datos invalidos: va directo a la DLQ, sin reintentar. */
    public static void nackSinReintento(Channel channel, long deliveryTag, Exception motivo) {
        log.error("Mensaje rechazado sin reintento (va a la DLQ), deliveryTag={}: {}", deliveryTag, motivo.getMessage(), motivo);
        try {
            channel.basicNack(deliveryTag, false, false);
        } catch (IOException e) {
            log.error("No se pudo rechazar (NACK sin requeue) el mensaje con deliveryTag={}", deliveryTag, e);
        }
    }

    /** Error transitorio: se reintenta hasta el limite de entregas (x-delivery-limit) de la cola. */
    public static void nackConReintento(Channel channel, long deliveryTag, Exception motivo) {
        log.warn("Error transitorio, se reintentara el mensaje, deliveryTag={}: {}", deliveryTag, motivo.getMessage());
        try {
            channel.basicNack(deliveryTag, false, true);
        } catch (IOException e) {
            log.error("No se pudo reencolar (NACK con requeue) el mensaje con deliveryTag={}", deliveryTag, e);
        }
    }
}
