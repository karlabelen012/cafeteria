package cl.duoc.cafeteria.inventario.service;

import cl.duoc.cafeteria.inventario.dto.RecetaItemRequest;
import cl.duoc.cafeteria.inventario.dto.RecetaItemResponse;
import cl.duoc.cafeteria.inventario.exception.RecursoNoEncontradoException;
import cl.duoc.cafeteria.inventario.model.RecetaItem;
import cl.duoc.cafeteria.inventario.repository.InsumoRepository;
import cl.duoc.cafeteria.inventario.repository.RecetaItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecetaItemServiceImpl implements RecetaItemService {

    private final RecetaItemRepository repository;
    private final InsumoRepository insumoRepository;

    public RecetaItemServiceImpl(RecetaItemRepository repository, InsumoRepository insumoRepository) {
        this.repository = repository;
        this.insumoRepository = insumoRepository;
    }

    @Override
    public List<RecetaItemResponse> listar(Long productoId) {
        List<RecetaItem> items = productoId != null
                ? repository.findByProductoId(productoId)
                : repository.findAll();
        return items.stream().map(this::aResponse).toList();
    }

    @Override
    public RecetaItemResponse obtener(Long id) {
        return aResponse(buscarOFallar(id));
    }

    @Override
    public RecetaItemResponse crear(RecetaItemRequest request) {
        validarInsumoExiste(request.insumoId());
        RecetaItem item = new RecetaItem();
        aplicarDatos(item, request);
        return aResponse(repository.save(item));
    }

    @Override
    public RecetaItemResponse actualizar(Long id, RecetaItemRequest request) {
        RecetaItem item = buscarOFallar(id);
        validarInsumoExiste(request.insumoId());
        aplicarDatos(item, request);
        return aResponse(repository.save(item));
    }

    @Override
    public void eliminar(Long id) {
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("No existe el item de receta con id " + id);
        }
        repository.deleteById(id);
    }

    // El insumo referenciado SI vive en este servicio (a diferencia del
    // productoId), por lo que su existencia se valida aqui antes de guardar.
    private void validarInsumoExiste(Long insumoId) {
        if (!insumoRepository.existsById(insumoId)) {
            throw new RecursoNoEncontradoException("No existe el insumo con id " + insumoId);
        }
    }

    private RecetaItem buscarOFallar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el item de receta con id " + id));
    }

    private void aplicarDatos(RecetaItem item, RecetaItemRequest request) {
        item.setProductoId(request.productoId());
        item.setInsumoId(request.insumoId());
        item.setCantidad(request.cantidad());
    }

    private RecetaItemResponse aResponse(RecetaItem item) {
        return new RecetaItemResponse(item.getId(), item.getProductoId(), item.getInsumoId(), item.getCantidad());
    }
}
