package cl.duoc.cafeteria.gateway.filter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

class OriginVerifyGlobalFilterTest {

    private final OriginVerifyGlobalFilter filtro = new OriginVerifyGlobalFilter();
    private final GatewayFilterChain chainQuePasa = exchange -> Mono.empty();

    @Test
    void dejaPasarTodoCuandoElSecretoEstaVacio() {
        ReflectionTestUtils.setField(filtro, "originVerifySecret", "");
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/pedidos"));

        filtro.filter(exchange, chainQuePasa).block();

        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    void rechazaConForbiddenSiFaltaElHeader() {
        ReflectionTestUtils.setField(filtro, "originVerifySecret", "secreto-123");
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/pedidos"));

        filtro.filter(exchange, chainQuePasa).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void dejaPasarSiElHeaderCoincideConElSecreto() {
        ReflectionTestUtils.setField(filtro, "originVerifySecret", "secreto-123");
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/pedidos").header("X-Origin-Verify", "secreto-123"));

        filtro.filter(exchange, chainQuePasa).block();

        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    void elHealthCheckQuedaExentoAunqueFalteElHeader() {
        ReflectionTestUtils.setField(filtro, "originVerifySecret", "secreto-123");
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/actuator/health"));

        filtro.filter(exchange, chainQuePasa).block();

        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }
}
