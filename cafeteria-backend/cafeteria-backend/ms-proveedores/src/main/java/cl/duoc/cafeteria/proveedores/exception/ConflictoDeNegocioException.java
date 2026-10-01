package cl.duoc.cafeteria.proveedores.exception;

/**
 * Se lanza ante una violacion de una regla de negocio: duplicados (email/nombre
 * unico), transiciones de estado invalidas, o cualquier conflicto que no es un
 * error de validacion de campos -> HTTP 409.
 */
public class ConflictoDeNegocioException extends RuntimeException {
    public ConflictoDeNegocioException(String message) {
        super(message);
    }
}
