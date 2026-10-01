package cl.duoc.cafeteria.reportes.service;

import cl.duoc.cafeteria.reportes.dto.DashboardResponse;
import cl.duoc.cafeteria.reportes.dto.DashboardResponse.EstadoConteo;
import cl.duoc.cafeteria.reportes.dto.DashboardResponse.FranjaConteo;
import cl.duoc.cafeteria.reportes.dto.DashboardResponse.HoraConteo;
import cl.duoc.cafeteria.reportes.dto.DashboardResponse.ProductoTop;
import cl.duoc.cafeteria.reportes.dto.DashboardResponse.VentaPorDia;
import cl.duoc.cafeteria.reportes.model.PedidoEstadoActual;
import cl.duoc.cafeteria.reportes.model.PedidosPorHora;
import cl.duoc.cafeteria.reportes.model.VentaDiaria;
import cl.duoc.cafeteria.reportes.model.VentaProducto;
import cl.duoc.cafeteria.reportes.repository.ClienteVistoRepository;
import cl.duoc.cafeteria.reportes.repository.PedidoEstadoActualRepository;
import cl.duoc.cafeteria.reportes.repository.PedidosPorHoraRepository;
import cl.duoc.cafeteria.reportes.repository.VentaDiariaRepository;
import cl.duoc.cafeteria.reportes.repository.VentaProductoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final int TOP_PRODUCTOS = 5;

    private final VentaDiariaRepository ventaDiariaRepository;
    private final VentaProductoRepository ventaProductoRepository;
    private final PedidosPorHoraRepository pedidosPorHoraRepository;
    private final PedidoEstadoActualRepository pedidoEstadoActualRepository;
    private final ClienteVistoRepository clienteVistoRepository;

    public DashboardServiceImpl(VentaDiariaRepository ventaDiariaRepository,
            VentaProductoRepository ventaProductoRepository,
            PedidosPorHoraRepository pedidosPorHoraRepository,
            PedidoEstadoActualRepository pedidoEstadoActualRepository,
            ClienteVistoRepository clienteVistoRepository) {
        this.ventaDiariaRepository = ventaDiariaRepository;
        this.ventaProductoRepository = ventaProductoRepository;
        this.pedidosPorHoraRepository = pedidosPorHoraRepository;
        this.pedidoEstadoActualRepository = pedidoEstadoActualRepository;
        this.clienteVistoRepository = clienteVistoRepository;
    }

    @Override
    public DashboardResponse calcular(LocalDate desde, LocalDate hasta) {
        LocalDate hoy = LocalDate.now();
        LocalDate rangoHasta = hasta != null ? hasta : hoy;
        LocalDate rangoDesde = desde != null ? desde : rangoHasta.minusDays(6);

        String fechaHoy = hoy.format(FORMATO_FECHA);
        VentaDiaria ventaHoy = ventaDiariaRepository.findByFecha(fechaHoy).orElse(null);
        double ventasHoy = ventaHoy != null ? ventaHoy.getTotalVentas() : 0d;
        int pedidosHoy = ventaHoy != null ? ventaHoy.getCantidadPedidos() : 0;
        double ticketPromedio = pedidosHoy > 0 ? ventasHoy / pedidosHoy : 0d;

        List<VentaPorDia> ventasRango = ventasPorDia(rangoDesde, rangoHasta);
        List<VentaPorDia> ventasRangoAnterior = ventasPorDia(
                rangoDesde.minusDays(7), rangoHasta.minusDays(7));

        double totalSemanaActual = ventasRango.stream().mapToDouble(VentaPorDia::totalVentas).sum();
        double totalSemanaAnterior = ventasRangoAnterior.stream().mapToDouble(VentaPorDia::totalVentas).sum();
        double variacion = variacionPorcentaje(totalSemanaActual, totalSemanaAnterior);

        int clientesNuevosHoy = (int) clienteVistoRepository.countByPrimeraFecha(fechaHoy);

        List<PedidosPorHora> horasHoy = pedidosPorHoraRepository.findByFecha(fechaHoy);
        List<HoraConteo> pedidosPorHoraHoy = horasHoy.stream()
                .sorted(Comparator.comparing(PedidosPorHora::getHora))
                .map(h -> new HoraConteo(h.getHora(), h.getCantidad()))
                .toList();

        List<PedidosPorHora> horasRango = pedidosPorHoraRepository.findByFechaBetween(
                rangoDesde.format(FORMATO_FECHA), rangoHasta.format(FORMATO_FECHA));
        List<FranjaConteo> pedidosPorFranja = pedidosPorFranja(horasRango);

        List<ProductoTop> topProductos = topProductos(rangoDesde, rangoHasta);

        List<EstadoConteo> pedidosPorEstado = pedidosPorEstado(rangoDesde, rangoHasta);

        return new DashboardResponse(
                ventasHoy, pedidosHoy, ticketPromedio, clientesNuevosHoy, variacion,
                ventasRango, ventasRangoAnterior, pedidosPorHoraHoy, pedidosPorFranja,
                topProductos, pedidosPorEstado);
    }

    private List<VentaPorDia> ventasPorDia(LocalDate desde, LocalDate hasta) {
        Map<String, VentaDiaria> porFecha = ventaDiariaRepository
                .findByFechaBetweenOrderByFecha(desde.format(FORMATO_FECHA), hasta.format(FORMATO_FECHA))
                .stream()
                .collect(LinkedHashMap::new, (map, v) -> map.put(v.getFecha(), v), Map::putAll);

        List<VentaPorDia> resultado = new ArrayList<>();
        for (LocalDate dia = desde; !dia.isAfter(hasta); dia = dia.plusDays(1)) {
            String fecha = dia.format(FORMATO_FECHA);
            VentaDiaria venta = porFecha.get(fecha);
            resultado.add(new VentaPorDia(fecha,
                    venta != null ? venta.getTotalVentas() : 0d,
                    venta != null ? venta.getCantidadPedidos() : 0));
        }
        return resultado;
    }

    private List<FranjaConteo> pedidosPorFranja(List<PedidosPorHora> horas) {
        int manana = 0;
        int tarde = 0;
        int noche = 0;
        for (PedidosPorHora h : horas) {
            int hora = h.getHora();
            if (hora >= 7 && hora < 12) {
                manana += h.getCantidad();
            } else if (hora >= 12 && hora < 18) {
                tarde += h.getCantidad();
            } else if (hora >= 18 && hora < 22) {
                noche += h.getCantidad();
            }
        }
        return List.of(
                new FranjaConteo("Mañana", manana),
                new FranjaConteo("Tarde", tarde),
                new FranjaConteo("Noche", noche));
    }

    private List<ProductoTop> topProductos(LocalDate desde, LocalDate hasta) {
        Map<Long, ProductoAcumulado> acumulado = new LinkedHashMap<>();
        for (VentaProducto registro : ventaProductoRepository.findByFechaBetween(
                desde.format(FORMATO_FECHA), hasta.format(FORMATO_FECHA))) {
            acumulado.computeIfAbsent(registro.getProductoId(),
                            id -> new ProductoAcumulado(registro.getNombreProducto()))
                    .sumar(registro.getCantidad(), registro.getMonto());
        }
        return acumulado.entrySet().stream()
                .map(e -> new ProductoTop(e.getKey(), e.getValue().nombre, e.getValue().cantidad, e.getValue().monto))
                .sorted(Comparator.comparingInt(ProductoTop::cantidad).reversed())
                .limit(TOP_PRODUCTOS)
                .toList();
    }

    private List<EstadoConteo> pedidosPorEstado(LocalDate desde, LocalDate hasta) {
        Map<String, Integer> conteo = new LinkedHashMap<>();
        for (PedidoEstadoActual registro : pedidoEstadoActualRepository.findByFechaBetween(
                desde.format(FORMATO_FECHA), hasta.format(FORMATO_FECHA))) {
            conteo.merge(registro.getEstado(), 1, Integer::sum);
        }
        return conteo.entrySet().stream()
                .map(e -> new EstadoConteo(e.getKey(), e.getValue()))
                .toList();
    }

    private static double variacionPorcentaje(double actual, double anterior) {
        if (anterior == 0d) {
            return actual > 0d ? 100d : 0d;
        }
        return ((actual - anterior) / anterior) * 100d;
    }

    private static final class ProductoAcumulado {
        private final String nombre;
        private int cantidad;
        private double monto;

        private ProductoAcumulado(String nombre) {
            this.nombre = nombre;
        }

        private void sumar(int cantidad, double monto) {
            this.cantidad += cantidad;
            this.monto += monto;
        }
    }
}
