package cl.duoc.cafeteria.productos.controller;

import cl.duoc.cafeteria.productos.config.SecurityConfig;
import cl.duoc.cafeteria.productos.dto.ProductoRequest;
import cl.duoc.cafeteria.productos.dto.ProductoResponse;
import cl.duoc.cafeteria.productos.exception.RecursoNoEncontradoException;
import cl.duoc.cafeteria.productos.service.ProductoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Prueba la capa HTTP (DTO + validacion + permisos) con el service mockeado.
 * Importa SecurityConfig (no se escanea solo con @WebMvcTest) para ejercitar
 * el mismo @PreAuthorize y el permitAll de los GET que corren en produccion.
 */
@WebMvcTest(ProductoController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductoService service;

    // Evita que Spring intente descargar el JWKS real del issuer configurado.
    @MockBean
    private JwtDecoder jwtDecoder;

    private ProductoRequest requestValido() {
        return new ProductoRequest("Latte Vainilla", "Rico latte", 3200.0, "Bebidas calientes", true,
                "/productos/latte-vainilla.jpg");
    }

    private ProductoResponse responseValido() {
        return new ProductoResponse(1L, "Latte Vainilla", "Rico latte", 3200.0, "Bebidas calientes", true,
                "/productos/latte-vainilla.jpg");
    }

    @Test
    void listar_sinAutenticacion_retorna200() throws Exception {
        when(service.listar(null, null)).thenReturn(List.of(responseValido()));

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Latte Vainilla"));
    }

    @Test
    void obtener_conIdInexistente_retorna404() throws Exception {
        when(service.obtener(99L)).thenThrow(new RecursoNoEncontradoException("No existe el producto con id 99"));

        mockMvc.perform(get("/api/productos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void crear_conNombreEnBlanco_retorna400ConFieldErrors() throws Exception {
        ProductoRequest invalido = new ProductoRequest("", "desc", 3200.0, "Bebidas calientes", true, null);

        mockMvc.perform(post("/api/productos")
                        .with(user("admin").authorities(() -> "ADMIN"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].campo").value("nombre"));
    }

    @Test
    @WithMockUser(authorities = "BARISTA")
    void crear_sinAutoridadAdmin_retorna403() throws Exception {
        mockMvc.perform(post("/api/productos")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void crear_comoAdmin_retorna201() throws Exception {
        when(service.crear(any(ProductoRequest.class))).thenReturn(responseValido());

        mockMvc.perform(post("/api/productos")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }
}
