package cl.duoc.cafeteria.inventario.controller;

import cl.duoc.cafeteria.inventario.config.SecurityConfig;
import cl.duoc.cafeteria.inventario.dto.MovimientoStockRequest;
import cl.duoc.cafeteria.inventario.dto.MovimientoStockResponse;
import cl.duoc.cafeteria.inventario.security.Roles;
import cl.duoc.cafeteria.inventario.service.MovimientoStockService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Registrar un movimiento es tarea de BODEGUERO (y ADMIN) segun la matriz de
 * roles de docs/EP2_PLAN.md seccion 4.
 */
@WebMvcTest(MovimientoStockController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class MovimientoStockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MovimientoStockService service;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void registrar_comoBodeguero_devuelve201YUsaElServicio() throws Exception {
        MovimientoStockRequest request = new MovimientoStockRequest("ENTRADA", 5.0, "Reposicion de stock");
        MovimientoStockResponse response = new MovimientoStockResponse(1L, 10L, "ENTRADA", 5.0,
                "Reposicion de stock", Instant.parse("2026-01-01T00:00:00Z"), "bodeguero@cafeteria.cl");
        when(service.registrar(eq(10L), any())).thenReturn(response);

        mockMvc.perform(post("/api/inventario/{insumoId}/movimientos", 10L)
                        .with(jwt().authorities(new SimpleGrantedAuthority(Roles.BODEGUERO)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("ENTRADA"));

        verify(service).registrar(eq(10L), any());
    }

    @Test
    void registrar_sinRolAutorizado_devuelve403() throws Exception {
        MovimientoStockRequest request = new MovimientoStockRequest("ENTRADA", 5.0, "Reposicion de stock");

        mockMvc.perform(post("/api/inventario/{insumoId}/movimientos", 10L)
                        .with(jwt().authorities(new SimpleGrantedAuthority(Roles.BARISTA)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
