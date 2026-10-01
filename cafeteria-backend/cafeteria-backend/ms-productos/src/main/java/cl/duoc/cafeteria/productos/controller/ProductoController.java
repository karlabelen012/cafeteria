package cl.duoc.cafeteria.productos.controller;

import cl.duoc.cafeteria.productos.dto.ProductoRequest;
import cl.duoc.cafeteria.productos.dto.ProductoResponse;
import cl.duoc.cafeteria.productos.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Solo traduce HTTP <-> DTO <-> ProductoService. Sin logica de negocio
 * (ver docs/EP2_PLAN.md seccion 3.7 / regla 4 del agente).
 */
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService service;

    public ProductoController(ProductoService service) {
        this.service = service;
    }

    // Publico (permitAll en SecurityConfig): tienda y cualquier usuario
    // autenticado pueden listar y filtrar por categoria/disponibilidad.
    @GetMapping
    public List<ProductoResponse> listar(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) Boolean disponible) {
        return service.listar(categoria, disponible);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    // Solo ADMIN puede crear/editar/eliminar productos (ver docs/EP2_PLAN.md
    // seccion 4, matriz de roles: "Menu (productos)").
    @PostMapping
    @PreAuthorize("hasAuthority(T(cl.duoc.cafeteria.productos.security.Roles).ADMIN)")
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(T(cl.duoc.cafeteria.productos.security.Roles).ADMIN)")
    public ResponseEntity<ProductoResponse> actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequest request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(T(cl.duoc.cafeteria.productos.security.Roles).ADMIN)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
