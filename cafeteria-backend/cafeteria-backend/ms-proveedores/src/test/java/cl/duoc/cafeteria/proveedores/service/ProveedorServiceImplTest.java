package cl.duoc.cafeteria.proveedores.service;

import cl.duoc.cafeteria.proveedores.dto.ProveedorRequest;
import cl.duoc.cafeteria.proveedores.dto.ProveedorResponse;
import cl.duoc.cafeteria.proveedores.exception.ConflictoDeNegocioException;
import cl.duoc.cafeteria.proveedores.exception.RecursoNoEncontradoException;
import cl.duoc.cafeteria.proveedores.model.Proveedor;
import cl.duoc.cafeteria.proveedores.repository.ProveedorRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProveedorServiceImplTest {

    @Mock
    private ProveedorRepository repository;

    @InjectMocks
    private ProveedorServiceImpl service;

    private ProveedorRequest request;

    @BeforeEach
    void setUp() {
        request = new ProveedorRequest("Distribuidora Cafetera del Sur", "76543210-3",
                "contacto@cafeteradelsur.cl", "+56 9 1234 5678", "Cafe en grano");
    }

    @Test
    void crearGuardaCuandoElRutNoExiste() {
        when(repository.existsByRut("76543210-3")).thenReturn(false);
        when(repository.save(any(Proveedor.class))).thenAnswer(invocacion -> {
            Proveedor p = invocacion.getArgument(0);
            p.setId(1L);
            return p;
        });

        ProveedorResponse respuesta = service.crear(request);

        assertThat(respuesta.id()).isEqualTo(1L);
        assertThat(respuesta.rut()).isEqualTo("76543210-3");
        assertThat(respuesta.nombre()).isEqualTo("Distribuidora Cafetera del Sur");
    }

    @Test
    void crearLanzaConflictoCuandoElRutYaExiste() {
        when(repository.existsByRut("76543210-3")).thenReturn(true);

        assertThatThrownBy(() -> service.crear(request))
                .isInstanceOf(ConflictoDeNegocioException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void obtenerLanza404CuandoNoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtener(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void eliminarLanza404CuandoNoExiste() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.eliminar(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void actualizarLanzaConflictoSiElNuevoRutPerteneceAOtroProveedor() {
        Proveedor existente = new Proveedor();
        existente.setId(1L);
        existente.setRut("11111111-1");

        Proveedor otro = new Proveedor();
        otro.setId(2L);
        otro.setRut("76543210-3");

        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.findByRut("76543210-3")).thenReturn(Optional.of(otro));

        assertThatThrownBy(() -> service.actualizar(1L, request))
                .isInstanceOf(ConflictoDeNegocioException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void actualizarPermiteConservarElMismoRutDelProveedor() {
        Proveedor existente = new Proveedor();
        existente.setId(1L);
        existente.setRut("76543210-3");

        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.findByRut("76543210-3")).thenReturn(Optional.of(existente));
        when(repository.save(any(Proveedor.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        ProveedorResponse respuesta = service.actualizar(1L, request);

        assertThat(respuesta.id()).isEqualTo(1L);
        assertThat(respuesta.rut()).isEqualTo("76543210-3");
    }
}
