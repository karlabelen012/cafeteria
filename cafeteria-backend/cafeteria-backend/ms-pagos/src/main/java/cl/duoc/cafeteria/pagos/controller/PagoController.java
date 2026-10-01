package cl.duoc.cafeteria.pagos.controller;

import cl.duoc.cafeteria.pagos.dto.PagoRequest;
import cl.duoc.cafeteria.pagos.dto.PagoResponse;
import cl.duoc.cafeteria.pagos.service.PagoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Solo traduce HTTP <-> DTO <-> PagoService. Sin logica de negocio ni de
 * RabbitMQ (ver docs/EP2_PLAN.md seccion 3.7).
 */
@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private final PagoService service;

    public PagoController(PagoService service) {
        this.service = service;
    }

    // Cualquier usuario autenticado (con un JWT valido) puede listar y consultar
    @GetMapping
    public List<PagoResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PagoResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    // Matriz de roles (ver docs/EP2_PLAN.md seccion 4): ADMIN = CRUD + anular;
    // CAJERO = ver, registrar (solo crear); GERENTE = ver.
    @PostMapping
    @PreAuthorize("hasAnyAuthority(T(cl.duoc.cafeteria.pagos.security.Roles).ADMIN, "
            + "T(cl.duoc.cafeteria.pagos.security.Roles).CAJERO)")
    public ResponseEntity<PagoResponse> crear(@Valid @RequestBody PagoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    // Modificar/eliminar/anular un pago ya registrado es una accion administrativa.
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(T(cl.duoc.cafeteria.pagos.security.Roles).ADMIN)")
    public ResponseEntity<PagoResponse> actualizar(@PathVariable Long id, @Valid @RequestBody PagoRequest request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(T(cl.duoc.cafeteria.pagos.security.Roles).ADMIN)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // Anular un pago (p.ej. un reembolso o un error de caja): solo ADMIN.
    @PatchMapping("/{id}/anular")
    @PreAuthorize("hasAuthority(T(cl.duoc.cafeteria.pagos.security.Roles).ADMIN)")
    public ResponseEntity<PagoResponse> anular(@PathVariable Long id) {
        return ResponseEntity.ok(service.anular(id));
    }
}
