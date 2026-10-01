package cl.duoc.cafeteria.pedidos.controller;

import cl.duoc.cafeteria.pedidos.dto.CheckoutRequest;
import cl.duoc.cafeteria.pedidos.dto.CheckoutResponse;
import cl.duoc.cafeteria.pedidos.dto.SeguimientoResponse;
import cl.duoc.cafeteria.pedidos.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Rutas publicas: sin autenticacion. El BFF ya las expone sin JWT bajo
// /api/public/**, pero este microservicio valida su propio JWT de forma
// independiente (ver config/SecurityConfig), por eso tambien se marcan como
// permitAll ahi. Nunca usar el id secuencial del pedido aqui, solo el
// codigoSeguimiento (ver docs/EP2_PLAN.md seccion 2, hallazgo #7).
@RestController
@RequestMapping("/api/public")
public class PublicoController {

    private final PedidoService service;

    public PublicoController(PedidoService service) {
        this.service = service;
    }

    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponse> checkout(@Valid @RequestBody CheckoutRequest request) {
        CheckoutResponse creado = service.checkout(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @GetMapping("/pedidos/{codigo}")
    public SeguimientoResponse seguimiento(@PathVariable String codigo) {
        return service.obtenerPorCodigoSeguimiento(codigo);
    }
}
