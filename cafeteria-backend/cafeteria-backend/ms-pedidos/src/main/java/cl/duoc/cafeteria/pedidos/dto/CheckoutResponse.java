package cl.duoc.cafeteria.pedidos.dto;

/** Respuesta del checkout publico: solo el codigo para hacer seguimiento. */
public record CheckoutResponse(String codigoSeguimiento) {
}
