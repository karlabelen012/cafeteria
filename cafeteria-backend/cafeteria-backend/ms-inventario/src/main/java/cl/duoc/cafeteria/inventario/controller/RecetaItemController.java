package cl.duoc.cafeteria.inventario.controller;

import cl.duoc.cafeteria.inventario.dto.RecetaItemRequest;
import cl.duoc.cafeteria.inventario.dto.RecetaItemResponse;
import cl.duoc.cafeteria.inventario.service.RecetaItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Solo traduce HTTP <-> DTO <-> RecetaItemService. Sin logica de negocio.
 * La seccion 4 de docs/EP2_PLAN.md agrupa "recetas" dentro de "Inventario",
 * asi que se usa la MISMA matriz de permisos que InsumoController: lectura
 * para ADMIN/GERENTE/BARISTA/BODEGUERO (CAJERO sin acceso), creacion/edicion
 * para ADMIN+BODEGUERO, eliminacion solo ADMIN.
 */
@RestController
@RequestMapping("/api/recetas")
public class RecetaItemController {

    private final RecetaItemService service;

    public RecetaItemController(RecetaItemService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority(T(cl.duoc.cafeteria.inventario.security.Roles).ADMIN, "
            + "T(cl.duoc.cafeteria.inventario.security.Roles).GERENTE, "
            + "T(cl.duoc.cafeteria.inventario.security.Roles).BARISTA, "
            + "T(cl.duoc.cafeteria.inventario.security.Roles).BODEGUERO)")
    public List<RecetaItemResponse> listar(@RequestParam(required = false) Long productoId) {
        return service.listar(productoId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority(T(cl.duoc.cafeteria.inventario.security.Roles).ADMIN, "
            + "T(cl.duoc.cafeteria.inventario.security.Roles).GERENTE, "
            + "T(cl.duoc.cafeteria.inventario.security.Roles).BARISTA, "
            + "T(cl.duoc.cafeteria.inventario.security.Roles).BODEGUERO)")
    public ResponseEntity<RecetaItemResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority(T(cl.duoc.cafeteria.inventario.security.Roles).ADMIN, "
            + "T(cl.duoc.cafeteria.inventario.security.Roles).BODEGUERO)")
    public ResponseEntity<RecetaItemResponse> crear(@Valid @RequestBody RecetaItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority(T(cl.duoc.cafeteria.inventario.security.Roles).ADMIN, "
            + "T(cl.duoc.cafeteria.inventario.security.Roles).BODEGUERO)")
    public ResponseEntity<RecetaItemResponse> actualizar(@PathVariable Long id, @Valid @RequestBody RecetaItemRequest request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(T(cl.duoc.cafeteria.inventario.security.Roles).ADMIN)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
