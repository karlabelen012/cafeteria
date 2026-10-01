package cl.duoc.cafeteria.proveedores.service;

import cl.duoc.cafeteria.proveedores.dto.ProveedorRequest;
import cl.duoc.cafeteria.proveedores.dto.ProveedorResponse;
import cl.duoc.cafeteria.proveedores.exception.ConflictoDeNegocioException;
import cl.duoc.cafeteria.proveedores.exception.RecursoNoEncontradoException;
import cl.duoc.cafeteria.proveedores.model.Proveedor;
import cl.duoc.cafeteria.proveedores.repository.ProveedorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProveedorServiceImpl implements ProveedorService {

    private final ProveedorRepository repository;

    public ProveedorServiceImpl(ProveedorRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProveedorResponse> listar() {
        return repository.findAll().stream()
                .map(ProveedorResponse::desde)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProveedorResponse obtener(Long id) {
        return ProveedorResponse.desde(buscarOFallar(id));
    }

    @Override
    public ProveedorResponse crear(ProveedorRequest request) {
        if (repository.existsByRut(request.rut())) {
            throw new ConflictoDeNegocioException("Ya existe un proveedor con el RUT " + request.rut());
        }

        Proveedor proveedor = new Proveedor();
        aplicarDatos(proveedor, request);

        return ProveedorResponse.desde(repository.save(proveedor));
    }

    @Override
    public ProveedorResponse actualizar(Long id, ProveedorRequest request) {
        Proveedor proveedor = buscarOFallar(id);

        repository.findByRut(request.rut())
                .filter(otro -> !otro.getId().equals(id))
                .ifPresent(otro -> {
                    throw new ConflictoDeNegocioException("Ya existe un proveedor con el RUT " + request.rut());
                });

        aplicarDatos(proveedor, request);
        return ProveedorResponse.desde(repository.save(proveedor));
    }

    @Override
    public void eliminar(Long id) {
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("No existe un proveedor con id " + id);
        }
        repository.deleteById(id);
    }

    private Proveedor buscarOFallar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un proveedor con id " + id));
    }

    private void aplicarDatos(Proveedor proveedor, ProveedorRequest request) {
        proveedor.setNombre(request.nombre());
        proveedor.setRut(request.rut());
        proveedor.setEmail(request.email());
        proveedor.setTelefono(request.telefono());
        proveedor.setInsumosQueProvee(request.insumosQueProvee());
    }
}
