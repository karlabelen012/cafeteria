package cl.duoc.cafeteria.proveedores.model;

import cl.duoc.cafeteria.proveedores.validation.Rut;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "proveedores")
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El RUT es obligatorio")
    @Rut
    @Column(unique = true)
    private String rut;

    @NotBlank(message = "El telefono es obligatorio")
    private String telefono;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato valido")
    private String email;

    /** Insumos que provee, en texto libre separado por comas (ej: "Cafe en grano, leche"). */
    @Column(length = 1000)
    private String insumosQueProvee;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getRut() { return rut; }
    public void setRut(String rut) { this.rut = rut; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getInsumosQueProvee() { return insumosQueProvee; }
    public void setInsumosQueProvee(String insumosQueProvee) { this.insumosQueProvee = insumosQueProvee; }
}
