package cl.duoc.cafeteria.pedidos.service;

import cl.duoc.cafeteria.common.evento.ItemEvent;
import cl.duoc.cafeteria.common.evento.PedidoCreadoEvent;
import cl.duoc.cafeteria.common.evento.PedidoEstadoActualizadoEvent;
import cl.duoc.cafeteria.pedidos.client.ProductoClient;
import cl.duoc.cafeteria.pedidos.client.ProductoDto;
import cl.duoc.cafeteria.pedidos.dto.CheckoutRequest;
import cl.duoc.cafeteria.pedidos.dto.CheckoutResponse;
import cl.duoc.cafeteria.pedidos.dto.ItemRequest;
import cl.duoc.cafeteria.pedidos.dto.ItemResponse;
import cl.duoc.cafeteria.pedidos.dto.PedidoRequest;
import cl.duoc.cafeteria.pedidos.dto.PedidoResponse;
import cl.duoc.cafeteria.pedidos.dto.SeguimientoResponse;
import cl.duoc.cafeteria.pedidos.exception.ConflictoDeNegocioException;
import cl.duoc.cafeteria.pedidos.exception.RecursoNoEncontradoException;
import cl.duoc.cafeteria.pedidos.messaging.producer.PedidoEventPublisher;
import cl.duoc.cafeteria.pedidos.model.ItemPedido;
import cl.duoc.cafeteria.pedidos.model.Pedido;
import cl.duoc.cafeteria.pedidos.repository.ItemPedidoRepository;
import cl.duoc.cafeteria.pedidos.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class PedidoServiceImpl implements PedidoService {

    private static final String CANAL_MOSTRADOR = "MOSTRADOR";
    private static final String CANAL_WEB = "WEB";

    // Metodo de pago por defecto para una venta de mostrador: el staff no pasa
    // numero de tarjeta por este flujo (no hay formulario de tarjeta en caja),
    // asi que el pedido.creado que recibe ms-pagos viaja sin ultimos4.
    private static final String METODO_PAGO_MOSTRADOR = "EFECTIVO";

    private final PedidoRepository pedidoRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final ProductoClient productoClient;
    private final PedidoEventPublisher eventPublisher;

    public PedidoServiceImpl(PedidoRepository pedidoRepository,
            ItemPedidoRepository itemPedidoRepository,
            ProductoClient productoClient,
            PedidoEventPublisher eventPublisher) {
        this.pedidoRepository = pedidoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
        this.productoClient = productoClient;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public List<PedidoResponse> listar() {
        return pedidoRepository.findAll().stream()
                .map(pedido -> PedidoResponse.desde(pedido, itemsDe(pedido.getId())))
                .toList();
    }

    @Override
    public PedidoResponse obtener(Long id) {
        Pedido pedido = buscarOFallar(id);
        return PedidoResponse.desde(pedido, itemsDe(pedido.getId()));
    }

    @Override
    @Transactional
    public PedidoResponse crear(PedidoRequest request, String canal) {
        String canalEfectivo = canal != null ? canal : CANAL_MOSTRADOR;

        ResultadoArmado armado = armarPedido(
                request.clienteId(),
                request.clienteNombre(),
                request.clienteEmail(),
                canalEfectivo,
                request.items());

        eventPublisher.publicarPedidoCreado(new PedidoCreadoEvent(
                UUID.randomUUID(),
                Instant.now(),
                1,
                armado.pedido().getId(),
                armado.pedido().getCodigoSeguimiento(),
                armado.pedido().getClienteEmail(),
                METODO_PAGO_MOSTRADOR,
                null,
                armado.pedido().getTotal(),
                armado.itemsEvento()));

        return PedidoResponse.desde(armado.pedido(), armado.itemsResponse());
    }

    @Override
    @Transactional
    public CheckoutResponse checkout(CheckoutRequest request) {
        ResultadoArmado armado = armarPedido(
                null,
                request.cliente().nombre(),
                request.cliente().email(),
                CANAL_WEB,
                request.items());

        // Nunca se persiste ni se publica el numero de tarjeta completo ni el
        // CVV: solo viajan los ultimos 4 digitos (ver docs/EP2_PLAN.md seccion 2 y 5).
        String numeroTarjeta = request.pago().numeroTarjeta();
        String ultimos4 = numeroTarjeta.substring(numeroTarjeta.length() - 4);

        eventPublisher.publicarPedidoCreado(new PedidoCreadoEvent(
                UUID.randomUUID(),
                Instant.now(),
                1,
                armado.pedido().getId(),
                armado.pedido().getCodigoSeguimiento(),
                armado.pedido().getClienteEmail(),
                request.pago().metodo(),
                ultimos4,
                armado.pedido().getTotal(),
                armado.itemsEvento()));

        return new CheckoutResponse(armado.pedido().getCodigoSeguimiento());
    }

    @Override
    public SeguimientoResponse obtenerPorCodigoSeguimiento(String codigoSeguimiento) {
        Pedido pedido = pedidoRepository.findByCodigoSeguimiento(codigoSeguimiento)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un pedido con codigo de seguimiento " + codigoSeguimiento));
        return SeguimientoResponse.desde(pedido, itemsDe(pedido.getId()));
    }

    @Override
    @Transactional
    public PedidoResponse cambiarEstado(Long id, String nuevoEstado) {
        Pedido pedido = buscarOFallar(id);
        transicionar(pedido, nuevoEstado);
        return PedidoResponse.desde(pedido, itemsDe(pedido.getId()));
    }

    @Override
    @Transactional
    public PedidoResponse cancelar(Long id) {
        return cambiarEstado(id, MaquinaEstadosPedido.CANCELADO);
    }

    @Override
    @Transactional
    public void actualizarEstadoPorResultadoPago(Long pedidoId, boolean aprobado) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un pedido con id " + pedidoId));
        String nuevoEstado = aprobado ? MaquinaEstadosPedido.PAGADO : MaquinaEstadosPedido.PAGO_RECHAZADO;
        transicionar(pedido, nuevoEstado);
    }

    // ---- privados ----

    private void transicionar(Pedido pedido, String nuevoEstado) {
        String estadoAnterior = pedido.getEstado();
        if (!MaquinaEstadosPedido.puedeTransicionarA(estadoAnterior, nuevoEstado)) {
            throw new ConflictoDeNegocioException(
                    "No se puede pasar el pedido de " + estadoAnterior + " a " + nuevoEstado);
        }
        pedido.setEstado(nuevoEstado);
        pedidoRepository.save(pedido);

        eventPublisher.publicarEstadoActualizado(new PedidoEstadoActualizadoEvent(
                UUID.randomUUID(),
                Instant.now(),
                1,
                pedido.getId(),
                pedido.getCodigoSeguimiento(),
                estadoAnterior,
                nuevoEstado));
    }

    private ResultadoArmado armarPedido(Long clienteId, String clienteNombre, String clienteEmail,
            String canal, List<ItemRequest> itemsRequest) {
        Pedido pedido = new Pedido();
        pedido.setCodigoSeguimiento(UUID.randomUUID().toString());
        pedido.setClienteId(clienteId);
        pedido.setClienteNombre(clienteNombre);
        pedido.setClienteEmail(clienteEmail);
        pedido.setCanal(canal);
        pedido.setEstado(MaquinaEstadosPedido.PENDIENTE_PAGO);
        pedido.setFechaCreacion(Instant.now());
        pedido.setTotal(0d);
        pedido = pedidoRepository.save(pedido);

        double total = 0d;
        List<ItemPedido> items = new java.util.ArrayList<>();
        List<ItemEvent> itemsEvento = new java.util.ArrayList<>();
        List<ItemResponse> itemsResponse = new java.util.ArrayList<>();

        for (ItemRequest itemRequest : itemsRequest) {
            // El precio y el nombre SIEMPRE se resuelven contra ms-productos: nunca
            // se confia en lo que venga del navegador/request (regla de seguridad
            // del proyecto, ver docs/EP2_PLAN.md seccion 2).
            ProductoDto producto = productoClient.obtener(itemRequest.productoId());

            ItemPedido item = new ItemPedido();
            item.setPedidoId(pedido.getId());
            item.setProductoId(producto.id());
            item.setNombreProducto(producto.nombre());
            item.setCantidad(itemRequest.cantidad());
            item.setPrecioUnitario(producto.precio());
            item = itemPedidoRepository.save(item);
            items.add(item);

            total += producto.precio() * itemRequest.cantidad();
            itemsEvento.add(new ItemEvent(producto.id(), producto.nombre(), itemRequest.cantidad(), producto.precio()));
            itemsResponse.add(ItemResponse.desde(item));
        }

        pedido.setTotal(total);
        pedido = pedidoRepository.save(pedido);

        return new ResultadoArmado(pedido, itemsResponse, itemsEvento);
    }

    private List<ItemResponse> itemsDe(Long pedidoId) {
        return itemPedidoRepository.findByPedidoId(pedidoId).stream()
                .map(ItemResponse::desde)
                .toList();
    }

    private Pedido buscarOFallar(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un pedido con id " + id));
    }

    private record ResultadoArmado(Pedido pedido, List<ItemResponse> itemsResponse, List<ItemEvent> itemsEvento) {
    }
}
