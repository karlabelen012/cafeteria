package cl.duoc.cafeteria.pedidos.controller;

import cl.duoc.cafeteria.pedidos.model.ItemPedido;
import cl.duoc.cafeteria.pedidos.repository.ItemPedidoRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Los items son parte del agregado Pedido: viven en el mismo microservicio
// para mantener la consistencia transaccional al crear un pedido completo.
@RestController
@RequestMapping("/api/pedidos/{pedidoId}/items")
public class ItemPedidoController {

    private final ItemPedidoRepository repository;

    public ItemPedidoController(ItemPedidoRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<ItemPedido> listarPorPedido(@PathVariable Long pedidoId) {
        return repository.findByPedidoId(pedidoId);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('BARISTA') or hasAuthority('ADMIN')")
    public ResponseEntity<ItemPedido> agregarItem(@PathVariable Long pedidoId, @Valid @RequestBody ItemPedido item) {
        item.setPedidoId(pedidoId);
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(item));
    }
}
