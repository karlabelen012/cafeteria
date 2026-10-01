package cl.duoc.cafeteria.pagos.messaging.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rutas de mensajeria de ms-pagos (ver docs/EP2_PLAN.md seccion 3). Todos los
 * nombres de exchanges, routing keys y colas vienen del bloque app.rabbitmq
 * de application.yml, mapeado por RabbitProperties.
 *
 * CONSUME:
 *   cafeteria.pedidos.exchange (topic) --[pedido.creado]--> pagos.pedido-creado.queue
 *     -> PedidoCreadoListener (simula la pasarela, ver seccion 3.5)
 *     (fallidos sin reintento -> pagos.pedido-creado.queue.dlq)
 *
 * PRODUCE:
 *   cafeteria.pagos.exchange (direct) --[pago.aprobado]-->  (lo consumen ms-pedidos, ms-inventario, ms-clientes, ms-reportes, ms-notificaciones)
 *   cafeteria.pagos.exchange (direct) --[pago.rechazado]--> (lo consume ms-pedidos)
 */
@Configuration
@EnableConfigurationProperties(RabbitProperties.class)
public class RabbitMQConfig {

    private final RabbitProperties props;

    public RabbitMQConfig(RabbitProperties props) {
        this.props = props;
    }

    @Bean
    public TopicExchange pedidosExchange() {
        return new TopicExchange(props.getExchanges().get("pedidos"), true, false);
    }

    @Bean
    public DirectExchange pagosExchange() {
        return new DirectExchange(props.getExchanges().get("pagos"), true, false);
    }

    @Bean
    public DirectExchange dlx() {
        return new DirectExchange(props.getExchanges().get("dlx"), true, false);
    }

    @Bean
    public Queue pedidoCreadoQueue() {
        String nombre = props.getQueues().get("pedido-creado");
        return QueueBuilder.durable(nombre)
                .quorum()
                .deadLetterExchange(props.getExchanges().get("dlx"))
                .deadLetterRoutingKey(nombre + ".dlq")
                .deliveryLimit(props.getPolicies().getDeliveryLimit())
                .ttl((int) props.getPolicies().getMessageTtlMs())
                .maxLength(props.getPolicies().getMaxLength())
                .build();
    }

    @Bean
    public Queue pedidoCreadoDlq() {
        String nombre = props.getQueues().get("pedido-creado") + ".dlq";
        return QueueBuilder.durable(nombre)
                .quorum()
                .ttl((int) props.getPolicies().getDlqTtlMs())
                .maxLength(props.getPolicies().getMaxLength())
                .build();
    }

    @Bean
    public Binding pedidoCreadoBinding() {
        return BindingBuilder.bind(pedidoCreadoQueue()).to(pedidosExchange())
                .with(props.getRoutingKeys().get("pedido-creado"));
    }

    @Bean
    public Binding pedidoCreadoDlqBinding() {
        return BindingBuilder.bind(pedidoCreadoDlq()).to(dlx())
                .with(props.getQueues().get("pedido-creado") + ".dlq");
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        Logger log = LoggerFactory.getLogger(RabbitTemplate.class);
        template.setMandatory(true);
        template.setConfirmCallback((correlation, ack, cause) -> {
            if (!ack) {
                log.error("RabbitMQ no confirmo el mensaje (publisher confirm), causa: {}", cause);
            }
        });
        template.setReturnsCallback(returned ->
                log.error("Mensaje no enrutable: exchange={}, routingKey={}, respuesta={}",
                        returned.getExchange(), returned.getRoutingKey(), returned.getReplyText()));
        return template;
    }
}
