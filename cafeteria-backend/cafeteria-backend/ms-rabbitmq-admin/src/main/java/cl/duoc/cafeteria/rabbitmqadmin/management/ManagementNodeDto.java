package cl.duoc.cafeteria.rabbitmqadmin.management;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Forma minima de lo que devuelve GET /api/nodes de la API de management de RabbitMQ. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ManagementNodeDto(String name, Boolean running, String type) {
}
