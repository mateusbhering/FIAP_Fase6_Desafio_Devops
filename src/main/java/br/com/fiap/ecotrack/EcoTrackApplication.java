package br.com.fiap.ecotrack;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(
        title = "EcoTrack API - Cidades ESGInteligentes",
        version = "1.0.0",
        description = "Inventario de emissoes de gases de efeito estufa (GHG Protocol) de cidades e organizacoes"))
public class EcoTrackApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcoTrackApplication.class, args);
    }
}
