package cl.duoc.cafeteria.gateway.filter;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Exige el header "X-Origin-Verify" en toda peticion cuando ORIGIN_VERIFY_SECRET
 * esta definido (ver docs/EP2_PLAN.md seccion 8.2 y prueba S8). AWS API Gateway
 * inyecta ese header con el valor del secreto antes de reenviar al BFF; si
 * alguien le pega directo al puerto 8080 de la EC2 (saltandose el API Gateway),
 * no trae el header y se rechaza con 403.
 *
 * Si ORIGIN_VERIFY_SECRET queda vacio (desarrollo local, Docker Compose sin
 * API Gateway delante), el filtro no exige nada y todo sigue funcionando como
 * hasta ahora.
 */
@Component
public class OriginVerifyGlobalFilter implements GlobalFilter, Ordered {

    private static final String HEADER = "X-Origin-Verify";

    @Value("${ORIGIN_VERIFY_SECRET:}")
    private String originVerifySecret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!StringUtils.hasText(originVerifySecret)) {
            return chain.filter(exchange);
        }

        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        if ("/actuator/health".equals(path)) {
            return chain.filter(exchange);
        }

        String header = request.getHeaders().getFirst(HEADER);
        if (originVerifySecret.equals(header)) {
            return chain.filter(exchange);
        }

        return rechazar(exchange.getResponse());
    }

    private Mono<Void> rechazar(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String cuerpo = """
                {"status":403,"error":"Forbidden","message":"Acceso directo no permitido: debe pasar por el API Gateway"}""";
        DataBuffer buffer = response.bufferFactory().wrap(cuerpo.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
