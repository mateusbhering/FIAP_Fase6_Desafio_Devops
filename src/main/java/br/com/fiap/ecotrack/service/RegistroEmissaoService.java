package br.com.fiap.ecotrack.service;

import br.com.fiap.ecotrack.dto.RegistroEmissaoRequest;
import br.com.fiap.ecotrack.dto.RegistroEmissaoResponse;
import br.com.fiap.ecotrack.dto.ResumoEmissoesResponse;
import br.com.fiap.ecotrack.exception.RecursoNaoEncontradoException;
import br.com.fiap.ecotrack.model.EscopoEmissao;
import br.com.fiap.ecotrack.model.RegistroEmissao;
import br.com.fiap.ecotrack.repository.RegistroEmissaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class RegistroEmissaoService {

    private static final BigDecimal KG_POR_TONELADA = new BigDecimal("1000");

    private final RegistroEmissaoRepository repository;

    public RegistroEmissaoService(RegistroEmissaoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public RegistroEmissaoResponse criar(RegistroEmissaoRequest request) {
        RegistroEmissao registro = new RegistroEmissao(
                request.empresa().trim(),
                request.escopo(),
                request.fonte().trim(),
                request.quantidadeCo2eKg(),
                request.dataReferencia());
        return RegistroEmissaoResponse.de(repository.save(registro));
    }

    @Transactional(readOnly = true)
    public List<RegistroEmissaoResponse> listar(String empresa, EscopoEmissao escopo) {
        return buscar(empresa, escopo).stream()
                .map(RegistroEmissaoResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public RegistroEmissaoResponse buscarPorId(Long id) {
        return RegistroEmissaoResponse.de(obter(id));
    }

    @Transactional
    public RegistroEmissaoResponse atualizar(Long id, RegistroEmissaoRequest request) {
        RegistroEmissao registro = obter(id);
        registro.atualizar(
                request.empresa().trim(),
                request.escopo(),
                request.fonte().trim(),
                request.quantidadeCo2eKg(),
                request.dataReferencia());
        return RegistroEmissaoResponse.de(repository.save(registro));
    }

    @Transactional
    public void remover(Long id) {
        repository.delete(obter(id));
    }

    @Transactional(readOnly = true)
    public ResumoEmissoesResponse resumo(String empresa) {
        List<RegistroEmissao> registros = buscar(empresa, null);

        Map<EscopoEmissao, BigDecimal> porEscopo = new EnumMap<>(EscopoEmissao.class);
        for (EscopoEmissao escopo : EscopoEmissao.values()) {
            porEscopo.put(escopo, BigDecimal.ZERO);
        }
        registros.forEach(r -> porEscopo.merge(r.getEscopo(), r.getQuantidadeCo2eKg(), BigDecimal::add));

        BigDecimal totalKg = porEscopo.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        porEscopo.replaceAll((escopo, kg) -> emToneladas(kg));

        return new ResumoEmissoesResponse(
                empresa == null || empresa.isBlank() ? "TODAS" : empresa,
                registros.size(),
                emToneladas(totalKg),
                porEscopo);
    }

    private List<RegistroEmissao> buscar(String empresa, EscopoEmissao escopo) {
        boolean filtraEmpresa = empresa != null && !empresa.isBlank();
        if (filtraEmpresa && escopo != null) {
            return repository.findByEmpresaIgnoreCaseAndEscopoOrderByDataReferenciaDesc(empresa.trim(), escopo);
        }
        if (filtraEmpresa) {
            return repository.findByEmpresaIgnoreCaseOrderByDataReferenciaDesc(empresa.trim());
        }
        if (escopo != null) {
            return repository.findByEscopoOrderByDataReferenciaDesc(escopo);
        }
        return repository.findAllByOrderByDataReferenciaDesc();
    }

    private RegistroEmissao obter(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Registro de emissao " + id + " nao encontrado"));
    }

    private static BigDecimal emToneladas(BigDecimal kg) {
        return kg.divide(KG_POR_TONELADA, 3, RoundingMode.HALF_UP);
    }
}
