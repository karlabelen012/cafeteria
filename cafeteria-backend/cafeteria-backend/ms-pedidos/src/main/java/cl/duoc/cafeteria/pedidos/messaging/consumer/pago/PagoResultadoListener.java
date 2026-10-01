package cl.duoc.cafeteria.pedidos.messaging.consumer.pago;

import cl.duoc.cafeteria.common.evento.PagoProcesadoEvent;
import cl.duoc.cafeteria.common.exception.NonRecoverableMessageException;
import cl.duoc.cafeteria.common.exception.RecoverableMessageException;
import cl.duoc.cafeteria.common.messaging.AckHandler;
import cl.duoc.cafeteria.pedidos.exception.ConflictoDeNegocioException;
import cl.duoc.cafeteria.pedidos.exception.RecursoNoEncontradoException;
import cl.duoc.cafeteria.pedidos.model.EventoProcesado;
import cl.duoc.cafeteria.pedidos.repository.EventoProcesadoRepository;
import cl.duoc.cafeteria.pedidos.service.PedidoService;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Consume pago.aprobado/pago.rechazado (cafeteria.pagos.exchange, 2 bindings
 * a la misma cola) y actualiza el estado del pedido a PAGADO o PAGO_RECHAZADO
 * (ver docs/EP2_PLAN.md seccion 3.3 y 5). El evento solo trae pedidoId (Long),
 * nunca el codigoSeguimiento, asi que la busqueda es por id secuencial.
 */
@Component
public class PagoResultadoListener {

    private static final Logger log = LoggerFactory.getLogger(PagoResultadoListener.class);

    private final PedidoService pedidoService;
    private final EventoProcesadoRepository eventoProcesadoRepository;

    public PagoResultadoListener(PedidoService pedidoService, EventoProcesadoRepository eventoProcesadoRepository) {
        this.pedidoService = pedidoService;
        this.eventoProcesadoRepository = eventoProcesadoRepository;
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.pago-resultado}")
    public void manejar(PagoProcesadoEvent evento, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            if (eventoProcesadoRepository.existsById(evento.eventId())) {
                log.info("Evento {} ya fue procesado antes, se descarta (idempotencia)", evento.eventId());
                AckHandler.ack(channel, deliveryTag);
                return;
            }

            try {
                pedidoService.actualizarEstadoPorResultadoPago(evento.pedidoId(), evento.aprobado());
            } catch (RecursoNoEncontradoException ex) {
                // No tiene sentido reintentar un pedido que no existe.
                throw new NonRecoverableMessageException(ex.getMessage(), ex);
            } catch (ConflictoDeNegocioException ex) {
                // Transicion de estado invalida (p.ej. evento duplicado o fuera de
                // orden): tampoco se arregla reintentando.
                throw new NonRecoverableMessageException(ex.getMessage(), ex);
            }

            eventoProcesadoRepository.save(new EventoProcesado(evento.eventId(), Instant.now()));
            AckHandler.ack(channel, deliveryTag);
        } catch (NonRecoverableMessageException ex) {
            AckHandler.nackSinReintento(channel, deliveryTag, ex);
        } catch (RecoverableMessageException ex) {
            AckHandler.nackConReintento(channel, deliveryTag, ex);
        } catch (Exception ex) {
            // Cualquier error inesperado (p.ej. BD caida) se trata como transitorio.
            AckHandler.nackConReintento(channel, deliveryTag, ex);
        }
    }
}
