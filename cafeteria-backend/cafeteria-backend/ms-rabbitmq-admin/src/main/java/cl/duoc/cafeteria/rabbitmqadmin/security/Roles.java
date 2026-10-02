package cl.duoc.cafeteria.rabbitmqadmin.security;

/**
 * Nombres de los App Roles definidos en Azure Entra ID (ver docs/EP2_PLAN.md
 * seccion 4 y 7), en MAYUSCULAS y sin prefijo, tal como los deja el claim
 * "roles" del JWT una vez normalizado por el JwtAuthenticationConverter
 * (ver config/SecurityConfig). Se usan en los @PreAuthorize de los
 * controllers via la sintaxis T(...).CONSTANTE para evitar strings sueltos.
 */
public final class Roles {

    public static final String ADMIN = "ADMIN";

    private Roles() {
    }
}
