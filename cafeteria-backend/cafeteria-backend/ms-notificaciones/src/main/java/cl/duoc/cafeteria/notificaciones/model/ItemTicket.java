package cl.duoc.cafeteria.notificaciones.model;

import jakarta.persistence.Embeddable;

/** Linea de detalle de un Ticket (ver docs/EP2_PLAN.md seccion 5). */
@Embeddable
public class ItemTicket {

    private Long productoId;
    private String nombreProducto;
    private Integer cantidad;
    private Double precioUnitario;

    public ItemTicket() {
    }

    public ItemTicket(Long productoId, String nombreProducto, Integer cantidad, Double precioUnitario) {
        this.productoId = productoId;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }
    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public Double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(Double precioUnitario) { this.precioUnitario = precioUnitario; }
}
