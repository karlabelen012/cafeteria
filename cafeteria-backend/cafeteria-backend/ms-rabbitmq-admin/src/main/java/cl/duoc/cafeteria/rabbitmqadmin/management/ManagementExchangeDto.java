package cl.duoc.cafeteria.rabbitmqadmin.management;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Forma minima de lo que devuelve GET /api/exchanges de la API de management de RabbitMQ. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ManagementExchangeDto(String name, String type, Boolean durable) {
}
