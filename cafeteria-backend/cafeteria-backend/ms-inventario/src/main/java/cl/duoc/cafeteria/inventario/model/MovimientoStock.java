package cl.duoc.cafeteria.inventario.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.Instant;

/**
 * Registro de auditoria de cada cambio de stock de un insumo (ver
 * docs/EP2_PLAN.md seccion 5). "cantidad" es siempre una magnitud positiva;
 * es "tipo" el que determina si el movimiento suma, resta o fija el stock
 * (ver MovimientoStockService para la logica de aplicacion).
 */
@Entity
@Table(name = "movimientos_stock")
public class MovimientoStock {

    /** Tipos de movimiento permitidos (ver docs/EP2_PLAN.md seccion 5). */
    public static final String TIPOS_REGEX = "^(ENTRADA|SALIDA|AJUSTE)$";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El id del insumo es obligatorio")
    private Long insumoId;

    @Pattern(regexp = TIPOS_REGEX, message = "El tipo debe ser uno de: ENTRADA, SALIDA, AJUSTE")
    private String tipo;

    // PositiveOrZero (no Positive) a nivel de entidad: el 0 solo lo usa
    // MovimientoStockServiceImpl.registrarSalidaForzada (ver esa clase) para
    // dejar constancia, como AJUSTE, de que el stock quedo en 0 cuando el
    // descuento automatico por pedido no alcanzaba. El flujo manual/API sigue
    // exigiendo cantidad > 0 via @Positive en MovimientoStockRequest.
    @NotNull(message = "La cantidad es obligatoria")
    @PositiveOrZero(message = "La cantidad debe ser mayor o igual a 0")
    private Double cantidad;

    @NotBlank(message = "El motivo es obligatorio")
    private String motivo;

    // Se fija en el servidor al crear el movimiento, nunca llega desde el request.
    private Instant fecha;

    // Usuario que registro el movimiento (claim "name"/"sub" del JWT, o
    // "sistema" si no hay autenticacion disponible, p.ej. perfil "noauth").
    private String usuario;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getInsumoId() { return insumoId; }
    public void setInsumoId(Long insumoId) { this.insumoId = insumoId; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Double getCantidad() { return cantidad; }
    public void setCantidad(Double cantidad) { this.cantidad = cantidad; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public Instant getFecha() { return fecha; }
    public void setFecha(Instant fecha) { this.fecha = fecha; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
}
