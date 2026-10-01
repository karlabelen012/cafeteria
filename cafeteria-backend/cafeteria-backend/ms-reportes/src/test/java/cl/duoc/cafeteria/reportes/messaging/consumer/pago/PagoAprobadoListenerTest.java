package cl.duoc.cafeteria.reportes.messaging.consumer.pago;

import cl.duoc.cafeteria.common.evento.PagoProcesadoEvent;
import cl.duoc.cafeteria.reportes.repository.EventoProcesadoRepository;
import cl.duoc.cafeteria.reportes.service.ReporteEventoService;
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
 * Prueba que PagoAprobadoListener traduce correctamente a ack/nack (ver
 * docs/EP2_PLAN.md seccion 3.5 y 5), sin necesitar un broker real.
 */
@ExtendWith(MockitoExtension.class)
class PagoAprobadoListenerTest {

    @Mock
    private ReporteEventoService reporteEventoService;

    @Mock
    private EventoProcesadoRepository eventoProcesadoRepository;

    @Mock
    private Channel channel;

    private static final long DELIVERY_TAG = 7L;

    private PagoAprobadoListener nuevoListener() {
        return new PagoAprobadoListener(reporteEventoService, eventoProcesadoRepository);
    }

    private PagoProcesadoEvent evento(UUID eventId) {
        return new PagoProcesadoEvent(eventId, Instant.now(), 1, 10L, 20L, true, 5000.0, "DEBITO");
    }

    @Test
    void recibir_eventoNuevo_registraYHaceAck() throws IOException {
        PagoAprobadoListener listener = nuevoListener();
        UUID eventId = UUID.randomUUID();
        PagoProcesadoEvent evento = evento(eventId);
        when(eventoProcesadoRepository.existsById(eventId)).thenReturn(false);

        listener.recibir(evento, channel, DELIVERY_TAG);

        verify(reporteEventoService).registrarPagoProcesado(evento);
        verify(eventoProcesadoRepository).save(argThat(e -> e.getEventId().equals(eventId)));
        verify(channel).basicAck(DELIVERY_TAG, false);
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test
    void recibir_eventoYaProcesado_soloHaceAckPorIdempotencia() throws IOException {
        PagoAprobadoListener listener = nuevoListener();
        UUID eventId = UUID.randomUUID();
        PagoProcesadoEvent evento = evento(eventId);
        when(eventoProcesadoRepository.existsById(eventId)).thenReturn(true);

        listener.recibir(evento, channel, DELIVERY_TAG);

        verify(reporteEventoService, never()).registrarPagoProcesado(any());
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    void recibir_errorInesperado_haceNackConReintento() throws IOException {
        PagoAprobadoListener listener = nuevoListener();
        UUID eventId = UUID.randomUUID();
        PagoProcesadoEvent evento = evento(eventId);
        when(eventoProcesadoRepository.existsById(eventId)).thenReturn(false);
        doThrow(new RuntimeException("BD caida")).when(reporteEventoService).registrarPagoProcesado(evento);

        listener.recibir(evento, channel, DELIVERY_TAG);

        verify(channel).basicNack(DELIVERY_TAG, false, true);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }
}
