package cl.duoc.cafeteria.pagos.controller;

import cl.duoc.cafeteria.pagos.model.Pago;
import cl.duoc.cafeteria.pagos.repository.PagoRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private final PagoRepository repository;

    public PagoController(PagoRepository repository) {
        this.repository = repository;
    }

    // Cualquier usuario autenticado (con un JWT valido) puede listar y consultar
    @GetMapping
    public List<Pago> listar() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pago> obtener(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // Solo roles autorizados (via claim "roles" del JWT) pueden crear/modificar/eliminar
    @PostMapping
    @PreAuthorize("hasAuthority('CAJERO') or hasAuthority('ADMIN')")
    public ResponseEntity<Pago> crear(@Valid @RequestBody Pago pago) {
        Pago guardado = repository.save(pago);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CAJERO') or hasAuthority('ADMIN')")
    public ResponseEntity<Pago> actualizar(@PathVariable Long id, @Valid @RequestBody Pago datos) {
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
