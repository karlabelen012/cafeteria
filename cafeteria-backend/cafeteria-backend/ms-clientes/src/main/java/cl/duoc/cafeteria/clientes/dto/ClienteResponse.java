package cl.duoc.cafeteria.clientes.dto;

import cl.duoc.cafeteria.clientes.model.Cliente;

/** Representacion de un cliente que se expone hacia afuera de la API. */
public record ClienteResponse(
        Long id,
        String nombre,
        String email,
        String telefono,
        Integer puntosFidelizacion) {

    public static ClienteResponse desde(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getNombre(),
                cliente.getEmail(),
                cliente.getTelefono(),
                cliente.getPuntosFidelizacion());
    }
}
