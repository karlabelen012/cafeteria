package cl.duoc.cafeteria.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

// Evita que el arranque del contexto completo dependa de una resolucion de
// red real contra un issuer de prueba (ver docs/EP2_PLAN.md addendum External
// ID / ciamlogin): SecurityConfig.reactiveJwtDecoder() ya no lo necesita
// porque el @MockBean lo reemplaza por completo en este test.
@SpringBootTest
class BffGatewayApplicationTests {

    @MockBean
    private ReactiveJwtDecoder reactiveJwtDecoder;

    @Test
    void contextLoads() {
        // Verifica que el gateway levanta con la configuracion de seguridad activa
    }
}
