package cl.duoc.cafeteria.rabbitmqadmin.dto;

/** Resumen de una DLQ para las alertas del dashboard (ver docs/EP2_PLAN.md seccion 3.8). */
public record DlqResumenResponse(String nombre, long mensajes) {
}
