package cl.duoc.cafeteria.rabbitmqadmin.dto;

import java.util.List;

public record ClusterResponse(List<NodeResponse> nodos) {
}
