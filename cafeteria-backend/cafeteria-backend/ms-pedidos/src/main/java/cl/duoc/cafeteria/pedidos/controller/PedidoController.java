package cl.duoc.cafeteria.pedidos.controller;

import cl.duoc.cafeteria.pedidos.dto.CambiarEstadoRequest;
import cl.duoc.cafeteria.pedidos.dto.PedidoRequest;
import cl.duoc.cafeteria.pedidos.dto.PedidoResponse;
import cl.duoc.cafeteria.pedidos.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Endpoints de staff (venta en mostrador y gestion del ciclo de vida del
// pedido). El checkout publico vive en PublicoController (/api/public/**).
@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService service;

    public PedidoController(PedidoService service) {
        this.service = service;
    }

    // Cualquier usuario autenticado (con un JWT valido) puede listar y consultar
    // (la exigencia de autenticacion la impone config/SecurityConfig).
    @GetMapping
    public List<PedidoResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public PedidoResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    // Matriz de roles (ver docs/EP2_PLAN.md seccion 4): ADMIN = CRUD + cancelar;
    // GERENTE = ver; BARISTA = ver, crear, cambiar estado; CAJERO = ver, crear.
    @PostMapping
    @PreAuthorize("hasAnyAuthority(T(cl.duoc.cafeteria.pedidos.security.Roles).ADMIN, "
            + "T(cl.duoc.cafeteria.pedidos.security.Roles).BARISTA, "
            + "T(cl.duoc.cafeteria.pedidos.security.Roles).CAJERO)")
    public ResponseEntity<PedidoResponse> crear(@Valid @RequestBody PedidoRequest request) {
        PedidoResponse creado = service.crear(request, "MOSTRADOR");
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    // Avanza (o rechaza/cancela) el estado de un pedido segun la maquina de
    // estados validada en el service. ADMIN puede cualquier transicion valida,
    // incluyendo CANCELADO; BARISTA puede avanzar el flujo normal pero NO
    // cancelar (eso es exclusivo de ADMIN, ver matriz de roles).
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAuthority(T(cl.duoc.cafeteria.pedidos.security.Roles).ADMIN) "
            + "or (hasAuthority(T(cl.duoc.cafeteria.pedidos.security.Roles).BARISTA) "
            + "and #request.nuevoEstado() != T(cl.duoc.cafeteria.pedidos.service.MaquinaEstadosPedido).CANCELADO)")
    public PedidoResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambiarEstadoRequest request) {
        return service.cambiarEstado(id, request.nuevoEstado());
    }

    // Cancelar un pedido: solo ADMIN (ver matriz de roles). Ya no borra el
    // registro (se pierde el historial de ventas); internamente pasa el
    // pedido a estado CANCELADO via la maquina de estados.
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(T(cl.duoc.cafeteria.pedidos.security.Roles).ADMIN)")
    public PedidoResponse cancelar(@PathVariable Long id) {
        return service.cancelar(id);
    }
}
