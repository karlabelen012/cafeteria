package cl.duoc.cafeteria.proveedores.controller;

import cl.duoc.cafeteria.proveedores.dto.ProveedorRequest;
import cl.duoc.cafeteria.proveedores.dto.ProveedorResponse;
import cl.duoc.cafeteria.proveedores.exception.RecursoNoEncontradoException;
import cl.duoc.cafeteria.proveedores.service.ProveedorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de la capa web de Proveedores: delega en ProveedorService (mockeado)
 * y verifica codigos de estado, forma del error (GlobalExceptionHandler) y la
 * matriz de autorizacion via @PreAuthorize (ver docs/EP2_PLAN.md seccion 4:
 * ADMIN=CRUD, GERENTE=ver, BODEGUERO=crear/editar, BARISTA/CAJERO=sin acceso).
 *
 * Los filtros servlet se desactivan (addFilters = false): lo que se prueba
 * aqui es seguridad a nivel de metodo (@PreAuthorize), no la cadena HTTP de
 * OAuth2 Resource Server (eso requeriria un JwtDecoder real / SpringBootTest).
 */
@WebMvcTest(ProveedorController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ProveedorControllerTest.MethodSecurityConfig.class)
class ProveedorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProveedorService service;

    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityConfig {
    }

    private ProveedorResponse respuestaDeEjemplo() {
        return new ProveedorResponse(1L, "Distribuidora Cafetera del Sur", "76543210-3",
                "contacto@cafeteradelsur.cl", "+56 9 1234 5678", "Cafe en grano");
    }

    @Test
    @WithMockUser(authorities = "GERENTE")
    void listar_comoGerente_devuelve200ConLaLista() throws Exception {
        when(service.listar()).thenReturn(List.of(respuestaDeEjemplo()));

        mockMvc.perform(get("/api/proveedores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rut").value("76543210-3"));
    }

    @Test
    @WithMockUser(authorities = "BARISTA")
    void listar_sinAutoridadPermitida_devuelve403() throws Exception {
        mockMvc.perform(get("/api/proveedores"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(service);
    }

    @Test
    @WithMockUser(authorities = "GERENTE")
    void obtener_idInexistente_devuelve404() throws Exception {
        when(service.obtener(99L))
                .thenThrow(new RecursoNoEncontradoException("No existe un proveedor con id 99"));

        mockMvc.perform(get("/api/proveedores/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("No existe un proveedor con id 99"));
    }

    @Test
    @WithMockUser(authorities = "BODEGUERO")
    void crear_rutInvalido_devuelve400ConFieldErrors() throws Exception {
        String cuerpo = objectMapper.writeValueAsString(
                new ProveedorRequest("Distribuidora Cafetera del Sur", "12345678-9",
                        "contacto@cafeteradelsur.cl", "+56 9 1234 5678", "Cafe en grano"));

        mockMvc.perform(post("/api/proveedores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.fieldErrors[?(@.campo=='rut')]").exists());

        verifyNoInteractions(service);
    }

    @Test
    @WithMockUser(authorities = "BARISTA")
    void crear_sinAutoridadAdminOBodeguero_devuelve403() throws Exception {
        String cuerpo = objectMapper.writeValueAsString(
                new ProveedorRequest("Distribuidora Cafetera del Sur", "76543210-3",
                        "contacto@cafeteradelsur.cl", "+56 9 1234 5678", "Cafe en grano"));

        mockMvc.perform(post("/api/proveedores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isForbidden());

        verifyNoInteractions(service);
    }

    @Test
    @WithMockUser(authorities = "BODEGUERO")
    void crear_comoBodeguero_devuelve201() throws Exception {
        ProveedorRequest request = new ProveedorRequest("Distribuidora Cafetera del Sur", "76543210-3",
                "contacto@cafeteradelsur.cl", "+56 9 1234 5678", "Cafe en grano");
        when(service.crear(any(ProveedorRequest.class))).thenReturn(respuestaDeEjemplo());

        mockMvc.perform(post("/api/proveedores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.rut").value("76543210-3"));
    }

    @Test
    @WithMockUser(authorities = "GERENTE")
    void eliminar_sinSerAdmin_devuelve403() throws Exception {
        mockMvc.perform(delete("/api/proveedores/1"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(service);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void eliminar_comoAdmin_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/proveedores/1"))
                .andExpect(status().isNoContent());

        verify(service).eliminar(1L);
    }
}
