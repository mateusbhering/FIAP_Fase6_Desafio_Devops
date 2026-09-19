#!/usr/bin/env bash
# Smoke test pos-deploy da EcoTrack API.
#
# Uso: ./scripts/smoke-test.sh <url-base> [--read-only]
#   --read-only  nao grava dados (usado em producao)
#
# Variavel opcional EXPECTED_ENV confere se /actuator/info reporta o ambiente esperado.
set -euo pipefail

BASE_URL="${1:?uso: smoke-test.sh <url-base> [--read-only]}"
BASE_URL="${BASE_URL%/}"
SOMENTE_LEITURA="${2:-}"
FALHAS=0

verificar() {
  local descricao="$1"; shift
  if "$@"; then
    echo "  [OK]    $descricao"
  else
    echo "  [FALHA] $descricao"
    FALHAS=$((FALHAS + 1))
  fi
}

status_http() { curl -s -o /dev/null -w '%{http_code}' "$@"; }

echo "Smoke test em ${BASE_URL}"

verificar "health UP" bash -c "curl -fsS '${BASE_URL}/actuator/health' | grep -q '\"status\":\"UP\"'"

if [[ -n "${EXPECTED_ENV:-}" ]]; then
  verificar "ambiente reportado = ${EXPECTED_ENV}" \
    bash -c "curl -fsS '${BASE_URL}/actuator/info' | grep -q '\"environment\":\"${EXPECTED_ENV}\"'"
fi

verificar "GET /api/emissoes -> 200" test "$(status_http "${BASE_URL}/api/emissoes")" = 200
verificar "GET /api/emissoes/resumo -> 200" test "$(status_http "${BASE_URL}/api/emissoes/resumo")" = 200
verificar "POST invalido -> 400" test "$(status_http -X POST -H 'Content-Type: application/json' -d '{}' "${BASE_URL}/api/emissoes")" = 400

if [[ "$SOMENTE_LEITURA" != "--read-only" ]]; then
  RESPOSTA="$(curl -fsS -X POST -H 'Content-Type: application/json' \
    -d "{\"empresa\":\"smoke-test\",\"escopo\":\"ESCOPO_2\",\"fonte\":\"Energia eletrica\",\"quantidadeCo2eKg\":1234.5,\"dataReferencia\":\"$(date +%F)\"}" \
    "${BASE_URL}/api/emissoes" || true)"
  ID="$(echo "$RESPOSTA" | sed -nE 's/.*"id":([0-9]+).*/\1/p')"
  verificar "POST /api/emissoes cria registro" test -n "$ID"
  if [[ -n "$ID" ]]; then
    verificar "GET /api/emissoes/${ID} -> 200" test "$(status_http "${BASE_URL}/api/emissoes/${ID}")" = 200
    verificar "resumo da empresa soma 1.235 tCO2e" \
      bash -c "curl -fsS '${BASE_URL}/api/emissoes/resumo?empresa=smoke-test' | grep -q '\"totalTCo2e\":1.235'"
    verificar "DELETE /api/emissoes/${ID} -> 204" test "$(status_http -X DELETE "${BASE_URL}/api/emissoes/${ID}")" = 204
  fi
fi

if (( FALHAS > 0 )); then
  echo "Smoke test falhou (${FALHAS} verificacao(oes))."
  exit 1
fi
echo "Smoke test OK."
