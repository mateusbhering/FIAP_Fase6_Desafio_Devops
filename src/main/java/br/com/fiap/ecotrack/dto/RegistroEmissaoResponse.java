package br.com.fiap.ecotrack.dto;

import br.com.fiap.ecotrack.model.EscopoEmissao;
import br.com.fiap.ecotrack.model.RegistroEmissao;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record RegistroEmissaoResponse(
        Long id,
        String empresa,
        EscopoEmissao escopo,
        String fonte,
        BigDecimal quantidadeCo2eKg,
        LocalDate dataReferencia,
        Instant criadoEm
) {
    public static RegistroEmissaoResponse de(RegistroEmissao registro) {
        return new RegistroEmissaoResponse(
                registro.getId(),
                registro.getEmpresa(),
                registro.getEscopo(),
                registro.getFonte(),
                registro.getQuantidadeCo2eKg(),
                registro.getDataReferencia(),
                registro.getCriadoEm());
    }
}
