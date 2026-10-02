package cl.duoc.cafeteria.rabbitmqadmin.dto;

import cl.duoc.cafeteria.rabbitmqadmin.management.ManagementQueueDto;

public record QueueResponse(String name, String type, long messagesReady, long messagesUnacknowledged,
        int consumers) {

    public static QueueResponse desde(ManagementQueueDto dto) {
        return new QueueResponse(
                dto.name(),
                dto.type(),
                dto.messagesReady() != null ? dto.messagesReady() : 0,
                dto.messagesUnacknowledged() != null ? dto.messagesUnacknowledged() : 0,
                dto.consumers() != null ? dto.consumers() : 0);
    }
}
