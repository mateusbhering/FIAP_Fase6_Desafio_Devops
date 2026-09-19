package br.com.fiap.ecotrack.dto;

import br.com.fiap.ecotrack.model.EscopoEmissao;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RegistroEmissaoRequest(
        @NotBlank @Size(max = 120) String empresa,
        @NotNull EscopoEmissao escopo,
        @NotBlank @Size(max = 120) String fonte,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal quantidadeCo2eKg,
        @NotNull @PastOrPresent LocalDate dataReferencia
) {
}
