package cl.duoc.cafeteria.rabbitmqadmin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapea el bloque app.rabbitmq de application.yml (ver docs/EP2_PLAN.md
 * seccion 3.8): la URL/credenciales de la API HTTP de administracion de
 * RabbitMQ, y la lista de recursos del sistema que esta API nunca debe poder
 * eliminar ni purgar.
 */
@ConfigurationProperties(prefix = "app.rabbitmq")
public class RabbitAdminProperties {

    private Management management = new Management();

    // "protected" es palabra reservada en Java y no puede ser un identificador,
    // asi que la propiedad se llama "protegidos" (app.rabbitmq.protegidos en el
    // yml) en vez de "app.rabbitmq.protected" como en el texto del plan.
    private List<String> protegidos = new ArrayList<>();

    public Management getManagement() {
        return management;
    }

    public void setManagement(Management management) {
        this.management = management;
    }

    public List<String> getProtegidos() {
        return protegidos;
    }

    public void setProtegidos(List<String> protegidos) {
        this.protegidos = protegidos;
    }

    public boolean esProtegido(String nombre) {
        return protegidos.contains(nombre);
    }

    public static class Management {
        private String baseUrl;
        private String username;
        private String password;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
