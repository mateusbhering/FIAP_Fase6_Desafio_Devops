package br.com.fiap.ecotrack.service;

import br.com.fiap.ecotrack.dto.RegistroEmissaoRequest;
import br.com.fiap.ecotrack.dto.RegistroEmissaoResponse;
import br.com.fiap.ecotrack.dto.ResumoEmissoesResponse;
import br.com.fiap.ecotrack.exception.RecursoNaoEncontradoException;
import br.com.fiap.ecotrack.model.EscopoEmissao;
import br.com.fiap.ecotrack.model.RegistroEmissao;
import br.com.fiap.ecotrack.repository.RegistroEmissaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistroEmissaoServiceTest {

    @Mock
    private RegistroEmissaoRepository repository;

    @InjectMocks
    private RegistroEmissaoService service;

    @Test
    void criarNormalizaCamposDeTextoEPersiste() {
        when(repository.save(any(RegistroEmissao.class))).thenAnswer(inv -> inv.getArgument(0));

        RegistroEmissaoResponse resposta = service.criar(new RegistroEmissaoRequest(
                "  FIAP  ", EscopoEmissao.ESCOPO_2, " Energia eletrica ", new BigDecimal("1500.5"), LocalDate.of(2026, 1, 31)));

        assertThat(resposta.empresa()).isEqualTo("FIAP");
        assertThat(resposta.fonte()).isEqualTo("Energia eletrica");
        assertThat(resposta.quantidadeCo2eKg()).isEqualByComparingTo("1500.5");
    }

    @Test
    void resumoConsolidaTotaisEmToneladasPorEscopo() {
        when(repository.findByEmpresaIgnoreCaseOrderByDataReferenciaDesc("FIAP")).thenReturn(List.of(
                registro(EscopoEmissao.ESCOPO_1, "1200"),
                registro(EscopoEmissao.ESCOPO_1, "300"),
                registro(EscopoEmissao.ESCOPO_2, "2500.250")));

        ResumoEmissoesResponse resumo = service.resumo("FIAP");

        assertThat(resumo.quantidadeRegistros()).isEqualTo(3);
        assertThat(resumo.totalTCo2e()).isEqualByComparingTo("4.000");
        assertThat(resumo.totalPorEscopoTCo2e().get(EscopoEmissao.ESCOPO_1)).isEqualByComparingTo("1.500");
        assertThat(resumo.totalPorEscopoTCo2e().get(EscopoEmissao.ESCOPO_2)).isEqualByComparingTo("2.500");
        assertThat(resumo.totalPorEscopoTCo2e().get(EscopoEmissao.ESCOPO_3)).isEqualByComparingTo("0");
    }

    @Test
    void resumoSemEmpresaConsideraTodosOsRegistros() {
        when(repository.findAllByOrderByDataReferenciaDesc()).thenReturn(List.of());

        ResumoEmissoesResponse resumo = service.resumo(null);

        assertThat(resumo.empresa()).isEqualTo("TODAS");
        assertThat(resumo.totalTCo2e()).isEqualByComparingTo("0");
    }

    @Test
    void listarAplicaFiltroCombinadoDeEmpresaEEscopo() {
        service.listar("fiap", EscopoEmissao.ESCOPO_3);

        verify(repository).findByEmpresaIgnoreCaseAndEscopoOrderByDataReferenciaDesc("fiap", EscopoEmissao.ESCOPO_3);
    }

    @Test
    void buscarIdInexistenteLancaExcecao() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("99");
    }

    @Test
    void removerIdInexistenteNaoApagaNada() {
        when(repository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.remover(7L)).isInstanceOf(RecursoNaoEncontradoException.class);
        verify(repository, never()).delete(any());
    }

    private static RegistroEmissao registro(EscopoEmissao escopo, String kg) {
        return new RegistroEmissao("FIAP", escopo, "Fonte", new BigDecimal(kg), LocalDate.of(2026, 1, 1));
    }
}
