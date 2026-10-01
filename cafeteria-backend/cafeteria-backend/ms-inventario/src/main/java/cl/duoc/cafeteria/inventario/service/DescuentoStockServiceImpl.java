package cl.duoc.cafeteria.inventario.service;

import cl.duoc.cafeteria.common.evento.StockBajoEvent;
import cl.duoc.cafeteria.inventario.client.ItemDto;
import cl.duoc.cafeteria.inventario.client.PedidoClient;
import cl.duoc.cafeteria.inventario.dto.InsumoResponse;
import cl.duoc.cafeteria.inventario.dto.MovimientoStockRequest;
import cl.duoc.cafeteria.inventario.exception.ConflictoDeNegocioException;
import cl.duoc.cafeteria.inventario.messaging.producer.InventarioEventPublisher;
import cl.duoc.cafeteria.inventario.model.RecetaItem;
import cl.duoc.cafeteria.inventario.repository.RecetaItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Logica de negocio del descuento automatico de stock (ver docs/EP2_PLAN.md
 * seccion 5): por cada producto del pedido pagado, busca su receta y
 * descuenta la cantidad de cada insumo que corresponde. Si para algun insumo
 * el stock no alcanza, en vez de fallar (como haria el flujo manual/API, ver
 * MovimientoStockService.registrar con tipo SALIDA) se fuerza el stock a 0 y
 * se audita como AJUSTE (ver MovimientoStockService.registrarSalidaForzada).
 * Tras cada descuento se verifica si el insumo quedo en stock bajo y, de ser
 * asi, se publica StockBajoEvent.
 */
@Service
public class DescuentoStockServiceImpl implements DescuentoStockService {

    private static final Logger log = LoggerFactory.getLogger(DescuentoStockServiceImpl.class);

    // Version del esquema del evento StockBajoEvent (ver cafeteria-common).
    private static final int VERSION_EVENTO = 1;
    private static final String TIPO_SALIDA = "SALIDA";

    private final PedidoClient pedidoClient;
    private final RecetaItemRepository recetaItemRepository;
    private final MovimientoStockService movimientoStockService;
    private final InsumoService insumoService;
    private final InventarioEventPublisher publisher;

    public DescuentoStockServiceImpl(PedidoClient pedidoClient,
            RecetaItemRepository recetaItemRepository,
            MovimientoStockService movimientoStockService,
            InsumoService insumoService,
            InventarioEventPublisher publisher) {
        this.pedidoClient = pedidoClient;
        this.recetaItemRepository = recetaItemRepository;
        this.movimientoStockService = movimientoStockService;
        this.insumoService = insumoService;
        this.publisher = publisher;
    }

    @Override
    public void descontarPorPedido(Long pedidoId) {
        List<ItemDto> items = pedidoClient.obtenerItems(pedidoId);

        for (ItemDto item : items) {
            List<RecetaItem> receta = recetaItemRepository.findByProductoId(item.productoId());
            if (receta.isEmpty()) {
                // No hay receta definida para este producto: no es un error, simplemente
                // no hay insumos que descontar para el (ver docs/EP2_PLAN.md seccion 5).
                continue;
            }
            for (RecetaItem recetaItem : receta) {
                double cantidadADescontar = recetaItem.getCantidad() * item.cantidad();
                descontarInsumo(recetaItem.getInsumoId(), cantidadADescontar, pedidoId);
            }
        }
    }

    private void descontarInsumo(Long insumoId, double cantidad, Long pedidoId) {
        String motivo = "Descuento automatico por pedido " + pedidoId;
        try {
            movimientoStockService.registrar(insumoId, new MovimientoStockRequest(TIPO_SALIDA, cantidad, motivo));
        } catch (ConflictoDeNegocioException ex) {
            // Stock insuficiente: a diferencia del flujo manual, este flujo
            // automatico no puede fallar (ver docs/EP2_PLAN.md seccion 5), asi
            // que se fuerza el stock a 0 y se audita como AJUSTE.
            log.warn("Stock insuficiente para el insumo {} al descontar el pedido {}: se ajusta a 0",
                    insumoId, pedidoId);
            movimientoStockService.registrarSalidaForzada(insumoId, cantidad, motivo);
        }
        verificarStockBajo(insumoId);
    }

    private void verificarStockBajo(Long insumoId) {
        InsumoResponse insumo = insumoService.obtener(insumoId);
        if (insumo.stockActual() <= insumo.stockMinimo()) {
            publisher.publicarStockBajo(new StockBajoEvent(
                    UUID.randomUUID(),
                    Instant.now(),
                    VERSION_EVENTO,
                    insumo.id(),
                    insumo.nombre(),
                    insumo.stockActual(),
                    insumo.stockMinimo()));
        }
    }
}
