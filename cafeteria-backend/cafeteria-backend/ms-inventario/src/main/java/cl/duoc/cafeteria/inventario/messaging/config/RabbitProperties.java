package cl.duoc.cafeteria.inventario.messaging.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Mapea el bloque app.rabbitmq de application.yml (ver docs/EP2_PLAN.md
 * seccion 3.6). Unica fuente de nombres de exchanges, routing keys y colas:
 * nunca hay strings sueltos en services, controllers ni listeners.
 */
@ConfigurationProperties(prefix = "app.rabbitmq")
public class RabbitProperties {

    private Map<String, String> exchanges = new HashMap<>();
    private Map<String, String> routingKeys = new HashMap<>();
    private Map<String, String> queues = new HashMap<>();
    private Policies policies = new Policies();

    public Map<String, String> getExchanges() {
        return exchanges;
    }

    public void setExchanges(Map<String, String> exchanges) {
        this.exchanges = exchanges;
    }

    public Map<String, String> getRoutingKeys() {
        return routingKeys;
    }

    public void setRoutingKeys(Map<String, String> routingKeys) {
        this.routingKeys = routingKeys;
    }

    public Map<String, String> getQueues() {
        return queues;
    }

    public void setQueues(Map<String, String> queues) {
        this.queues = queues;
    }

    public Policies getPolicies() {
        return policies;
    }

    public void setPolicies(Policies policies) {
        this.policies = policies;
    }

    public static class Policies {
        private int deliveryLimit;
        private long messageTtlMs;
        private int maxLength;
        private long dlqTtlMs;

        public int getDeliveryLimit() {
            return deliveryLimit;
        }

        public void setDeliveryLimit(int deliveryLimit) {
            this.deliveryLimit = deliveryLimit;
        }

        public long getMessageTtlMs() {
            return messageTtlMs;
        }

        public void setMessageTtlMs(long messageTtlMs) {
            this.messageTtlMs = messageTtlMs;
        }

        public int getMaxLength() {
            return maxLength;
        }

        public void setMaxLength(int maxLength) {
            this.maxLength = maxLength;
        }

        public long getDlqTtlMs() {
            return dlqTtlMs;
        }

        public void setDlqTtlMs(long dlqTtlMs) {
            this.dlqTtlMs = dlqTtlMs;
        }
    }
}
