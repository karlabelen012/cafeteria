package cl.duoc.cafeteria.empleados.service;

import cl.duoc.cafeteria.empleados.dto.EmpleadoRequest;
import cl.duoc.cafeteria.empleados.dto.EmpleadoResponse;
import cl.duoc.cafeteria.empleados.exception.ConflictoDeNegocioException;
import cl.duoc.cafeteria.empleados.exception.RecursoNoEncontradoException;
import cl.duoc.cafeteria.empleados.model.Empleado;
import cl.duoc.cafeteria.empleados.repository.EmpleadoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Pruebas unitarias de las reglas de negocio de empleados (sin levantar Spring). */
@ExtendWith(MockitoExtension.class)
class EmpleadoServiceImplTest {

    @Mock
    private EmpleadoRepository repository;

    @InjectMocks
    private EmpleadoServiceImpl service;

    private EmpleadoRequest request;

    @BeforeEach
    void setUp() {
        request = new EmpleadoRequest("Ana Soto", "ana.soto@cafegestion360.cl", "BARISTA", true,
                LocalDate.of(2023, 1, 15));
    }

    @Test
    void crear_conEmailDuplicado_lanzaConflicto() {
        when(repository.existsByEmail("ana.soto@cafegestion360.cl")).thenReturn(true);

        assertThatThrownBy(() -> service.crear(request))
                .isInstanceOf(ConflictoDeNegocioException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void crear_conEmailNuevo_guardaActivoPorDefecto() {
        EmpleadoRequest sinActivo = new EmpleadoRequest("Ana Soto", "ana.soto@cafegestion360.cl", "BARISTA", null,
                LocalDate.of(2023, 1, 15));
        when(repository.existsByEmail("ana.soto@cafegestion360.cl")).thenReturn(false);
        when(repository.save(any(Empleado.class))).thenAnswer(invocation -> {
            Empleado e = invocation.getArgument(0);
            e.setId(1L);
            return e;
        });

        EmpleadoResponse response = service.crear(sinActivo);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.activo()).isTrue();
    }

    @Test
    void actualizar_conEmailDeOtroEmpleado_lanzaConflicto() {
        Empleado existente = empleadoExistente();
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.existsByEmailAndIdNot("ana.soto@cafegestion360.cl", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.actualizar(1L, request))
                .isInstanceOf(ConflictoDeNegocioException.class);

        verify(repository, never()).save(any());
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
    void desactivar_conIdExistente_dejaActivoEnFalse() {
        Empleado existente = empleadoExistente();
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(any(Empleado.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EmpleadoResponse response = service.desactivar(1L);

        assertThat(response.activo()).isFalse();
        verify(repository).save(argThat(e -> Boolean.FALSE.equals(e.getActivo())));
    }

    @Test
    void desactivar_conIdInexistente_lanzaNoEncontrado() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.desactivar(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(repository, never()).save(any());
    }

    private Empleado empleadoExistente() {
        Empleado empleado = new Empleado();
        empleado.setId(1L);
        empleado.setNombre("Ana Soto");
        empleado.setEmail("ana.soto@cafegestion360.cl");
        empleado.setRol("BARISTA");
        empleado.setActivo(true);
        empleado.setFechaIngreso(LocalDate.of(2023, 1, 15));
        return empleado;
    }
}
