package cl.duoc.cafeteria.rabbitmqadmin.dto;

import cl.duoc.cafeteria.rabbitmqadmin.management.ManagementBindingDto;

public record BindingResponse(String source, String destination, String destinationType, String routingKey) {

    public static BindingResponse desde(ManagementBindingDto dto) {
        return new BindingResponse(dto.source(), dto.destination(), dto.destinationType(), dto.routingKey());
    }
}
