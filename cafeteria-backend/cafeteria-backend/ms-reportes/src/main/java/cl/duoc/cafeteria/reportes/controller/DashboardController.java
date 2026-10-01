package cl.duoc.cafeteria.reportes.controller;

import cl.duoc.cafeteria.reportes.dto.DashboardResponse;
import cl.duoc.cafeteria.reportes.service.DashboardService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * GET /api/reportes/dashboard (ver docs/EP2_PLAN.md seccion 5): resumen de
 * KPIs del dashboard, calculado por DashboardService a partir del modelo de
 * lectura construido desde eventos. Sin logica de negocio en el controller.
 */
@RestController
@RequestMapping("/api/reportes")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public DashboardResponse dashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return dashboardService.calcular(desde, hasta);
    }
}
