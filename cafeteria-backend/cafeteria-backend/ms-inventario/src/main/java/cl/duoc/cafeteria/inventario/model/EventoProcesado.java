package cl.duoc.cafeteria.inventario.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Registro de idempotencia para los eventos consumidos por este
 * microservicio (ver docs/EP2_PLAN.md seccion 3.5): si el eventId de un
 * mensaje ya esta aqui, significa que ya fue procesado antes (reintento,
 * redelivery tras un crash antes del ack, duplicado de RabbitMQ) y no debe
 * aplicarse de nuevo.
 */
@Entity
@Table(name = "eventos_procesados")
public class EventoProcesado {

    @Id
    private UUID eventId;

    private Instant procesadoEn;

    protected EventoProcesado() {
        // Requerido por JPA.
    }

    public EventoProcesado(UUID eventId, Instant procesadoEn) {
        this.eventId = eventId;
        this.procesadoEn = procesadoEn;
    }

    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }

    public Instant getProcesadoEn() { return procesadoEn; }
    public void setProcesadoEn(Instant procesadoEn) { this.procesadoEn = procesadoEn; }
}
