package cl.duoc.cafeteria.gateway.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.reactive.CorsConfigurationSource;

/**
 * El BFF es la primera linea de defensa detras de AWS API Gateway: aqui se
 * valida en profundidad el JWT emitido por Azure Entra ID (issuer, audience,
 * firma vigente contra el JWKS) ANTES de reenviar la peticion a cualquier
 * microservicio. Si el token no es valido, el BFF responde 401 sin siquiera
 * contactar al backend de negocio.
 *
 * Activa solo si app.security.enabled=true (default). Para desarrollo local
 * sin Azure configurado, usa el perfil "noauth" (ver NoAuthSecurityConfig).
 */
@Configuration
@EnableWebFluxSecurity
@ConditionalOnProperty(name = "app.security.enabled", havingValue = "true", matchIfMissing = true)
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http, CorsConfigurationSource corsConfigurationSource) {
        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .authorizeExchange(exchange -> exchange
                .pathMatchers(HttpMethod.OPTIONS).permitAll()
                .pathMatchers("/actuator/health").permitAll()
                // Menu publico: un cliente puede ver que se vende sin loguearse.
                // Crear/editar/eliminar productos SIGUE exigiendo JWT + rol ADMIN
                // (ver @PreAuthorize en ProductoController, se valida ahi y en
                // el propio ms-productos).
                .pathMatchers(HttpMethod.GET, "/api/productos", "/api/productos/**").permitAll()
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}));
        return http.build();
    }
}
