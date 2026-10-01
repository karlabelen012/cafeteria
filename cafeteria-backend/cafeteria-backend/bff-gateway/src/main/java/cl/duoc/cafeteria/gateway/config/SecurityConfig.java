package cl.duoc.cafeteria.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

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
                // Rutas publicas de la tienda (checkout, seguimiento de pedidos):
                // se agregan en fases posteriores en ms-pedidos/ms-notificaciones,
                // pero la regla de acceso ya queda declarada aqui.
                .pathMatchers("/api/public/**").permitAll()
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );
        return http.build();
    }

    // Mismo motivo que en cada microservicio (ver docs/EP2_PLAN.md addendum
    // External ID / ciamlogin): se arma el ReactiveJwtDecoder a mano para
    // decidir en Java si usar jwk-set-uri o issuer-uri, en vez de dejar que
    // Boot registre dos beans ambiguos cuando jwk-set-uri queda vacio.
    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder(
            @Value("${AZURE_ISSUER_URI:https://login.microsoftonline.com/<TENANT_ID>/v2.0}") String issuerUri,
            @Value("${AZURE_JWK_SET_URI:}") String jwkSetUri,
            @Value("${AZURE_AUDIENCES:api://cafeteria-backend}") String audiencesRaw) {

        NimbusReactiveJwtDecoder decoder = StringUtils.hasText(jwkSetUri)
                ? NimbusReactiveJwtDecoder.withJwkSetUri(jwkSetUri).build()
                : NimbusReactiveJwtDecoder.withIssuerLocation(issuerUri).build();

        List<String> audiencias = Arrays.stream(audiencesRaw.split(","))
                .map(String::trim)
                .filter(a -> !a.isEmpty())
                .toList();
        OAuth2TokenValidator<Jwt> validadorAudiencia = new JwtClaimValidator<List<String>>(
                "aud", aud -> aud != null && aud.stream().anyMatch(audiencias::contains));
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(issuerUri), validadorAudiencia));

        return decoder;
    }

    // Mismo conversor de roles que los microservicios (ver docs/EP2_PLAN.md
    // seccion 7, paso 4): lee el claim "roles", lo normaliza a MAYUSCULAS y
    // sin prefijo. El BFF no usa estas authorities para autorizar rutas (eso
    // lo hace cada microservicio con @PreAuthorize), pero las necesita para
    // poder exponerlas tal cual en GET /api/me.
    private Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("roles");
        authoritiesConverter.setAuthorityPrefix("");

        Converter<Jwt, Collection<GrantedAuthority>> rolesEnMayusculas = jwt -> {
            Collection<GrantedAuthority> authorities = authoritiesConverter.convert(jwt);
            if (authorities == null) {
                return List.of();
            }
            return authorities.stream()
                    .map(authority -> (GrantedAuthority) new SimpleGrantedAuthority(
                            authority.getAuthority().trim().toUpperCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        };

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(rolesEnMayusculas);
        return new ReactiveJwtAuthenticationConverterAdapter(converter);
    }
}
