package cl.duoc.cafeteria.notificaciones.service;

import cl.duoc.cafeteria.common.evento.PagoProcesadoEvent;
import cl.duoc.cafeteria.common.evento.PedidoEstadoActualizadoEvent;
import cl.duoc.cafeteria.common.evento.StockBajoEvent;
import cl.duoc.cafeteria.notificaciones.client.PedidoClient;
import cl.duoc.cafeteria.notificaciones.client.PedidoDto;
import cl.duoc.cafeteria.notificaciones.exception.RecursoNoEncontradoException;
import cl.duoc.cafeteria.notificaciones.model.Alerta;
import cl.duoc.cafeteria.notificaciones.model.Ticket;
import cl.duoc.cafeteria.notificaciones.repository.AlertaRepository;
import cl.duoc.cafeteria.notificaciones.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private AlertaRepository alertaRepository;
    @Mock
    private PedidoClient pedidoClient;

    private NotificacionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new NotificacionServiceImpl(ticketRepository, alertaRepository, pedidoClient);
    }

    @Test
    void generarTicket_armaElTicketConElDetalleDelPedido() {
        PagoProcesadoEvent evento = new PagoProcesadoEvent(
                UUID.randomUUID(), Instant.now(), 1, 10L, 20L, true, 5000.0, "DEBITO");
        when(pedidoClient.obtener(10L)).thenReturn(new PedidoDto(10L, "COD-10", "Camila Rojas",
                "camila@test.cl", List.of(new PedidoDto.ItemDto(1L, "Latte", 2, 2500.0))));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> {
            Ticket t = inv.getArgument(0);
            t.setId(1L);
            return t;
        });

        service.generarTicket(evento);

        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository).save(captor.capture());
        assertThat(captor.getValue().getCodigoSeguimiento()).isEqualTo("COD-10");
        assertThat(captor.getValue().getClienteEmail()).isEqualTo("camila@test.cl");
        assertThat(captor.getValue().getTotal()).isEqualTo(5000.0);
        assertThat(captor.getValue().getMetodoPago()).isEqualTo("DEBITO");
        assertThat(captor.getValue().getItems()).hasSize(1);
    }

    @Test
    void registrarAlertaStockBajo_creaUnaAlertaDeTipoStockBajo() {
        StockBajoEvent evento = new StockBajoEvent(UUID.randomUUID(), Instant.now(), 1, 1L, "Leche", 2.0, 5.0);

        service.registrarAlertaStockBajo(evento);

        ArgumentCaptor<Alerta> captor = ArgumentCaptor.forClass(Alerta.class);
        verify(alertaRepository).save(captor.capture());
        assertThat(captor.getValue().getTipo()).isEqualTo("STOCK_BAJO");
        assertThat(captor.getValue().getMensaje()).contains("Leche");
    }

    @Test
    void registrarAlertaCambioEstado_soloCreaAlertaCuandoEsListo() {
        PedidoEstadoActualizadoEvent noListo = new PedidoEstadoActualizadoEvent(
                UUID.randomUUID(), Instant.now(), 1, 10L, "COD-10", "PAGADO", "EN_PREPARACION");
        service.registrarAlertaCambioEstado(noListo);
        verifyNoInteractions(alertaRepository);

        PedidoEstadoActualizadoEvent listo = new PedidoEstadoActualizadoEvent(
                UUID.randomUUID(), Instant.now(), 1, 10L, "COD-10", "EN_PREPARACION", "LISTO");
        service.registrarAlertaCambioEstado(listo);

        ArgumentCaptor<Alerta> captor = ArgumentCaptor.forClass(Alerta.class);
        verify(alertaRepository).save(captor.capture());
        assertThat(captor.getValue().getTipo()).isEqualTo("PEDIDO_LISTO");
        assertThat(captor.getValue().getMensaje()).contains("COD-10");
    }

    @Test
    void obtenerTicketPorCodigoSeguimiento_noExiste_lanza404() {
        when(ticketRepository.findByCodigoSeguimiento("XXX")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerTicketPorCodigoSeguimiento("XXX"))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void marcarAlertaLeida_marcaLaAlertaComoLeida() {
        Alerta alerta = new Alerta();
        alerta.setId(1L);
        alerta.setLeida(false);
        when(alertaRepository.findById(1L)).thenReturn(Optional.of(alerta));
        when(alertaRepository.save(any(Alerta.class))).thenAnswer(inv -> inv.getArgument(0));

        var respuesta = service.marcarAlertaLeida(1L);

        assertThat(respuesta.leida()).isTrue();
    }
}
