package cl.duoc.cafeteria.empleados.controller;

import cl.duoc.cafeteria.empleados.dto.EmpleadoRequest;
import cl.duoc.cafeteria.empleados.dto.EmpleadoResponse;
import cl.duoc.cafeteria.empleados.service.EmpleadoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Expone la ficha de RR.HH. de los empleados (ver docs/EP2_PLAN.md seccion 4:
 * matriz de roles, y seccion 5: reglas de negocio). Este controller no
 * contiene logica de negocio, solo delega en EmpleadoService.
 *
 * Matriz de permisos de este modulo: ADMIN = CRUD completo; GERENTE = solo
 * ver (lista y detalle); BARISTA/CAJERO/BODEGUERO = sin acceso. Es el unico
 * modulo donde incluso el GET queda restringido, porque la informacion es
 * ficha de RR.HH. (datos sensibles de personal), a diferencia de otros
 * modulos donde la lectura queda abierta a cualquier usuario autenticado.
 */
@RestController
@RequestMapping("/api/empleados")
public class EmpleadoController {

    private final EmpleadoService service;

    public EmpleadoController(EmpleadoService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority(T(cl.duoc.cafeteria.empleados.security.Roles).ADMIN, "
            + "T(cl.duoc.cafeteria.empleados.security.Roles).GERENTE)")
    public List<EmpleadoResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority(T(cl.duoc.cafeteria.empleados.security.Roles).ADMIN, "
            + "T(cl.duoc.cafeteria.empleados.security.Roles).GERENTE)")
    public ResponseEntity<EmpleadoResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority(T(cl.duoc.cafeteria.empleados.security.Roles).ADMIN)")
    public ResponseEntity<EmpleadoResponse> crear(@Valid @RequestBody EmpleadoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(T(cl.duoc.cafeteria.empleados.security.Roles).ADMIN)")
    public ResponseEntity<EmpleadoResponse> actualizar(@PathVariable Long id,
            @Valid @RequestBody EmpleadoRequest request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    // Baja definitiva. Segun docs/EP2_PLAN.md seccion 5, lo recomendado es
    // desactivar (ver PATCH /desactivar) en vez de borrar; este endpoint se
    // mantiene como la excepcion explicita reservada solo a ADMIN.
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(T(cl.duoc.cafeteria.empleados.security.Roles).ADMIN)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // Alternativa preferida al borrado duro: marca al empleado como inactivo
    // sin eliminar su ficha historica.
    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasAuthority(T(cl.duoc.cafeteria.empleados.security.Roles).ADMIN)")
    public ResponseEntity<EmpleadoResponse> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(service.desactivar(id));
    }
}
