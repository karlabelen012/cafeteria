package cl.duoc.cafeteria.reportes.model;

import jakarta.persistence.*;

/**
 * Cuenta pedidos creados por hora del dia (ver docs/EP2_PLAN.md seccion 5),
 * para el grafico "Pedidos por hora hoy" y el donut "Pedidos por franja" del
 * dashboard (seccion 6.2).
 */
@Entity
@Table(name = "pedidos_por_hora", uniqueConstraints = @UniqueConstraint(columnNames = {"fecha", "hora"}))
public class PedidosPorHora {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Formato yyyy-MM-dd, igual que VentaDiaria.fecha. */
    private String fecha;
    /** Hora del dia, 0-23. */
    private Integer hora;
    private Integer cantidad;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public Integer getHora() { return hora; }
    public void setHora(Integer hora) { this.hora = hora; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
}
