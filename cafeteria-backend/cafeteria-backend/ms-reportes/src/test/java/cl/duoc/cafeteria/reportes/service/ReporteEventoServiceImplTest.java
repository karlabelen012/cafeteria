package cl.duoc.cafeteria.reportes.service;

import cl.duoc.cafeteria.common.evento.ItemEvent;
import cl.duoc.cafeteria.common.evento.PagoProcesadoEvent;
import cl.duoc.cafeteria.common.evento.PedidoCreadoEvent;
import cl.duoc.cafeteria.common.evento.PedidoEstadoActualizadoEvent;
import cl.duoc.cafeteria.reportes.model.ClienteVisto;
import cl.duoc.cafeteria.reportes.model.PedidoEstadoActual;
import cl.duoc.cafeteria.reportes.model.PedidosPorHora;
import cl.duoc.cafeteria.reportes.model.VentaDiaria;
import cl.duoc.cafeteria.reportes.model.VentaProducto;
import cl.duoc.cafeteria.reportes.repository.ClienteVistoRepository;
import cl.duoc.cafeteria.reportes.repository.PedidoEstadoActualRepository;
import cl.duoc.cafeteria.reportes.repository.PedidosPorHoraRepository;
import cl.duoc.cafeteria.reportes.repository.VentaDiariaRepository;
import cl.duoc.cafeteria.reportes.repository.VentaProductoRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Prueba unitaria de como ReporteEventoServiceImpl aplica cada evento al
 * modelo de lectura (ver docs/EP2_PLAN.md seccion 5).
 */
@ExtendWith(MockitoExtension.class)
class ReporteEventoServiceImplTest {

    @Mock
    private VentaDiariaRepository ventaDiariaRepository;
    @Mock
    private VentaProductoRepository ventaProductoRepository;
    @Mock
    private PedidosPorHoraRepository pedidosPorHoraRepository;
    @Mock
    private PedidoEstadoActualRepository pedidoEstadoActualRepository;
    @Mock
    private ClienteVistoRepository clienteVistoRepository;

    private ReporteEventoServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ReporteEventoServiceImpl(ventaDiariaRepository, ventaProductoRepository,
                pedidosPorHoraRepository, pedidoEstadoActualRepository, clienteVistoRepository);
    }

    @Test
    void registrarPagoProcesado_aprobado_sumaVentaDiaria() {
        when(ventaDiariaRepository.findByFecha(any())).thenReturn(Optional.empty());
        PagoProcesadoEvent evento = new PagoProcesadoEvent(
                UUID.randomUUID(), Instant.now(), 1, 10L, 20L, true, 5000.0, "DEBITO");

        service.registrarPagoProcesado(evento);

        ArgumentCaptor<VentaDiaria> captor = ArgumentCaptor.forClass(VentaDiaria.class);
        verify(ventaDiariaRepository).save(captor.capture());
        assertThat(captor.getValue().getTotalVentas()).isEqualTo(5000.0);
        assertThat(captor.getValue().getCantidadPedidos()).isEqualTo(1);
    }

    @Test
    void registrarPagoProcesado_rechazado_noTocaVentaDiaria() {
        PagoProcesadoEvent evento = new PagoProcesadoEvent(
                UUID.randomUUID(), Instant.now(), 1, 10L, 20L, false, 5000.0, "DEBITO");

        service.registrarPagoProcesado(evento);

        verifyNoInteractions(ventaDiariaRepository);
    }

    @Test
    void registrarPagoProcesado_ventaExistente_acumulaSobreLoYaGuardado() {
        VentaDiaria existente = new VentaDiaria();
        existente.setFecha("2026-10-01");
        existente.setTotalVentas(1000.0);
        existente.setCantidadPedidos(2);
        when(ventaDiariaRepository.findByFecha(any())).thenReturn(Optional.of(existente));
        PagoProcesadoEvent evento = new PagoProcesadoEvent(
                UUID.randomUUID(), Instant.now(), 1, 10L, 20L, true, 2500.0, "DEBITO");

        service.registrarPagoProcesado(evento);

        ArgumentCaptor<VentaDiaria> captor = ArgumentCaptor.forClass(VentaDiaria.class);
        verify(ventaDiariaRepository).save(captor.capture());
        assertThat(captor.getValue().getTotalVentas()).isEqualTo(3500.0);
        assertThat(captor.getValue().getCantidadPedidos()).isEqualTo(3);
    }

    @Test
    void registrarPedidoCreado_sumaHoraProductoYClienteNuevo() {
        when(pedidosPorHoraRepository.findByFechaAndHora(any(), any())).thenReturn(Optional.empty());
        when(ventaProductoRepository.findByFechaAndProductoId(any(), any())).thenReturn(Optional.empty());
        when(clienteVistoRepository.existsById("nueva@test.cl")).thenReturn(false);

        PedidoCreadoEvent evento = new PedidoCreadoEvent(
                UUID.randomUUID(), Instant.now(), 1, 10L, "COD-10", "nueva@test.cl",
                "CREDITO", "1234", 5000.0,
                List.of(new ItemEvent(1L, "Latte", 2, 2500.0)));

        service.registrarPedidoCreado(evento);

        verify(pedidosPorHoraRepository).save(any(PedidosPorHora.class));
        ArgumentCaptor<VentaProducto> productoCaptor = ArgumentCaptor.forClass(VentaProducto.class);
        verify(ventaProductoRepository).save(productoCaptor.capture());
        assertThat(productoCaptor.getValue().getCantidad()).isEqualTo(2);
        assertThat(productoCaptor.getValue().getMonto()).isEqualTo(5000.0);
        verify(clienteVistoRepository).save(any(ClienteVisto.class));
        verify(pedidoEstadoActualRepository).save(argThat(
                estado -> estado.getEstado().equals("PENDIENTE_PAGO") && estado.getPedidoId().equals(10L)));
    }

    @Test
    void registrarPedidoCreado_clienteYaVisto_noLoVuelveARegistrar() {
        when(pedidosPorHoraRepository.findByFechaAndHora(any(), any())).thenReturn(Optional.empty());
        when(clienteVistoRepository.existsById("conocida@test.cl")).thenReturn(true);

        PedidoCreadoEvent evento = new PedidoCreadoEvent(
                UUID.randomUUID(), Instant.now(), 1, 10L, "COD-10", "conocida@test.cl",
                "CREDITO", "1234", 0.0, List.of());

        service.registrarPedidoCreado(evento);

        verify(clienteVistoRepository, never()).save(any());
    }

    @Test
    void registrarEstadoActualizado_actualizaElEstadoVigente() {
        PedidoEstadoActual actual = new PedidoEstadoActual(10L, "PAGADO", "2026-10-01");
        when(pedidoEstadoActualRepository.findById(10L)).thenReturn(Optional.of(actual));
        PedidoEstadoActualizadoEvent evento = new PedidoEstadoActualizadoEvent(
                UUID.randomUUID(), Instant.now(), 1, 10L, "COD-10", "PAGADO", "EN_PREPARACION");

        service.registrarEstadoActualizado(evento);

        verify(pedidoEstadoActualRepository).save(argThat(e -> e.getEstado().equals("EN_PREPARACION")));
    }
}
