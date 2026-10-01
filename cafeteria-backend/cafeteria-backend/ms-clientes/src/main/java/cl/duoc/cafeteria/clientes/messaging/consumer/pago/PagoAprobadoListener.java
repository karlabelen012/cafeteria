package cl.duoc.cafeteria.clientes.messaging.consumer.pago;

import cl.duoc.cafeteria.clientes.client.PedidoClient;
import cl.duoc.cafeteria.clientes.client.PedidoDto;
import cl.duoc.cafeteria.clientes.model.EventoProcesado;
import cl.duoc.cafeteria.clientes.repository.EventoProcesadoRepository;
import cl.duoc.cafeteria.clientes.service.ClienteService;
import cl.duoc.cafeteria.common.evento.PagoProcesadoEvent;
import cl.duoc.cafeteria.common.exception.NonRecoverableMessageException;
import cl.duoc.cafeteria.common.exception.RecoverableMessageException;
import cl.duoc.cafeteria.common.messaging.AckHandler;
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
 * docs/EP2_PLAN.md seccion 5): por cada pago aprobado, busca el snapshot de
 * cliente (nombre/email) del pedido en ms-pedidos y hace upsert del cliente
 * en ms-clientes sumando puntos de fidelizacion (1 punto cada $1.000).
 *
 * Los pagos rechazados (aprobado=false) solo se confirman (ack), no generan
 * ninguna accion sobre el cliente.
 */
@Component
public class PagoAprobadoListener {

    private static final Logger log = LoggerFactory.getLogger(PagoAprobadoListener.class);

    private final PedidoClient pedidoClient;
    private final ClienteService clienteService;
    private final EventoProcesadoRepository eventoProcesadoRepository;

    public PagoAprobadoListener(PedidoClient pedidoClient,
                                 ClienteService clienteService,
                                 EventoProcesadoRepository eventoProcesadoRepository) {
        this.pedidoClient = pedidoClient;
        this.clienteService = clienteService;
        this.eventoProcesadoRepository = eventoProcesadoRepository;
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.pago-aprobado}")
    public void recibir(PagoProcesadoEvent evento, Channel channel,
                         @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            if (!evento.aprobado()) {
                log.debug("Evento {} de pago no aprobado, se descarta sin accion sobre el cliente", evento.eventId());
                AckHandler.ack(channel, deliveryTag);
                return;
            }

            if (eventoProcesadoRepository.existsById(evento.eventId())) {
                log.info("Evento {} ya habia sido procesado antes, se descarta (idempotencia)", evento.eventId());
                AckHandler.ack(channel, deliveryTag);
                return;
            }

            PedidoDto pedido = pedidoClient.obtener(evento.pedidoId());
            if (pedido == null || pedido.clienteEmail() == null || pedido.clienteEmail().isBlank()) {
                throw new NonRecoverableMessageException(
                        "El pedido " + evento.pedidoId() + " no trae clienteEmail en ms-pedidos, "
                                + "no se puede hacer upsert del cliente");
            }

            clienteService.registrarCompra(pedido.clienteNombre(), pedido.clienteEmail(), evento.monto());
            eventoProcesadoRepository.save(new EventoProcesado(evento.eventId(), Instant.now()));

            AckHandler.ack(channel, deliveryTag);
        } catch (RecoverableMessageException e) {
            AckHandler.nackConReintento(channel, deliveryTag, e);
        } catch (NonRecoverableMessageException e) {
            AckHandler.nackSinReintento(channel, deliveryTag, e);
        } catch (Exception e) {
            log.error("Error inesperado procesando evento de pago aprobado {}", evento.eventId(), e);
            AckHandler.nackConReintento(channel, deliveryTag, e);
        }
    }
}
