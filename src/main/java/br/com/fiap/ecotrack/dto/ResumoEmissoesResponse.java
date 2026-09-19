package br.com.fiap.ecotrack.dto;

import br.com.fiap.ecotrack.model.EscopoEmissao;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Inventario consolidado de emissoes, em toneladas de CO2 equivalente (tCO2e).
 */
public record ResumoEmissoesResponse(
        String empresa,
        long quantidadeRegistros,
        BigDecimal totalTCo2e,
        Map<EscopoEmissao, BigDecimal> totalPorEscopoTCo2e
) {
}
