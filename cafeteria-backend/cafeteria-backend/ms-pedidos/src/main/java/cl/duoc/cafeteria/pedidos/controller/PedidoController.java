package cl.duoc.cafeteria.pedidos.controller;

import cl.duoc.cafeteria.pedidos.model.Pedido;
import cl.duoc.cafeteria.pedidos.repository.PedidoRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoRepository repository;

    public PedidoController(PedidoRepository repository) {
        this.repository = repository;
    }

    // Cualquier usuario autenticado (con un JWT valido) puede listar y consultar
    @GetMapping
    public List<Pedido> listar() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pedido> obtener(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // Matriz de roles (ver docs/EP2_PLAN.md seccion 4): ADMIN = CRUD + cancelar;
    // BARISTA = ver, crear, cambiar estado; CAJERO = ver, crear; GERENTE = ver.
    @PostMapping
    @PreAuthorize("hasAnyAuthority(T(cl.duoc.cafeteria.pedidos.security.Roles).ADMIN, "
            + "T(cl.duoc.cafeteria.pedidos.security.Roles).BARISTA, "
            + "T(cl.duoc.cafeteria.pedidos.security.Roles).CAJERO)")
    public ResponseEntity<Pedido> crear(@Valid @RequestBody Pedido pedido) {
        Pedido guardado = repository.save(pedido);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority(T(cl.duoc.cafeteria.pedidos.security.Roles).ADMIN, "
            + "T(cl.duoc.cafeteria.pedidos.security.Roles).BARISTA)")
    public ResponseEntity<Pedido> actualizar(@PathVariable Long id, @Valid @RequestBody Pedido datos) {
        return repository.findById(id).map(existente -> {
            datos.setId(existente.getId());
            return ResponseEntity.ok(repository.save(datos));
        }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // Cancelar un pedido es, en la practica, eliminarlo: solo ADMIN.
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(T(cl.duoc.cafeteria.pedidos.security.Roles).ADMIN)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
