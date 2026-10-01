package cl.duoc.cafeteria.clientes.messaging.consumer.pago;

import cl.duoc.cafeteria.clientes.client.PedidoClient;
import cl.duoc.cafeteria.clientes.client.PedidoDto;
import cl.duoc.cafeteria.clientes.dto.ClienteResponse;
import cl.duoc.cafeteria.clientes.repository.EventoProcesadoRepository;
import cl.duoc.cafeteria.clientes.service.ClienteService;
import cl.duoc.cafeteria.common.evento.PagoProcesadoEvent;
import cl.duoc.cafeteria.common.exception.RecoverableMessageException;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Prueba que PagoAprobadoListener traduce correctamente a ack/nack segun el
 * resultado de consultar ms-pedidos y de ClienteService.registrarCompra, sin
 * necesitar un broker real ni levantar el contexto de Spring (ver
 * docs/EP2_PLAN.md seccion 3.5 y 5).
 */
@ExtendWith(MockitoExtension.class)
class PagoAprobadoListenerTest {

    @Mock
    private PedidoClient pedidoClient;

    @Mock
    private ClienteService clienteService;

    @Mock
    private EventoProcesadoRepository eventoProcesadoRepository;

    @Mock
    private Channel channel;

    private PagoAprobadoListener listener;

    private static final long DELIVERY_TAG = 7L;

    private PagoAprobadoListener nuevoListener() {
        return new PagoAprobadoListener(pedidoClient, clienteService, eventoProcesadoRepository);
    }

    private PagoProcesadoEvent eventoAprobado(UUID eventId) {
        return new PagoProcesadoEvent(eventId, Instant.now(), 1, 10L, 20L, true, 5000.0, "DEBITO");
    }

    @Test
    void recibir_pagoAprobadoYPedidoOk_registraCompraYHaceAck() throws IOException {
        listener = nuevoListener();
        UUID eventId = UUID.randomUUID();
        PagoProcesadoEvent evento = eventoAprobado(eventId);

        when(eventoProcesadoRepository.existsById(eventId)).thenReturn(false);
        when(pedidoClient.obtener(10L)).thenReturn(new PedidoDto(10L, "Camila Rojas", "camila.rojas@example.cl"));
        when(clienteService.registrarCompra("Camila Rojas", "camila.rojas@example.cl", 5000.0))
                .thenReturn(new ClienteResponse(1L, "Camila Rojas", "camila.rojas@example.cl", null, 5));

        listener.recibir(evento, channel, DELIVERY_TAG);

        verify(clienteService).registrarCompra("Camila Rojas", "camila.rojas@example.cl", 5000.0);
        verify(eventoProcesadoRepository).save(argThat(e -> e.getEventId().equals(eventId)));
        verify(channel).basicAck(DELIVERY_TAG, false);
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test
    void recibir_pagoNoAprobado_soloHaceAckSinTocarCliente() throws IOException {
        listener = nuevoListener();
        PagoProcesadoEvent evento = new PagoProcesadoEvent(
                UUID.randomUUID(), Instant.now(), 1, 10L, 20L, false, 5000.0, "DEBITO");

        listener.recibir(evento, channel, DELIVERY_TAG);

        verify(channel).basicAck(DELIVERY_TAG, false);
        verifyNoInteractions(clienteService, pedidoClient);
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test
    void recibir_eventoYaProcesado_soloHaceAckPorIdempotencia() throws IOException {
        listener = nuevoListener();
        UUID eventId = UUID.randomUUID();
        PagoProcesadoEvent evento = eventoAprobado(eventId);

        when(eventoProcesadoRepository.existsById(eventId)).thenReturn(true);

        listener.recibir(evento, channel, DELIVERY_TAG);

        verify(channel).basicAck(DELIVERY_TAG, false);
        verifyNoInteractions(pedidoClient, clienteService);
        verify(eventoProcesadoRepository, never()).save(any());
    }

    @Test
    void recibir_pedidoClientFalla401_haceNackConReintento() throws IOException {
        listener = nuevoListener();
        UUID eventId = UUID.randomUUID();
        PagoProcesadoEvent evento = eventoAprobado(eventId);

        when(eventoProcesadoRepository.existsById(eventId)).thenReturn(false);
        when(pedidoClient.obtener(10L))
                .thenThrow(new RecoverableMessageException("ms-pedidos devolvio 401, sin auth servicio-a-servicio"));

        listener.recibir(evento, channel, DELIVERY_TAG);

        verify(channel).basicNack(DELIVERY_TAG, false, true);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
        verifyNoInteractions(clienteService);
    }
}
