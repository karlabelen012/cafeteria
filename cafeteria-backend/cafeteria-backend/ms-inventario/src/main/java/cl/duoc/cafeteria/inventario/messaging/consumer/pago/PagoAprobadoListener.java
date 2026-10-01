package cl.duoc.cafeteria.inventario.messaging.consumer.pago;

import cl.duoc.cafeteria.common.evento.PagoProcesadoEvent;
import cl.duoc.cafeteria.common.exception.NonRecoverableMessageException;
import cl.duoc.cafeteria.common.exception.RecoverableMessageException;
import cl.duoc.cafeteria.common.messaging.AckHandler;
import cl.duoc.cafeteria.inventario.model.EventoProcesado;
import cl.duoc.cafeteria.inventario.repository.EventoProcesadoRepository;
import cl.duoc.cafeteria.inventario.service.DescuentoStockService;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Consume pago.aprobado (y pago.rechazado, que llega a la misma cola/binding)
 * desde cafeteria.pagos.exchange y descuenta stock segun receta cuando el
 * pago fue aprobado (ver docs/EP2_PLAN.md seccion 3.5 y 5).
 */
@Component
public class PagoAprobadoListener {

    private static final Logger log = LoggerFactory.getLogger(PagoAprobadoListener.class);

    private final DescuentoStockService descuentoStockService;
    private final EventoProcesadoRepository eventoProcesadoRepository;

    public PagoAprobadoListener(DescuentoStockService descuentoStockService,
            EventoProcesadoRepository eventoProcesadoRepository) {
        this.descuentoStockService = descuentoStockService;
        this.eventoProcesadoRepository = eventoProcesadoRepository;
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.pago-aprobado}")
    public void escuchar(PagoProcesadoEvent evento, Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            if (!evento.aprobado()) {
                // A este consumidor solo le interesan los pagos aprobados: un
                // pago.rechazado no descuenta stock.
                AckHandler.ack(channel, deliveryTag);
                return;
            }

            if (eventoProcesadoRepository.existsById(evento.eventId())) {
                // Idempotencia: este evento ya fue procesado (reintento/redelivery),
                // no se vuelve a descontar stock.
                log.info("Evento {} ya fue procesado antes, se ignora (idempotencia)", evento.eventId());
                AckHandler.ack(channel, deliveryTag);
                return;
            }

            descuentoStockService.descontarPorPedido(evento.pedidoId());
            eventoProcesadoRepository.save(new EventoProcesado(evento.eventId(), Instant.now()));

            AckHandler.ack(channel, deliveryTag);
        } catch (NonRecoverableMessageException ex) {
            AckHandler.nackSinReintento(channel, deliveryTag, ex);
        } catch (RecoverableMessageException ex) {
            AckHandler.nackConReintento(channel, deliveryTag, ex);
        } catch (Exception ex) {
            // Error no previsto: se trata como no recuperable para no reintentar
            // indefinidamente algo que probablemente nunca va a funcionar.
            AckHandler.nackSinReintento(channel, deliveryTag, ex);
        }
    }
}
