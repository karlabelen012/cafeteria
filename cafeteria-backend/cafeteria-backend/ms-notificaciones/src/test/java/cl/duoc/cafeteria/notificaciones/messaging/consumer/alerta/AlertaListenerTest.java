package cl.duoc.cafeteria.notificaciones.messaging.consumer.alerta;

import cl.duoc.cafeteria.common.evento.PedidoEstadoActualizadoEvent;
import cl.duoc.cafeteria.common.evento.StockBajoEvent;
import cl.duoc.cafeteria.notificaciones.repository.EventoProcesadoRepository;
import cl.duoc.cafeteria.notificaciones.service.NotificacionService;
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
 * Prueba que AlertaListener despacha por tipo de evento (stock.bajo vs
 * pedido.estado.actualizado) y traduce a ack/nack (ver docs/EP2_PLAN.md
 * seccion 3.5 y 5), sin necesitar un broker real.
 */
@ExtendWith(MockitoExtension.class)
class AlertaListenerTest {

    @Mock
    private NotificacionService notificacionService;

    @Mock
    private EventoProcesadoRepository eventoProcesadoRepository;

    @Mock
    private Channel channel;

    private static final long DELIVERY_TAG = 5L;

    private AlertaListener nuevoListener() {
        return new AlertaListener(notificacionService, eventoProcesadoRepository);
    }

    private StockBajoEvent eventoStock(UUID eventId) {
        return new StockBajoEvent(eventId, Instant.now(), 1, 1L, "Leche", 2.0, 5.0);
    }

    private PedidoEstadoActualizadoEvent eventoEstado(UUID eventId) {
        return new PedidoEstadoActualizadoEvent(eventId, Instant.now(), 1, 10L, "COD-10",
                "EN_PREPARACION", "LISTO");
    }

    @Test
    void recibirStockBajo_eventoNuevo_registraYHaceAck() throws IOException {
        AlertaListener listener = nuevoListener();
        UUID eventId = UUID.randomUUID();
        StockBajoEvent evento = eventoStock(eventId);
        when(eventoProcesadoRepository.existsById(eventId)).thenReturn(false);

        listener.recibirStockBajo(evento, channel, DELIVERY_TAG);

        verify(notificacionService).registrarAlertaStockBajo(evento);
        verify(channel).basicAck(DELIVERY_TAG, false);
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test
    void recibirEstadoActualizado_eventoNuevo_registraYHaceAck() throws IOException {
        AlertaListener listener = nuevoListener();
        UUID eventId = UUID.randomUUID();
        PedidoEstadoActualizadoEvent evento = eventoEstado(eventId);
        when(eventoProcesadoRepository.existsById(eventId)).thenReturn(false);

        listener.recibirEstadoActualizado(evento, channel, DELIVERY_TAG);

        verify(notificacionService).registrarAlertaCambioEstado(evento);
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    void recibirStockBajo_eventoYaProcesado_soloHaceAck() throws IOException {
        AlertaListener listener = nuevoListener();
        UUID eventId = UUID.randomUUID();
        StockBajoEvent evento = eventoStock(eventId);
        when(eventoProcesadoRepository.existsById(eventId)).thenReturn(true);

        listener.recibirStockBajo(evento, channel, DELIVERY_TAG);

        verify(notificacionService, never()).registrarAlertaStockBajo(any());
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    void recibirEstadoActualizado_errorInesperado_haceNackConReintento() throws IOException {
        AlertaListener listener = nuevoListener();
        UUID eventId = UUID.randomUUID();
        PedidoEstadoActualizadoEvent evento = eventoEstado(eventId);
        when(eventoProcesadoRepository.existsById(eventId)).thenReturn(false);
        doThrow(new RuntimeException("BD caida")).when(notificacionService).registrarAlertaCambioEstado(evento);

        listener.recibirEstadoActualizado(evento, channel, DELIVERY_TAG);

        verify(channel).basicNack(DELIVERY_TAG, false, true);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }
}
