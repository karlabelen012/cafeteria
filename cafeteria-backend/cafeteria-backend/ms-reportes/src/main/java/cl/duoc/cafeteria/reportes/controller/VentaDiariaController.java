package cl.duoc.cafeteria.reportes.controller;

import cl.duoc.cafeteria.reportes.model.VentaDiaria;
import cl.duoc.cafeteria.reportes.repository.VentaDiariaRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reportes")
public class VentaDiariaController {

    private final VentaDiariaRepository repository;

    public VentaDiariaController(VentaDiariaRepository repository) {
        this.repository = repository;
    }

    // Cualquier usuario autenticado (con un JWT valido) puede listar y consultar
    @GetMapping
    public List<VentaDiaria> listar() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<VentaDiaria> obtener(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // Matriz de roles (ver docs/EP2_PLAN.md seccion 4): ADMIN y GERENTE pueden
    // registrar/corregir ventas diarias (en fases posteriores esto lo hara el
    // consumidor de eventos de RabbitMQ, no un POST manual). CAJERO solo ve
    // el dia (restriccion de visibilidad pendiente para una fase posterior).
    @PostMapping
    @PreAuthorize("hasAnyAuthority(T(cl.duoc.cafeteria.reportes.security.Roles).ADMIN, "
            + "T(cl.duoc.cafeteria.reportes.security.Roles).GERENTE)")
    public ResponseEntity<VentaDiaria> crear(@Valid @RequestBody VentaDiaria ventaDiaria) {
        VentaDiaria guardado = repository.save(ventaDiaria);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority(T(cl.duoc.cafeteria.reportes.security.Roles).ADMIN, "
            + "T(cl.duoc.cafeteria.reportes.security.Roles).GERENTE)")
    public ResponseEntity<VentaDiaria> actualizar(@PathVariable Long id, @Valid @RequestBody VentaDiaria datos) {
        return repository.findById(id).map(existente -> {
            datos.setId(existente.getId());
            return ResponseEntity.ok(repository.save(datos));
        }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(T(cl.duoc.cafeteria.reportes.security.Roles).ADMIN)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
