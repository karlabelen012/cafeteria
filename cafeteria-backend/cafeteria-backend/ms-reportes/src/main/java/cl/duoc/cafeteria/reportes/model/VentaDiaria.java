package cl.duoc.cafeteria.reportes.model;

import jakarta.persistence.*;

// A partir de esta fase (ver docs/EP2_PLAN.md seccion 5), el consumidor de
// eventos de RabbitMQ hace upsert por fecha ("buscar por fecha de hoy, si no
// existe crear en 0, sumar valores"): la restriccion unique en fecha evita
// filas duplicadas para un mismo dia. fecha se mantiene como String libre en
// formato yyyy-MM-dd (no se migra a LocalDate) para no romper el controller
// CRUD manual preexistente ni los valores ya guardados.
@Entity
@Table(name = "ventas_diarias", uniqueConstraints = @UniqueConstraint(columnNames = "fecha"))
public class VentaDiaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String fecha;
    private Double totalVentas;
    private Integer cantidadPedidos;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public Double getTotalVentas() { return totalVentas; }
    public void setTotalVentas(Double totalVentas) { this.totalVentas = totalVentas; }
    public Integer getCantidadPedidos() { return cantidadPedidos; }
    public void setCantidadPedidos(Integer cantidadPedidos) { this.cantidadPedidos = cantidadPedidos; }
}
