package cl.duoc.cafeteria.reportes.messaging.consumer.pago;

import cl.duoc.cafeteria.common.evento.PagoProcesadoEvent;
import cl.duoc.cafeteria.common.exception.NonRecoverableMessageException;
import cl.duoc.cafeteria.common.exception.RecoverableMessageException;
import cl.duoc.cafeteria.common.messaging.AckHandler;
import cl.duoc.cafeteria.reportes.model.EventoProcesado;
import cl.duoc.cafeteria.reportes.repository.EventoProcesadoRepository;
import cl.duoc.cafeteria.reportes.service.ReporteEventoService;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Consume cafeteria.pagos.exchange / routing key "pago.aprobado" (ver
 * docs/EP2_PLAN.md seccion 5) y actualiza las ventas diarias. Los pagos
 * rechazados tambien llegan aqui (mismo binding) pero ReporteEventoService
 * los ignora: solo cuentan las ventas efectivamente cobradas.
 */
@Component
public class PagoAprobadoListener {

    private static final Logger log = LoggerFactory.getLogger(PagoAprobadoListener.class);

    private final ReporteEventoService reporteEventoService;
    private final EventoProcesadoRepository eventoProcesadoRepository;

    public PagoAprobadoListener(ReporteEventoService reporteEventoService,
            EventoProcesadoRepository eventoProcesadoRepository) {
        this.reporteEventoService = reporteEventoService;
        this.eventoProcesadoRepository = eventoProcesadoRepository;
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.pago-aprobado}")
    public void recibir(PagoProcesadoEvent evento, Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            if (eventoProcesadoRepository.existsById(evento.eventId())) {
                log.info("Evento {} ya fue procesado antes, se descarta (idempotencia)", evento.eventId());
                AckHandler.ack(channel, deliveryTag);
                return;
            }

            reporteEventoService.registrarPagoProcesado(evento);
            eventoProcesadoRepository.save(new EventoProcesado(evento.eventId(), Instant.now()));
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
