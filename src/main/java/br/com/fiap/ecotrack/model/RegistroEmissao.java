package br.com.fiap.ecotrack.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "registro_emissao")
public class RegistroEmissao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String empresa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EscopoEmissao escopo;

    @Column(nullable = false, length = 120)
    private String fonte;

    @Column(name = "quantidade_co2e_kg", nullable = false, precision = 15, scale = 3)
    private BigDecimal quantidadeCo2eKg;

    @Column(name = "data_referencia", nullable = false)
    private LocalDate dataReferencia;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    protected RegistroEmissao() {
    }

    public RegistroEmissao(String empresa, EscopoEmissao escopo, String fonte,
                           BigDecimal quantidadeCo2eKg, LocalDate dataReferencia) {
        this.empresa = empresa;
        this.escopo = escopo;
        this.fonte = fonte;
        this.quantidadeCo2eKg = quantidadeCo2eKg;
        this.dataReferencia = dataReferencia;
    }

    @PrePersist
    void aoCriar() {
        this.criadoEm = Instant.now();
    }

    public void atualizar(String empresa, EscopoEmissao escopo, String fonte,
                          BigDecimal quantidadeCo2eKg, LocalDate dataReferencia) {
        this.empresa = empresa;
        this.escopo = escopo;
        this.fonte = fonte;
        this.quantidadeCo2eKg = quantidadeCo2eKg;
        this.dataReferencia = dataReferencia;
    }

    public Long getId() {
        return id;
    }

    public String getEmpresa() {
        return empresa;
    }

    public EscopoEmissao getEscopo() {
        return escopo;
    }

    public String getFonte() {
        return fonte;
    }

    public BigDecimal getQuantidadeCo2eKg() {
        return quantidadeCo2eKg;
    }

    public LocalDate getDataReferencia() {
        return dataReferencia;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}
