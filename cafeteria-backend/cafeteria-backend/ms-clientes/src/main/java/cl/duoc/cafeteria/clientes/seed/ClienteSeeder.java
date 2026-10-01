package cl.duoc.cafeteria.clientes.seed;

import cl.duoc.cafeteria.clientes.model.Cliente;
import cl.duoc.cafeteria.clientes.repository.ClienteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Carga datos de ejemplo para probar el modulo de Clientes sin depender de
 * carga manual. Se activa SOLO con el perfil "seed" (combinable con "noauth",
 * p.ej. SPRING_PROFILES_ACTIVE=noauth,seed) y no hace nada si ya hay datos.
 */
@Component
@Profile("seed")
public class ClienteSeeder implements CommandLineRunner {

    private final ClienteRepository repository;

    public ClienteSeeder(ClienteRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }
        List<Cliente> clientes = List.of(
                nuevoCliente("Camila Rojas", "camila.rojas@example.cl", "+56912345678", 120),
                nuevoCliente("Matias Fuentes", "matias.fuentes@example.cl", "+56923456789", 45),
                nuevoCliente("Javiera Soto", "javiera.soto@example.cl", "+56934567890", 300),
                nuevoCliente("Benjamin Castro", "benjamin.castro@example.cl", "+56945678901", 0),
                nuevoCliente("Francisca Munoz", "francisca.munoz@example.cl", "+56956789012", 480));
        repository.saveAll(clientes);
    }

    private Cliente nuevoCliente(String nombre, String email, String telefono, int puntosFidelizacion) {
        Cliente cliente = new Cliente();
        cliente.setNombre(nombre);
        cliente.setEmail(email);
        cliente.setTelefono(telefono);
        cliente.setPuntosFidelizacion(puntosFidelizacion);
        return cliente;
    }
}
