package cl.duoc.cafeteria.rabbitmqadmin.config;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.client.RestClient;

/**
 * Beans de bajo nivel para hablar con RabbitMQ (ver docs/EP2_PLAN.md seccion
 * 3.8): RabbitAdmin (protocolo AMQP, via Spring AMQP) para crear/eliminar
 * colas, exchanges y bindings; un RestClient con autenticacion basica para la
 * API HTTP de administracion (puerto 15672) que se usa para listar y ver el
 * estado del cluster. Todo esto queda encapsulado en RabbitAdminService: el
 * controller no conoce ninguno de los dos.
 */
@Configuration
@EnableConfigurationProperties(RabbitAdminProperties.class)
public class RabbitAdminConfig {

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    // Usado solo para reprocesar mensajes de una DLQ hacia su exchange de
    // origen (ver RabbitAdminServiceImpl.reprocesarDlq): reenvia el payload
    // crudo, sin convertidor JSON propio (cada mensaje ya viene serializado
    // por su productor original).
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        return new RabbitTemplate(connectionFactory);
    }

    @Bean
    public RestClient rabbitManagementRestClient(RabbitAdminProperties props) {
        RabbitAdminProperties.Management management = props.getManagement();
        return RestClient.builder()
                .baseUrl(management.getBaseUrl())
                .requestInterceptor(new BasicAuthenticationInterceptor(
                        management.getUsername(), management.getPassword()))
                .build();
    }
}
