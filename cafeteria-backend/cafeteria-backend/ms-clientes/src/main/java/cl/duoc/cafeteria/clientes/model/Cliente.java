package cl.duoc.cafeteria.clientes.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato valido")
    @Column(unique = true)
    private String email;

    // Numero movil chileno, con o sin "+", con o sin espacio tras el 56 (ej: +56912345678 o 56 912345678)
    @Pattern(regexp = "^\\+?56 ?9\\d{8}$", message = "El telefono debe ser un numero movil chileno valido (ej: +56912345678)")
    private String telefono;

    @Min(value = 0, message = "Los puntos de fidelizacion no pueden ser negativos")
    private Integer puntosFidelizacion;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public Integer getPuntosFidelizacion() { return puntosFidelizacion; }
    public void setPuntosFidelizacion(Integer puntosFidelizacion) { this.puntosFidelizacion = puntosFidelizacion; }
}
