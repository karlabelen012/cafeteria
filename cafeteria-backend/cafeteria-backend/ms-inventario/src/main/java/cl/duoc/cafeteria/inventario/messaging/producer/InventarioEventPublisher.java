package cl.duoc.cafeteria.inventario.messaging.producer;

import cl.duoc.cafeteria.common.evento.StockBajoEvent;

/** Eventos que publica ms-inventario (ver RabbitMQConfig). */
public interface InventarioEventPublisher {

    /** Publica stock.bajo en cafeteria.inventario.exchange (lo consume ms-notificaciones). */
    void publicarStockBajo(StockBajoEvent evento);
}
