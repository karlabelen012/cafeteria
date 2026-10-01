package cl.duoc.cafeteria.inventario.messaging.consumer.pago;

import cl.duoc.cafeteria.common.evento.PagoProcesadoEvent;
import cl.duoc.cafeteria.common.exception.RecoverableMessageException;
import cl.duoc.cafeteria.inventario.repository.EventoProcesadoRepository;
import cl.duoc.cafeteria.inventario.service.DescuentoStockService;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ACK manual e idempotencia del consumidor de pago.aprobado (ver
 * docs/EP2_PLAN.md seccion 3.5 y PagoAprobadoListener).
 */
@ExtendWith(MockitoExtension.class)
class PagoAprobadoListenerTest {

    @Mock
    private DescuentoStockService descuentoStockService;

    @Mock
    private EventoProcesadoRepository eventoProcesadoRepository;

    @Mock
    private Channel channel;

    private PagoAprobadoListener listener;

    private static final long DELIVERY_TAG = 42L;

    @BeforeEach
    void setUp() {
        listener = new PagoAprobadoListener(descuentoStockService, eventoProcesadoRepository);
    }

    private PagoProcesadoEvent evento(boolean aprobado) {
        return new PagoProcesadoEvent(UUID.randomUUID(), Instant.now(), 1, 100L, 200L, aprobado, 5000.0, "DEBITO");
    }

    @Test
    void pagoAprobado_descuentaStockYHaceAck() throws Exception {
        PagoProcesadoEvent evento = evento(true);
        when(eventoProcesadoRepository.existsById(evento.eventId())).thenReturn(false);

        listener.escuchar(evento, channel, DELIVERY_TAG);

        verify(descuentoStockService).descontarPorPedido(evento.pedidoId());
        verify(eventoProcesadoRepository).save(any());
        verify(channel).basicAck(DELIVERY_TAG, false);
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test
    void pagoRechazado_soloHaceAckYNoDescuentaStock() throws Exception {
        PagoProcesadoEvent evento = evento(false);

        listener.escuchar(evento, channel, DELIVERY_TAG);

        verify(descuentoStockService, never()).descontarPorPedido(any());
        verify(eventoProcesadoRepository, never()).existsById(any());
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    void errorRecuperableAlConsultarElPedido_nackConReintento() throws Exception {
        PagoProcesadoEvent evento = evento(true);
        when(eventoProcesadoRepository.existsById(evento.eventId())).thenReturn(false);
        doThrow(new RecoverableMessageException("ms-pedidos no responde"))
                .when(descuentoStockService).descontarPorPedido(evento.pedidoId());

        listener.escuchar(evento, channel, DELIVERY_TAG);

        verify(channel).basicNack(DELIVERY_TAG, false, true);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
        verify(eventoProcesadoRepository, never()).save(any());
    }

    @Test
    void eventoYaProcesado_soloHaceAckYNoReprocesa() throws Exception {
        PagoProcesadoEvent evento = evento(true);
        when(eventoProcesadoRepository.existsById(evento.eventId())).thenReturn(true);

        listener.escuchar(evento, channel, DELIVERY_TAG);

        verify(descuentoStockService, never()).descontarPorPedido(any());
        verify(eventoProcesadoRepository, never()).save(any());
        verify(channel).basicAck(DELIVERY_TAG, false);
    }
}
