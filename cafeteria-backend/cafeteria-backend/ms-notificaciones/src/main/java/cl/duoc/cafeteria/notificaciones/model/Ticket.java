package cl.duoc.cafeteria.notificaciones.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Boleta/ticket generado al aprobarse un pago (ver docs/EP2_PLAN.md seccion
 * 5). El numero correlativo es el id autogenerado: al ser IDENTITY, es
 * secuencial por construccion, no hace falta un contador aparte.
 */
@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long pedidoId;
    private String codigoSeguimiento;
    private String clienteNombre;
    private String clienteEmail;
    private Double total;
    private String metodoPago;
    private Instant fecha;

    @ElementCollection
    @CollectionTable(name = "ticket_items", joinColumns = @JoinColumn(name = "ticket_id"))
    private List<ItemTicket> items = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPedidoId() { return pedidoId; }
    public void setPedidoId(Long pedidoId) { this.pedidoId = pedidoId; }
    public String getCodigoSeguimiento() { return codigoSeguimiento; }
    public void setCodigoSeguimiento(String codigoSeguimiento) { this.codigoSeguimiento = codigoSeguimiento; }
    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }
    public String getClienteEmail() { return clienteEmail; }
    public void setClienteEmail(String clienteEmail) { this.clienteEmail = clienteEmail; }
    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
    public Instant getFecha() { return fecha; }
    public void setFecha(Instant fecha) { this.fecha = fecha; }
    public List<ItemTicket> getItems() { return items; }
    public void setItems(List<ItemTicket> items) { this.items = items; }
}
