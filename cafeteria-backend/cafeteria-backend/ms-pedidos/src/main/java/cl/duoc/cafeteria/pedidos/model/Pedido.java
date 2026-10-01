package cl.duoc.cafeteria.pedidos.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.Instant;

/**
 * Agregado raiz de una venta. El codigoSeguimiento (UUID) es lo unico que se
 * expone en rutas publicas (/api/public/**): el id secuencial jamas se usa
 * ahi, para que nadie pueda ir cambiando el numero y ver pedidos ajenos (ver
 * docs/EP2_PLAN.md seccion 2, hallazgo #7).
 *
 * clienteNombre/clienteEmail son un snapshot tomado al momento de la compra,
 * no una relacion con ms-clientes: el historial de ventas no debe cambiar si
 * despues el cliente actualiza sus datos.
 */
@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, updatable = false)
    private String codigoSeguimiento;

    private Long clienteId;
    private Long empleadoId;

    private String clienteNombre;
    private String clienteEmail;

    // WEB (checkout publico) o MOSTRADOR (venta presencial por el staff).
    private String canal;

    // Maquina de estados validada en service/PedidoServiceImpl, no aqui ni en
    // el controller (ver docs/EP2_PLAN.md seccion 5):
    // PENDIENTE_PAGO -> PAGADO -> EN_PREPARACION -> LISTO -> ENTREGADO
    // PENDIENTE_PAGO -> PAGO_RECHAZADO
    // PAGADO|EN_PREPARACION -> CANCELADO (solo ADMIN)
    @Pattern(regexp = "PENDIENTE_PAGO|PAGADO|PAGO_RECHAZADO|EN_PREPARACION|LISTO|ENTREGADO|CANCELADO",
            message = "Estado de pedido invalido")
    private String estado;

    @PositiveOrZero(message = "El total no puede ser negativo")
    private Double total;

    private Instant fechaCreacion;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigoSeguimiento() { return codigoSeguimiento; }
    public void setCodigoSeguimiento(String codigoSeguimiento) { this.codigoSeguimiento = codigoSeguimiento; }

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    public Long getEmpleadoId() { return empleadoId; }
    public void setEmpleadoId(Long empleadoId) { this.empleadoId = empleadoId; }

    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }
    public String getClienteEmail() { return clienteEmail; }
    public void setClienteEmail(String clienteEmail) { this.clienteEmail = clienteEmail; }

    public String getCanal() { return canal; }
    public void setCanal(String canal) { this.canal = canal; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }

    public Instant getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Instant fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
