package cl.duoc.cafeteria.inventario.controller;

import cl.duoc.cafeteria.inventario.config.SecurityConfig;
import cl.duoc.cafeteria.inventario.dto.InsumoRequest;
import cl.duoc.cafeteria.inventario.dto.InsumoResponse;
import cl.duoc.cafeteria.inventario.exception.RecursoNoEncontradoException;
import cl.duoc.cafeteria.inventario.security.Roles;
import cl.duoc.cafeteria.inventario.service.InsumoService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Cubre la matriz de permisos de /api/inventario (ver docs/EP2_PLAN.md
 * seccion 4): ADMIN=CRUD, GERENTE=ver, BARISTA=ver, BODEGUERO=crear/editar,
 * CAJERO=sin acceso.
 */
@WebMvcTest(InsumoController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class InsumoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InsumoService service;

    // Satisface la dependencia que SecurityConfig necesita para construir el
    // SecurityFilterChain (oauth2ResourceServer().jwt()). El decoder real
    // nunca se invoca: el processor jwt() de spring-security-test inyecta la
    // autenticacion directamente, sin pasar por el filtro de bearer token.
    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void listar_sinRolesValidos_devuelve403() throws Exception {
        mockMvc.perform(get("/api/inventario").with(jwt().authorities(List.of())))
                .andExpect(status().isForbidden());
    }

    @Test
    void listar_comoBarista_devuelve200() throws Exception {
        when(service.listar()).thenReturn(List.of(new InsumoResponse(1L, "Leche", "ml", 100.0, 20.0)));

        mockMvc.perform(get("/api/inventario").with(jwt().authorities(new SimpleGrantedAuthority(Roles.BARISTA))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Leche"));
    }

    @Test
    void listar_comoCajero_devuelve403() throws Exception {
        mockMvc.perform(get("/api/inventario").with(jwt().authorities(new SimpleGrantedAuthority(Roles.CAJERO))))
                .andExpect(status().isForbidden());
    }

    @Test
    void crear_unidadMedidaInvalida_devuelve400ConFieldErrors() throws Exception {
        String payload = """
                {"nombre":"Cafe en grano","unidadMedida":"litros","stockActual":10.0,"stockMinimo":2.0}
                """;

        mockMvc.perform(post("/api/inventario")
                        .with(jwt().authorities(new SimpleGrantedAuthority(Roles.BODEGUERO)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.fieldErrors[0].campo").value("unidadMedida"));
    }

    @Test
    void crear_sinRolAutorizado_devuelve403() throws Exception {
        InsumoRequest request = new InsumoRequest("Cafe en grano", "g", 10.0, 2.0);

        mockMvc.perform(post("/api/inventario")
                        .with(jwt().authorities(new SimpleGrantedAuthority(Roles.BARISTA)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void crear_comoBodeguero_devuelve201() throws Exception {
        InsumoRequest request = new InsumoRequest("Cafe en grano", "g", 10.0, 2.0);
        when(service.crear(any(InsumoRequest.class)))
                .thenReturn(new InsumoResponse(1L, "Cafe en grano", "g", 10.0, 2.0));

        mockMvc.perform(post("/api/inventario")
                        .with(jwt().authorities(new SimpleGrantedAuthority(Roles.BODEGUERO)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void obtener_idInexistente_devuelve404() throws Exception {
        when(service.obtener(eq(99L))).thenThrow(new RecursoNoEncontradoException("No existe el insumo con id 99"));

        mockMvc.perform(get("/api/inventario/{id}", 99L)
                        .with(jwt().authorities(new SimpleGrantedAuthority(Roles.ADMIN))))
                .andExpect(status().isNotFound());
    }
}
