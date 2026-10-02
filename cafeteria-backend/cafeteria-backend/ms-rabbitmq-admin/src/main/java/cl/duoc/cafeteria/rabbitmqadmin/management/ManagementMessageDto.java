package cl.duoc.cafeteria.rabbitmqadmin.management;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Forma minima de un mensaje leido con POST /api/queues/{vhost}/{name}/get
 * (ver docs/EP2_PLAN.md seccion 3.8, endpoint de reprocesar DLQ). "properties"
 * trae, entre otras cosas, el header x-death con el exchange/routing-key de
 * origen del mensaje antes de caer a la DLQ.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ManagementMessageDto(
        String payload,
        @JsonProperty("payload_encoding") String payloadEncoding,
        Map<String, Object> properties,
        @JsonProperty("routing_key") String routingKey,
        String exchange) {
}
