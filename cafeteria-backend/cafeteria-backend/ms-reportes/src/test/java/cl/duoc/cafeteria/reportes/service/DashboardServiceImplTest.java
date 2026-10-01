package cl.duoc.cafeteria.reportes.service;

import cl.duoc.cafeteria.reportes.dto.DashboardResponse;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Prueba unitaria del calculo del dashboard (ver docs/EP2_PLAN.md seccion 5):
 * ticket promedio, variacion semanal, franjas y top productos.
 */
@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

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

    private DashboardServiceImpl service;
    private final LocalDate hoy = LocalDate.now();

    @BeforeEach
    void setUp() {
        service = new DashboardServiceImpl(ventaDiariaRepository, ventaProductoRepository,
                pedidosPorHoraRepository, pedidoEstadoActualRepository, clienteVistoRepository);

        VentaDiaria ventaHoy = new VentaDiaria();
        ventaHoy.setFecha(hoy.toString());
        ventaHoy.setTotalVentas(10000.0);
        ventaHoy.setCantidadPedidos(4);
        when(ventaDiariaRepository.findByFecha(hoy.toString())).thenReturn(java.util.Optional.of(ventaHoy));
        when(ventaDiariaRepository.findByFechaBetweenOrderByFecha(anyString(), anyString()))
                .thenReturn(List.of(ventaHoy));
        when(clienteVistoRepository.countByPrimeraFecha(hoy.toString())).thenReturn(2L);
        when(pedidosPorHoraRepository.findByFecha(anyString())).thenReturn(List.of());
        when(pedidosPorHoraRepository.findByFechaBetween(anyString(), anyString())).thenReturn(List.of(
                hora(8, 3), hora(14, 5), hora(20, 2), hora(23, 1)));
        when(ventaProductoRepository.findByFechaBetween(anyString(), anyString())).thenReturn(List.of(
                producto(1L, "Latte", 10, 25000.0),
                producto(2L, "Espresso", 15, 15000.0)));
        when(pedidoEstadoActualRepository.findByFechaBetween(anyString(), anyString())).thenReturn(List.of(
                new PedidoEstadoActual(1L, "ENTREGADO", hoy.toString()),
                new PedidoEstadoActual(2L, "EN_PREPARACION", hoy.toString()),
                new PedidoEstadoActual(3L, "ENTREGADO", hoy.toString())));
    }

    private PedidosPorHora hora(int hora, int cantidad) {
        PedidosPorHora p = new PedidosPorHora();
        p.setHora(hora);
        p.setCantidad(cantidad);
        return p;
    }

    private VentaProducto producto(Long id, String nombre, int cantidad, double monto) {
        VentaProducto v = new VentaProducto();
        v.setProductoId(id);
        v.setNombreProducto(nombre);
        v.setCantidad(cantidad);
        v.setMonto(monto);
        return v;
    }

    @Test
    void calcular_ticketPromedioYClientesNuevos() {
        DashboardResponse resultado = service.calcular(null, null);

        assertThat(resultado.ventasHoy()).isEqualTo(10000.0);
        assertThat(resultado.pedidosHoy()).isEqualTo(4);
        assertThat(resultado.ticketPromedio()).isEqualTo(2500.0);
        assertThat(resultado.clientesNuevosHoy()).isEqualTo(2);
    }

    @Test
    void calcular_pedidosPorFranjaAgrupaCorrectamente() {
        DashboardResponse resultado = service.calcular(null, null);

        assertThat(resultado.pedidosPorFranja()).extracting(DashboardResponse.FranjaConteo::franja,
                        DashboardResponse.FranjaConteo::cantidad)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("Mañana", 3),
                        org.assertj.core.groups.Tuple.tuple("Tarde", 5),
                        org.assertj.core.groups.Tuple.tuple("Noche", 2));
    }

    @Test
    void calcular_topProductosOrdenaPorCantidadDescendente() {
        DashboardResponse resultado = service.calcular(null, null);

        assertThat(resultado.topProductos()).hasSize(2);
        assertThat(resultado.topProductos().get(0).nombreProducto()).isEqualTo("Espresso");
        assertThat(resultado.topProductos().get(0).cantidad()).isEqualTo(15);
    }

    @Test
    void calcular_pedidosPorEstadoCuentaCadaEstado() {
        DashboardResponse resultado = service.calcular(null, null);

        assertThat(resultado.pedidosPorEstado()).extracting(DashboardResponse.EstadoConteo::estado,
                        DashboardResponse.EstadoConteo::cantidad)
                .contains(
                        org.assertj.core.groups.Tuple.tuple("ENTREGADO", 2),
                        org.assertj.core.groups.Tuple.tuple("EN_PREPARACION", 1));
    }
}
