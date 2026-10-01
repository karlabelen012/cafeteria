package cl.duoc.cafeteria.inventario.client;

import cl.duoc.cafeteria.common.exception.NonRecoverableMessageException;
import cl.duoc.cafeteria.common.exception.RecoverableMessageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

/**
 * Cliente HTTP hacia ms-pedidos para resolver que productos (y en que
 * cantidad) componen un pedido ya pagado: PagoProcesadoEvent (cafeteria-common)
 * NO trae esa lista, solo pedidoId/pagoId/aprobado/monto/metodoPago, asi que
 * DescuentoStockService necesita consultarla aqui antes de poder aplicar la
 * receta de cada producto (ver docs/EP2_PLAN.md seccion 5).
 *
 * LIMITACION ARQUITECTONICA CONOCIDA: GET /api/pedidos/{id} en ms-pedidos
 * exige un JWT valido (SecurityConfig de ms-pedidos: ".anyRequest().authenticated()",
 * sin excepcion para llamadas internas de otro microservicio), y este
 * proyecto todavia NO implementa ningun mecanismo de autenticacion
 * servicio-a-servicio (p.ej. client_credentials contra Azure Entra ID) que le
 * permita a un consumidor de eventos como este obtener un token propio. Esta
 * llamada sale hoy sin header Authorization, por lo que contra un ms-pedidos
 * con "app.security.enabled=true" (el valor por defecto) SIEMPRE devolvera
 * 401. Ese caso se trata como un error NO recuperable (reintentar un 401/403
 * no va a conseguir autorizacion) para que el evento pago.aprobado caiga a la
 * DLQ de forma visible en vez de reintentarse indefinidamente sin exito. Para
 * que este flujo funcione de punta a punta falta ese mecanismo; mientras
 * tanto, solo funciona de verdad contra un ms-pedidos corriendo con el
 * perfil "noauth".
 */
@Component
public class PedidoClient {

    private final RestClient restClient;

    public PedidoClient(@Value("${app.clients.pedidos-url:http://localhost:8083}") String pedidosUrl) {
        this.restClient = RestClient.builder().baseUrl(pedidosUrl).build();
    }

    public List<ItemDto> obtenerItems(Long pedidoId) {
        try {
            PedidoDto pedido = restClient.get()
                    .uri("/api/pedidos/{id}", pedidoId)
                    .retrieve()
                    .body(PedidoDto.class);

            if (pedido == null || pedido.items() == null) {
                return List.of();
            }
            return pedido.items();
        } catch (HttpClientErrorException ex) {
            HttpStatusCode status = ex.getStatusCode();
            if (status.value() == 401 || status.value() == 403) {
                throw new NonRecoverableMessageException(
                        "ms-pedidos rechazo la consulta del pedido " + pedidoId + " por falta de autorizacion ("
                                + status.value() + "); este proyecto aun no tiene autenticacion "
                                + "servicio-a-servicio para llamadas internas (ver PedidoClient)", ex);
            }
            if (status.value() == 404) {
                throw new NonRecoverableMessageException(
                        "No existe el pedido " + pedidoId + " en ms-pedidos", ex);
            }
            throw new RecoverableMessageException(
                    "Error HTTP al consultar el pedido " + pedidoId + " en ms-pedidos: " + status, ex);
        } catch (RestClientException ex) {
            // Timeout, conexion rechazada, 5xx, ms-pedidos caido, etc: error
            // transitorio, se reintenta (ver AckHandler.nackConReintento).
            throw new RecoverableMessageException(
                    "No se pudo consultar el pedido " + pedidoId + " en ms-pedidos", ex);
        }
    }
}
