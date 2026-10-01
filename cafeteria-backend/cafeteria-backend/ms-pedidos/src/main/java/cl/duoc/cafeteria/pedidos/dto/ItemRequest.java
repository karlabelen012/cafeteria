package cl.duoc.cafeteria.pedidos.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Item que llega desde el request (venta en mostrador o checkout publico).
 * Nunca trae precio ni nombre: eso se resuelve siempre contra ms-productos
 * (ver ProductoClient), nunca se confia en lo que mande el navegador.
 */
public record ItemRequest(

        @NotNull(message = "El producto es obligatorio")
        Long productoId,

        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor a cero")
        Integer cantidad) {
}
