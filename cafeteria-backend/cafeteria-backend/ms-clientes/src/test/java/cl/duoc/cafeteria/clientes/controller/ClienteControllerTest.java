package cl.duoc.cafeteria.clientes.controller;

import cl.duoc.cafeteria.clientes.dto.CanjeRequest;
import cl.duoc.cafeteria.clientes.dto.ClienteRequest;
import cl.duoc.cafeteria.clientes.dto.ClienteResponse;
import cl.duoc.cafeteria.clientes.exception.ConflictoDeNegocioException;
import cl.duoc.cafeteria.clientes.exception.RecursoNoEncontradoException;
import cl.duoc.cafeteria.clientes.service.ClienteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de la capa web de Clientes: delega en ClienteService (mockeado) y
 * verifica codigos de estado, forma del error (GlobalExceptionHandler) y la
 * matriz de autorizacion via @PreAuthorize (ver docs/EP2_PLAN.md seccion 4).
 *
 * Los filtros servlet se desactivan (addFilters = false): lo que se prueba
 * aqui es seguridad a nivel de metodo (@PreAuthorize), no la cadena HTTP de
 * OAuth2 Resource Server (eso requeriria un JwtDecoder real / SpringBootTest).
 */
@WebMvcTest(ClienteController.class)
@Import(ClienteControllerTest.MethodSecurityConfig.class)
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClienteService service;

    // Replica el estilo stateless de la app real (sin CSRF, sin formLogin): aqui
    // solo nos interesa que @EnableMethodSecurity evalue los @PreAuthorize de
    // ClienteController usando el Authentication que deja @WithMockUser, no la
    // cadena HTTP de OAuth2 Resource Server (eso exigiria un JwtDecoder real).
    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityConfig {

        @Bean
        SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            http.csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }

    private ClienteResponse respuestaDeEjemplo() {
        return new ClienteResponse(1L, "Camila Rojas", "camila.rojas@example.cl", "+56912345678", 120);
    }

    @Test
    @WithMockUser
    void listar_devuelve200ConLaLista() throws Exception {
        when(service.listar()).thenReturn(List.of(respuestaDeEjemplo()));

        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("camila.rojas@example.cl"));
    }

    @Test
    @WithMockUser
    void obtener_idInexistente_devuelve404() throws Exception {
        when(service.obtener(99L)).thenThrow(new RecursoNoEncontradoException("No existe un cliente con id 99"));

        mockMvc.perform(get("/api/clientes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("No existe un cliente con id 99"));
    }

    @Test
    @WithMockUser(authorities = "CAJERO")
    void crear_emailInvalido_devuelve400ConFieldErrors() throws Exception {
        String cuerpo = objectMapper.writeValueAsString(
                new ClienteRequest("Camila Rojas", "no-es-un-email", "+56912345678", 0));

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.fieldErrors[?(@.campo=='email')]").exists());

        verifyNoInteractions(service);
    }

    @Test
    @WithMockUser(authorities = "BARISTA")
    void crear_sinAutoridadCajeroOAdmin_devuelve403() throws Exception {
        String cuerpo = objectMapper.writeValueAsString(
                new ClienteRequest("Camila Rojas", "camila.rojas@example.cl", "+56912345678", 0));

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isForbidden());

        verifyNoInteractions(service);
    }

    @Test
    @WithMockUser(authorities = "CAJERO")
    void crear_comoCajero_devuelve201() throws Exception {
        ClienteRequest request = new ClienteRequest("Camila Rojas", "camila.rojas@example.cl", "+56912345678", 0);
        when(service.crear(any(ClienteRequest.class))).thenReturn(respuestaDeEjemplo());

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("camila.rojas@example.cl"));
    }

    @Test
    @WithMockUser(authorities = "GERENTE")
    void eliminar_sinSerAdmin_devuelve403() throws Exception {
        mockMvc.perform(delete("/api/clientes/1"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(service);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void eliminar_comoAdmin_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/clientes/1"))
                .andExpect(status().isNoContent());

        verify(service).eliminar(1L);
    }

    @Test
    @WithMockUser(authorities = "CAJERO")
    void canjear_puntosInsuficientes_devuelve409() throws Exception {
        when(service.canjearPuntos(eq(1L), anyInt()))
                .thenThrow(new ConflictoDeNegocioException(
                        "El cliente no tiene puntos de fidelizacion suficientes para canjear 500"));

        mockMvc.perform(post("/api/clientes/1/canje")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CanjeRequest(500))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(
                        "El cliente no tiene puntos de fidelizacion suficientes para canjear 500"));
    }
}
