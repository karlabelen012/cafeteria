package cl.duoc.cafeteria.clientes.service;

import cl.duoc.cafeteria.clientes.dto.ClienteRequest;
import cl.duoc.cafeteria.clientes.dto.ClienteResponse;
import cl.duoc.cafeteria.clientes.exception.ConflictoDeNegocioException;
import cl.duoc.cafeteria.clientes.exception.RecursoNoEncontradoException;
import cl.duoc.cafeteria.clientes.model.Cliente;
import cl.duoc.cafeteria.clientes.repository.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository repository;

    public ClienteServiceImpl(ClienteRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<ClienteResponse> listar() {
        return repository.findAll().stream()
                .map(ClienteResponse::desde)
                .toList();
    }

    @Override
    public ClienteResponse obtener(Long id) {
        return ClienteResponse.desde(buscarOFallar(id));
    }

    @Override
    public ClienteResponse crear(ClienteRequest request) {
        if (repository.existsByEmail(request.email())) {
            throw new ConflictoDeNegocioException("Ya existe un cliente con el email " + request.email());
        }
        Cliente cliente = new Cliente();
        aplicarDatos(cliente, request);
        return ClienteResponse.desde(repository.save(cliente));
    }

    @Override
    public ClienteResponse actualizar(Long id, ClienteRequest request) {
        Cliente existente = buscarOFallar(id);
        repository.findByEmail(request.email())
                .filter(otro -> !otro.getId().equals(id))
                .ifPresent(otro -> {
                    throw new ConflictoDeNegocioException("Ya existe un cliente con el email " + request.email());
                });
        aplicarDatos(existente, request);
        return ClienteResponse.desde(repository.save(existente));
    }

    @Override
    public void eliminar(Long id) {
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("No existe un cliente con id " + id);
        }
        repository.deleteById(id);
    }

    @Override
    public ClienteResponse canjearPuntos(Long id, int puntos) {
        Cliente cliente = buscarOFallar(id);
        int disponibles = cliente.getPuntosFidelizacion() != null ? cliente.getPuntosFidelizacion() : 0;
        if (disponibles < puntos) {
            throw new ConflictoDeNegocioException(
                    "El cliente no tiene puntos de fidelizacion suficientes para canjear " + puntos);
        }
        cliente.setPuntosFidelizacion(disponibles - puntos);
        return ClienteResponse.desde(repository.save(cliente));
    }

    @Override
    public ClienteResponse registrarCompra(String nombre, String email, double montoTotal) {
        Cliente cliente = repository.findByEmail(email).orElseGet(() -> {
            Cliente nuevo = new Cliente();
            nuevo.setEmail(email);
            nuevo.setPuntosFidelizacion(0);
            return nuevo;
        });

        // Si el nombre viene vacio (no deberia pasar, pero el snapshot de
        // ms-pedidos es un dato externo) se usa el email como respaldo para no
        // violar la validacion @NotBlank de Cliente.nombre al crear uno nuevo.
        if (nombre != null && !nombre.isBlank()) {
            cliente.setNombre(nombre);
        } else if (cliente.getNombre() == null || cliente.getNombre().isBlank()) {
            cliente.setNombre(email);
        }

        int puntosGanados = (int) (montoTotal / 1000);
        int puntosActuales = cliente.getPuntosFidelizacion() != null ? cliente.getPuntosFidelizacion() : 0;
        cliente.setPuntosFidelizacion(puntosActuales + puntosGanados);

        return ClienteResponse.desde(repository.save(cliente));
    }

    private Cliente buscarOFallar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un cliente con id " + id));
    }

    private void aplicarDatos(Cliente cliente, ClienteRequest request) {
        cliente.setNombre(request.nombre());
        cliente.setEmail(request.email());
        cliente.setTelefono(request.telefono());
        cliente.setPuntosFidelizacion(request.puntosFidelizacion() != null ? request.puntosFidelizacion() : 0);
    }
}
