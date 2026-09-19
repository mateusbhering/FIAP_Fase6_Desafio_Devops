# Evidências em texto

Saídas completas dos comandos executados com Docker Desktop. Os prints correspondentes estão em [`../prints`](../prints)
e reunidos na [documentação em PDF](../Documentacao-Cidades-ESGInteligentes.pdf).

| Arquivo | Conteúdo |
|---|---|
| `testes-automatizados.txt` | `./mvnw verify`: 11 testes aprovados e cobertura JaCoCo |
| `deploy-staging.txt`, `deploy-production.txt` | Deploy com Docker Compose nos dois ambientes |
| `smoke-test-staging.txt`, `smoke-test-production.txt` | Verificações pós-deploy |
| `containers-volumes-redes.txt` | Containers, volumes, redes (backend interna), variáveis de ambiente, usuário não-root |
| `api-staging.txt` | Requisições reais à API |
| `persistencia-volume.txt` | Dados preservados no volume após recriar os containers |
| `rollback-automatico.txt` | Versão quebrada revertida automaticamente |

Execuções do pipeline no GitHub Actions: https://github.com/mateusbhering/FIAP_Fase6_Desafio_Devops/actions/workflows/ci-cd.yml
