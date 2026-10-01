package cl.duoc.cafeteria.pagos.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "pagos")
public class Pago {

    /** Metodos de pago soportados (ver docs/EP2_PLAN.md seccion 5). */
    public static final String METODOS_REGEX = "^(EFECTIVO|DEBITO|CREDITO|TRANSFERENCIA)$";

    /** Estados posibles de un pago. */
    public static final String ESTADOS_REGEX = "^(APROBADO|RECHAZADO|ANULADO)$";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El id del pedido es obligatorio")
    private Long pedidoId;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor que 0")
    private Double monto;

    @Pattern(regexp = METODOS_REGEX, message = "El metodo de pago debe ser uno de: EFECTIVO, DEBITO, CREDITO, TRANSFERENCIA")
    private String metodoPago;

    @Pattern(regexp = ESTADOS_REGEX, message = "El estado debe ser uno de: APROBADO, RECHAZADO, ANULADO")
    private String estado;

    // Ultimos 4 digitos de la tarjeta (nullable para EFECTIVO). Regla de
    // seguridad del proyecto: NUNCA se guarda el numero completo de la
    // tarjeta ni el CVV (ver docs/EP2_PLAN.md seccion 5).
    @Pattern(regexp = "^\\d{4}$", message = "ultimos4 debe tener exactamente 4 digitos")
    private String ultimos4;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPedidoId() { return pedidoId; }
    public void setPedidoId(Long pedidoId) { this.pedidoId = pedidoId; }
    public Double getMonto() { return monto; }
    public void setMonto(Double monto) { this.monto = monto; }
    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getUltimos4() { return ultimos4; }
    public void setUltimos4(String ultimos4) { this.ultimos4 = ultimos4; }
}
