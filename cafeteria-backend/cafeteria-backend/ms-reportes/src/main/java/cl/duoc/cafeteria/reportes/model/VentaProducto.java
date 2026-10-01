package cl.duoc.cafeteria.reportes.model;

import jakarta.persistence.*;

/**
 * Modelo de lectura "top productos" (ver docs/EP2_PLAN.md seccion 5): se
 * actualiza con los items de cada pedido.creado (PedidoEventosListener), no
 * con pago.aprobado, porque PagoProcesadoEvent no trae el detalle de items.
 * Esto cuenta productos PEDIDOS por dia, no solo los que terminaron pagados;
 * es una simplificacion deliberada para no tener que ampliar el contrato de
 * PagoProcesadoEvent entre 4 microservicios ya construidos.
 */
@Entity
@Table(name = "ventas_producto", uniqueConstraints = @UniqueConstraint(columnNames = {"fecha", "productoId"}))
public class VentaProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Formato yyyy-MM-dd, igual que VentaDiaria.fecha. */
    private String fecha;
    private Long productoId;
    private String nombreProducto;
    private Integer cantidad;
    private Double monto;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }
    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public Double getMonto() { return monto; }
    public void setMonto(Double monto) { this.monto = monto; }
}
