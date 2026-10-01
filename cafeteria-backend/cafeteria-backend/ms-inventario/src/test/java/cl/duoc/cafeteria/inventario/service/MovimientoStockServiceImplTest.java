package cl.duoc.cafeteria.inventario.service;

import cl.duoc.cafeteria.inventario.dto.MovimientoStockRequest;
import cl.duoc.cafeteria.inventario.dto.MovimientoStockResponse;
import cl.duoc.cafeteria.inventario.exception.ConflictoDeNegocioException;
import cl.duoc.cafeteria.inventario.exception.RecursoNoEncontradoException;
import cl.duoc.cafeteria.inventario.model.Insumo;
import cl.duoc.cafeteria.inventario.model.MovimientoStock;
import cl.duoc.cafeteria.inventario.repository.InsumoRepository;
import cl.duoc.cafeteria.inventario.repository.MovimientoStockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * El comportamiento mas importante del modulo: aplicar correctamente cada
 * tipo de movimiento sobre stockActual, y sobre todo, rechazar una SALIDA
 * que dejaria el stock negativo SIN modificar nada (ver docs/EP2_PLAN.md
 * seccion 5 y las instrucciones del agente de ms-inventario).
 */
@ExtendWith(MockitoExtension.class)
class MovimientoStockServiceImplTest {

    @Mock
    private MovimientoStockRepository repository;

    @Mock
    private InsumoRepository insumoRepository;

    private MovimientoStockServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new MovimientoStockServiceImpl(repository, insumoRepository);
    }

    private Insumo insumoConStock(double stockActual) {
        Insumo insumo = new Insumo();
        insumo.setId(1L);
        insumo.setNombre("Leche");
        insumo.setUnidadMedida("ml");
        insumo.setStockActual(stockActual);
        insumo.setStockMinimo(10.0);
        return insumo;
    }

    @Test
    void entrada_aumentaElStock() {
        Insumo insumo = insumoConStock(100.0);
        when(insumoRepository.findById(1L)).thenReturn(Optional.of(insumo));
        when(repository.save(any(MovimientoStock.class))).thenAnswer(inv -> inv.getArgument(0));

        MovimientoStockResponse response = service.registrar(1L, new MovimientoStockRequest("ENTRADA", 20.0, "Compra"));

        assertThat(insumo.getStockActual()).isEqualTo(120.0);
        assertThat(response.tipo()).isEqualTo("ENTRADA");
        verify(insumoRepository).save(insumo);
    }

    @Test
    void salida_disminuyeElStock() {
        Insumo insumo = insumoConStock(100.0);
        when(insumoRepository.findById(1L)).thenReturn(Optional.of(insumo));
        when(repository.save(any(MovimientoStock.class))).thenAnswer(inv -> inv.getArgument(0));

        service.registrar(1L, new MovimientoStockRequest("SALIDA", 30.0, "Consumo en preparacion"));

        assertThat(insumo.getStockActual()).isEqualTo(70.0);
        verify(insumoRepository).save(insumo);
    }

    @Test
    void salida_queDejariaStockNegativo_lanzaConflictoYNoModificaNada() {
        Insumo insumo = insumoConStock(10.0);
        when(insumoRepository.findById(1L)).thenReturn(Optional.of(insumo));

        assertThatThrownBy(() -> service.registrar(1L, new MovimientoStockRequest("SALIDA", 50.0, "Consumo")))
                .isInstanceOf(ConflictoDeNegocioException.class)
                .hasMessageContaining("Stock insuficiente");

        // El stock del insumo no debe cambiar, y no debe persistirse nada.
        assertThat(insumo.getStockActual()).isEqualTo(10.0);
        verify(insumoRepository, never()).save(any());
        verify(repository, never()).save(any());
    }

    @Test
    void ajuste_fijaElStockAlValorIndicado() {
        Insumo insumo = insumoConStock(100.0);
        when(insumoRepository.findById(1L)).thenReturn(Optional.of(insumo));
        when(repository.save(any(MovimientoStock.class))).thenAnswer(inv -> inv.getArgument(0));

        service.registrar(1L, new MovimientoStockRequest("AJUSTE", 42.0, "Conteo fisico"));

        assertThat(insumo.getStockActual()).isEqualTo(42.0);
    }

    @Test
    void registrar_insumoInexistente_lanza404() {
        when(insumoRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.registrar(99L, new MovimientoStockRequest("ENTRADA", 1.0, "x")))
                .isInstanceOf(RecursoNoEncontradoException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void registrar_guardaElMovimientoConLosDatosCorrectos() {
        Insumo insumo = insumoConStock(100.0);
        when(insumoRepository.findById(1L)).thenReturn(Optional.of(insumo));
        ArgumentCaptor<MovimientoStock> captor = ArgumentCaptor.forClass(MovimientoStock.class);
        when(repository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        service.registrar(1L, new MovimientoStockRequest("ENTRADA", 15.0, "Reposicion"));

        MovimientoStock guardado = captor.getValue();
        assertThat(guardado.getInsumoId()).isEqualTo(1L);
        assertThat(guardado.getTipo()).isEqualTo("ENTRADA");
        assertThat(guardado.getCantidad()).isEqualTo(15.0);
        assertThat(guardado.getMotivo()).isEqualTo("Reposicion");
        assertThat(guardado.getFecha()).isNotNull();
        assertThat(guardado.getUsuario()).isEqualTo("sistema");
    }
}
