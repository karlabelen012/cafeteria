package cl.duoc.cafeteria.proveedores.dto;

import cl.duoc.cafeteria.proveedores.model.Proveedor;

/** Datos de salida de un proveedor, incluyendo su id. */
public record ProveedorResponse(
        Long id,
        String nombre,
        String rut,
        String email,
        String telefono,
        String insumosQueProvee
) {

    public static ProveedorResponse desde(Proveedor proveedor) {
        return new ProveedorResponse(
                proveedor.getId(),
                proveedor.getNombre(),
                proveedor.getRut(),
                proveedor.getEmail(),
                proveedor.getTelefono(),
                proveedor.getInsumosQueProvee()
        );
    }
}
