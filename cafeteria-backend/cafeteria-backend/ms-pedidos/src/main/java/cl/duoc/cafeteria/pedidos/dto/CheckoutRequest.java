package cl.duoc.cafeteria.pedidos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** Datos de entrada del checkout publico (sin autenticacion, canal=WEB). */
public record CheckoutRequest(

        @NotNull(message = "Los datos del cliente son obligatorios")
        @Valid
        ClienteCheckoutRequest cliente,

        @NotEmpty(message = "El pedido debe tener al menos un item")
        @Valid
        List<ItemRequest> items,

        @NotNull(message = "Los datos de pago son obligatorios")
        @Valid
        PagoCheckoutRequest pago) {
}
