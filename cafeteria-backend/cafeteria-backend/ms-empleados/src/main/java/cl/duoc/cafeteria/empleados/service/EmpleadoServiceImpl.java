package cl.duoc.cafeteria.empleados.service;

import cl.duoc.cafeteria.empleados.dto.EmpleadoRequest;
import cl.duoc.cafeteria.empleados.dto.EmpleadoResponse;
import cl.duoc.cafeteria.empleados.exception.ConflictoDeNegocioException;
import cl.duoc.cafeteria.empleados.exception.RecursoNoEncontradoException;
import cl.duoc.cafeteria.empleados.model.Empleado;
import cl.duoc.cafeteria.empleados.repository.EmpleadoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EmpleadoServiceImpl implements EmpleadoService {

    private final EmpleadoRepository repository;

    public EmpleadoServiceImpl(EmpleadoRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmpleadoResponse> listar() {
        return repository.findAll().stream()
                .map(this::aResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EmpleadoResponse obtener(Long id) {
        return aResponse(buscarOFallar(id));
    }

    @Override
    public EmpleadoResponse crear(EmpleadoRequest request) {
        if (repository.existsByEmail(request.email())) {
            throw new ConflictoDeNegocioException(
                    "Ya existe un empleado con el email " + request.email());
        }
        Empleado empleado = new Empleado();
        aplicarDatos(empleado, request);
        // Por defecto, un empleado nuevo queda activo salvo que se indique lo contrario.
        empleado.setActivo(request.activo() != null ? request.activo() : Boolean.TRUE);
        return aResponse(repository.save(empleado));
    }

    @Override
    public EmpleadoResponse actualizar(Long id, EmpleadoRequest request) {
        Empleado existente = buscarOFallar(id);
        if (repository.existsByEmailAndIdNot(request.email(), id)) {
            throw new ConflictoDeNegocioException(
                    "Ya existe otro empleado con el email " + request.email());
        }
        aplicarDatos(existente, request);
        existente.setActivo(request.activo() != null ? request.activo() : existente.getActivo());
        return aResponse(repository.save(existente));
    }

    @Override
    public void eliminar(Long id) {
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("No existe el empleado con id " + id);
        }
        // Baja definitiva. Segun docs/EP2_PLAN.md seccion 5, lo preferido es
        // desactivar en vez de borrar; este borrado duro queda reservado
        // exclusivamente a ADMIN (ver EmpleadoController) como excepcion.
        repository.deleteById(id);
    }

    @Override
    public EmpleadoResponse desactivar(Long id) {
        Empleado empleado = buscarOFallar(id);
        empleado.setActivo(Boolean.FALSE);
        return aResponse(repository.save(empleado));
    }

    private Empleado buscarOFallar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el empleado con id " + id));
    }

    private void aplicarDatos(Empleado empleado, EmpleadoRequest request) {
        empleado.setNombre(request.nombre());
        empleado.setEmail(request.email());
        empleado.setRol(request.rol());
        empleado.setFechaIngreso(request.fechaIngreso());
    }

    private EmpleadoResponse aResponse(Empleado empleado) {
        return new EmpleadoResponse(
                empleado.getId(),
                empleado.getNombre(),
                empleado.getEmail(),
                empleado.getRol(),
                empleado.getActivo(),
                empleado.getFechaIngreso());
    }
}
