package cl.duoc.cafeteria.inventario.service;

import cl.duoc.cafeteria.inventario.dto.InsumoRequest;
import cl.duoc.cafeteria.inventario.dto.InsumoResponse;
import cl.duoc.cafeteria.inventario.exception.ConflictoDeNegocioException;
import cl.duoc.cafeteria.inventario.exception.RecursoNoEncontradoException;
import cl.duoc.cafeteria.inventario.model.Insumo;
import cl.duoc.cafeteria.inventario.repository.InsumoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InsumoServiceImpl implements InsumoService {

    private final InsumoRepository repository;

    public InsumoServiceImpl(InsumoRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<InsumoResponse> listar() {
        return repository.findAll().stream().map(this::aResponse).toList();
    }

    @Override
    public InsumoResponse obtener(Long id) {
        return aResponse(buscarOFallar(id));
    }

    @Override
    public InsumoResponse crear(InsumoRequest request) {
        if (repository.existsByNombre(request.nombre())) {
            throw new ConflictoDeNegocioException("Ya existe un insumo con el nombre '" + request.nombre() + "'");
        }
        Insumo insumo = new Insumo();
        aplicarDatos(insumo, request);
        return aResponse(repository.save(insumo));
    }

    @Override
    public InsumoResponse actualizar(Long id, InsumoRequest request) {
        Insumo insumo = buscarOFallar(id);
        // Si el nombre cambia, verifica que el nuevo nombre no choque con otro insumo existente.
        if (!insumo.getNombre().equals(request.nombre()) && repository.existsByNombre(request.nombre())) {
            throw new ConflictoDeNegocioException("Ya existe un insumo con el nombre '" + request.nombre() + "'");
        }
        aplicarDatos(insumo, request);
        return aResponse(repository.save(insumo));
    }

    @Override
    public void eliminar(Long id) {
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("No existe el insumo con id " + id);
        }
        repository.deleteById(id);
    }

    @Override
    public List<InsumoResponse> alertas() {
        return repository.findConStockBajo().stream().map(this::aResponse).toList();
    }

    private Insumo buscarOFallar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el insumo con id " + id));
    }

    private void aplicarDatos(Insumo insumo, InsumoRequest request) {
        insumo.setNombre(request.nombre());
        insumo.setUnidadMedida(request.unidadMedida());
        insumo.setStockActual(request.stockActual());
        insumo.setStockMinimo(request.stockMinimo());
    }

    private InsumoResponse aResponse(Insumo insumo) {
        return new InsumoResponse(
                insumo.getId(),
                insumo.getNombre(),
                insumo.getUnidadMedida(),
                insumo.getStockActual(),
                insumo.getStockMinimo());
    }
}
