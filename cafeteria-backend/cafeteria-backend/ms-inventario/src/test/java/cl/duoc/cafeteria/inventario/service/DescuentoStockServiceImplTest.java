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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Logica de negocio del descuento automatico de stock al procesar un pago
 * aprobado (ver docs/EP2_PLAN.md seccion 5 y DescuentoStockServiceImpl).
 */
@ExtendWith(MockitoExtension.class)
class DescuentoStockServiceImplTest {

    @Mock
    private PedidoClient pedidoClient;

    @Mock
    private RecetaItemRepository recetaItemRepository;

    @Mock
    private MovimientoStockService movimientoStockService;

    @Mock
    private InsumoService insumoService;

    @Mock
    private InventarioEventPublisher publisher;

    private DescuentoStockServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DescuentoStockServiceImpl(pedidoClient, recetaItemRepository, movimientoStockService,
                insumoService, publisher);
    }

    private RecetaItem recetaItem(Long insumoId, double cantidad) {
        RecetaItem item = new RecetaItem();
        item.setProductoId(1L);
        item.setInsumoId(insumoId);
        item.setCantidad(cantidad);
        return item;
    }

    private InsumoResponse insumo(Long id, double stockActual, double stockMinimo) {
        return new InsumoResponse(id, "Leche", "ml", stockActual, stockMinimo);
    }

    @Test
    void stockAlcanza_descuentaNormalYNoPublicaStockBajo() {
        when(pedidoClient.obtenerItems(100L)).thenReturn(List.of(new ItemDto(1L, 2)));
        when(recetaItemRepository.findByProductoId(1L)).thenReturn(List.of(recetaItem(10L, 5.0)));
        when(insumoService.obtener(10L)).thenReturn(insumo(10L, 90.0, 10.0));

        service.descontarPorPedido(100L);

        ArgumentCaptor<MovimientoStockRequest> captor = ArgumentCaptor.forClass(MovimientoStockRequest.class);
        verify(movimientoStockService).registrar(eq(10L), captor.capture());
        assertThat(captor.getValue().tipo()).isEqualTo("SALIDA");
        // cantidad de la receta (5.0) * cantidad del item (2) = 10.0
        assertThat(captor.getValue().cantidad()).isEqualTo(10.0);
        verify(movimientoStockService, never()).registrarSalidaForzada(anyLong(), any(), any());
        verify(publisher, never()).publicarStockBajo(any());
    }

    @Test
    void stockQuedaJustoEnElMinimo_publicaStockBajo() {
        when(pedidoClient.obtenerItems(100L)).thenReturn(List.of(new ItemDto(1L, 1)));
        when(recetaItemRepository.findByProductoId(1L)).thenReturn(List.of(recetaItem(10L, 5.0)));
        when(insumoService.obtener(10L)).thenReturn(insumo(10L, 5.0, 5.0));

        service.descontarPorPedido(100L);

        verify(movimientoStockService).registrar(eq(10L), any());
        ArgumentCaptor<StockBajoEvent> captor = ArgumentCaptor.forClass(StockBajoEvent.class);
        verify(publisher).publicarStockBajo(captor.capture());
        assertThat(captor.getValue().insumoId()).isEqualTo(10L);
        assertThat(captor.getValue().stockActual()).isEqualTo(5.0);
        assertThat(captor.getValue().stockMinimo()).isEqualTo(5.0);
    }

    @Test
    void stockInsuficiente_ajustaACeroEnVezDeFallarYPublicaStockBajo() {
        when(pedidoClient.obtenerItems(100L)).thenReturn(List.of(new ItemDto(1L, 1)));
        when(recetaItemRepository.findByProductoId(1L)).thenReturn(List.of(recetaItem(10L, 50.0)));
        when(movimientoStockService.registrar(eq(10L), any()))
                .thenThrow(new ConflictoDeNegocioException("Stock insuficiente para registrar la salida"));
        when(insumoService.obtener(10L)).thenReturn(insumo(10L, 0.0, 5.0));

        service.descontarPorPedido(100L);

        ArgumentCaptor<Double> cantidadCaptor = ArgumentCaptor.forClass(Double.class);
        verify(movimientoStockService).registrarSalidaForzada(eq(10L), cantidadCaptor.capture(), any());
        assertThat(cantidadCaptor.getValue()).isEqualTo(50.0);
        verify(publisher).publicarStockBajo(any());
    }

    @Test
    void productoSinRecetaDefinida_noHaceNadaYNoFalla() {
        when(pedidoClient.obtenerItems(100L)).thenReturn(List.of(new ItemDto(1L, 2)));
        when(recetaItemRepository.findByProductoId(1L)).thenReturn(List.of());

        service.descontarPorPedido(100L);

        verify(movimientoStockService, never()).registrar(anyLong(), any());
        verify(movimientoStockService, never()).registrarSalidaForzada(anyLong(), any(), any());
        verify(insumoService, never()).obtener(anyLong());
        verify(publisher, never()).publicarStockBajo(any());
    }
}
