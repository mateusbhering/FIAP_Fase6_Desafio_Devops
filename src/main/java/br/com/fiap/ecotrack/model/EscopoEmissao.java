package br.com.fiap.ecotrack.model;

/**
 * Escopos de emissao definidos pelo GHG Protocol.
 */
public enum EscopoEmissao {
    /** Emissoes diretas (combustao em fontes proprias, frota, processos). */
    ESCOPO_1,
    /** Emissoes indiretas pela compra de energia eletrica e termica. */
    ESCOPO_2,
    /** Demais emissoes indiretas da cadeia de valor. */
    ESCOPO_3
}
