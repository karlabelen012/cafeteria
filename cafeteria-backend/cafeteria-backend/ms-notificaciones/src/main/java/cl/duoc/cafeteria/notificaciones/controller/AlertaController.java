package cl.duoc.cafeteria.notificaciones.controller;

import cl.duoc.cafeteria.notificaciones.dto.AlertaResponse;
import cl.duoc.cafeteria.notificaciones.service.NotificacionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Alertas para el panel del dashboard (ver docs/EP2_PLAN.md seccion 5 y 6.2):
 * stock bajo, pedido listo y mensajes en DLQ. Sin logica de negocio ni de
 * RabbitMQ en el controller.
 */
@RestController
@RequestMapping("/api/notificaciones/alertas")
public class AlertaController {

    private final NotificacionService notificacionService;

    public AlertaController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @GetMapping
    public List<AlertaResponse> listar() {
        return notificacionService.listarAlertas();
    }

    @PatchMapping("/{id}/leida")
    public AlertaResponse marcarLeida(@PathVariable Long id) {
        return notificacionService.marcarAlertaLeida(id);
    }
}
