package br.com.fiap.ecotrack;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sobe o contexto completo (Flyway + JPA + Web) sobre H2 em modo PostgreSQL.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EcoTrackIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void cicloCompletoDeUmRegistroDeEmissao() throws Exception {
        String location = mockMvc.perform(post("/api/emissoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"empresa":"Integracao SA","escopo":"ESCOPO_2","fonte":"Energia eletrica",
                                 "quantidadeCo2eKg":2000,"dataReferencia":"2026-03-31"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");

        mockMvc.perform(post("/api/emissoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"empresa":"Integracao SA","escopo":"ESCOPO_1","fonte":"Caldeira a gas",
                                 "quantidadeCo2eKg":500,"dataReferencia":"2026-03-31"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/emissoes").param("empresa", "integracao sa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mockMvc.perform(get("/api/emissoes/resumo").param("empresa", "Integracao SA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTCo2e").value(2.5))
                .andExpect(jsonPath("$.totalPorEscopoTCo2e.ESCOPO_2").value(2.0));

        mockMvc.perform(put(location)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"empresa":"Integracao SA","escopo":"ESCOPO_2","fonte":"Energia eletrica (revisado)",
                                 "quantidadeCo2eKg":1800,"dataReferencia":"2026-03-31"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fonte").value("Energia eletrica (revisado)"));

        mockMvc.perform(delete(location)).andExpect(status().isNoContent());
        mockMvc.perform(get(location)).andExpect(status().isNotFound());
    }

    @Test
    void actuatorHealthExpoeStatusUp() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
