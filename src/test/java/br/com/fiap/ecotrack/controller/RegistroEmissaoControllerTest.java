package br.com.fiap.ecotrack.controller;

import br.com.fiap.ecotrack.dto.RegistroEmissaoResponse;
import br.com.fiap.ecotrack.exception.RecursoNaoEncontradoException;
import br.com.fiap.ecotrack.model.EscopoEmissao;
import br.com.fiap.ecotrack.service.RegistroEmissaoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RegistroEmissaoController.class)
class RegistroEmissaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegistroEmissaoService service;

    @Test
    void postValidoRetorna201ComLocation() throws Exception {
        when(service.criar(any())).thenReturn(new RegistroEmissaoResponse(
                1L, "FIAP", EscopoEmissao.ESCOPO_1, "Frota", new BigDecimal("320.5"),
                LocalDate.of(2026, 2, 1), Instant.now()));

        mockMvc.perform(post("/api/emissoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"empresa":"FIAP","escopo":"ESCOPO_1","fonte":"Frota",
                                 "quantidadeCo2eKg":320.5,"dataReferencia":"2026-02-01"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/emissoes/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.escopo").value("ESCOPO_1"));
    }

    @Test
    void postInvalidoRetorna400ComErrosPorCampo() throws Exception {
        mockMvc.perform(post("/api/emissoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"empresa":"","escopo":"ESCOPO_1","fonte":"Frota",
                                 "quantidadeCo2eKg":-10,"dataReferencia":"2999-01-01"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Erro de validacao"))
                .andExpect(jsonPath("$.erros.empresa").exists())
                .andExpect(jsonPath("$.erros.quantidadeCo2eKg").exists())
                .andExpect(jsonPath("$.erros.dataReferencia").exists());

        verifyNoInteractions(service);
    }

    @Test
    void getInexistenteRetorna404() throws Exception {
        when(service.buscarPorId(42L)).thenThrow(new RecursoNaoEncontradoException("Registro de emissao 42 nao encontrado"));

        mockMvc.perform(get("/api/emissoes/42"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Registro de emissao 42 nao encontrado"));
    }
}
