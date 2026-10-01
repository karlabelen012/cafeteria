package cl.duoc.cafeteria.notificaciones.controller;

import cl.duoc.cafeteria.notificaciones.dto.TicketResponse;
import cl.duoc.cafeteria.notificaciones.service.NotificacionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Tickets/boletas (ver docs/EP2_PLAN.md seccion 5): listado para el staff y
 * la boleta publica que ve el cliente por su codigo de seguimiento. Sin
 * logica de negocio ni de RabbitMQ en el controller.
 */
@RestController
public class TicketController {

    private final NotificacionService notificacionService;

    public TicketController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @GetMapping("/api/notificaciones/tickets")
    public List<TicketResponse> listar() {
        return notificacionService.listarTickets();
    }

    // Publico (ver SecurityConfig: "/api/public/**" permitAll): el cliente ve
    // su boleta por el codigo de seguimiento de la pagina de seguimiento, sin
    // necesitar login ni exponer el id secuencial del ticket.
    @GetMapping("/api/public/tickets/{codigoSeguimiento}")
    public TicketResponse obtenerPorCodigoSeguimiento(@PathVariable String codigoSeguimiento) {
        return notificacionService.obtenerTicketPorCodigoSeguimiento(codigoSeguimiento);
    }
}
