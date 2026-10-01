package cl.duoc.cafeteria.reportes.messaging.config;

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
 * Rutas de mensajeria de ms-reportes (ver docs/EP2_PLAN.md seccion 3). Todos
 * los nombres de exchanges, routing keys y colas vienen del bloque
 * app.rabbitmq de application.yml, mapeado por RabbitProperties.
 *
 * CONSUME:
 *   cafeteria.pagos.exchange (direct) --[pago.aprobado]--> reportes.pago-aprobado.queue
 *     -> PagoAprobadoListener (ventas diarias; fallidos sin reintento -> reportes.pago-aprobado.queue.dlq)
 *   cafeteria.pedidos.exchange (topic) --[pedido.#]--> reportes.pedido-eventos.queue
 *     -> PedidoEventosListener (top productos, pedidos por hora/estado, clientes nuevos;
 *        fallidos sin reintento -> reportes.pedido-eventos.queue.dlq)
 *
 * Este servicio no publica ningun evento: es un modelo de lectura.
 */
@Configuration
@EnableConfigurationProperties(RabbitProperties.class)
public class RabbitMQConfig {

    private final RabbitProperties props;

    public RabbitMQConfig(RabbitProperties props) {
        this.props = props;
    }

    @Bean
    public DirectExchange pagosExchange() {
        return new DirectExchange(props.getExchanges().get("pagos"), true, false);
    }

    @Bean
    public TopicExchange pedidosExchange() {
        return new TopicExchange(props.getExchanges().get("pedidos"), true, false);
    }

    @Bean
    public DirectExchange dlx() {
        return new DirectExchange(props.getExchanges().get("dlx"), true, false);
    }

    @Bean
    public Queue pagoAprobadoQueue() {
        return colaPrincipal("pago-aprobado");
    }

    @Bean
    public Queue pagoAprobadoDlq() {
        return colaDlq("pago-aprobado");
    }

    @Bean
    public Binding pagoAprobadoBinding() {
        return BindingBuilder.bind(pagoAprobadoQueue()).to(pagosExchange())
                .with(props.getRoutingKeys().get("pago-aprobado"));
    }

    @Bean
    public Binding pagoAprobadoDlqBinding() {
        return BindingBuilder.bind(pagoAprobadoDlq()).to(dlx())
                .with(props.getQueues().get("pago-aprobado") + ".dlq");
    }

    @Bean
    public Queue pedidoEventosQueue() {
        return colaPrincipal("pedido-eventos");
    }

    @Bean
    public Queue pedidoEventosDlq() {
        return colaDlq("pedido-eventos");
    }

    @Bean
    public Binding pedidoEventosBinding() {
        return BindingBuilder.bind(pedidoEventosQueue()).to(pedidosExchange())
                .with(props.getRoutingKeys().get("pedido-eventos"));
    }

    @Bean
    public Binding pedidoEventosDlqBinding() {
        return BindingBuilder.bind(pedidoEventosDlq()).to(dlx())
                .with(props.getQueues().get("pedido-eventos") + ".dlq");
    }

    private Queue colaPrincipal(String clave) {
        String nombre = props.getQueues().get(clave);
        return QueueBuilder.durable(nombre)
                .quorum()
                .deadLetterExchange(props.getExchanges().get("dlx"))
                .deadLetterRoutingKey(nombre + ".dlq")
                .deliveryLimit(props.getPolicies().getDeliveryLimit())
                .ttl((int) props.getPolicies().getMessageTtlMs())
                .maxLength(props.getPolicies().getMaxLength())
                .build();
    }

    private Queue colaDlq(String clave) {
        String nombre = props.getQueues().get(clave) + ".dlq";
        return QueueBuilder.durable(nombre)
                .quorum()
                .ttl((int) props.getPolicies().getDlqTtlMs())
                .maxLength(props.getPolicies().getMaxLength())
                .build();
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
