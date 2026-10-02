package cl.duoc.cafeteria.pagos.controller;

import cl.duoc.cafeteria.pagos.config.SecurityConfig;
import cl.duoc.cafeteria.pagos.dto.PagoRequest;
import cl.duoc.cafeteria.pagos.dto.PagoResponse;
import cl.duoc.cafeteria.pagos.exception.ConflictoDeNegocioException;
import cl.duoc.cafeteria.pagos.exception.RecursoNoEncontradoException;
import cl.duoc.cafeteria.pagos.service.PagoService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Prueba la capa HTTP (DTO + validacion + permisos) con el service mockeado.
 * Importa SecurityConfig (no se escanea solo con @WebMvcTest) para ejercitar
 * el mismo @PreAuthorize que corre en produccion.
 */
@WebMvcTest(PagoController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class PagoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PagoService service;

    // Evita que Spring intente descargar el JWKS real del issuer configurado.
    @MockBean
    private JwtDecoder jwtDecoder;

    private PagoRequest requestValido() {
        return new PagoRequest(10L, 5000.0, "EFECTIVO", null, null);
    }

    private PagoResponse responseValido() {
        return new PagoResponse(1L, 10L, 5000.0, "EFECTIVO", "APROBADO", null, java.time.Instant.now());
    }

    @Test
    @WithMockUser
    void listar_autenticado_retorna200() throws Exception {
        when(service.listar()).thenReturn(List.of(responseValido()));

        mockMvc.perform(get("/api/pagos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estado").value("APROBADO"));
    }

    @Test
    void listar_sinAutenticacion_retorna401o403() throws Exception {
        mockMvc.perform(get("/api/pagos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void obtener_conIdInexistente_retorna404() throws Exception {
        when(service.obtener(99L)).thenThrow(new RecursoNoEncontradoException("No existe el pago con id 99"));

        mockMvc.perform(get("/api/pagos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void crear_conMontoInvalido_retorna400ConFieldErrors() throws Exception {
        PagoRequest invalido = new PagoRequest(10L, -100.0, "EFECTIVO", null, null);

        mockMvc.perform(post("/api/pagos")
                        .with(user("cajero").authorities(() -> "CAJERO"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].campo").value("monto"));
    }

    @Test
    @WithMockUser(authorities = "BARISTA")
    void crear_sinAutoridadCajeroOAdmin_retorna403() throws Exception {
        mockMvc.perform(post("/api/pagos")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "CAJERO")
    void crear_comoCajero_retorna201() throws Exception {
        when(service.crear(any(PagoRequest.class))).thenReturn(responseValido());

        mockMvc.perform(post("/api/pagos")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void actualizar_comoAdmin_retorna200() throws Exception {
        when(service.actualizar(eq(1L), any(PagoRequest.class))).thenReturn(responseValido());

        mockMvc.perform(put("/api/pagos/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(authorities = "CAJERO")
    void eliminar_sinAutoridadAdmin_retorna403() throws Exception {
        mockMvc.perform(delete("/api/pagos/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void eliminar_comoAdmin_retorna204() throws Exception {
        mockMvc.perform(delete("/api/pagos/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void anular_comoAdmin_retorna200() throws Exception {
        PagoResponse anulado = new PagoResponse(1L, 10L, 5000.0, "EFECTIVO", "ANULADO", null, java.time.Instant.now());
        when(service.anular(1L)).thenReturn(anulado);

        mockMvc.perform(patch("/api/pagos/1/anular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ANULADO"));
    }

    @Test
    @WithMockUser(authorities = "CAJERO")
    void anular_sinAutoridadAdmin_retorna403() throws Exception {
        mockMvc.perform(patch("/api/pagos/1/anular"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void anular_conPagoYaAnulado_retorna409() throws Exception {
        when(service.anular(1L)).thenThrow(new ConflictoDeNegocioException("El pago con id 1 ya esta anulado"));

        mockMvc.perform(patch("/api/pagos/1/anular"))
                .andExpect(status().isConflict());
    }
}
