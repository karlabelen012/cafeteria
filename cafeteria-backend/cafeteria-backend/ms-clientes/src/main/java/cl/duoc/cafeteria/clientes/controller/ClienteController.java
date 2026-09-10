package cl.duoc.cafeteria.clientes.controller;

import cl.duoc.cafeteria.clientes.model.Cliente;
import cl.duoc.cafeteria.clientes.repository.ClienteRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteRepository repository;

    public ClienteController(ClienteRepository repository) {
        this.repository = repository;
    }

    // Cualquier usuario autenticado (con un JWT valido) puede listar y consultar
    @GetMapping
    public List<Cliente> listar() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> obtener(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // Solo roles autorizados (via claim "roles" del JWT) pueden crear/modificar/eliminar
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('ADMIN')")
    public ResponseEntity<Cliente> crear(@Valid @RequestBody Cliente cliente) {
        Cliente guardado = repository.save(cliente);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('ADMIN')")
    public ResponseEntity<Cliente> actualizar(@PathVariable Long id, @Valid @RequestBody Cliente datos) {
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
