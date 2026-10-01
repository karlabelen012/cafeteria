package cl.duoc.cafeteria.inventario.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Relaciona un producto (de ms-productos) con un insumo (de este servicio)
 * y la cantidad que consume cada vez que se vende una unidad del producto.
 *
 * productoId se guarda como un simple Long (no una relacion JPA): el
 * Producto vive en otra microservicio/base de datos, asi que no hay forma
 * de mapear una FK real. insumoId tambien se guarda como Long plano (y no
 * como @ManyToOne a Insumo) para ser consistente con productoId: ambos son
 * "ids de otra entidad" desde el punto de vista de este modelo, y la
 * existencia de insumoId se valida explicitamente en RecetaItemService.
 */
@Entity
@Table(name = "receta_items")
public class RecetaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El id del producto es obligatorio")
    private Long productoId;

    @NotNull(message = "El id del insumo es obligatorio")
    private Long insumoId;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor que 0")
    private Double cantidad;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }

    public Long getInsumoId() { return insumoId; }
    public void setInsumoId(Long insumoId) { this.insumoId = insumoId; }

    public Double getCantidad() { return cantidad; }
    public void setCantidad(Double cantidad) { this.cantidad = cantidad; }
}
