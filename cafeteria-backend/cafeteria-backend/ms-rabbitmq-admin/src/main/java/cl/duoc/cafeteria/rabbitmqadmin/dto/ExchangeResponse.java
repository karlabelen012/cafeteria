package cl.duoc.cafeteria.rabbitmqadmin.dto;

import cl.duoc.cafeteria.rabbitmqadmin.management.ManagementExchangeDto;

public record ExchangeResponse(String name, String type, boolean durable) {

    public static ExchangeResponse desde(ManagementExchangeDto dto) {
        return new ExchangeResponse(dto.name(), dto.type(), Boolean.TRUE.equals(dto.durable()));
    }
}
