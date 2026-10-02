package cl.duoc.cafeteria.rabbitmqadmin.dto;

import cl.duoc.cafeteria.rabbitmqadmin.management.ManagementNodeDto;

public record NodeResponse(String name, boolean running) {

    public static NodeResponse desde(ManagementNodeDto dto) {
        return new NodeResponse(dto.name(), Boolean.TRUE.equals(dto.running()));
    }
}
