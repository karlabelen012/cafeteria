package cl.duoc.cafeteria.reportes.messaging.consumer.pedido;

import cl.duoc.cafeteria.common.evento.PedidoCreadoEvent;
import cl.duoc.cafeteria.common.evento.PedidoEstadoActualizadoEvent;
import cl.duoc.cafeteria.common.exception.NonRecoverableMessageException;
import cl.duoc.cafeteria.common.exception.RecoverableMessageException;
import cl.duoc.cafeteria.common.messaging.AckHandler;
import cl.duoc.cafeteria.reportes.model.EventoProcesado;
import cl.duoc.cafeteria.reportes.repository.EventoProcesadoRepository;
import cl.duoc.cafeteria.reportes.service.ReporteEventoService;
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
 * Consume cafeteria.pedidos.exchange / routing key "pedido.#" (ver
 * docs/EP2_PLAN.md seccion 5): a esta cola llegan DOS tipos de evento
 * (pedido.creado y pedido.estado.actualizado), asi que se despachan por tipo
 * con @RabbitHandler segun el payload (ver guia de Spring AMQP "multi-method
 * listeners"). Alimenta pedidos por hora/estado, top productos y clientes
 * nuevos.
 */
@Component
@RabbitListener(queues = "${app.rabbitmq.queues.pedido-eventos}")
public class PedidoEventosListener {

    private static final Logger log = LoggerFactory.getLogger(PedidoEventosListener.class);

    private final ReporteEventoService reporteEventoService;
    private final EventoProcesadoRepository eventoProcesadoRepository;

    public PedidoEventosListener(ReporteEventoService reporteEventoService,
            EventoProcesadoRepository eventoProcesadoRepository) {
        this.reporteEventoService = reporteEventoService;
        this.eventoProcesadoRepository = eventoProcesadoRepository;
    }

    @RabbitHandler
    public void recibirPedidoCreado(PedidoCreadoEvent evento, Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        procesar(evento.eventId(), channel, deliveryTag, () -> reporteEventoService.registrarPedidoCreado(evento));
    }

    @RabbitHandler
    public void recibirEstadoActualizado(PedidoEstadoActualizadoEvent evento, Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        procesar(evento.eventId(), channel, deliveryTag,
                () -> reporteEventoService.registrarEstadoActualizado(evento));
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
