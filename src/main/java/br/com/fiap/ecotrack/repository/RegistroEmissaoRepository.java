package br.com.fiap.ecotrack.repository;

import br.com.fiap.ecotrack.model.EscopoEmissao;
import br.com.fiap.ecotrack.model.RegistroEmissao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegistroEmissaoRepository extends JpaRepository<RegistroEmissao, Long> {

    List<RegistroEmissao> findByEmpresaIgnoreCaseOrderByDataReferenciaDesc(String empresa);

    List<RegistroEmissao> findByEscopoOrderByDataReferenciaDesc(EscopoEmissao escopo);

    List<RegistroEmissao> findByEmpresaIgnoreCaseAndEscopoOrderByDataReferenciaDesc(String empresa, EscopoEmissao escopo);

    List<RegistroEmissao> findAllByOrderByDataReferenciaDesc();
}
