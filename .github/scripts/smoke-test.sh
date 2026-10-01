#!/usr/bin/env bash
# Smoke test do vehicle-sales-service implantado.
# Uso: smoke-test.sh <url-do-serviço>   (ex.: http://localhost:8081)
set -euo pipefail

SALES_URL=${1:?informe a URL do serviço de vendas}
JSON='Content-Type: application/json'
TODAY=$(TZ=America/Sao_Paulo date +%F)
VEHICLE_ID=$(cat /proc/sys/kernel/random/uuid)

step() { echo; echo "▶ $*"; }
fail() { echo "❌ $*"; exit 1; }

step "Health"
curl -sf "$SALES_URL/actuator/health" | jq -e '.status == "UP"' >/dev/null || fail "serviço não está UP"

step "Sincronização de veículo (endpoint interno)"
curl -sf -X PUT "$SALES_URL/internal/vehicles/$VEHICLE_ID" -H "$JSON" \
  -d '{"brand":"Toyota","model":"Corolla","year":2022,"color":"Prata","price":85000.00}' | jq .

step "Veículo aparece na listagem de veículos à venda"
curl -sf "$SALES_URL/vehicles/available" | jq -e --arg id "$VEHICLE_ID" 'any(.content[]; .id == $id)' >/dev/null \
  || fail "veículo não está à venda"

step "Compra do veículo"
PAYMENT_CODE=$(curl -sf -X POST "$SALES_URL/sales" -H "$JSON" \
  -d "{\"vehicleId\":\"$VEHICLE_ID\",\"buyerCpf\":\"529.982.247-25\",\"saleDate\":\"$TODAY\"}" | jq -r '.paymentCode')
echo "paymentCode: $PAYMENT_CODE"

step "Pagamento confirmado"
curl -sf -X PATCH "$SALES_URL/internal/sales/$PAYMENT_CODE/payment" -H "$JSON" -d '{"status":"PAID"}' | jq .

step "Veículo aparece na listagem de vendidos"
curl -sf "$SALES_URL/vehicles/sold" | jq -e --arg id "$VEHICLE_ID" 'any(.content[]; .id == $id)' >/dev/null \
  || fail "veículo não está na listagem de vendidos"

echo; echo "✅ Smoke test concluído com sucesso"
