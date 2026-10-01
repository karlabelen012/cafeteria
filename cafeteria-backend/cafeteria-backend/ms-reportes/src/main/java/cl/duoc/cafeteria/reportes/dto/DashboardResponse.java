package cl.duoc.cafeteria.reportes.dto;

import java.util.List;

/**
 * Respuesta de GET /api/reportes/dashboard (ver docs/EP2_PLAN.md seccion 5).
 */
public record DashboardResponse(
        double ventasHoy,
        int pedidosHoy,
        double ticketPromedio,
        int clientesNuevosHoy,
        double variacionVentasSemanaPorcentaje,
        List<VentaPorDia> ventasUltimos7Dias,
        List<VentaPorDia> ventasSemanaAnterior,
        List<HoraConteo> pedidosPorHoraHoy,
        List<FranjaConteo> pedidosPorFranja,
        List<ProductoTop> topProductos,
        List<EstadoConteo> pedidosPorEstado) {

    public record VentaPorDia(String fecha, double totalVentas, int cantidadPedidos) {
    }

    public record HoraConteo(int hora, int cantidad) {
    }

    public record FranjaConteo(String franja, int cantidad) {
    }

    public record ProductoTop(Long productoId, String nombreProducto, int cantidad, double monto) {
    }

    public record EstadoConteo(String estado, int cantidad) {
    }
}
