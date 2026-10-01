package cl.duoc.cafeteria.clientes.dto;

import jakarta.validation.constraints.Positive;

/** Cantidad de puntos de fidelizacion que un cliente quiere canjear. */
public record CanjeRequest(

        @Positive(message = "Los puntos a canjear deben ser un numero positivo")
        int puntos) {
}
