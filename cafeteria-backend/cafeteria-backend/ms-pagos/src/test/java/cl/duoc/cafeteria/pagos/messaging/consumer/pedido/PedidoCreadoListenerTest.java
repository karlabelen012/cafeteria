package cl.duoc.cafeteria.pagos.messaging.consumer.pedido;

import cl.duoc.cafeteria.common.evento.PedidoCreadoEvent;
import cl.duoc.cafeteria.common.exception.NonRecoverableMessageException;
import cl.duoc.cafeteria.common.exception.RecoverableMessageException;
import cl.duoc.cafeteria.pagos.service.PasarelaPagoService;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

/**
 * Prueba que PedidoCreadoListener traduce correctamente el resultado (o la
 * excepcion) de PasarelaPagoService a basicAck/basicNack, sin necesitar un
 * broker real (ver docs/EP2_PLAN.md seccion 3.5).
 */
@ExtendWith(MockitoExtension.class)
class PedidoCreadoListenerTest {

    @Mock
    private PasarelaPagoService pasarelaPagoService;

    @Mock
    private Channel channel;

    private PedidoCreadoListener listener;

    private static final long DELIVERY_TAG = 42L;

    private PedidoCreadoListener nuevoListener() {
        return new PedidoCreadoListener(pasarelaPagoService);
    }

    private PedidoCreadoEvent eventoCualquiera() {
        return new PedidoCreadoEvent(UUID.randomUUID(), Instant.now(), 1, 10L, "COD-10",
                "cliente@test.cl", "CREDITO", "1234", 5000.0, List.of());
    }

    @Test
    void recibir_procesadoOk_haceAck() throws IOException {
        listener = nuevoListener();
        PedidoCreadoEvent evento = eventoCualquiera();
        doNothing().when(pasarelaPagoService).procesar(evento);

        listener.recibir(evento, channel, DELIVERY_TAG);

        verify(channel).basicAck(DELIVERY_TAG, false);
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test
    void recibir_conNonRecoverableMessageException_haceNackSinReintento() throws IOException {
        listener = nuevoListener();
        PedidoCreadoEvent evento = eventoCualquiera();
        doThrow(new NonRecoverableMessageException("Tarjeta rechazada por la pasarela"))
                .when(pasarelaPagoService).procesar(evento);

        listener.recibir(evento, channel, DELIVERY_TAG);

        verify(channel).basicNack(DELIVERY_TAG, false, false);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }

    @Test
    void recibir_conRecoverableMessageException_haceNackConReintento() throws IOException {
        listener = nuevoListener();
        PedidoCreadoEvent evento = eventoCualquiera();
        doThrow(new RecoverableMessageException("Error transitorio simulado de la pasarela"))
                .when(pasarelaPagoService).procesar(evento);

        listener.recibir(evento, channel, DELIVERY_TAG);

        verify(channel).basicNack(DELIVERY_TAG, false, true);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }

    @Test
    void recibir_conExcepcionInesperada_haceNackConReintento() throws IOException {
        listener = nuevoListener();
        PedidoCreadoEvent evento = eventoCualquiera();
        doThrow(new RuntimeException("Fallo inesperado"))
                .when(pasarelaPagoService).procesar(evento);

        listener.recibir(evento, channel, DELIVERY_TAG);

        verify(channel).basicNack(DELIVERY_TAG, false, true);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }
}
