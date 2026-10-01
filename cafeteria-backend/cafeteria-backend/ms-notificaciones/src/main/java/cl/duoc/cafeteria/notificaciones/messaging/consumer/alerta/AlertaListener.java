package cl.duoc.cafeteria.notificaciones.messaging.consumer.alerta;

import cl.duoc.cafeteria.common.evento.PedidoEstadoActualizadoEvent;
import cl.duoc.cafeteria.common.evento.StockBajoEvent;
import cl.duoc.cafeteria.common.exception.NonRecoverableMessageException;
import cl.duoc.cafeteria.common.exception.RecoverableMessageException;
import cl.duoc.cafeteria.common.messaging.AckHandler;
import cl.duoc.cafeteria.notificaciones.model.EventoProcesado;
import cl.duoc.cafeteria.notificaciones.repository.EventoProcesadoRepository;
import cl.duoc.cafeteria.notificaciones.service.NotificacionService;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Consume notificaciones.alertas.queue (ver docs/EP2_PLAN.md seccion 5): a
 * esta cola llegan DOS tipos de evento (stock.bajo desde
 * cafeteria.inventario.exchange y pedido.estado.actualizado desde
 * cafeteria.pedidos.exchange), asi que se despachan por tipo con
 * @RabbitHandler segun el payload.
 */
@Component
@RabbitListener(queues = "${app.rabbitmq.queues.alertas}")
public class AlertaListener {

    private static final Logger log = LoggerFactory.getLogger(AlertaListener.class);

    private final NotificacionService notificacionService;
    private final EventoProcesadoRepository eventoProcesadoRepository;

    public AlertaListener(NotificacionService notificacionService,
            EventoProcesadoRepository eventoProcesadoRepository) {
        this.notificacionService = notificacionService;
        this.eventoProcesadoRepository = eventoProcesadoRepository;
    }

    @RabbitHandler
    public void recibirStockBajo(StockBajoEvent evento, Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        procesar(evento.eventId(), channel, deliveryTag,
                () -> notificacionService.registrarAlertaStockBajo(evento));
    }

    @RabbitHandler
    public void recibirEstadoActualizado(PedidoEstadoActualizadoEvent evento, Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        procesar(evento.eventId(), channel, deliveryTag,
                () -> notificacionService.registrarAlertaCambioEstado(evento));
    }

    private void procesar(UUID eventId, Channel channel, long deliveryTag, Runnable accion) {
        try {
            if (eventoProcesadoRepository.existsById(eventId)) {
                log.info("Evento {} ya fue procesado antes, se descarta (idempotencia)", eventId);
                AckHandler.ack(channel, deliveryTag);
                return;
            }

            accion.run();
            eventoProcesadoRepository.save(new EventoProcesado(eventId, Instant.now()));
            AckHandler.ack(channel, deliveryTag);
        } catch (NonRecoverableMessageException e) {
            AckHandler.nackSinReintento(channel, deliveryTag, e);
        } catch (RecoverableMessageException e) {
            AckHandler.nackConReintento(channel, deliveryTag, e);
        } catch (Exception e) {
            AckHandler.nackConReintento(channel, deliveryTag, e);
        }
    }
}
