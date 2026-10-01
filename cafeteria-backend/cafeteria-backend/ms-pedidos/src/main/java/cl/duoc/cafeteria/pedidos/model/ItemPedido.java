package cl.duoc.cafeteria.pedidos.model;

import jakarta.persistence.*;

/**
 * Linea de un pedido. nombreProducto y precioUnitario son un snapshot tomado
 * de ms-productos al momento de crear el pedido (ver ProductoClient): nunca
 * se vuelven a consultar despues, para que el historial de ventas no cambie
 * si el producto cambia de nombre/precio mas adelante. precioUnitario jamas
 * se setea con lo que venga del navegador/request (regla de seguridad del
 * proyecto, ver docs/EP2_PLAN.md seccion 2).
 */
@Entity
@Table(name = "items_pedido")
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long pedidoId;
    private Long productoId;
    private String nombreProducto;
    private Integer cantidad;
    private Double precioUnitario;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPedidoId() { return pedidoId; }
    public void setPedidoId(Long pedidoId) { this.pedidoId = pedidoId; }
    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }
    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public Double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(Double precioUnitario) { this.precioUnitario = precioUnitario; }
}
