package cl.duoc.cafeteria.reportes.service;

import cl.duoc.cafeteria.reportes.dto.DashboardResponse;

import java.time.LocalDate;

/**
 * Calcula el resumen de GET /api/reportes/dashboard (ver docs/EP2_PLAN.md
 * seccion 5) a partir del modelo de lectura construido desde eventos.
 */
public interface DashboardService {

    DashboardResponse calcular(LocalDate desde, LocalDate hasta);
}
