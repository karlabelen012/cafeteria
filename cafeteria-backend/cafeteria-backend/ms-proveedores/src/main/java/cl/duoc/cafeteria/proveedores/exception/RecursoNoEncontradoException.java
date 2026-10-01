package cl.duoc.cafeteria.proveedores.exception;

/** Se lanza cuando se busca por id (u otra clave) y el recurso no existe -> HTTP 404. */
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String message) {
        super(message);
    }
}
