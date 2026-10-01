package cl.duoc.cafeteria.clientes.controller;

import cl.duoc.cafeteria.clientes.dto.CanjeRequest;
import cl.duoc.cafeteria.clientes.dto.ClienteRequest;
import cl.duoc.cafeteria.clientes.dto.ClienteResponse;
import cl.duoc.cafeteria.clientes.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service;
    }

    // Clientes ya no es publico: cualquier usuario autenticado (JWT valido) puede
    // listar y consultar (ver docs/EP2_PLAN.md seccion 5). La exigencia de
    // autenticacion la impone la cadena de filtros en config/SecurityConfig.
    @GetMapping
    public List<ClienteResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public ClienteResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    // Matriz de roles (ver docs/EP2_PLAN.md seccion 4): ADMIN = CRUD;
    // GERENTE = ver; BARISTA = ver; CAJERO = ver, crear, editar.
    @PostMapping
    @PreAuthorize("hasAnyAuthority(T(cl.duoc.cafeteria.clientes.security.Roles).ADMIN, "
            + "T(cl.duoc.cafeteria.clientes.security.Roles).CAJERO)")
    public ResponseEntity<ClienteResponse> crear(@Valid @RequestBody ClienteRequest request) {
        ClienteResponse creado = service.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority(T(cl.duoc.cafeteria.clientes.security.Roles).ADMIN, "
            + "T(cl.duoc.cafeteria.clientes.security.Roles).CAJERO)")
    public ClienteResponse actualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequest request) {
        return service.actualizar(id, request);
    }

    // Solo ADMIN puede eliminar clientes.
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(T(cl.duoc.cafeteria.clientes.security.Roles).ADMIN)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // Canje de puntos de fidelizacion: lo gestionan dia a dia ADMIN y CAJERO.
    @PostMapping("/{id}/canje")
    @PreAuthorize("hasAnyAuthority(T(cl.duoc.cafeteria.clientes.security.Roles).ADMIN, "
            + "T(cl.duoc.cafeteria.clientes.security.Roles).CAJERO)")
    public ClienteResponse canjear(@PathVariable Long id, @Valid @RequestBody CanjeRequest request) {
        return service.canjearPuntos(id, request.puntos());
    }
}
