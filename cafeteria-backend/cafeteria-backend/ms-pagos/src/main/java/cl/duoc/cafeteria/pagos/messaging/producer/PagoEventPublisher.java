package cl.duoc.cafeteria.pagos.messaging.producer;

import cl.duoc.cafeteria.common.evento.PagoProcesadoEvent;

/**
 * Productor de los eventos de resultado de pago (pago.aprobado /
 * pago.rechazado). PagoService y PasarelaPagoService dependen de esta
 * interfaz, nunca de RabbitTemplate directamente (ver docs/EP2_PLAN.md
 * seccion 3.7).
 */
public interface PagoEventPublisher {

    void publicar(PagoProcesadoEvent evento);
}
