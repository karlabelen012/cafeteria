package cl.duoc.cafeteria.reportes.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Primer dia en que se vio un email de cliente en un pedido.creado, para
 * calcular "clientes nuevos" en el dashboard (ver docs/EP2_PLAN.md seccion 5).
 * Si el email ya estaba aqui, el cliente no es nuevo.
 */
@Entity
@Table(name = "clientes_vistos")
public class ClienteVisto {

    @Id
    private String email;

    private String primeraFecha;

    public ClienteVisto() {
    }

    public ClienteVisto(String email, String primeraFecha) {
        this.email = email;
        this.primeraFecha = primeraFecha;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPrimeraFecha() { return primeraFecha; }
    public void setPrimeraFecha(String primeraFecha) { this.primeraFecha = primeraFecha; }
}
