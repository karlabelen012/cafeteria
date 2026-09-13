package cl.duoc.cafeteria.gateway.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.reactive.CorsConfigurationSource;

/**
 * SOLO para desarrollo local, mientras Azure Entra ID todavia no esta
 * configurado. Se activa con SPRING_PROFILES_ACTIVE=noauth. Deja pasar todo
 * sin pedir JWT. Nunca debe quedar activo en la entrega final.
 */
@Configuration
@EnableWebFluxSecurity
@ConditionalOnProperty(name = "app.security.enabled", havingValue = "false")
public class NoAuthSecurityConfig {

    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http, CorsConfigurationSource corsConfigurationSource) {
        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .authorizeExchange(exchange -> exchange.anyExchange().permitAll());
        return http.build();
    }
}
