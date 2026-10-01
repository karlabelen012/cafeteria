package cl.duoc.cafeteria.productos.service;

import cl.duoc.cafeteria.productos.dto.ProductoRequest;
import cl.duoc.cafeteria.productos.dto.ProductoResponse;
import cl.duoc.cafeteria.productos.exception.ConflictoDeNegocioException;
import cl.duoc.cafeteria.productos.exception.RecursoNoEncontradoException;
import cl.duoc.cafeteria.productos.model.Producto;
import cl.duoc.cafeteria.productos.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Pruebas unitarias de las reglas de negocio del menu (sin levantar Spring). */
@ExtendWith(MockitoExtension.class)
class ProductoServiceImplTest {

    @Mock
    private ProductoRepository repository;

    @InjectMocks
    private ProductoServiceImpl service;

    private ProductoRequest request;

    @BeforeEach
    void setUp() {
        request = new ProductoRequest("Latte Vainilla", "Rico latte", 3200.0, "Bebidas calientes", true,
                "/productos/latte-vainilla.jpg");
    }

    @Test
    void crear_conNombreDuplicado_lanzaConflicto() {
        when(repository.existsByNombre("Latte Vainilla")).thenReturn(true);

        assertThatThrownBy(() -> service.crear(request))
                .isInstanceOf(ConflictoDeNegocioException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void crear_conNombreNuevo_guardaYRetornaResponse() {
        when(repository.existsByNombre("Latte Vainilla")).thenReturn(false);
        when(repository.save(any(Producto.class))).thenAnswer(invocation -> {
            Producto p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });

        ProductoResponse response = service.crear(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.nombre()).isEqualTo("Latte Vainilla");
        assertThat(response.disponible()).isTrue();
    }

    @Test
    void obtener_conIdInexistente_lanzaNoEncontrado() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtener(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void eliminar_conIdInexistente_lanzaNoEncontrado() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.eliminar(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(repository, never()).deleteById(any());
    }

    @Test
    void eliminar_conIdExistente_borra() {
        when(repository.existsById(1L)).thenReturn(true);

        service.eliminar(1L);

        verify(repository).deleteById(1L);
    }
}
