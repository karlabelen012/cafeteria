package cl.duoc.cafeteria.clientes.service;

import cl.duoc.cafeteria.clientes.dto.ClienteRequest;
import cl.duoc.cafeteria.clientes.dto.ClienteResponse;
import cl.duoc.cafeteria.clientes.exception.ConflictoDeNegocioException;
import cl.duoc.cafeteria.clientes.exception.RecursoNoEncontradoException;
import cl.duoc.cafeteria.clientes.model.Cliente;
import cl.duoc.cafeteria.clientes.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceImplTest {

    @Mock
    private ClienteRepository repository;

    @InjectMocks
    private ClienteServiceImpl service;

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNombre("Camila Rojas");
        cliente.setEmail("camila.rojas@example.cl");
        cliente.setTelefono("+56912345678");
        cliente.setPuntosFidelizacion(100);
    }

    @Test
    void crear_conEmailDuplicado_lanzaConflicto() {
        ClienteRequest request = new ClienteRequest("Otro Nombre", "camila.rojas@example.cl", "+56923456789", 0);
        when(repository.existsByEmail("camila.rojas@example.cl")).thenReturn(true);

        assertThatThrownBy(() -> service.crear(request))
                .isInstanceOf(ConflictoDeNegocioException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void crear_conEmailNuevo_guardaYRetornaRespuesta() {
        ClienteRequest request = new ClienteRequest("Camila Rojas", "camila.rojas@example.cl", "+56912345678", 10);
        when(repository.existsByEmail("camila.rojas@example.cl")).thenReturn(false);
        when(repository.save(any(Cliente.class))).thenReturn(cliente);

        ClienteResponse respuesta = service.crear(request);

        assertThat(respuesta.id()).isEqualTo(1L);
        assertThat(respuesta.email()).isEqualTo("camila.rojas@example.cl");

        ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getPuntosFidelizacion()).isEqualTo(10);
    }

    @Test
    void obtener_idInexistente_lanzaNoEncontrado() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtener(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void canjearPuntos_sinPuntosSuficientes_lanzaConflicto() {
        when(repository.findById(1L)).thenReturn(Optional.of(cliente));

        assertThatThrownBy(() -> service.canjearPuntos(1L, 500))
                .isInstanceOf(ConflictoDeNegocioException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void canjearPuntos_conPuntosSuficientes_descuentaYGuarda() {
        when(repository.findById(1L)).thenReturn(Optional.of(cliente));
        when(repository.save(any(Cliente.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        ClienteResponse respuesta = service.canjearPuntos(1L, 40);

        assertThat(respuesta.puntosFidelizacion()).isEqualTo(60);
        verify(repository).save(cliente);
    }

    @Test
    void canjearPuntos_idInexistente_lanzaNoEncontrado() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.canjearPuntos(99L, 10))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void registrarCompra_clienteNuevo_loCreaConPuntosCorrectos() {
        when(repository.findByEmail("nuevo@example.cl")).thenReturn(Optional.empty());
        when(repository.save(any(Cliente.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        ClienteResponse respuesta = service.registrarCompra("Nuevo Cliente", "nuevo@example.cl", 5000.0);

        assertThat(respuesta.nombre()).isEqualTo("Nuevo Cliente");
        assertThat(respuesta.email()).isEqualTo("nuevo@example.cl");
        assertThat(respuesta.puntosFidelizacion()).isEqualTo(5);

        ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getPuntosFidelizacion()).isEqualTo(5);
    }

    @Test
    void registrarCompra_clienteExistente_sumaPuntosALosQueYaTenia() {
        when(repository.findByEmail("camila.rojas@example.cl")).thenReturn(Optional.of(cliente));
        when(repository.save(any(Cliente.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        ClienteResponse respuesta = service.registrarCompra("Camila Rojas", "camila.rojas@example.cl", 3000.0);

        // cliente ya tenia 100 puntos (ver setUp), + 3 por los 3000 de esta compra
        assertThat(respuesta.puntosFidelizacion()).isEqualTo(103);
        verify(repository).save(cliente);
    }

    @Test
    void registrarCompra_montoNoMultiploDeMil_redondeaHaciaAbajo() {
        cliente.setPuntosFidelizacion(0);
        when(repository.findByEmail("camila.rojas@example.cl")).thenReturn(Optional.of(cliente));
        when(repository.save(any(Cliente.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        ClienteResponse respuesta = service.registrarCompra("Camila Rojas", "camila.rojas@example.cl", 1999.0);

        assertThat(respuesta.puntosFidelizacion()).isEqualTo(1);
    }

    @Test
    void registrarCompra_clienteConPuntosNulos_losTrataComoCero() {
        cliente.setPuntosFidelizacion(null);
        when(repository.findByEmail("camila.rojas@example.cl")).thenReturn(Optional.of(cliente));
        when(repository.save(any(Cliente.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        ClienteResponse respuesta = service.registrarCompra("Camila Rojas", "camila.rojas@example.cl", 2000.0);

        assertThat(respuesta.puntosFidelizacion()).isEqualTo(2);
    }

    @Test
    void actualizar_conEmailDeOtroCliente_lanzaConflicto() {
        Cliente otro = new Cliente();
        otro.setId(2L);
        otro.setEmail("otro@example.cl");

        ClienteRequest request = new ClienteRequest("Camila Rojas", "otro@example.cl", "+56912345678", 0);
        when(repository.findById(1L)).thenReturn(Optional.of(cliente));
        when(repository.findByEmail("otro@example.cl")).thenReturn(Optional.of(otro));

        assertThatThrownBy(() -> service.actualizar(1L, request))
                .isInstanceOf(ConflictoDeNegocioException.class);

        verify(repository, never()).save(any());
    }
}
