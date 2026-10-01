package cl.duoc.cafeteria.inventario.controller;

import cl.duoc.cafeteria.inventario.config.SecurityConfig;
import cl.duoc.cafeteria.inventario.dto.RecetaItemRequest;
import cl.duoc.cafeteria.inventario.dto.RecetaItemResponse;
import cl.duoc.cafeteria.inventario.exception.RecursoNoEncontradoException;
import cl.duoc.cafeteria.inventario.security.Roles;
import cl.duoc.cafeteria.inventario.service.RecetaItemService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Cubre la matriz de permisos de /api/recetas: misma que /api/inventario
 * (ver RecetaItemController y docs/EP2_PLAN.md seccion 4).
 */
@WebMvcTest(RecetaItemController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class RecetaItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RecetaItemService service;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void listar_sinRolesValidos_devuelve403() throws Exception {
        mockMvc.perform(get("/api/recetas").with(jwt().authorities(new SimpleGrantedAuthority(Roles.CAJERO))))
                .andExpect(status().isForbidden());
    }

    @Test
    void crear_comoBodeguero_devuelve201() throws Exception {
        RecetaItemRequest request = new RecetaItemRequest(1L, 2L, 10.0);
        when(service.crear(any(RecetaItemRequest.class))).thenReturn(new RecetaItemResponse(1L, 1L, 2L, 10.0));

        mockMvc.perform(post("/api/recetas")
                        .with(jwt().authorities(new SimpleGrantedAuthority(Roles.BODEGUERO)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void crear_sinRolAutorizado_devuelve403() throws Exception {
        RecetaItemRequest request = new RecetaItemRequest(1L, 2L, 10.0);

        mockMvc.perform(post("/api/recetas")
                        .with(jwt().authorities(new SimpleGrantedAuthority(Roles.GERENTE)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void obtener_idInexistente_devuelve404() throws Exception {
        when(service.obtener(eq(99L))).thenThrow(new RecursoNoEncontradoException("No existe el item de receta con id 99"));

        mockMvc.perform(get("/api/recetas/{id}", 99L)
                        .with(jwt().authorities(new SimpleGrantedAuthority(Roles.ADMIN))))
                .andExpect(status().isNotFound());
    }
}
