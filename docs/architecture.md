# Arquitetura — Vehicle Sales Service

Este diagrama representa **aplicação e infraestrutura reais**, com base em `k8s/`, `.github/workflows/` e nos contratos HTTP. O destaque é a listagem, a compra, os estados de venda e o escalonamento. Não há acesso cruzado entre os bancos.

```mermaid
flowchart TB
  Client["Cliente HTTP / Swagger / Postman"]
  Payment["Processadora de pagamentos<br/>(simulada na demonstração)"]

  subgraph CICD["GitHub Actions - CI/CD"]
    Git["feature → develop → main"]
    CI["CI: Maven verify / JaCoCo 80%<br/>Docker build / kubeconform"]
    Registry[("GitHub Container Registry")]
    Kind["CD: runner GitHub cria cluster kind<br/>kubectl apply -k + smoke tests"]
    Git --> CI --> Registry --> Kind
  end

  subgraph K8S["Kubernetes kind - namespace revenda-veiculos"]
    direction LR
    subgraph Principal["vehicle-platform-api"]
      PSvc["Service ClusterIP<br/>vehicle-platform-api:8080"]
      PDeploy["Deployment: 2 réplicas<br/>Spring Boot / Java 25<br/>startup, readiness e liveness probes"]
      PCfg["ConfigMap + Secret<br/>SALES_SERVICE_URL / DB credentials"]
      PDBSvc["Service vehicle-platform-db:5432"]
      PDB[("StatefulSet PostgreSQL 17<br/>vehicle_platform / PVC 1Gi")]
      PSvc --> PDeploy --> PDBSvc --> PDB
      PCfg -.-> PDeploy
    end

    subgraph Vendas["vehicle-sales-service"]
      SSvc["Service ClusterIP<br/>vehicle-sales-service:8081"]
      SDeploy["Deployment: 2 réplicas<br/>Spring Boot / Java 25<br/>startup, readiness e liveness probes"]
      SCfg["ConfigMap + Secret<br/>DB credentials"]
      SHPA["HPA 2 a 6 réplicas<br/>CPU alvo 70% / metrics-server"]
      SDBSvc["Service vehicle-sales-db:5432"]
      SDB[("StatefulSet PostgreSQL 17<br/>vehicle_sales / PVC 1Gi")]
      SSvc --> SDeploy --> SDBSvc --> SDB
      SCfg -.-> SDeploy
      SHPA -.-> SDeploy
    end
    PDeploy -->|"HTTP: PUT /internal/vehicles/{id}<br/>PATCH /internal/sales/{paymentCode}/payment"| SSvc
  end

  Client -->|"Cadastro e edição"| PSvc
  Client -->|"Listagens e compra"| SSvc
  Payment -->|"POST /webhooks/payments"| PSvc
  Kind -.-> PDeploy
  Kind -.-> SDeploy
  Registry -.-> Kind

  classDef primary fill:#ffe5f0,stroke:#e91e63,color:#3d1223
  classDef secondary fill:#e9f7ec,stroke:#299447,color:#15351d
  classDef infra fill:#e9f2ff,stroke:#3674b6,color:#152f50
  class PSvc,PDeploy,PCfg,PDBSvc,PDB primary
  class SSvc,SDeploy,SCfg,SHPA,SDBSvc,SDB secondary
  class Git,CI,Registry,Kind infra
```

## Legenda

- **Setas contínuas:** chamadas e dependências em execução; **tracejadas:** configuração, escalonamento ou implantação.
- **Deployment:** instâncias do serviço Java. **Service ClusterIP:** DNS e porta internos do cluster.
- **StatefulSet + PVC:** PostgreSQL com volume persistente no ambiente kind.
- **HPA:** escala exclusivamente o serviço de vendas de 2 a 6 réplicas, conforme CPU, com métricas do metrics-server.
- **CI/CD:** o cluster kind é **efêmero**, criado no runner do workflow e removido ao fim; não corresponde a um ambiente público de produção permanente.
- **Limite transacional:** cada banco tem transações locais. Chamadas HTTP entre serviços não compõem transação distribuída.

Para a jornada funcional detalhada, consulte [Fluxo de ponta a ponta](./flow.md).
