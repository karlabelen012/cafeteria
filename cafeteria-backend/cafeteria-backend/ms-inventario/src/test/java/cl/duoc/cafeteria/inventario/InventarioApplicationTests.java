package cl.duoc.cafeteria.inventario;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class InventarioApplicationTests {

    // Evita que el arranque del contexto completo dependa de una resolucion
    // de red real contra el issuer de prueba (ver docs/EP2_PLAN.md addendum
    // External ID / ciamlogin): SecurityConfig.jwtDecoder() ya no lo necesita
    // porque este mock lo reemplaza por completo en este test.
    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void contextLoads() {
        // Verifica que el contexto de Spring levanta correctamente
        // (entidades, repositorios y seguridad bien configurados)
    }
}
