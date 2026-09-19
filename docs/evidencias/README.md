# Evidências

## Coletadas localmente (arquivos `.txt` desta pasta)

Geradas executando os mesmos scripts que o pipeline usa, com Docker Desktop:

- `testes-automatizados.txt`: build Maven com 11 testes aprovados e cobertura JaCoCo
- `deploy-staging.txt`, `deploy-production.txt`: deploy com Docker Compose nos dois ambientes
- `smoke-test-staging.txt`, `smoke-test-production.txt`: verificações pós-deploy
- `containers-volumes-redes.txt`: containers, volumes, redes (backend interna), variáveis de ambiente, usuário não-root
- `api-staging.txt`: requisições reais à API
- `persistencia-volume.txt`: dados preservados no volume após recriar os containers
- `rollback-automatico.txt`: versão quebrada revertida automaticamente

## Prints para capturar no GitHub após o primeiro push

Salve as imagens nesta pasta (ex.: `01-pipeline.png`) e referencie-as no README principal.

1. **Actions → execução do CI/CD na `main`**: o grafo com os 4 jobs verdes (`build-test` → `docker-image` → `Staging` → `Producao`).
2. **Summary da execução**: tabela de testes, cobertura, imagem publicada e resumo de cada deploy.
3. **Job `build-test`**: log do `./mvnw verify` com `Tests run: 11, Failures: 0` e `BUILD SUCCESS`.
4. **Aprovação de produção**: o pipeline parado em “Waiting for review” no environment `production` e, em seguida, a aprovação.
5. **Jobs de deploy**: logs do `deploy.sh` e do `smoke-test.sh` com todos os `[OK]`.
6. **Packages**: a imagem `ecotrack-api` no GHCR com as tags `<sha>` e `main`.
7. **Settings → Environments**: `staging` e `production` configurados, com required reviewers em produção.
8. **Artifacts**: `relatorios-testes` e `ecotrack-api-jar` disponíveis para download.
9. *(Opcional)* **Pull request**: o CI rodando só build e testes, sem deploy.
