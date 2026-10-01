package cl.duoc.cafeteria.inventario.controller;

import cl.duoc.cafeteria.inventario.dto.InsumoRequest;
import cl.duoc.cafeteria.inventario.dto.InsumoResponse;
import cl.duoc.cafeteria.inventario.service.InsumoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Solo traduce HTTP <-> DTO <-> InsumoService. Sin logica de negocio (ver
 * docs/EP2_PLAN.md seccion 3.7 / seccion 4, matriz de roles "Inventario
 * (insumos, recetas, movimientos)": ADMIN=CRUD, GERENTE=ver, BARISTA=ver,
 * BODEGUERO=crear/editar/movimientos, CAJERO=sin acceso).
 */
@RestController
@RequestMapping("/api/inventario")
public class InsumoController {

    private final InsumoService service;

    public InsumoController(InsumoService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority(T(cl.duoc.cafeteria.inventario.security.Roles).ADMIN, "
            + "T(cl.duoc.cafeteria.inventario.security.Roles).GERENTE, "
            + "T(cl.duoc.cafeteria.inventario.security.Roles).BARISTA, "
            + "T(cl.duoc.cafeteria.inventario.security.Roles).BODEGUERO)")
    public List<InsumoResponse> listar() {
        return service.listar();
    }

    @GetMapping("/alertas")
    @PreAuthorize("hasAnyAuthority(T(cl.duoc.cafeteria.inventario.security.Roles).ADMIN, "
            + "T(cl.duoc.cafeteria.inventario.security.Roles).GERENTE, "
            + "T(cl.duoc.cafeteria.inventario.security.Roles).BARISTA, "
            + "T(cl.duoc.cafeteria.inventario.security.Roles).BODEGUERO)")
    public List<InsumoResponse> alertas() {
        return service.alertas();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority(T(cl.duoc.cafeteria.inventario.security.Roles).ADMIN, "
            + "T(cl.duoc.cafeteria.inventario.security.Roles).GERENTE, "
            + "T(cl.duoc.cafeteria.inventario.security.Roles).BARISTA, "
            + "T(cl.duoc.cafeteria.inventario.security.Roles).BODEGUERO)")
    public ResponseEntity<InsumoResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority(T(cl.duoc.cafeteria.inventario.security.Roles).ADMIN, "
            + "T(cl.duoc.cafeteria.inventario.security.Roles).BODEGUERO)")
    public ResponseEntity<InsumoResponse> crear(@Valid @RequestBody InsumoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority(T(cl.duoc.cafeteria.inventario.security.Roles).ADMIN, "
            + "T(cl.duoc.cafeteria.inventario.security.Roles).BODEGUERO)")
    public ResponseEntity<InsumoResponse> actualizar(@PathVariable Long id, @Valid @RequestBody InsumoRequest request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(T(cl.duoc.cafeteria.inventario.security.Roles).ADMIN)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
