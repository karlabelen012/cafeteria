package cl.duoc.cafeteria.productos.service;

import cl.duoc.cafeteria.productos.dto.ProductoRequest;
import cl.duoc.cafeteria.productos.dto.ProductoResponse;
import cl.duoc.cafeteria.productos.exception.ConflictoDeNegocioException;
import cl.duoc.cafeteria.productos.exception.RecursoNoEncontradoException;
import cl.duoc.cafeteria.productos.model.Producto;
import cl.duoc.cafeteria.productos.repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository repository;

    public ProductoServiceImpl(ProductoRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<ProductoResponse> listar(String categoria, Boolean disponible) {
        List<Producto> productos;
        if (categoria != null && disponible != null) {
            productos = repository.findByCategoriaAndDisponible(categoria, disponible);
        } else if (categoria != null) {
            productos = repository.findByCategoria(categoria);
        } else if (disponible != null) {
            productos = repository.findByDisponible(disponible);
        } else {
            productos = repository.findAll();
        }
        return productos.stream().map(this::aResponse).toList();
    }

    @Override
    public ProductoResponse obtener(Long id) {
        return aResponse(buscarOFallar(id));
    }

    @Override
    public ProductoResponse crear(ProductoRequest request) {
        if (repository.existsByNombre(request.nombre())) {
            throw new ConflictoDeNegocioException("Ya existe un producto con el nombre '" + request.nombre() + "'");
        }
        Producto producto = new Producto();
        aplicarDatos(producto, request);
        return aResponse(repository.save(producto));
    }

    @Override
    public ProductoResponse actualizar(Long id, ProductoRequest request) {
        Producto producto = buscarOFallar(id);
        // Si el nombre cambia, verifica que el nuevo nombre no choque con otro producto existente.
        if (!producto.getNombre().equals(request.nombre()) && repository.existsByNombre(request.nombre())) {
            throw new ConflictoDeNegocioException("Ya existe un producto con el nombre '" + request.nombre() + "'");
        }
        aplicarDatos(producto, request);
        return aResponse(repository.save(producto));
    }

    @Override
    public void eliminar(Long id) {
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("No existe el producto con id " + id);
        }
        repository.deleteById(id);
    }

    private Producto buscarOFallar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el producto con id " + id));
    }

    private void aplicarDatos(Producto producto, ProductoRequest request) {
        producto.setNombre(request.nombre());
        producto.setDescripcion(request.descripcion());
        producto.setPrecio(request.precio());
        producto.setCategoria(request.categoria());
        producto.setDisponible(request.disponible() != null ? request.disponible() : Boolean.TRUE);
        producto.setImagenUrl(request.imagenUrl());
    }

    private ProductoResponse aResponse(Producto producto) {
        return new ProductoResponse(
                producto.getId(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.getCategoria(),
                producto.getDisponible(),
                producto.getImagenUrl());
    }
}
