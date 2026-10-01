package cl.duoc.cafeteria.common.exception;

/**
 * Error transitorio al procesar un mensaje (BD caida, timeout, servicio
 * externo no disponible): se reintenta (nack con requeue) hasta el limite de
 * entregas configurado (x-delivery-limit), luego RabbitMQ lo manda a la DLQ
 * solo (ver docs/EP2_PLAN.md seccion 3.5).
 */
public class RecoverableMessageException extends RuntimeException {

    public RecoverableMessageException(String message) {
        super(message);
    }

    public RecoverableMessageException(String message, Throwable cause) {
        super(message, cause);
    }
}
