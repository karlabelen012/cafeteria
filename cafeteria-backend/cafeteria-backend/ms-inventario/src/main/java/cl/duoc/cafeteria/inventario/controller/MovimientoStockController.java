package cl.duoc.cafeteria.inventario.controller;

import cl.duoc.cafeteria.inventario.dto.MovimientoStockRequest;
import cl.duoc.cafeteria.inventario.dto.MovimientoStockResponse;
import cl.duoc.cafeteria.inventario.service.MovimientoStockService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Solo traduce HTTP <-> DTO <-> MovimientoStockService. Sin logica de
 * negocio (la aplicacion del movimiento sobre el stock vive en el service,
 * ver docs/EP2_PLAN.md seccion 3.7 / regla 4 del agente). Registrar un
 * movimiento es tarea de BODEGUERO (ademas de ADMIN) segun la matriz de
 * roles de la seccion 4; para listar se usa el mismo set de lectura que
 * InsumoController (ADMIN/GERENTE/BARISTA/BODEGUERO, sin CAJERO).
 */
@RestController
@RequestMapping("/api/inventario/{insumoId}/movimientos")
public class MovimientoStockController {

    private final MovimientoStockService service;

    public MovimientoStockController(MovimientoStockService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority(T(cl.duoc.cafeteria.inventario.security.Roles).ADMIN, "
            + "T(cl.duoc.cafeteria.inventario.security.Roles).GERENTE, "
            + "T(cl.duoc.cafeteria.inventario.security.Roles).BARISTA, "
            + "T(cl.duoc.cafeteria.inventario.security.Roles).BODEGUERO)")
    public List<MovimientoStockResponse> listar(@PathVariable Long insumoId) {
        return service.listar(insumoId);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority(T(cl.duoc.cafeteria.inventario.security.Roles).ADMIN, "
            + "T(cl.duoc.cafeteria.inventario.security.Roles).BODEGUERO)")
    public ResponseEntity<MovimientoStockResponse> registrar(@PathVariable Long insumoId,
            @Valid @RequestBody MovimientoStockRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(insumoId, request));
    }
}
