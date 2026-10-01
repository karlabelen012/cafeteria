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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class ReporteEventoServiceImpl implements ReporteEventoService {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final ZoneId ZONA = ZoneId.systemDefault();

    private final VentaDiariaRepository ventaDiariaRepository;
    private final VentaProductoRepository ventaProductoRepository;
    private final PedidosPorHoraRepository pedidosPorHoraRepository;
    private final PedidoEstadoActualRepository pedidoEstadoActualRepository;
    private final ClienteVistoRepository clienteVistoRepository;

    public ReporteEventoServiceImpl(VentaDiariaRepository ventaDiariaRepository,
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
    @Transactional
    public void registrarPedidoCreado(PedidoCreadoEvent evento) {
        String fecha = fechaDe(evento.ocurridoEn());
        int hora = horaDe(evento.ocurridoEn());

        registrarPedidoPorHora(fecha, hora);
        for (ItemEvent item : evento.items()) {
            registrarVentaProducto(fecha, item);
        }
        registrarClienteVistoSiCorresponde(evento.clienteEmail(), fecha);

        // Estado inicial: lo normal es que pedido.estado.actualizado llegue poco
        // despues y lo sobrescriba (PAGADO, EN_PREPARACION, etc.).
        pedidoEstadoActualRepository.save(
                new PedidoEstadoActual(evento.pedidoId(), "PENDIENTE_PAGO", fecha));
    }

    @Override
    @Transactional
    public void registrarEstadoActualizado(PedidoEstadoActualizadoEvent evento) {
        PedidoEstadoActual actual = pedidoEstadoActualRepository.findById(evento.pedidoId())
                .orElseGet(() -> new PedidoEstadoActual(evento.pedidoId(), evento.estadoNuevo(),
                        fechaDe(evento.ocurridoEn())));
        actual.setEstado(evento.estadoNuevo());
        pedidoEstadoActualRepository.save(actual);
    }

    @Override
    @Transactional
    public void registrarPagoProcesado(PagoProcesadoEvent evento) {
        if (!evento.aprobado()) {
            // Solo las ventas efectivamente cobradas cuentan como venta del dia.
            return;
        }
        String fecha = fechaDe(evento.ocurridoEn());
        VentaDiaria venta = ventaDiariaRepository.findByFecha(fecha)
                .orElseGet(() -> {
                    VentaDiaria nueva = new VentaDiaria();
                    nueva.setFecha(fecha);
                    nueva.setTotalVentas(0d);
                    nueva.setCantidadPedidos(0);
                    return nueva;
                });
        venta.setTotalVentas(venta.getTotalVentas() + evento.monto());
        venta.setCantidadPedidos(venta.getCantidadPedidos() + 1);
        ventaDiariaRepository.save(venta);
    }

    private void registrarPedidoPorHora(String fecha, int hora) {
        PedidosPorHora registro = pedidosPorHoraRepository.findByFechaAndHora(fecha, hora)
                .orElseGet(() -> {
                    PedidosPorHora nuevo = new PedidosPorHora();
                    nuevo.setFecha(fecha);
                    nuevo.setHora(hora);
                    nuevo.setCantidad(0);
                    return nuevo;
                });
        registro.setCantidad(registro.getCantidad() + 1);
        pedidosPorHoraRepository.save(registro);
    }

    private void registrarVentaProducto(String fecha, ItemEvent item) {
        VentaProducto registro = ventaProductoRepository.findByFechaAndProductoId(fecha, item.productoId())
                .orElseGet(() -> {
                    VentaProducto nuevo = new VentaProducto();
                    nuevo.setFecha(fecha);
                    nuevo.setProductoId(item.productoId());
                    nuevo.setNombreProducto(item.nombreProducto());
                    nuevo.setCantidad(0);
                    nuevo.setMonto(0d);
                    return nuevo;
                });
        registro.setCantidad(registro.getCantidad() + item.cantidad());
        registro.setMonto(registro.getMonto() + item.cantidad() * item.precioUnitario());
        ventaProductoRepository.save(registro);
    }

    private void registrarClienteVistoSiCorresponde(String clienteEmail, String fecha) {
        if (clienteEmail == null || clienteEmail.isBlank()) {
            return;
        }
        if (!clienteVistoRepository.existsById(clienteEmail)) {
            clienteVistoRepository.save(new ClienteVisto(clienteEmail, fecha));
        }
    }

    private static String fechaDe(Instant instante) {
        return LocalDate.ofInstant(instante, ZONA).format(FORMATO_FECHA);
    }

    private static int horaDe(Instant instante) {
        return instante.atZone(ZONA).getHour();
    }
}
