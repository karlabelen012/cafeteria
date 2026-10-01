package cl.duoc.cafeteria.proveedores.controller;

import cl.duoc.cafeteria.proveedores.dto.ProveedorRequest;
import cl.duoc.cafeteria.proveedores.dto.ProveedorResponse;
import cl.duoc.cafeteria.proveedores.service.ProveedorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Matriz de roles (ver docs/EP2_PLAN.md seccion 4):
 * ADMIN: CRUD completo. GERENTE: solo ver. BODEGUERO: ver, crear y editar.
 * BARISTA/CAJERO: sin acceso a este modulo.
 */
@RestController
@RequestMapping("/api/proveedores")
public class ProveedorController {

    private final ProveedorService service;

    public ProveedorController(ProveedorService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority(T(cl.duoc.cafeteria.proveedores.security.Roles).ADMIN, "
            + "T(cl.duoc.cafeteria.proveedores.security.Roles).GERENTE, "
            + "T(cl.duoc.cafeteria.proveedores.security.Roles).BODEGUERO)")
    public List<ProveedorResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority(T(cl.duoc.cafeteria.proveedores.security.Roles).ADMIN, "
            + "T(cl.duoc.cafeteria.proveedores.security.Roles).GERENTE, "
            + "T(cl.duoc.cafeteria.proveedores.security.Roles).BODEGUERO)")
    public ProveedorResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority(T(cl.duoc.cafeteria.proveedores.security.Roles).ADMIN, "
            + "T(cl.duoc.cafeteria.proveedores.security.Roles).BODEGUERO)")
    public ResponseEntity<ProveedorResponse> crear(@Valid @RequestBody ProveedorRequest request) {
        ProveedorResponse creado = service.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority(T(cl.duoc.cafeteria.proveedores.security.Roles).ADMIN, "
            + "T(cl.duoc.cafeteria.proveedores.security.Roles).BODEGUERO)")
    public ProveedorResponse actualizar(@PathVariable Long id, @Valid @RequestBody ProveedorRequest request) {
        return service.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(T(cl.duoc.cafeteria.proveedores.security.Roles).ADMIN)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
