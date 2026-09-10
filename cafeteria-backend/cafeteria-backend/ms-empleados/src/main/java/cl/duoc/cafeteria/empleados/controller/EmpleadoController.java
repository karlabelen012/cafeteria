package cl.duoc.cafeteria.empleados.controller;

import cl.duoc.cafeteria.empleados.model.Empleado;
import cl.duoc.cafeteria.empleados.repository.EmpleadoRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empleados")
public class EmpleadoController {

    private final EmpleadoRepository repository;

    public EmpleadoController(EmpleadoRepository repository) {
        this.repository = repository;
    }

    // Cualquier usuario autenticado (con un JWT valido) puede listar y consultar
    @GetMapping
    public List<Empleado> listar() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Empleado> obtener(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // Solo roles autorizados (via claim "roles" del JWT) pueden crear/modificar/eliminar
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('ADMIN')")
    public ResponseEntity<Empleado> crear(@Valid @RequestBody Empleado empleado) {
        Empleado guardado = repository.save(empleado);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('ADMIN')")
    public ResponseEntity<Empleado> actualizar(@PathVariable Long id, @Valid @RequestBody Empleado datos) {
        return repository.findById(id).map(existente -> {
            datos.setId(existente.getId());
            return ResponseEntity.ok(repository.save(datos));
        }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
