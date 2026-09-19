#!/usr/bin/env bash
# Faz o deploy da EcoTrack API em um ambiente usando Docker Compose.
#
# Uso: APP_IMAGE=<imagem:tag> POSTGRES_PASSWORD=<senha> ./scripts/deploy.sh <staging|production>
#
# - Isola cada ambiente por nome de projeto do Compose (containers, redes e volumes proprios)
# - Aguarda a aplicacao ficar pronta (readiness probe do Actuator)
# - Se a nova versao nao subir, faz rollback automatico para a imagem anterior
set -euo pipefail

AMBIENTE="${1:?uso: deploy.sh <staging|production>}"
RAIZ="$(cd "$(dirname "$0")/.." && pwd)"
cd "$RAIZ"

case "$AMBIENTE" in
  staging)    PROJETO="ecotrack-staging"; ENV_FILE="deploy/staging.env" ;;
  production) PROJETO="ecotrack-prod";    ENV_FILE="deploy/production.env" ;;
  *) echo "Ambiente invalido: $AMBIENTE (use staging ou production)" >&2; exit 2 ;;
esac

: "${APP_IMAGE:?defina APP_IMAGE com a imagem a ser implantada}"
: "${POSTGRES_PASSWORD:?defina POSTGRES_PASSWORD}"
export APP_IMAGE POSTGRES_PASSWORD
export APP_VERSION="${APP_VERSION:-${APP_IMAGE##*:}}"

APP_PORT="$(grep -E '^APP_PORT=' "$ENV_FILE" | cut -d= -f2)"
TIMEOUT_SEGUNDOS="${DEPLOY_TIMEOUT:-180}"
compose() { docker compose -p "$PROJETO" --env-file "$ENV_FILE" -f docker-compose.yml "$@"; }

aguardar_pronto() {
  local limite=$((SECONDS + TIMEOUT_SEGUNDOS))
  echo "Aguardando http://localhost:${APP_PORT}/actuator/health/readiness (timeout ${TIMEOUT_SEGUNDOS}s)..."
  until curl -fsS "http://localhost:${APP_PORT}/actuator/health/readiness" 2>/dev/null | grep -q '"status":"UP"'; do
    if (( SECONDS >= limite )); then
      return 1
    fi
    sleep 3
  done
}

IMAGEM_ANTERIOR=""
CONTAINER_ATUAL="$(compose ps -q app 2>/dev/null || true)"
if [[ -n "$CONTAINER_ATUAL" ]]; then
  IMAGEM_ANTERIOR="$(docker inspect --format '{{.Config.Image}}' "$CONTAINER_ATUAL" 2>/dev/null || true)"
fi

echo "==> Deploy de ${APP_IMAGE} em ${AMBIENTE} (projeto ${PROJETO}, porta ${APP_PORT})"
[[ -n "$IMAGEM_ANTERIOR" ]] && echo "    Versao atual: ${IMAGEM_ANTERIOR}"

if [[ "${SKIP_PULL:-false}" != "true" ]]; then
  compose pull app
fi
compose up -d --no-build --remove-orphans

if aguardar_pronto; then
  echo "==> ${AMBIENTE} no ar com ${APP_IMAGE}"
  compose ps
  exit 0
fi

echo "!! A nova versao nao ficou pronta. Logs da aplicacao:" >&2
compose logs --tail 150 app >&2 || true

if [[ -n "$IMAGEM_ANTERIOR" && "$IMAGEM_ANTERIOR" != "$APP_IMAGE" ]]; then
  echo "!! Rollback para ${IMAGEM_ANTERIOR}" >&2
  APP_IMAGE="$IMAGEM_ANTERIOR" APP_VERSION="${IMAGEM_ANTERIOR##*:}" compose up -d --no-build app
  aguardar_pronto && echo "!! Rollback concluido; ${AMBIENTE} segue na versao anterior" >&2
fi
exit 1
