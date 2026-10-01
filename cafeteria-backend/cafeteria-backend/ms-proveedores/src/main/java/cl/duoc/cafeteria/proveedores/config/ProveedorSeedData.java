package cl.duoc.cafeteria.proveedores.config;

import cl.duoc.cafeteria.proveedores.model.Proveedor;
import cl.duoc.cafeteria.proveedores.repository.ProveedorRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Carga proveedores de ejemplo para que la demo no empiece vacia (ver
 * docs/EP2_PLAN.md seccion 5). Se activa solo con el perfil "seed",
 * normalmente combinado con "noauth" para desarrollo local:
 * SPRING_PROFILES_ACTIVE=noauth,seed
 *
 * Los RUT usados aqui fueron calculados con el mismo algoritmo modulo 11
 * implementado en validation/RutValidator, por lo que son validos para
 * el validador @Rut (no son RUT de personas reales).
 */
@Component
@Profile("seed")
public class ProveedorSeedData implements CommandLineRunner {

    private final ProveedorRepository repository;

    public ProveedorSeedData(ProveedorRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }
        repository.saveAll(List.of(
                nuevoProveedor("Distribuidora Cafetera del Sur", "76543210-3",
                        "contacto@cafeteradelsur.cl", "+56 9 1234 5678",
                        "Cafe en grano, cafe molido"),
                nuevoProveedor("Lacteos La Vaquita Feliz", "9867654-6",
                        "ventas@lavaquitafeliz.cl", "+56 9 8765 4321",
                        "Leche, crema de leche, mantequilla"),
                nuevoProveedor("Importadora Dulce Andina", "20123456-5",
                        "pedidos@dulceandina.cl", "+56 2 2345 6789",
                        "Azucar, endulzantes, jarabes saborizados"),
                nuevoProveedor("Panaderia y Pasteleria Trigo Dorado", "18765432-7",
                        "contacto@trigodorado.cl", "+56 9 5555 1122",
                        "Pan, croissants, tortas, pasteleria en general")
        ));
    }

    private Proveedor nuevoProveedor(String nombre, String rut, String email, String telefono,
            String insumosQueProvee) {
        Proveedor proveedor = new Proveedor();
        proveedor.setNombre(nombre);
        proveedor.setRut(rut);
        proveedor.setEmail(email);
        proveedor.setTelefono(telefono);
        proveedor.setInsumosQueProvee(insumosQueProvee);
        return proveedor;
    }
}
