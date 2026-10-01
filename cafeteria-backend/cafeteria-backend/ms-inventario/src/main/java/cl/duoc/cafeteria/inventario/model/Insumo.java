package cl.duoc.cafeteria.inventario.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

@Entity
@Table(name = "insumos")
public class Insumo {

    /** Unidades de medida permitidas para un insumo (ver docs/EP2_PLAN.md seccion 5). */
    public static final String UNIDADES_REGEX = "^(g|ml|unidad)$";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(unique = true)
    private String nombre;

    @Pattern(regexp = UNIDADES_REGEX, message = "La unidad de medida debe ser una de: g, ml, unidad")
    private String unidadMedida;

    @NotNull(message = "El stock actual es obligatorio")
    @PositiveOrZero(message = "El stock actual debe ser mayor o igual a 0")
    private Double stockActual;

    @NotNull(message = "El stock minimo es obligatorio")
    @PositiveOrZero(message = "El stock minimo debe ser mayor o igual a 0")
    private Double stockMinimo;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }
    public Double getStockActual() { return stockActual; }
    public void setStockActual(Double stockActual) { this.stockActual = stockActual; }
    public Double getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(Double stockMinimo) { this.stockMinimo = stockMinimo; }
}
