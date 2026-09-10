package cl.duoc.cafeteria.empleados;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class EmpleadosApplicationTests {

    @Test
    void contextLoads() {
        // Verifica que el contexto de Spring levanta correctamente
        // (entidades, repositorios y seguridad bien configurados)
    }
}
