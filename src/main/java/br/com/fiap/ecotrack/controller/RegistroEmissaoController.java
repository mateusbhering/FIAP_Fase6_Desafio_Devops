package br.com.fiap.ecotrack.controller;

import br.com.fiap.ecotrack.dto.RegistroEmissaoRequest;
import br.com.fiap.ecotrack.dto.RegistroEmissaoResponse;
import br.com.fiap.ecotrack.dto.ResumoEmissoesResponse;
import br.com.fiap.ecotrack.model.EscopoEmissao;
import br.com.fiap.ecotrack.service.RegistroEmissaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/emissoes")
@Tag(name = "Emissoes", description = "Inventario de emissoes de gases de efeito estufa (GHG Protocol)")
public class RegistroEmissaoController {

    private final RegistroEmissaoService service;

    public RegistroEmissaoController(RegistroEmissaoService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Registra uma nova emissao")
    public ResponseEntity<RegistroEmissaoResponse> criar(@Valid @RequestBody RegistroEmissaoRequest request) {
        RegistroEmissaoResponse criado = service.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.id())
                .toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @GetMapping
    @Operation(summary = "Lista emissoes, com filtros opcionais por empresa e escopo")
    public List<RegistroEmissaoResponse> listar(@RequestParam(required = false) String empresa,
                                                @RequestParam(required = false) EscopoEmissao escopo) {
        return service.listar(empresa, escopo);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma emissao pelo id")
    public RegistroEmissaoResponse buscar(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma emissao")
    public RegistroEmissaoResponse atualizar(@PathVariable Long id,
                                             @Valid @RequestBody RegistroEmissaoRequest request) {
        return service.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove uma emissao")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/resumo")
    @Operation(summary = "Inventario consolidado em tCO2e, total e por escopo")
    public ResumoEmissoesResponse resumo(@RequestParam(required = false) String empresa) {
        return service.resumo(empresa);
    }
}
