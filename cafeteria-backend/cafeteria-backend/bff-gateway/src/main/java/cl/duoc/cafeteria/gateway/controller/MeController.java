package cl.duoc.cafeteria.gateway.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Fuente de verdad del rol: el frontend NUNCA debe decodificar el JWT por su
 * cuenta ni confiar en un selector local. Este endpoint devuelve exactamente
 * lo que el BFF (ya validado el JWT: firma, issuer y audience) leyo del
 * token, para que el menu y los permisos del dashboard se construyan sobre
 * eso (ver docs/EP2_PLAN.md seccion 4).
 */
@RestController
public class MeController {

    public record Yo(String nombre, String email, List<String> roles) {
    }

    @GetMapping("/api/me")
    public Mono<Yo> yo(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return Mono.just(new Yo(null, null, List.of()));
        }

        String nombre = primero(jwt.getClaimAsString("name"), jwt.getClaimAsString("given_name"));
        String email = primero(jwt.getClaimAsString("preferred_username"), jwt.getClaimAsString("email"),
                jwt.getClaimAsString("upn"));

        List<String> roles = jwt.getClaimAsStringList("roles");
        List<String> rolesNormalizados = roles == null
                ? List.of()
                : roles.stream()
                        .filter(Objects::nonNull)
                        .map(r -> r.trim().toUpperCase(Locale.ROOT))
                        .toList();

        return Mono.just(new Yo(nombre, email, rolesNormalizados));
    }

    private String primero(String... valores) {
        for (String valor : valores) {
            if (valor != null && !valor.isBlank()) {
                return valor;
            }
        }
        return null;
    }
}
