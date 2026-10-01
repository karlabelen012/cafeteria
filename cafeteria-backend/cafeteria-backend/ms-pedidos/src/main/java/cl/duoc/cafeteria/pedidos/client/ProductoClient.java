package cl.duoc.cafeteria.pedidos.client;

import cl.duoc.cafeteria.pedidos.exception.ConflictoDeNegocioException;
import cl.duoc.cafeteria.pedidos.exception.RecursoNoEncontradoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Cliente REST sincrono hacia ms-productos (GET /api/productos/{id}, publico,
 * sin JWT). Se usa durante el checkout/creacion de un pedido para resolver el
 * nombre y precio reales del producto: nunca se confia en lo que mande el
 * navegador/request (ver docs/EP2_PLAN.md seccion 2 y 5).
 *
 * Se usa RestClient (Spring 6 / Boot 3) en vez de RestTemplate, que esta en
 * modo mantenimiento.
 */
@Component
public class ProductoClient {

    private final RestClient restClient;

    public ProductoClient(@Value("${app.clients.productos-url:http://localhost:8081}") String productosUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(productosUrl)
                .build();
    }

    /**
     * Obtiene un producto por id y valida que exista y este disponible.
     *
     * @throws RecursoNoEncontradoException si ms-productos responde 404 (producto inexistente)
     * @throws ConflictoDeNegocioException  si el producto existe pero no esta disponible para la venta
     */
    public ProductoDto obtener(Long productoId) {
        ProductoDto producto;
        try {
            producto = restClient.get()
                    .uri("/api/productos/{id}", productoId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        if (response.getStatusCode().value() == 404) {
                            throw new RecursoNoEncontradoException(
                                    "No existe un producto con id " + productoId);
                        }
                        throw new ConflictoDeNegocioException(
                                "No se pudo validar el producto " + productoId + " en ms-productos");
                    })
                    .body(ProductoDto.class);
        } catch (RecursoNoEncontradoException | ConflictoDeNegocioException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ConflictoDeNegocioException(
                    "No se pudo contactar a ms-productos para validar el producto " + productoId);
        }

        if (producto == null) {
            throw new RecursoNoEncontradoException("No existe un producto con id " + productoId);
        }
        if (Boolean.FALSE.equals(producto.disponible())) {
            throw new ConflictoDeNegocioException(
                    "El producto '" + producto.nombre() + "' no esta disponible actualmente");
        }
        return producto;
    }
}
