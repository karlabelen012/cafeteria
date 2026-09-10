package cl.duoc.cafeteria.reportes.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SOLO para desarrollo local, mientras Azure Entra ID todavia no esta
 * configurado. Se activa con: -Dspring.profiles.active=noauth (o
 * SPRING_PROFILES_ACTIVE=noauth), que carga application-noauth.properties
 * con app.security.enabled=false.
 *
 * Deja todos los endpoints abiertos, SIN pedir JWT. Nunca debe quedar
 * activo en la entrega final: el indicador de la pauta exige que el
 * backend SI valide el token.
 */
@Configuration
@ConditionalOnProperty(name = "app.security.enabled", havingValue = "false")
public class NoAuthSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
