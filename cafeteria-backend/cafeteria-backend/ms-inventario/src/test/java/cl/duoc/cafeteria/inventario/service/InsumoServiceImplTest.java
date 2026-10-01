package cl.duoc.cafeteria.inventario.service;

import cl.duoc.cafeteria.inventario.dto.InsumoRequest;
import cl.duoc.cafeteria.inventario.dto.InsumoResponse;
import cl.duoc.cafeteria.inventario.exception.ConflictoDeNegocioException;
import cl.duoc.cafeteria.inventario.exception.RecursoNoEncontradoException;
import cl.duoc.cafeteria.inventario.model.Insumo;
import cl.duoc.cafeteria.inventario.repository.InsumoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InsumoServiceImplTest {

    @Mock
    private InsumoRepository repository;

    private InsumoServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new InsumoServiceImpl(repository);
    }

    private Insumo insumo(Long id, String nombre, double stockActual, double stockMinimo) {
        Insumo insumo = new Insumo();
        insumo.setId(id);
        insumo.setNombre(nombre);
        insumo.setUnidadMedida("g");
        insumo.setStockActual(stockActual);
        insumo.setStockMinimo(stockMinimo);
        return insumo;
    }

    @Test
    void alertas_devuelveSoloLosInsumosConStockBajo() {
        when(repository.findConStockBajo()).thenReturn(List.of(insumo(1L, "Azucar", 5.0, 10.0)));

        List<InsumoResponse> alertas = service.alertas();

        assertThat(alertas).hasSize(1);
        assertThat(alertas.get(0).nombre()).isEqualTo("Azucar");
        verify(repository).findConStockBajo();
    }

    @Test
    void alertas_sinInsumosConStockBajo_devuelveListaVacia() {
        when(repository.findConStockBajo()).thenReturn(List.of());

        assertThat(service.alertas()).isEmpty();
    }

    @Test
    void crear_conNombreDuplicado_lanzaConflicto() {
        when(repository.existsByNombre("Leche")).thenReturn(true);

        assertThatThrownBy(() -> service.crear(new InsumoRequest("Leche", "ml", 10.0, 2.0)))
                .isInstanceOf(ConflictoDeNegocioException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void obtener_idInexistente_lanza404() {
        when(repository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtener(99L)).isInstanceOf(RecursoNoEncontradoException.class);
    }
}
