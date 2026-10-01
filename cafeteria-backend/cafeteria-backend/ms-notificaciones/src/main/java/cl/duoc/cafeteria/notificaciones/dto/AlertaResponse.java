package cl.duoc.cafeteria.notificaciones.dto;

import cl.duoc.cafeteria.notificaciones.model.Alerta;

import java.time.Instant;

public record AlertaResponse(Long id, String tipo, String mensaje, boolean leida, Instant fecha) {

    public static AlertaResponse desde(Alerta alerta) {
        return new AlertaResponse(
                alerta.getId(), alerta.getTipo(), alerta.getMensaje(), alerta.isLeida(), alerta.getFecha());
    }
}
