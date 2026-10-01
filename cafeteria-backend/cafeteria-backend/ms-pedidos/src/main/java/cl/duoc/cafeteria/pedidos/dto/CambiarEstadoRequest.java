package cl.duoc.cafeteria.pedidos.dto;

import jakarta.validation.constraints.NotBlank;

public record CambiarEstadoRequest(

        @NotBlank(message = "El nuevo estado es obligatorio")
        String nuevoEstado) {
}
