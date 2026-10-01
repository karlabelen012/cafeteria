package cl.duoc.cafeteria.empleados.config;

import cl.duoc.cafeteria.empleados.model.Empleado;
import cl.duoc.cafeteria.empleados.repository.EmpleadoRepository;
import cl.duoc.cafeteria.empleados.security.Roles;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Carga datos de ejemplo para que la demo no empiece vacia (ver
 * docs/EP2_PLAN.md secciones 5 y 7). Se activa solo con el perfil "seed",
 * normalmente combinado con "noauth" para desarrollo local:
 * SPRING_PROFILES_ACTIVE=noauth,seed
 *
 * Los emails siguen la convencion de usuarios de prueba de la seccion 7
 * (uno por cada App Role de Azure Entra ID).
 */
@Component
@Profile("seed")
public class EmpleadoSeedData implements CommandLineRunner {

    private final EmpleadoRepository repository;

    public EmpleadoSeedData(EmpleadoRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }
        repository.saveAll(List.of(
                nuevoEmpleado("Admin General", "admin@cafegestion360.cl", Roles.ADMIN,
                        LocalDate.of(2022, 1, 10)),
                nuevoEmpleado("Gerencia Local", "gerente@cafegestion360.cl", Roles.GERENTE,
                        LocalDate.of(2022, 3, 1)),
                nuevoEmpleado("Barista Turno", "barista@cafegestion360.cl", Roles.BARISTA,
                        LocalDate.of(2023, 2, 15)),
                nuevoEmpleado("Cajero Turno", "cajero@cafegestion360.cl", Roles.CAJERO,
                        LocalDate.of(2023, 5, 20)),
                nuevoEmpleado("Bodeguero Turno", "bodega@cafegestion360.cl", Roles.BODEGUERO,
                        LocalDate.of(2023, 7, 3))
        ));
    }

    private Empleado nuevoEmpleado(String nombre, String email, String rol, LocalDate fechaIngreso) {
        Empleado empleado = new Empleado();
        empleado.setNombre(nombre);
        empleado.setEmail(email);
        empleado.setRol(rol);
        empleado.setActivo(Boolean.TRUE);
        empleado.setFechaIngreso(fechaIngreso);
        return empleado;
    }
}
