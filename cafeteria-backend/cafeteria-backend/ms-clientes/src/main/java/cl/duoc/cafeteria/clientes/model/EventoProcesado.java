package cl.duoc.cafeteria.clientes.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Registro de idempotencia para eventos de mensajeria ya procesados (ver
 * docs/EP2_PLAN.md seccion 5): si un eventId ya esta aqui, PagoAprobadoListener
 * lo descarta (solo ack) en vez de volver a sumar puntos de fidelizacion.
 */
@Entity
@Table(name = "eventos_procesados")
public class EventoProcesado {

    @Id
    private UUID eventId;

    private Instant procesadoEn;

    protected EventoProcesado() {
        // JPA
    }

    public EventoProcesado(UUID eventId, Instant procesadoEn) {
        this.eventId = eventId;
        this.procesadoEn = procesadoEn;
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public Instant getProcesadoEn() {
        return procesadoEn;
    }

    public void setProcesadoEn(Instant procesadoEn) {
        this.procesadoEn = procesadoEn;
    }
}
