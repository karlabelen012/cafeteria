package cl.duoc.cafeteria.rabbitmqadmin.exception;

/** Se lanza cuando se busca una cola, exchange o binding que no existe -> HTTP 404. */
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String message) {
        super(message);
    }
}
