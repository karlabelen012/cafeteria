package cl.duoc.cafeteria.reportes.messaging.consumer.pedido;

import cl.duoc.cafeteria.common.evento.PedidoCreadoEvent;
import cl.duoc.cafeteria.common.evento.PedidoEstadoActualizadoEvent;
import cl.duoc.cafeteria.reportes.repository.EventoProcesadoRepository;
import cl.duoc.cafeteria.reportes.service.ReporteEventoService;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Prueba que PedidoEventosListener despacha por tipo de evento (pedido.creado
 * vs pedido.estado.actualizado) y traduce a ack/nack (ver docs/EP2_PLAN.md
 * seccion 3.5 y 5), sin necesitar un broker real.
 */
@ExtendWith(MockitoExtension.class)
class PedidoEventosListenerTest {

    @Mock
    private ReporteEventoService reporteEventoService;

    @Mock
    private EventoProcesadoRepository eventoProcesadoRepository;

    @Mock
    private Channel channel;

    private static final long DELIVERY_TAG = 9L;

    private PedidoEventosListener nuevoListener() {
        return new PedidoEventosListener(reporteEventoService, eventoProcesadoRepository);
    }

    private PedidoCreadoEvent eventoCreado(UUID eventId) {
        return new PedidoCreadoEvent(eventId, Instant.now(), 1, 10L, "COD-10",
                "cliente@test.cl", "CREDITO", "1234", 5000.0, List.of());
    }

    private PedidoEstadoActualizadoEvent eventoEstado(UUID eventId) {
        return new PedidoEstadoActualizadoEvent(eventId, Instant.now(), 1, 10L, "COD-10",
                "PAGADO", "EN_PREPARACION");
    }

    @Test
    void recibirPedidoCreado_eventoNuevo_registraYHaceAck() throws IOException {
        PedidoEventosListener listener = nuevoListener();
        UUID eventId = UUID.randomUUID();
        PedidoCreadoEvent evento = eventoCreado(eventId);
        when(eventoProcesadoRepository.existsById(eventId)).thenReturn(false);

        listener.recibirPedidoCreado(evento, channel, DELIVERY_TAG);

        verify(reporteEventoService).registrarPedidoCreado(evento);
        verify(channel).basicAck(DELIVERY_TAG, false);
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test
    void recibirEstadoActualizado_eventoNuevo_registraYHaceAck() throws IOException {
        PedidoEventosListener listener = nuevoListener();
        UUID eventId = UUID.randomUUID();
        PedidoEstadoActualizadoEvent evento = eventoEstado(eventId);
        when(eventoProcesadoRepository.existsById(eventId)).thenReturn(false);

        listener.recibirEstadoActualizado(evento, channel, DELIVERY_TAG);

        verify(reporteEventoService).registrarEstadoActualizado(evento);
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    void recibirPedidoCreado_eventoYaProcesado_soloHaceAck() throws IOException {
        PedidoEventosListener listener = nuevoListener();
        UUID eventId = UUID.randomUUID();
        PedidoCreadoEvent evento = eventoCreado(eventId);
        when(eventoProcesadoRepository.existsById(eventId)).thenReturn(true);

        listener.recibirPedidoCreado(evento, channel, DELIVERY_TAG);

        verify(reporteEventoService, never()).registrarPedidoCreado(any());
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    void recibirEstadoActualizado_errorInesperado_haceNackConReintento() throws IOException {
        PedidoEventosListener listener = nuevoListener();
        UUID eventId = UUID.randomUUID();
        PedidoEstadoActualizadoEvent evento = eventoEstado(eventId);
        when(eventoProcesadoRepository.existsById(eventId)).thenReturn(false);
        doThrow(new RuntimeException("BD caida")).when(reporteEventoService).registrarEstadoActualizado(evento);

        listener.recibirEstadoActualizado(evento, channel, DELIVERY_TAG);

        verify(channel).basicNack(DELIVERY_TAG, false, true);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }
}
