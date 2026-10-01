package cl.duoc.cafeteria.pedidos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Datos de entrada para crear un pedido desde el staff (venta en mostrador,
 * canal=MOSTRADOR). clienteId es opcional (venta sin cliente registrado).
 */
public record PedidoRequest(

        Long clienteId,

        @NotBlank(message = "El nombre del cliente es obligatorio")
        String clienteNombre,

        @NotBlank(message = "El email del cliente es obligatorio")
        @Email(message = "El email debe tener un formato valido")
        String clienteEmail,

        @NotEmpty(message = "El pedido debe tener al menos un item")
        @Valid
        List<ItemRequest> items) {
}
