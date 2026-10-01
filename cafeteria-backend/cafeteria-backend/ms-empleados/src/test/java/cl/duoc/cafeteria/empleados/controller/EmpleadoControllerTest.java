package cl.duoc.cafeteria.empleados.controller;

import cl.duoc.cafeteria.empleados.config.SecurityConfig;
import cl.duoc.cafeteria.empleados.dto.EmpleadoRequest;
import cl.duoc.cafeteria.empleados.dto.EmpleadoResponse;
import cl.duoc.cafeteria.empleados.exception.RecursoNoEncontradoException;
import cl.duoc.cafeteria.empleados.service.EmpleadoService;
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

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Prueba la capa HTTP (DTO + validacion + permisos) con el service mockeado.
 * Importa SecurityConfig (no se escanea solo con @WebMvcTest) para ejercitar
 * el mismo @PreAuthorize que corre en produccion: en este modulo, a
 * diferencia de otros, incluso los GET quedan restringidos a ADMIN/GERENTE.
 */
@WebMvcTest(EmpleadoController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class EmpleadoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmpleadoService service;

    // Evita que Spring intente descargar el JWKS real del issuer configurado.
    @MockBean
    private JwtDecoder jwtDecoder;

    private EmpleadoRequest requestValido() {
        return new EmpleadoRequest("Ana Soto", "ana.soto@cafegestion360.cl", "BARISTA", true,
                LocalDate.of(2023, 1, 15));
    }

    private EmpleadoResponse responseValido() {
        return new EmpleadoResponse(1L, "Ana Soto", "ana.soto@cafegestion360.cl", "BARISTA", true,
                LocalDate.of(2023, 1, 15));
    }

    @Test
    @WithMockUser(authorities = "BARISTA")
    void listar_sinAutoridadAdminOGerente_retorna403() throws Exception {
        mockMvc.perform(get("/api/empleados"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "GERENTE")
    void listar_comoGerente_retorna200() throws Exception {
        when(service.listar()).thenReturn(List.of(responseValido()));

        mockMvc.perform(get("/api/empleados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Ana Soto"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void crear_conEmailInvalido_retorna400ConFieldErrors() throws Exception {
        EmpleadoRequest invalido = new EmpleadoRequest("Ana Soto", "no-es-un-email", "BARISTA", true,
                LocalDate.of(2023, 1, 15));

        mockMvc.perform(post("/api/empleados")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].campo").value("email"));
    }

    @Test
    @WithMockUser(authorities = "GERENTE")
    void crear_sinAutoridadAdmin_retorna403() throws Exception {
        mockMvc.perform(post("/api/empleados")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void crear_comoAdmin_retorna201() throws Exception {
        when(service.crear(any(EmpleadoRequest.class))).thenReturn(responseValido());

        mockMvc.perform(post("/api/empleados")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void desactivar_conIdInexistente_retorna404() throws Exception {
        when(service.desactivar(99L)).thenThrow(new RecursoNoEncontradoException("No existe el empleado con id 99"));

        mockMvc.perform(patch("/api/empleados/99/desactivar"))
                .andExpect(status().isNotFound());
    }
}
