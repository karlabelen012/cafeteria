package cl.duoc.cafeteria.notificaciones.client;

import cl.duoc.cafeteria.common.exception.NonRecoverableMessageException;
import cl.duoc.cafeteria.common.exception.RecoverableMessageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * Cliente HTTP hacia ms-pedidos para obtener el detalle (items, codigo de
 * seguimiento, cliente) de un pedido pagado y armar el ticket (ver
 * docs/EP2_PLAN.md seccion 5): PagoProcesadoEvent solo trae pedidoId, monto y
 * metodoPago, no el detalle de items ni el codigo de seguimiento.
 *
 * LIMITACION CONOCIDA (arquitectonica, no un bug de este archivo): igual que
 * en ms-clientes/client/PedidoClient, ms-pedidos exige un JWT valido para
 * GET /api/pedidos/{id}, pero este cliente se invoca desde un listener de
 * mensajeria (TicketListener), sin usuario logueado ni token. Mientras no
 * exista autenticacion servicio-a-servicio, un 401/403 aqui se trata como
 * error TRANSITORIO (RecoverableMessageException).
 */
@Component
public class PedidoClient {

    private final RestClient restClient;

    public PedidoClient(@Value("${app.clients.pedidos-url:http://localhost:8083}") String pedidosUrl) {
        this.restClient = RestClient.builder().baseUrl(pedidosUrl).build();
    }

    public PedidoDto obtener(Long pedidoId) {
        try {
            return restClient.get()
                    .uri("/api/pedidos/{id}", pedidoId)
                    .retrieve()
                    .body(PedidoDto.class);
        } catch (RestClientResponseException e) {
            HttpStatusCode status = e.getStatusCode();
            if (status.value() == 401 || status.value() == 403) {
                throw new RecoverableMessageException(
                        "ms-pedidos rechazo la autenticacion (HTTP " + status.value() + ") al consultar el pedido "
                                + pedidoId + ". Limitacion conocida: no existe auth servicio-a-servicio todavia, "
                                + "se reintentara.", e);
            }
            if (status.value() == 404) {
                throw new NonRecoverableMessageException(
                        "No existe el pedido " + pedidoId + " en ms-pedidos (HTTP 404)", e);
            }
            throw new RecoverableMessageException(
                    "Error HTTP " + status.value() + " al consultar el pedido " + pedidoId + " en ms-pedidos", e);
        } catch (RestClientException e) {
            throw new RecoverableMessageException(
                    "No se pudo contactar a ms-pedidos para obtener el pedido " + pedidoId, e);
        }
    }
}
