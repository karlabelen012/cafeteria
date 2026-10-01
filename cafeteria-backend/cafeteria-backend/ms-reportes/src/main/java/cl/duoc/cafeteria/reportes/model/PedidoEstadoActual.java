package cl.duoc.cafeteria.reportes.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Estado vigente de cada pedido (se pisa con cada pedido.estado.actualizado),
 * usado para el conteo "pedidos por estado" del dashboard (ver
 * docs/EP2_PLAN.md seccion 5). fecha es la fecha de CREACION del pedido
 * (yyyy-MM-dd), para poder filtrar "pedidos de hoy por estado".
 */
@Entity
@Table(name = "pedido_estado_actual")
public class PedidoEstadoActual {

    @Id
    private Long pedidoId;

    private String estado;
    private String fecha;

    public PedidoEstadoActual() {
    }

    public PedidoEstadoActual(Long pedidoId, String estado, String fecha) {
        this.pedidoId = pedidoId;
        this.estado = estado;
        this.fecha = fecha;
    }

    public Long getPedidoId() { return pedidoId; }
    public void setPedidoId(Long pedidoId) { this.pedidoId = pedidoId; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
}
