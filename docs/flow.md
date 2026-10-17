# Fluxo ponta a ponta — Vehicle Sales Service

A sequência mostra **os dois microsserviços e os dois bancos** porque a jornada atravessa os limites dos repositórios. A entrada inclui as listagens e a compra no Serviço de Vendas. O processador de pagamento é **simulado** para fins de teste.

```mermaid
sequenceDiagram
  autonumber
  actor C as Cliente / Postman
  participant P as Platform API :8080
  participant DBP as PostgreSQL platform
  participant S as Sales Service :8081
  participant DBS as PostgreSQL sales
  actor PG as Processadora (simulada)

  rect rgb(245, 248, 252)
    Note over C,DBS: Cadastro e sincronização por HTTP
    C->>P: POST /vehicles (marca, modelo, ano, cor, preço)
    P->>DBP: Persistir cadastro em transação local
    P->>S: PUT /internal/vehicles/{id}
    S->>DBS: Persistir cópia para listagem
    S-->>P: 201 Created / 200 OK
    P-->>C: 201 Created + vehicleId
  end

  rect rgb(238, 250, 242)
    Note over C,DBS: Consulta e início da compra
    C->>S: GET /vehicles/available?sort=price
    S->>DBS: Consultar disponíveis ordenados
    S-->>C: 200 OK + veículos
    C->>S: POST /sales (vehicleId, buyerCpf, saleDate)
    S->>DBS: Reservar veículo e criar venda PENDING
    S-->>C: 201 Created + paymentCode
  end

  rect rgb(255, 246, 234)
    Note over PG,DBS: Webhook público recebido pela API Principal
    PG->>P: POST /webhooks/payments (paymentCode, PAID ou CANCELED)
    P->>S: PATCH /internal/sales/{paymentCode}/payment
    S->>DBS: Localizar venda e validar transição idempotente
    alt PAID
      S->>DBS: Venda PAID / veículo SOLD
      S-->>P: 200 OK
      P-->>PG: 200 OK
      C->>S: GET /vehicles/sold
      S-->>C: Veículo vendido
    else CANCELED
      S->>DBS: Venda CANCELED / veículo AVAILABLE
      S-->>P: 200 OK
      P-->>PG: 200 OK
      C->>S: GET /vehicles/available
      S-->>C: Veículo novamente disponível
    else Código inexistente ou conflito
      S-->>P: 404 ou 409
      P-->>PG: Erro correspondente
    end
  end
```

## Infraestrutura envolvida nas etapas

| Etapa | Entrada HTTP | Componente Kubernetes | Persistência |
|---|---|---|---|
| Cadastro/edição | Platform API :8080 | Service → Deployment da API Principal | PostgreSQL `vehicle_platform` |
| Sincronização | `PUT /internal/vehicles/{id}` | Deployment principal → Service de Vendas :8081 | PostgreSQL `vehicle_sales` |
| Listagem/compra | Sales Service :8081 | Service → Deployment de Vendas (com HPA) | PostgreSQL `vehicle_sales` |
| Webhook | `POST /webhooks/payments` | Service → Deployment principal | Encaminha HTTP ao Serviço de Vendas |
| Confirmação/cancelamento | `PATCH /internal/sales/{paymentCode}/payment` | Service → Deployment de Vendas | PostgreSQL `vehicle_sales` |

**Observações:** o processamento do webhook é idempotente para reenvios do mesmo resultado. Um resultado contrário após a conclusão é rejeitado (`409`); código desconhecido recebe `404`. Falha de sincronização HTTP não equivale a rollback atômico dos dois bancos. O cluster kind usado no CD é temporário. Consulte também [Arquitetura e infraestrutura](./architecture.md).
