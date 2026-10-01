package cl.duoc.cafeteria.common.exception;

/**
 * Error de negocio o datos invalidos al procesar un mensaje (payload malo,
 * referencia a un recurso que no existe, validacion): no tiene sentido
 * reintentar, el mensaje va directo a la DLQ (ver docs/EP2_PLAN.md seccion 3.5).
 */
public class NonRecoverableMessageException extends RuntimeException {

    public NonRecoverableMessageException(String message) {
        super(message);
    }

    public NonRecoverableMessageException(String message, Throwable cause) {
        super(message, cause);
    }
}
