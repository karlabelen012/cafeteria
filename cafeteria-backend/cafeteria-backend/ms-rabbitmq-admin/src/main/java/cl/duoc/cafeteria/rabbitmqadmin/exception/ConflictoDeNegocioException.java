package cl.duoc.cafeteria.rabbitmqadmin.exception;

/**
 * Se lanza cuando el recurso ya existe, o cuando se intenta eliminar/purgar
 * un recurso protegido del sistema (ver docs/EP2_PLAN.md seccion 3.8) -> HTTP 409.
 */
public class ConflictoDeNegocioException extends RuntimeException {
    public ConflictoDeNegocioException(String message) {
        super(message);
    }
}
