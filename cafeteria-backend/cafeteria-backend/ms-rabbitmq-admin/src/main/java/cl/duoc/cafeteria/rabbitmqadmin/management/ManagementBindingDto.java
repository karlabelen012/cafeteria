package cl.duoc.cafeteria.rabbitmqadmin.management;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Forma minima de lo que devuelve GET /api/bindings de la API de management de RabbitMQ. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ManagementBindingDto(
        String source,
        String destination,
        @JsonProperty("destination_type") String destinationType,
        @JsonProperty("routing_key") String routingKey) {
}
