package cl.duoc.cafeteria.productos.service;

import cl.duoc.cafeteria.productos.dto.ProductoRequest;
import cl.duoc.cafeteria.productos.dto.ProductoResponse;

import java.util.List;

/** Reglas de negocio del menu de productos (ver docs/EP2_PLAN.md seccion 5). */
public interface ProductoService {

    List<ProductoResponse> listar(String categoria, Boolean disponible);

    ProductoResponse obtener(Long id);

    ProductoResponse crear(ProductoRequest request);

    ProductoResponse actualizar(Long id, ProductoRequest request);

    void eliminar(Long id);
}
