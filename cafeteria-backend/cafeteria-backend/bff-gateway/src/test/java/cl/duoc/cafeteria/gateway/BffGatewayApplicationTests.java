package cl.duoc.cafeteria.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.security.oauth2.resourceserver.jwt.issuer-uri=https://login.microsoftonline.com/test-tenant/v2.0"
})
class BffGatewayApplicationTests {

    @Test
    void contextLoads() {
        // Verifica que el gateway levanta con la configuracion de seguridad activa
    }
}
