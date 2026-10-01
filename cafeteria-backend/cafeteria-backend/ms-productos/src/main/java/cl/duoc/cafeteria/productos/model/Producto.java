package cl.duoc.cafeteria.productos.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "productos")
public class Producto {

    /** Categorias permitidas para el menu (ver docs/EP2_PLAN.md seccion 5). */
    public static final String CATEGORIAS_REGEX = "^(Bebidas calientes|Bebidas frías|Pastelería|Galletas)$";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(unique = true)
    private String nombre;

    private String descripcion;

    @Positive(message = "El precio debe ser mayor que 0")
    private Double precio;

    @Pattern(regexp = CATEGORIAS_REGEX, message = "La categoria debe ser una de: Bebidas calientes, Bebidas frías, Pastelería, Galletas")
    private String categoria;

    private Boolean disponible = true;

    private String imagenUrl;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public Boolean getDisponible() { return disponible; }
    public void setDisponible(Boolean disponible) { this.disponible = disponible; }
    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
}
