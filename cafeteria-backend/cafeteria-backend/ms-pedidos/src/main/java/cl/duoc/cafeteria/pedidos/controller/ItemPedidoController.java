package cl.duoc.cafeteria.pedidos.controller;

import cl.duoc.cafeteria.pedidos.dto.ItemResponse;
import cl.duoc.cafeteria.pedidos.repository.ItemPedidoRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Sub-recurso de solo lectura: ahora que PedidoService.crear()/checkout()
// resuelven y guardan los items completos (con nombre y precio tomados de
// ms-productos) al crear el pedido, ya no tiene sentido un POST directo aqui
// que permitiera mandar un item suelto con precio arbitrario desde el
// navegador (ver docs/EP2_PLAN.md seccion 2 y 5). Se elimino el POST que
// existia antes.
@RestController
@RequestMapping("/api/pedidos/{pedidoId}/items")
public class ItemPedidoController {

    private final ItemPedidoRepository repository;

    public ItemPedidoController(ItemPedidoRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<ItemResponse> listarPorPedido(@PathVariable Long pedidoId) {
        return repository.findByPedidoId(pedidoId).stream()
                .map(ItemResponse::desde)
                .toList();
    }
}
