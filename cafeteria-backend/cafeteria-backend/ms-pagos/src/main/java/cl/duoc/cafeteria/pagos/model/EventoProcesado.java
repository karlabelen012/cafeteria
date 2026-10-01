package cl.duoc.cafeteria.pagos.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Registro de idempotencia: si el eventId de un mensaje ya esta aqui, el
 * mensaje ya fue procesado antes y no debe reprocesarse (ver
 * docs/EP2_PLAN.md seccion 3.5).
 */
@Entity
@Table(name = "eventos_procesados")
public class EventoProcesado {

    @Id
    private UUID eventId;

    private Instant procesadoEn;

    public EventoProcesado() {
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
